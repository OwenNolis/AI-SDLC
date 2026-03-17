#!/bin/bash
# ──────────────────────────────────────────────────────────────
# AI Code Fixing Utilities – Generic, AI-powered error fixing
# Sends real errors + real source code to Gemini and applies
# the returned fixes. No hard-coded file patterns.
# ──────────────────────────────────────────────────────────────
# NOTE: No 'set -e' at top level — this file is sourced by the
# workflow, so errexit would bleed into the caller.

RED='\033[0;31m'; GREEN='\033[0;32m'; YELLOW='\033[1;33m'; BLUE='\033[0;34m'; NC='\033[0m'
log_info()    { echo -e "${BLUE}[INFO]${NC} $1"; }
log_success() { echo -e "${GREEN}[SUCCESS]${NC} $1"; }
log_warning() { echo -e "${YELLOW}[WARNING]${NC} $1"; }
log_error()   { echo -e "${RED}[ERROR]${NC} $1"; }

# Count UNIQUE compile errors by file:line — ignores Maven boilerplate
# like "[ERROR] -> [Help 1]", "[ERROR] Re-run Maven...", etc.
# This prevents the cascade-reveal problem where fixing one error
# unmasks hidden errors, temporarily increasing raw [ERROR] count.
count_unique_errors() {
    local log_file="$1"
    # Match lines like: [ERROR] /path/File.java:[42,10] message
    # The { grep ... || true; } ensures exit-code 0 even when grep finds
    # nothing, which is critical under set -e / set -o pipefail.
    { grep -oE '\[ERROR\] [^ ]+\.java:\[[0-9]+,[0-9]+\]' "$log_file" 2>/dev/null || true; } \
        | sort -u | wc -l | tr -d ' '
}

# Count test failures from Maven surefire output.
# Looks for the summary line: "Tests run: X, Failures: Y, Errors: Z"
# Returns total of Failures + Errors across all test classes.
count_test_failures() {
    local log_file="$1"
    { grep 'Tests run:' "$log_file" 2>/dev/null || true; } \
        | awk -F'[, ]+' '{
            for(i=1;i<=NF;i++) {
                if($i=="Failures:") f+=$(i+1);
                if($i=="Errors:")   e+=$(i+1);
            }
          } END { print f+e+0 }'
}

# Count frontend errors: unique TypeScript compile errors + Jest test failures.
count_frontend_errors() {
    local log_file="$1"
    local ts_errors=0 jest_failures=0
    ts_errors=$({ grep -c 'error TS[0-9]' "$log_file" 2>/dev/null || true; })
    jest_failures=$({ grep -oE '[0-9]+ failed' "$log_file" 2>/dev/null | head -1 | grep -oE '[0-9]+' || true; })
    [ -z "$ts_errors" ] && ts_errors=0
    [ -z "$jest_failures" ] && jest_failures=0
    echo $(( ts_errors + jest_failures ))
}

# Run frontend checks (npm test). Returns 0 on success, 1 on failure.
# Appends errors to the given error file and logs to the given log file.
run_frontend_checks() {
    local log_file="$1" error_file="$2"
    if [ ! -f frontend/package.json ]; then
        return 0  # no frontend in this project
    fi
    log_info "Running frontend checks …"
    local fc=0
    (cd frontend && npm install --legacy-peer-deps --silent > /dev/null 2>&1 || true
     npm test -- --passWithNoTests --no-coverage 2>&1) > "$log_file" 2>&1 || fc=$?
    if [ $fc -ne 0 ]; then
        log_info "Frontend checks failed – extracting frontend errors"
        extract_all_errors "$log_file" /tmp/_fe_errors.txt
        cat /tmp/_fe_errors.txt >> "$error_file"
    fi
    return $fc
}

# Run static analysis (ESLint). Returns 0 on clean, 1 on findings.
# Appends lint errors to the given error file.
run_static_analysis() {
    local log_file="$1" error_file="$2"
    local lint_rc=0

    # ── Frontend: ESLint ──
    if [ -f frontend/package.json ] && grep -q '"lint"' frontend/package.json 2>/dev/null; then
        log_info "Running ESLint on frontend …"
        (cd frontend && npm install --legacy-peer-deps --silent > /dev/null 2>&1 || true
         npx eslint . 2>&1) > "$log_file" 2>&1 || lint_rc=$?
        if [ $lint_rc -ne 0 ]; then
            log_info "ESLint found issues – extracting lint errors"
            # ESLint default (stylish) format:  /path/file.tsx
            #   10:5  error  message  rule-name
            # Capture the file header + indented error lines
            grep -E '^\S.*\.(tsx?|jsx?|css)$' "$log_file" >> "$error_file" 2>/dev/null || true
            grep -E '^\s+[0-9]+:[0-9]+\s+(error|warning)' "$log_file" >> "$error_file" 2>/dev/null || true
            # Also grab summary lines (✖ N problems)
            grep -E '^✖|problems?\s*\(' "$log_file" >> "$error_file" 2>/dev/null || true
        else
            log_success "ESLint: no issues found"
        fi
    fi

    return $lint_rc
}

# ──────────────────────────────────────────────────────────────
# SonarQube Cloud Integration
# ──────────────────────────────────────────────────────────────

# Read the project key from sonar-project.properties.
_sonar_project_key() {
    if [ -f sonar-project.properties ]; then
        grep -E '^sonar\.projectKey=' sonar-project.properties 2>/dev/null \
            | head -1 | cut -d= -f2- | tr -d ' '
    fi
}

