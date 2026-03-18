#!/usr/bin/env bash
# ──────────────────────────────────────────────────────────────
# sonar-pr-comment.sh
# Posts a detailed SonarQube analysis comment on a GitHub PR,
# appearing right after the SonarCloud bot's own comment.
#
# Usage: sonar-pr-comment.sh <project_key> <pr_number> <repo>
# Requires env: SONAR_TOKEN, GITHUB_TOKEN, GEMINI_API_KEY
# ──────────────────────────────────────────────────────────────
set -euo pipefail

PROJECT_KEY="${1:-}"
PR_NUMBER="${2:-}"
REPO="${3:-}"

[[ -z "$PROJECT_KEY" || -z "$PR_NUMBER" || -z "$REPO" ]] && {
    echo "Usage: $0 <project_key> <pr_number> <repo>" >&2
    exit 1
}

SONAR_BASE="https://sonarcloud.io/api"
OUT="/tmp/_sonar_pr_comment_${PR_NUMBER}.md"
ISSUES_FILE="/tmp/_sonar_pr_issues_${PR_NUMBER}.json"
: > "$OUT"

# ── 1. Quality Gate status for this PR ──────────────────────
echo "Fetching Quality Gate for PR #$PR_NUMBER..."
QG_RESP=$(curl -sf -u "$SONAR_TOKEN:" \
    "$SONAR_BASE/qualitygates/project_status?projectKey=$PROJECT_KEY&pullRequest=$PR_NUMBER" \
    2>/dev/null || echo '{}')
QG_STATUS=$(echo "$QG_RESP" | jq -r '.projectStatus.status // "UNKNOWN"')

# ── 2. Open issues scoped to this PR ────────────────────────
echo "Fetching issues for PR #$PR_NUMBER..."
ISSUES_RESP=$(curl -sf -u "$SONAR_TOKEN:" \
    "$SONAR_BASE/issues/search?componentKeys=$PROJECT_KEY&pullRequest=$PR_NUMBER&statuses=OPEN&impactSeverities=HIGH,MEDIUM,LOW&ps=20" \
    2>/dev/null || echo '{"issues":[],"total":0}')
TOTAL=$(echo "$ISSUES_RESP" | jq '.total // 0')
echo "$ISSUES_RESP" | jq '.issues // []' > "$ISSUES_FILE"

# ── 3. Coverage metric for this PR ──────────────────────────
COV_RESP=$(curl -sf -u "$SONAR_TOKEN:" \
    "$SONAR_BASE/measures/component?component=$PROJECT_KEY&pullRequest=$PR_NUMBER&metricKeys=new_coverage,coverage" \
    2>/dev/null || echo '{"component":{"measures":[]}}')