# Fetch open issues from SonarQube Cloud API and save as JSON.
# Usage:  fetch_sonar_issues [output_json]
# Requires: SONAR_TOKEN env var
fetch_sonar_issues() {
    local out_json="${1:-sonar_issues.json}"
    local project_key
    project_key=$(_sonar_project_key)

    if [ -z "$project_key" ] || echo "$project_key" | grep -q '<replace'; then
        log_warning "SonarQube project key not configured – skipping issue fetch"
        echo '{"total":0,"issues":[]}' > "$out_json"
        return 0
    fi
    if [ -z "$SONAR_TOKEN" ]; then
        log_warning "SONAR_TOKEN not set – skipping SonarQube issue fetch"
        echo '{"total":0,"issues":[]}' > "$out_json"
        return 0
    fi

    log_info "Fetching SonarQube issues for project: $project_key"

    local api_url="https://sonarcloud.io/api/issues/search"
    local page=1 page_size=100 total=0
    : > "$out_json.tmp"

    while true; do
        local resp
        resp=$(curl -sf -u "${SONAR_TOKEN}:" \
            "${api_url}?componentKeys=${project_key}&statuses=OPEN,CONFIRMED,REOPENED&types=BUG,VULNERABILITY,CODE_SMELL&severities=BLOCKER,CRITICAL,MAJOR,MINOR,INFO&ps=${page_size}&p=${page}" \
            2>/dev/null) || { log_warning "SonarQube API request failed"; break; }

        if [ $page -eq 1 ]; then
            total=$(echo "$resp" | jq '.total // 0' 2>/dev/null)
            log_info "SonarQube reports $total issue(s)"
        fi

        # Append issues from this page
        echo "$resp" | jq -c '.issues // [] | .[]?' >> "$out_json.tmp" 2>/dev/null || true

        # Check if there are more pages
        local fetched; fetched=$(wc -l < "$out_json.tmp" | tr -d ' ')
        if [ "$fetched" -ge "$total" ] || [ "$fetched" -ge 500 ]; then
            break
        fi
        page=$((page + 1))
    done

    # Build final JSON
    local count; count=$(wc -l < "$out_json.tmp" | tr -d ' ')
    echo "{\"total\":${count},\"issues\":[" > "$out_json"
    if [ "$count" -gt 0 ]; then
        # Join lines with commas
        sed '$!s/$/,/' "$out_json.tmp" >> "$out_json"
    fi
    echo "]}" >> "$out_json"
    rm -f "$out_json.tmp"

    log_success "Saved $count SonarQube issue(s) to $out_json"
    return 0
}

# Convert SonarQube issues JSON into human-readable error lines
# and append them to the error file used by the fix pipeline.
# Usage:  extract_sonar_errors <sonar_json> <error_file>
extract_sonar_errors() {
    local sonar_json="${1:-sonar_issues.json}" error_file="$2"
    [ ! -f "$sonar_json" ] && return 0

    local count
    count=$(jq '.total // 0' "$sonar_json" 2>/dev/null)
    [ "$count" -eq 0 ] && return 0

    log_info "Formatting $count SonarQube issue(s) for AI analysis …"

    {
        echo ""
        echo "## SonarQube Static Analysis Issues"
        echo "The following issues were detected by SonarQube."
        echo "Fix the root cause of each issue. Do NOT suppress with @SuppressWarnings or // NOSONAR."
        echo ""
        jq -r '.issues // [] | .[] |
            "[\(.severity)] \(.type) in \(.component | split(":") | last) (line \(.line // "?")):\n" +
            "  Rule: \(.rule)\n" +
            "  Message: \(.message)\n"' "$sonar_json" 2>/dev/null || true
    } >> "$error_file"

    log_success "Appended SonarQube issues to error file"
}

# ──────────────────────────────────────────────────────────────
# CLI sub-command: fetch Sonar issues and append to error file
# Usage:  ai-fix-utils.sh fetch-sonar-issues <error_file>
# ──────────────────────────────────────────────────────────────
_cmd_fetch_sonar_issues() {
    local error_file="${1:-error_analysis.md}"
    fetch_sonar_issues sonar_issues.json
    extract_sonar_errors sonar_issues.json "$error_file"
}

# ──────────────────────────────────────────────────────────────
# 1. Extract errors from any build / test log
# ──────────────────────────────────────────────────────────────
extract_all_errors() {
    local log_file="$1" output_file="$2"
    log_info "Extracting errors from build output..."
    : > "$output_file"

    # ── Maven compilation errors ──
    grep -A4 "cannot find symbol\|package.*does not exist\|incompatible types\|COMPILATION ERROR\|method.*cannot be applied" \
        "$log_file" >> "$output_file" 2>/dev/null || true

    # ── Maven / JUnit test failures (summary + immediate context) ──
    grep -A10 "<<< FAILURE\|<<< ERROR\|Tests run:.*Failures:\|Tests run:.*Errors:" \
        "$log_file" >> "$output_file" 2>/dev/null || true

    # ── HTTP errors surfacing in integration tests ──
    grep -A6 "HttpServerErrorException\|HttpClientErrorException\|500.*Internal Server\|404.*Not Found" \
        "$log_file" >> "$output_file" 2>/dev/null || true

    # ── Runtime stacktraces: "Caused by" chains (most informative part) ──
    grep -A5 "^Caused by:\|^[[:space:]]*Caused by:" \
        "$log_file" >> "$output_file" 2>/dev/null || true

    # ── Spring context failures ──
    grep -A8 "BeanCreationException\|UnsatisfiedDependencyException\|NoSuchBeanDefinitionException\|BeanCurrentlyInCreationException\|ApplicationContextException\|circular reference\|Error creating bean" \
        "$log_file" >> "$output_file" 2>/dev/null || true

    # ── NullPointerException / ClassCast / NoSuchMethod / ClassNotFound ──
    grep -A6 "NullPointerException\|ClassCastException\|NoSuchMethodError\|NoSuchMethodException\|ClassNotFoundException\|NoClassDefFoundError\|IllegalArgumentException\|IllegalStateException" \
        "$log_file" >> "$output_file" 2>/dev/null || true

    # ── JUnit assertion failures with expected/actual values ──
    grep -A6 "AssertionError\|AssertionFailedError\|expected:.*but was:\|Expected.*but.*got\|org\.opentest4j\.\|ComparisonFailure" \
        "$log_file" >> "$output_file" 2>/dev/null || true

    # ── Stack frames from project source (useful for locating the bug) ──
    grep "at be\.ap\.student\.\|at ${FEATURE_ID:-be\.ap}.*(" \
        "$log_file" >> "$output_file" 2>/dev/null || true

    # ── Surefire XML failure messages (richest test failure info) ──
    if [ -d backend/target/surefire-reports ]; then
        for xml in backend/target/surefire-reports/TEST-*.xml; do
            [ -f "$xml" ] || continue
            # Extract <failure> and <error> elements with message + type
            grep -oP '(failure|error) message="\K[^"]*' "$xml" >> "$output_file" 2>/dev/null || true
            # Extract the text content of failure/error elements (stacktrace)
            sed -n '/<failure\|<error/,/<\/failure\|<\/error/p' "$xml" \
                | head -30 >> "$output_file" 2>/dev/null || true
        done
    fi

    # ── NPM / TypeScript / Jest ──
    grep -A6 "npm ERR\|TS[0-9]\+:\|Cannot find module\|FAIL.*\.test\.\|SyntaxError" \
        "$log_file" >> "$output_file" 2>/dev/null || true

    # ── TypeScript compiler errors (tsc output:  src/File.tsx(10,5): error TS2xxx) ──
    grep -A3 'error TS[0-9]\+:' "$log_file" >> "$output_file" 2>/dev/null || true

    # ── Jest detailed failures: FAIL line + expect / received blocks ──
    grep -B1 -A10 'Expected\|Received\|expect(.*)\.\|● ' \
        "$log_file" >> "$output_file" 2>/dev/null || true

    # ── Jest test suite errors (crash before tests run) ──
    grep -A8 'Test suite failed to run\|SyntaxError:\|ReferenceError:\|TypeError:' \
        "$log_file" >> "$output_file" 2>/dev/null || true

    # ── ESLint errors (stylish: indented "10:5 error …"; unix: "file.tsx:10:5: …") ──
    grep -E '^\s+[0-9]+:[0-9]+\s+(error|warning)' \
        "$log_file" >> "$output_file" 2>/dev/null || true
    grep -E '^[^ ]+\.(tsx?|jsx?|css):[0-9]+:[0-9]+:' \
        "$log_file" >> "$output_file" 2>/dev/null || true

    # ── Generic [ERROR] lines ──
    grep "^\[ERROR\]\|^Error:" "$log_file" >> "$output_file" 2>/dev/null || true

    # Deduplicate while preserving order (removes exact duplicate lines)
    local tmp; tmp=$(mktemp)
    awk '!seen[$0]++' "$output_file" > "$tmp" && mv "$tmp" "$output_file"

    local n; n=$(wc -l < "$output_file" | tr -d ' ')
    log_info "Extracted $n lines of error context"
}

# ──────────────────────────────────────────────────────────────
# 2. Discover which source files are affected
# ──────────────────────────────────────────────────────────────
extract_affected_files() {
    local error_file="$1"
    local files=""

    # Absolute paths in Maven output  (/home/runner/.../File.java:[line,col])
    files=$(grep -oE '/[^ ]*\.java' "$error_file" 2>/dev/null \
        | sed 's|.*/AI-SDLC/||' | sort -u || true)

    # Relative paths (backend/src/...)
    local rel; rel=$(grep -oE 'backend/src/[^ ]*\.java' "$error_file" 2>/dev/null | sort -u || true)
    files=$(printf '%s\n%s' "$files" "$rel" | sort -u | grep -v '^$' || true)

    # Fully-qualified class names → file paths  (covers stacktrace frames)
    local classes; classes=$(grep -oE 'be(\.[a-zA-Z_]+)+' "$error_file" 2>/dev/null | sort -u || true)
    for cls in $classes; do
        local p; p=$(echo "$cls" | tr '.' '/')
        for base in backend/src/main/java backend/src/test/java; do
            if [ -f "$base/$p.java" ]; then
                files=$(printf '%s\n%s' "$files" "$base/$p.java")
            fi
        done
    done

    # Simple class names from stacktraces  (e.g. "TicketService.java:45")
    local simple; simple=$(grep -oE '[A-Z][A-Za-z0-9]*\.java' "$error_file" 2>/dev/null | sort -u || true)
    for sname in $simple; do
        local found; found=$(find backend/src -name "$sname" 2>/dev/null | head -3)
        [ -n "$found" ] && files=$(printf '%s\n%s' "$files" "$found")
    done

    # ── Frontend: relative paths from TypeScript / Jest output ──
    # TSC output:  frontend/src/ui/TicketForm.tsx(10,5): error TS2xxx
    local fe_paths; fe_paths=$(grep -oE 'frontend/src/[^ (:]+\.(tsx?|jsx?|css)' "$error_file" 2>/dev/null | sort -u || true)
    [ -n "$fe_paths" ] && files=$(printf '%s\n%s' "$files" "$fe_paths")

    # Jest FAIL lines:  FAIL src/ui/BrokenComponent.test.tsx
    local jest_files; jest_files=$(grep -oE 'FAIL [^ ]+\.(tsx?|jsx?)' "$error_file" 2>/dev/null | sed 's/^FAIL /frontend\//' | sort -u || true)
    [ -n "$jest_files" ] && files=$(printf '%s\n%s' "$files" "$jest_files")

    # Simple component names from Jest/TS errors (e.g. "TicketForm.tsx")
    local fe_simple; fe_simple=$(grep -oE '[A-Z][A-Za-z0-9]*\.tsx?' "$error_file" 2>/dev/null | sort -u || true)
    for fname in $fe_simple; do
        local fefound; fefound=$(find frontend/src -name "$fname" 2>/dev/null | head -3)
        [ -n "$fefound" ] && files=$(printf '%s\n%s' "$files" "$fefound")
    done

    # ESLint: absolute paths from stylish/unix format → strip to relative
    # Stylish: /abs/path/file.tsx  (alone on a line)
    # Unix:    /abs/path/file.tsx:10:5: message
    local eslint_abs; eslint_abs=$(grep -oE '^/[^ :]+\.(tsx?|jsx?|css)' "$error_file" 2>/dev/null | sort -u || true)
    for apath in $eslint_abs; do
        [ -z "$apath" ] && continue
        # Convert absolute to workspace-relative path
        local rel; rel=$(echo "$apath" | sed 's|.*/frontend/|frontend/|' 2>/dev/null || true)
        [ -n "$rel" ] && [ -f "$rel" ] && files=$(printf '%s\n%s' "$files" "$rel")
    done

    # De-duplicate and keep only files that actually exist on disk
    local result=""
    while IFS= read -r f; do
        [ -n "$f" ] && [ -f "$f" ] && result=$(printf '%s\n%s' "$result" "$f")
    done <<< "$(echo "$files" | sort -u)"
    echo "$result" | sed '/^$/d'
}