NEW_COV=$(echo "$COV_RESP" | jq -r '
    [.component.measures[]? | select(.metric == "new_coverage") | (.period.value // .value)] |
    first // "n/a"' 2>/dev/null)
[[ -z "$NEW_COV" || "$NEW_COV" == "null" ]] && NEW_COV="n/a"

# ── 4. Changed files via GitHub API ─────────────────────────
echo "Fetching changed files..."
FILES_RESP=$(curl -sf \
    -H "Authorization: Bearer $GITHUB_TOKEN" \
    -H "Accept: application/vnd.github+json" \
    "https://api.github.com/repos/$REPO/pulls/$PR_NUMBER/files?per_page=50" \
    2>/dev/null || echo '[]')
CHANGED_FILES=$(echo "$FILES_RESP" | jq -r '.[].filename' 2>/dev/null || true)

# ── 5. Rule descriptions (batch fetch unique rules) ─────────
declare -A RULE_NAMES
declare -A RULE_DESCS
declare -A RULE_TAGS
if [ "$TOTAL" -gt 0 ]; then
    while IFS= read -r rule; do
        [[ -z "$rule" || "$rule" == "null" ]] && continue
        RULE_RESP=$(curl -sf -u "$SONAR_TOKEN:" \
            "$SONAR_BASE/rules/show?key=$rule" 2>/dev/null || echo '{}')
        RULE_NAMES["$rule"]=$(echo "$RULE_RESP" | jq -r '.rule.name // ""')
        RULE_DESCS["$rule"]=$(echo "$RULE_RESP" | \
            jq -r '.rule.htmlDesc // .rule.mdDesc // ""' | \
            python3 -c "
import sys, re
txt = sys.stdin.read()
txt = re.sub(r'<[^>]+>', '', txt)   # strip HTML tags
txt = re.sub(r'\s+', ' ', txt).strip()
print(txt[:350])
" 2>/dev/null || true)
        # Merge system tags + user tags into one comma-separated list
        RULE_TAGS["$rule"]=$(echo "$RULE_RESP" | \
            jq -r '(.rule.sysTags // []) + (.rule.tags // []) | unique | join(", ")' \
            2>/dev/null || true)
    done < <(jq -r '.[].rule // empty' "$ISSUES_FILE" | sort -u)
fi

# ── 6. Gemini overview explanation ──────────────────────────
GEMINI_TEXT=""
if [[ -n "${GEMINI_API_KEY:-}" && "$TOTAL" -gt 0 ]]; then
    echo "Generating AI overview..."
    ISSUE_LIST=$(jq -r '.[] |
        "[\(.severity)] \(.rule) in \((.component // "") | split(":") | last) line \(.line // "?"): \(.message)"
    ' "$ISSUES_FILE" | head -10)
    FILES_SAMPLE=$(echo "$CHANGED_FILES" | head -10)

    PROMPT="You are a senior code quality engineer reviewing a pull request.

SonarQube found the following issues in this PR:
${ISSUE_LIST}

Changed files:
${FILES_SAMPLE}

Write a concise, developer-friendly analysis of 4-6 sentences that covers:
1. What kind of quality issues were found and in which files
2. Why these issues are problematic (maintainability, security, reliability)
3. Concrete guidance on how to address them

Return plain text only — no JSON, no markdown headers, no bullet points."

    ESCAPED=$(python3 -c "import sys,json; print(json.dumps(sys.stdin.read()))" <<< "$PROMPT")
    MODEL="${GEMINI_MODEL:-gemini-2.5-flash}"
    URL="https://generativelanguage.googleapis.com/v1beta/models/${MODEL}:generateContent?key=$GEMINI_API_KEY"
    BODY="{\"contents\":[{\"parts\":[{\"text\":$ESCAPED}]}],\"generationConfig\":{\"temperature\":0.3,\"maxOutputTokens\":600}}"

    RESP=$(timeout 60 curl -s -X POST "$URL" -H 'Content-Type: application/json' -d "$BODY" 2>/dev/null || true)
    GEMINI_TEXT=$(echo "$RESP" | jq -r '.candidates[0].content.parts[0].text // empty' 2>/dev/null \
        | tr -d '\000-\010\013\014\016-\037' || true)
fi

# ── 7. Build the markdown comment ───────────────────────────
QG_ICON="✅"; [[ "$QG_STATUS" != "OK" ]] && QG_ICON="❌"

{
cat << HEADER
## 📋 Detailed SonarQube Analysis

| Quality Gate | New Code Coverage | Open Issues |
|:---:|:---:|:---:|
| ${QG_ICON} **${QG_STATUS}** | ${NEW_COV}% | ${TOTAL} |

HEADER

# Failed Quality Gate conditions
FAILED_CONDS=$(echo "$QG_RESP" | jq -r '
    .projectStatus.conditions[]? |
    select(.status != "OK") |
    "| `\(.metricKey)` | \(.actualValue // "n/a") | \(.comparator) \(.errorThreshold // "n/a") |"
' 2>/dev/null || true)
if [[ -n "$FAILED_CONDS" ]]; then
    echo "### ❌ Failed Conditions"
    echo ""
    echo "| Metric | Actual Value | Threshold |"
    echo "|---|---|---|"
    echo "$FAILED_CONDS"
    echo ""
fi

# Changed files
echo "### 📁 Files Changed"
echo ""
if [[ -n "$CHANGED_FILES" ]]; then
    echo "$CHANGED_FILES" | while IFS= read -r f; do
        [[ -n "$f" ]] && echo "- \`$f\`"
    done
else
    echo "- *(no files detected)*"
fi
echo ""

# AI overview
if [[ -n "$GEMINI_TEXT" ]]; then
    echo "### 🔍 Analysis Overview"
    echo ""
    echo "$GEMINI_TEXT"
    echo ""
fi

# Per-issue breakdown
if [[ "$TOTAL" -gt 0 ]]; then
    echo "### ⚠️ What Is Wrong"
    echo ""
    while IFS= read -r issue; do
        SEV=$(echo "$issue"  | jq -r '.severity // "?"')
        RULE=$(echo "$issue" | jq -r '.rule // "?"')
        MSG=$(echo "$issue"  | jq -r '.message // "?"')
        FILE=$(echo "$issue" | jq -r '(.component // "") | split(":") | last')
        LINE=$(echo "$issue" | jq -r '.line // "?"')
        TYPE=$(echo "$issue" | jq -r '.type // "CODE_SMELL"')

        SEV_ICON="🔵"
        case "$SEV" in
            BLOCKER)  SEV_ICON="🔴" ;;
            CRITICAL) SEV_ICON="🟠" ;;
            MAJOR)    SEV_ICON="🟡" ;;
            MINOR)    SEV_ICON="🔵" ;;
            INFO)     SEV_ICON="⚪" ;;
        esac

        RNAME="${RULE_NAMES[$RULE]:-}"
        RDESC="${RULE_DESCS[$RULE]:-}"
        RTAGS="${RULE_TAGS[$RULE]:-}"

        echo "#### ${SEV_ICON} \`${FILE}:${LINE}\` — ${SEV} ${TYPE}"
        echo "- **Rule**: \`${RULE}\`${RNAME:+ — ${RNAME}}"
        [[ -n "$RTAGS" ]] && echo "- **Ruleset / Tags**: \`${RTAGS//,  / \` \`}\`"
        echo "- **What is wrong**: ${MSG}"
        [[ -n "$RDESC" ]] && echo "- **Why it matters**: ${RDESC}"
        echo ""
    done < <(jq -c '.[]' "$ISSUES_FILE" 2>/dev/null)

    echo "### 🛠️ How to Fix"
    echo ""
    echo "For each issue above:"
    echo "1. Fix the **root cause** — do not use \`@SuppressWarnings\` or \`// NOSONAR\` to suppress"
    echo "2. **Coverage gaps**: add unit or integration tests for uncovered code paths"
    echo "3. **Security/reliability**: apply the secure coding pattern described in the rule above"
    echo "4. **Code smells**: refactor following clean-code principles (extract method, reduce complexity, etc.)"
    echo ""
else
    echo "### ✅ No Open Issues"
    echo ""
    echo "No new issues were introduced by this PR."
    if [[ "$QG_STATUS" != "OK" ]]; then
        echo ""
        echo "> The Quality Gate failure is due to the metric conditions above (e.g. insufficient coverage on new code)."
    fi
    echo ""
fi

echo "---"
echo "*Generated by the AI-SDLC pipeline · [View full analysis on SonarCloud](https://sonarcloud.io/project/overview?id=${PROJECT_KEY})*"
} > "$OUT"

echo "Comment built ($(wc -l < "$OUT") lines)"

# ── 8. Post the comment ─────────────────────────────────────
BODY_JSON=$(python3 -c "import sys,json; print(json.dumps(sys.stdin.read()))" < "$OUT")
RESP=$(curl -sf -X POST \
    -H "Authorization: Bearer $GITHUB_TOKEN" \
    -H "Accept: application/vnd.github+json" \
    "https://api.github.com/repos/$REPO/issues/$PR_NUMBER/comments" \
    -d "{\"body\": $BODY_JSON}" 2>/dev/null || echo '{}')

COMMENT_ID=$(echo "$RESP" | jq -r '.id // empty' 2>/dev/null)
if [[ -n "$COMMENT_ID" ]]; then
    echo "✅ Posted detailed Sonar comment #$COMMENT_ID on PR #$PR_NUMBER"
else
    echo "⚠️ Failed to post comment: $(echo "$RESP" | head -c 300)" >&2
    exit 1
fi

rm -f "$OUT" "$ISSUES_FILE"