# ──────────────────────────────────────────────────────────────
# 3. Build a source-context block for Gemini
# ──────────────────────────────────────────────────────────────
build_source_context() {
    local file_list="$1" ctx=""
    while IFS= read -r f; do
        [ -z "$f" ] && continue
        ctx="${ctx}
--- FILE: ${f} ---
$(cat "$f")
--- END FILE ---
"
    done <<< "$file_list"
    echo "$ctx"
}

# ──────────────────────────────────────────────────────────────
# 4. Call Gemini API  (errors + source code → JSON fixes)
# ──────────────────────────────────────────────────────────────
call_gemini() {
    local error_content="$1" source_context="$2" out_file="$3"

    [ -z "$GEMINI_API_KEY" ] && { log_error "GEMINI_API_KEY not set"; return 1; }

    log_info "Calling Gemini AI with errors + source code …"

    # ── build prompt ────────────────────────────────────
    cat > /tmp/ai_prompt.txt << 'PROMPT'
You are an expert Java / Spring Boot and React / TypeScript developer.
Analyse the ERRORS below together with the SOURCE CODE of the affected files
and return a JSON object with concrete fixes.

RULES:
• Each fix must contain the COMPLETE file content (package, imports, class body).
  Never use placeholders like "// … rest of code".
• Only touch files that are actually broken.  Keep working code intact.
• NEVER delete or remove source files. Always use action "modify" to fix them.
  Deleting a file breaks the project. Fix the code inside the file instead.
• For TypeScript/React errors: fix the actual component or test file.
  Preserve existing imports, hooks, and component structure.
  Use proper React/JSX syntax and TypeScript types.
• For ESLint / static-analysis warnings: fix the root cause, not just suppress.
  Prefer proper typing over @ts-ignore. Use the correct ESLint-recommended pattern.
  Only add // eslint-disable as a last resort when the rule is a false positive.
• For SonarQube issues (BUG, VULNERABILITY, CODE_SMELL):
  – Fix the ROOT CAUSE. Do NOT add @SuppressWarnings or // NOSONAR to suppress.
  – For null-pointer bugs: add proper null checks or use Optional.
  – For security vulnerabilities: apply the recommended secure coding pattern.
  – For code smells: refactor to follow clean-code principles.
  – The SonarQube rule ID is provided (e.g. java:S1854) — use it to understand
    the exact issue type and apply the correct fix pattern.
• For missing imports → add the right import.
• For undefined classes/methods used in production code → remove the broken
  usage or replace it with a minimal working alternative.
• For test failures caused by missing endpoints → create the endpoint or fix
  the test expectation so the test passes.
• When compilation passes but TESTS FAIL: focus on the test output carefully.
  – If the test expectations are wrong (e.g. wrong status code, wrong field name),
    fix the TEST file.
  – If the production code has a logic bug (e.g. NPE, wrong return value,
    missing null check), fix the PRODUCTION file.
  – Include both test and production files in your fixes when both need changes.
  – NEVER just delete or skip a failing test to make the build pass.
• Return ONLY valid JSON. No markdown fences, no explanation outside the JSON.

RESPONSE FORMAT (strict JSON):
{
  "analysis": "one-paragraph summary",
  "fixes": [
    {
      "file": "backend/src/main/java/com/example/Foo.java",
      "issue": "what was wrong",
      "action": "modify",
      "content": "full file content …"
    }
  ]
}
PROMPT

    printf '\nERRORS:\n'            >> /tmp/ai_prompt.txt
    head -400 <<< "$error_content"  >> /tmp/ai_prompt.txt
    printf '\nSOURCE CODE:\n'       >> /tmp/ai_prompt.txt
    head -1500 <<< "$source_context" >> /tmp/ai_prompt.txt

    # Escape for JSON
    local escaped
    escaped=$(python3 -c "import sys,json; print(json.dumps(sys.stdin.read()))" < /tmp/ai_prompt.txt)

    # ── API call with retry ─────────────────────────────
    local model="${GEMINI_MODEL:-gemini-2.5-flash}"
    local url="https://generativelanguage.googleapis.com/v1beta/models/${model}:generateContent?key=$GEMINI_API_KEY"
    local body="{
      \"contents\":[{\"parts\":[{\"text\":$escaped}]}],
      \"generationConfig\":{\"temperature\":0.1,\"maxOutputTokens\":65536,\"responseMimeType\":\"application/json\"}
    }"

    local resp="" attempt=0 max_attempts=4 wait_secs=5
    while [ $attempt -lt $max_attempts ]; do
        attempt=$((attempt+1))
        log_info "  Gemini request attempt $attempt/$max_attempts (model: $model)"
        resp=$(timeout 180 curl -s -X POST "$url" \
            -H 'Content-Type: application/json' \
            -d "$body" 2>&1) || true

        # Check for retryable errors (503, 429, empty)
        if [ -z "$resp" ]; then
            log_warning "  Empty response – retrying in ${wait_secs}s …"
        elif echo "$resp" | jq -e '.error' > /dev/null 2>&1; then
            local http_code; http_code=$(echo "$resp" | jq -r '.error.code // 0' 2>/dev/null)
            local api_err; api_err=$(echo "$resp" | jq -r '.error.message // empty' 2>/dev/null)
            if [ "$http_code" = "503" ] || [ "$http_code" = "429" ]; then
                log_warning "  Retryable API error ($http_code): ${api_err} – retrying in ${wait_secs}s …"
            else
                log_warning "  API error ($http_code): ${api_err}"
                break  # non-retryable API error
            fi
        elif ! echo "$resp" | jq -e '.candidates[0]' > /dev/null 2>&1; then
            log_warning "  Unexpected response format – retrying in ${wait_secs}s …"
        else
            break   # got a real response with candidates
        fi

        [ $attempt -lt $max_attempts ] && sleep $wait_secs
        wait_secs=$((wait_secs * 2))
    done

    [ -z "$resp" ] && { log_warning "Gemini API returned nothing after $max_attempts attempts"; rm -f /tmp/ai_prompt.txt; return 1; }

    # Check for API-level errors
    local err; err=$(echo "$resp" | jq -r '.error.message // empty' 2>/dev/null)
    [ -n "$err" ] && { log_warning "Gemini error: $err"; rm -f /tmp/ai_prompt.txt; return 1; }

    # Extract text from Gemini response
    local finish_reason; finish_reason=$(echo "$resp" | jq -r '.candidates[0].finishReason // "STOP"' 2>/dev/null)
    if [ "$finish_reason" = "MAX_TOKENS" ]; then
        log_warning "  Gemini response was TRUNCATED (hit output token limit)"
    fi

    local raw; raw=$(echo "$resp" | jq -r '.candidates[0].content.parts[0].text // empty' 2>/dev/null)
    [ -z "$raw" ] && { log_warning "Empty candidate text"; rm -f /tmp/ai_prompt.txt; return 1; }

    # Strip optional markdown fences
    local clean; clean=$(echo "$raw" | sed 's/^```json//;s/^```//;s/```$//' | sed '/^[[:space:]]*$/d')

    if echo "$clean" | jq . > /dev/null 2>&1; then
        echo "$clean" > "$out_file"
    else
        # Last-ditch: use Python json.JSONDecoder for robust extraction of first JSON object
        clean=$(python3 -c "
import sys, json
text = sys.stdin.read()
# Try the whole text first
try:
    o = json.loads(text)
    print(json.dumps(o))
    sys.exit(0)
except: pass
# Find the first { and use raw_decode
idx = text.find('{')
if idx >= 0:
    try:
        decoder = json.JSONDecoder()
        o, _ = decoder.raw_decode(text, idx)
        print(json.dumps(o))
        sys.exit(0)
    except: pass
sys.exit(1)
" <<< "$raw" 2>/dev/null)
        if [ -n "$clean" ] && echo "$clean" | jq . > /dev/null 2>&1; then
            echo "$clean" > "$out_file"
        else
            log_warning "Could not extract valid JSON from Gemini"
            log_info "First 500 chars: $(head -c500 <<< "$raw")"
            rm -f /tmp/ai_prompt.txt
            return 1
        fi
    fi

    local cnt; cnt=$(jq '.fixes|length' "$out_file" 2>/dev/null || echo 0)
    log_success "Gemini returned $cnt fix(es)"
    rm -f /tmp/ai_prompt.txt
    return 0
}

# ──────────────────────────────────────────────────────────────
# 5. Apply fixes from the JSON file
# ──────────────────────────────────────────────────────────────
apply_fixes() {
    local json_file="$1"
    [ ! -s "$json_file" ] && { log_error "No fixes file"; return 1; }

    local n; n=$(jq '.fixes|length' "$json_file" 2>/dev/null || echo 0)
    [ "$n" -eq 0 ] && { log_warning "Zero fixes in JSON"; return 1; }

    log_info "Applying $n fix(es) …"
    local ok=0
    for ((i=0;i<n;i++)); do
        local fp; fp=$(jq -r ".fixes[$i].file"    "$json_file")
        local act; act=$(jq -r ".fixes[$i].action" "$json_file")
        local iss; iss=$(jq -r ".fixes[$i].issue"  "$json_file")
        local con; con=$(jq -r ".fixes[$i].content" "$json_file")

        [ "$fp" = "null" ] || [ -z "$fp" ] && continue
        [ "$con" = "null" ] && con=""

        log_info "  [$((i+1))/$n] $act $fp — $iss"

        case "$act" in
            create|modify)
                mkdir -p "$(dirname "$fp")"
                printf '%s\n' "$con" > "$fp"
                log_success "  Written $fp"
                ok=$((ok+1))
                ;;
            delete|remove)
                # Safety: never delete source files — skip with warning
                if echo "$fp" | grep -qE '\.(java|kt|ts|tsx|js|jsx|py|go|rs)$'; then
                    log_warning "  SKIPPED deletion of source file $fp (safety guard)"
                else
                    [ -f "$fp" ] && { rm -f "$fp"; log_success "  Deleted $fp"; ok=$((ok+1)); }
                fi
                ;;
        esac
    done
    log_success "Applied $ok of $n fix(es)"
}

# ──────────────────────────────────────────────────────────────
# 6. Orchestrator – iterative fix loop
# ──────────────────────────────────────────────────────────────
run_fix_pipeline() {
    local error_file="$1"
    local analysis="/tmp/ai_analysis.json"
    local max=5 iter=0
    local regression_detected=false

    log_info "╔══════════════════════════════════════╗"
    log_info "║   AI Fix Pipeline  (max $max iterations)  ║"
    log_info "╚══════════════════════════════════════╝"

    # ── Pre-loop: Fetch SonarQube issues (once) and append to error file ──
    if [ -n "$SONAR_TOKEN" ]; then
        fetch_sonar_issues /tmp/_sonar_issues.json
        extract_sonar_errors /tmp/_sonar_issues.json "$error_file"
    fi

    while [ $iter -lt $max ]; do
        iter=$((iter+1))
        log_info "── iteration $iter/$max ──────────────────────"

        # Make sure we have fresh error content
        local errs; errs=$(cat "$error_file" 2>/dev/null | head -400 || true)
        if [ -z "$errs" ] || [ "$(wc -l <<< "$errs" | tr -d ' ')" -lt 2 ]; then
            log_info "Error file thin – running build to capture errors …"

            local cc=0
            (cd backend && mvn test-compile -q > /tmp/_compile.log 2>&1) || cc=$?

            if [ $cc -ne 0 ]; then
                extract_all_errors /tmp/_compile.log "$error_file"
            else
                local tc=0
                (cd backend && mvn test > /tmp/_test.log 2>&1) || tc=$?
                if [ $tc -ne 0 ]; then
                    extract_all_errors /tmp/_test.log "$error_file"
                fi
            fi

            # Also check frontend
            run_frontend_checks /tmp/_fe_check.log "$error_file" || true

            # Static analysis (lint)
            run_static_analysis /tmp/_lint_check.log "$error_file" || true

            # Re-fetch SonarQube issues (may have changed after previous iteration's fixes)
            if [ -n "$SONAR_TOKEN" ] && [ $iter -gt 1 ]; then
                fetch_sonar_issues /tmp/_sonar_issues.json
                extract_sonar_errors /tmp/_sonar_issues.json "$error_file"
            fi

            errs=$(cat "$error_file" 2>/dev/null | head -400 || true)
            if [ -z "$errs" ] || [ "$(wc -l <<< "$errs" | tr -d ' ')" -lt 2 ]; then
                log_success "Backend + frontend + lint + Sonar pass – nothing to fix!"
                return 0
            fi
        fi

        # Discover affected files & read their source
        local files; files=$(extract_affected_files "$error_file")
        [ -z "$files" ] && files=$(grep -oE '(backend/src/[^ ]*\.java|frontend/src/[^ ]*\.tsx?)' "$error_file" 2>/dev/null | sort -u || true)
        log_info "Affected files: $(echo $files | tr '\n' ', ')"

        local src; src=$(build_source_context "$files")

        # Ask Gemini
        if ! call_gemini "$errs" "$src" "$analysis"; then
            log_error "Gemini call failed on iteration $iter"
            if [ $iter -gt 1 ]; then
                log_warning "Continuing with fixes from previous iterations"
                break
            fi
            return 1
        fi

        # ── Regression guard: snapshot & count errors BEFORE fixes ──
        local savepoint
        savepoint=$(git stash create 2>/dev/null || true)
        local errors_before=0
        (cd backend && mvn test-compile > /tmp/_pre_fix.log 2>&1) || true
        errors_before=$(count_unique_errors /tmp/_pre_fix.log)
        log_info "Unique error count before iteration $iter fixes: $errors_before"

        # Apply
        apply_fixes "$analysis"

        # ── Verify & regression check ──
        local cc2=0
        (cd backend && mvn test-compile > /tmp/_post_fix.log 2>&1) || cc2=$?
        local errors_after=0
        errors_after=$(count_unique_errors /tmp/_post_fix.log)
        log_info "Unique error count after iteration $iter fixes: $errors_after"

        # If AI made things WORSE, revert this iteration and stop
        if [ "$errors_after" -gt "$errors_before" ] && [ "$errors_before" -gt 0 ]; then
            log_error "🚨 REGRESSION: errors increased from $errors_before → $errors_after"
            log_error "Reverting iteration $iter fixes to prevent further damage"
            git checkout -- . 2>/dev/null || true
            git clean -fd 2>/dev/null || true
            if [ -n "$savepoint" ]; then
                git stash apply "$savepoint" 2>/dev/null || true
            fi
            regression_detected=true
            break
        fi

        if [ $cc2 -eq 0 ]; then
            log_success "Compilation OK after iteration $iter"

            # ── Test-only mode: compilation passed, run tests ──
            local test_failures_before=0
            test_failures_before=$(count_test_failures /tmp/_post_fix.log)

            local tc2=0
            (cd backend && mvn test > /tmp/_retest.log 2>&1) || tc2=$?

            if [ $tc2 -eq 0 ]; then
                # Backend passes — now check frontend too
                local fe2=0
                run_frontend_checks /tmp/_fe_retest.log /tmp/_fe_errors_iter.txt || fe2=$?
                if [ $fe2 -ne 0 ]; then
                    log_info "Backend passes but frontend still failing – extracting frontend errors"
                    cat /tmp/_fe_errors_iter.txt >> "$error_file" 2>/dev/null || true
                fi

                # Run lint check
                local lint2=0
                : > /tmp/_lint_errors_iter.txt
                run_static_analysis /tmp/_lint_retest.log /tmp/_lint_errors_iter.txt || lint2=$?
                if [ $lint2 -ne 0 ]; then
                    log_info "Static analysis found issues – feeding to next iteration"
                    cat /tmp/_lint_errors_iter.txt >> "$error_file" 2>/dev/null || true
                fi

                if [ $fe2 -eq 0 ] && [ $lint2 -eq 0 ]; then
                    log_success "All tests + lint pass after iteration $iter!"
                    return 0
                fi
            else
                local test_failures_after=0
                test_failures_after=$(count_test_failures /tmp/_retest.log)
                log_info "Test failures: $test_failures_after (was $test_failures_before before this iteration)"

                # Test regression guard: if we made test results worse, revert
                if [ "$test_failures_after" -gt "$test_failures_before" ] && [ "$test_failures_before" -gt 0 ]; then
                    log_error "🚨 TEST REGRESSION: failures increased from $test_failures_before → $test_failures_after"
                    log_error "Reverting iteration $iter fixes"
                    git checkout -- . 2>/dev/null || true
                    git clean -fd 2>/dev/null || true
                    if [ -n "$savepoint" ]; then
                        git stash apply "$savepoint" 2>/dev/null || true
                    fi
                    regression_detected=true
                    break
                fi

                log_info "Tests still failing – extracting errors for next iteration"
                extract_all_errors /tmp/_retest.log "$error_file"

                # Include test source files in the context for next iteration
                local test_files; test_files=$(extract_affected_files "$error_file" | grep '/test/' || true)
                if [ -n "$test_files" ]; then
                    log_info "Test-only mode: including $(echo "$test_files" | wc -l | tr -d ' ') test file(s) in next iteration context"
                fi
            fi
        else
            log_info "Compilation still broken – feeding new errors to next iteration"
            (cd backend && mvn test-compile > /tmp/_recompile.log 2>&1) || true
            extract_all_errors /tmp/_recompile.log "$error_file"
        fi
    done

    # Final check: even if we hit max iterations or Gemini failed on a later
    # iteration, previous iterations may have fixed things.
    # But if regression was detected and no prior iterations succeeded, fail hard.
    if [ "$regression_detected" = "true" ] && [ $iter -le 1 ]; then
        log_error "Regression detected on first iteration — no useful fixes to apply"
        return 1
    fi

    log_info "Running final compilation check..."
    local final_cc=0
    (cd backend && mvn test-compile -q > /dev/null 2>&1) || final_cc=$?

    if [ $final_cc -eq 0 ]; then
        log_success "Compilation passes after $iter iteration(s)"
        local final_tc=0
        (cd backend && mvn test > /tmp/_final_test.log 2>&1) || final_tc=$?
        if [ $final_tc -eq 0 ]; then
            log_success "Backend tests pass!"
        else
            log_warning "Compilation OK but some backend tests still fail"
        fi
        # Also run frontend final check
        local final_fe=0
        run_frontend_checks /tmp/_final_fe.log /tmp/_final_fe_errors.txt || final_fe=$?
        if [ $final_fe -eq 0 ]; then
            log_success "Frontend checks pass!"
        else
            log_warning "Some frontend checks still fail"
        fi
        # Static analysis final check
        local final_lint=0
        run_static_analysis /tmp/_final_lint.log /tmp/_final_lint_errors.txt || final_lint=$?
        if [ $final_lint -eq 0 ]; then
            log_success "Static analysis clean!"
        else
            log_warning "Some lint issues remain"
        fi
        return 0  # fixes were applied, PR should be created
    else
        log_warning "Compilation still broken after $iter iteration(s)"
        # Check if ANY source files were changed — if so, still create a PR with partial fixes
        local changed; changed=$(git diff --name-only 2>/dev/null | grep -cE '\.(java|kt|ts|tsx|js|jsx|py|go|rs|xml)$' || echo 0)
        if [ "$changed" -gt 0 ]; then
            log_info "$changed source file(s) were modified — creating PR with partial fixes"
            return 0  # partial fix is better than nothing
        fi
        return 1
    fi
}

# ──────────────────────────────────────────────────────────────
# 7. AI PR Review  – self-review the generated diff
# ──────────────────────────────────────────────────────────────
# Usage:  review_ai_pr <repo> <pr_number>
# Requires: GEMINI_API_KEY, GITHUB_TOKEN
review_ai_pr() {
    local repo="$1" pr_number="$2"
    [ -z "$pr_number" ] && { log_error "review_ai_pr: PR number required"; return 1; }
    [ -z "$GEMINI_API_KEY" ] && { log_error "review_ai_pr: GEMINI_API_KEY not set"; return 1; }
    [ -z "$GITHUB_TOKEN" ]  && { log_error "review_ai_pr: GITHUB_TOKEN not set"; return 1; }

    log_info "Running AI self-review on PR #$pr_number …"

    # ── Fetch the PR diff ──
    local diff
    diff=$(curl -sf -H "Accept: application/vnd.github.v3.diff" \
        -H "Authorization: Bearer $GITHUB_TOKEN" \
        "https://api.github.com/repos/$repo/pulls/$pr_number" 2>/dev/null || true)
    [ -z "$diff" ] && { log_warning "Could not fetch diff for PR #$pr_number"; return 0; }

    # Truncate very large diffs to fit context window
    diff=$(head -800 <<< "$diff")

    # ── Build the review prompt ──
    cat > /tmp/ai_review_prompt.txt << 'REVIEW_PROMPT'
You are an expert code reviewer for a Java / Spring Boot + React / TypeScript project.
Review the DIFF below from an AI-generated fix PR.

Focus on:
1. Correctness: Do the changes actually fix the problem?
2. Completeness: Are there any missing imports, annotations, or edge cases?
3. Style: Do the changes follow project conventions?
4. Safety: Any risk of regressions, NPEs, or broken tests?
5. Unnecessary changes: Is anything modified that should have been left alone?

Return a JSON object:
{
  "summary": "2-3 sentence overall assessment",
  "verdict": "approve" | "request_changes" | "comment",
  "comments": [
    {
      "severity": "must_fix" | "should_fix" | "nit",
      "file": "path/to/file",
      "line": 42,
      "body": "description of issue and suggestion"
    }
  ]
}

Rules:
- Return ONLY valid JSON. No markdown fences.
- If the diff looks correct with no issues, return verdict "approve" with an empty comments array.
- Be pragmatic: AI-generated fixes often rewrite entire files — focus on functional correctness.
- "line" should reference the NEW file line number from the diff hunks.
REVIEW_PROMPT

    printf '\nDIFF:\n' >> /tmp/ai_review_prompt.txt
    cat <<< "$diff" >> /tmp/ai_review_prompt.txt

    # ── Call Gemini ──
    local escaped
    escaped=$(python3 -c "import sys,json; print(json.dumps(sys.stdin.read()))" < /tmp/ai_review_prompt.txt)

    local model="${GEMINI_MODEL:-gemini-2.5-flash}"
    local url="https://generativelanguage.googleapis.com/v1beta/models/${model}:generateContent?key=$GEMINI_API_KEY"
    local body="{
      \"contents\":[{\"parts\":[{\"text\":$escaped}]}],
      \"generationConfig\":{\"temperature\":0.2,\"maxOutputTokens\":8192,\"responseMimeType\":\"application/json\"}
    }"

    local resp
    resp=$(timeout 120 curl -s -X POST "$url" \
        -H 'Content-Type: application/json' \
        -d "$body" 2>&1) || true

    if [ -z "$resp" ] || ! echo "$resp" | jq -e '.candidates[0]' > /dev/null 2>&1; then
        log_warning "Gemini review call failed — skipping self-review"
        rm -f /tmp/ai_review_prompt.txt
        return 0
    fi

    # ── Extract review JSON ──
    local raw
    raw=$(echo "$resp" | jq -r '.candidates[0].content.parts[0].text // empty' 2>/dev/null)
    [ -z "$raw" ] && { log_warning "Empty review response"; return 0; }

    # Parse through python to handle any leading/trailing noise
    local review_json
    review_json=$(python3 -c "
import sys, json
try:
    data = sys.stdin.read()
    dec = json.JSONDecoder()
    obj, _ = dec.raw_decode(data.lstrip())
    print(json.dumps(obj))
except Exception as e:
    print('{}', file=sys.stderr)
    sys.exit(1)
" <<< "$raw" 2>/dev/null) || { log_warning "Could not parse review JSON"; return 0; }

    local summary verdict
    summary=$(echo "$review_json" | jq -r '.summary // "No summary"')
    verdict=$(echo "$review_json" | jq -r '.verdict // "comment"')
    local num_comments
    num_comments=$(echo "$review_json" | jq -r '.comments | length // 0')

    log_info "Review verdict: $verdict ($num_comments comment(s))"
    log_info "Summary: $summary"

    # ── Post as PR comment ──
    # Build a formatted markdown comment body
    local review_body=""
    review_body+="## 🔍 AI Self-Review\n\n"
    review_body+="**Verdict:** "
    case "$verdict" in
        approve)         review_body+="✅ Approved\n\n" ;;
        request_changes) review_body+="⚠️ Changes Requested\n\n" ;;
        *)               review_body+="💬 Comments\n\n" ;;
    esac
    review_body+="**Summary:** ${summary}\n\n"

    if [ "$num_comments" -gt 0 ]; then
        review_body+="### Comments\n\n"
        review_body+=$(echo "$review_json" | jq -r '.comments[] | "- **[\(.severity)]** `\(.file)` L\(.line): \(.body)"' 2>/dev/null || true)
        review_body+="\n"
    fi

    review_body+="\n---\n*Self-review generated by Gemini AI (${model}).*"

    # Post as issue comment (simpler & works without fine-grained token scopes)
    local comment_body_json
    comment_body_json=$(printf '%s' "$review_body" | python3 -c "import sys,json; print(json.dumps(sys.stdin.read()))")

    local post_resp
    post_resp=$(curl -sf -X POST \
        -H "Accept: application/vnd.github+json" \
        -H "Authorization: Bearer $GITHUB_TOKEN" \
        -H "X-GitHub-Api-Version: 2022-11-28" \
        "https://api.github.com/repos/$repo/issues/$pr_number/comments" \
        -d "{\"body\": $comment_body_json}" 2>&1) || true

    if echo "$post_resp" | jq -e '.id' > /dev/null 2>&1; then
        log_success "Posted self-review comment on PR #$pr_number"
    else
        log_warning "Failed to post review comment: $(echo "$post_resp" | head -c 200)"
    fi

    rm -f /tmp/ai_review_prompt.txt
    return 0
}

# ──────────────────────────────────────────────────────────────
# CLI
# ──────────────────────────────────────────────────────────────
if [[ "${BASH_SOURCE[0]}" == "${0}" ]]; then
    set -e          # strict mode only when running standalone
    set -o pipefail
    case "${1:-}" in
        extract-errors)
            [[ $# -eq 3 ]] || { log_error "Usage: $0 extract-errors <log> <out>"; exit 1; }
            extract_all_errors "$2" "$3"
            ;;
        apply-ai-fixes)
            [[ $# -eq 2 ]] || { log_error "Usage: $0 apply-ai-fixes <error_file>"; exit 1; }
            run_fix_pipeline "$2"
            ;;
        review-pr)
            [[ $# -eq 3 ]] || { log_error "Usage: $0 review-pr <repo> <pr_number>"; exit 1; }
            review_ai_pr "$2" "$3"
            ;;
        fetch-sonar-issues)
            [[ $# -ge 1 ]] || { log_error "Usage: $0 fetch-sonar-issues [error_file]"; exit 1; }
            _cmd_fetch_sonar_issues "${2:-error_analysis.md}"
            ;;
        *)
            log_info "AI Code Fixing Utilities (generic)"
            log_info "  extract-errors <log> <out>     Extract errors from build log"
            log_info "  apply-ai-fixes <error_file>    Full AI-powered fix pipeline"
            log_info "  review-pr <repo> <pr_number>   AI self-review of a PR"
            log_info "  fetch-sonar-issues [err_file]  Fetch SonarQube issues & append"
            ;;
    esac
fi
