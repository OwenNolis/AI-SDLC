#!/usr/bin/env bash
set -euo pipefail

FEATURE="${1:-}"
if [[ -z "$FEATURE" ]]; then
  echo "Usage: ./ai/sync-from-fa.sh <feature-id>"
  exit 1
fi

FA="docs/functional-analysis/${FEATURE}.md"
TA="docs/technical-analysis/${FEATURE}.ta.json"
FLOW="docs/test-scenarios/${FEATURE}.flow.json"
CTX="docs/test-context/${FEATURE}.md"

if [[ ! -f "$FA" || ! -f "$TA" || ! -f "$FLOW" ]]; then
  echo "Missing files. Expected:"
  echo "  $FA"
  echo "  $TA"
  echo "  $FLOW"
  exit 1
fi

touch "$CTX"

echo "==> Syncing from FA: $FA"

# Ensure scenarios exists (optional in schema, but useful for generators)
if ! jq -e '.scenarios' "$FLOW" >/dev/null 2>&1; then
  jq '.scenarios = []' "$FLOW" > "$FLOW.tmp" && mv "$FLOW.tmp" "$FLOW"
fi

# ------------------------------------------------------------
# Generic: Additional business rules (FA) -> flow.scenarios[]
# ------------------------------------------------------------

# slugify: stable scenario ids derived from rule text
slugify() {
  echo "$1" \
    | tr '[:upper:]' '[:lower:]' \
    | sed -E 's/[^a-z0-9]+/_/g' \
    | sed -E 's/^_+|_+$//g' \
    | cut -c1-60
}

# extract bullets under "### Additional business rules"
extract_additional_business_rules() {
  awk '
    BEGIN { in=0 }
    /^[[:space:]]*##[[:space:]]/ { if (in==1) exit }          # stop at next H2
    /^[[:space:]]*### / {
      if (tolower($0) ~ /^### additional business rules/) { in=1; next }
      if (in==1) exit                                         # stop if another H3 starts
    }
    {
      if (in==1 && $0 ~ /^[[:space:]]*-[[:space:]]+/) {
        sub(/^[[:space:]]*-[[:space:]]+/, "", $0)
        print $0
      }
    }
  ' "$FA"
}

add_flow_scenario_generic() {
  local ruleText="$1"
  local sid="br_$(slugify "$ruleText")"

  # already exists?
  if jq -e --arg id "$sid" '.scenarios[]? | select(.id==$id)' "$FLOW" >/dev/null 2>&1; then
    return 0
  fi

  # heuristic type (keep it simple + deterministic)
  local t="$(echo "$ruleText" | tr '[:upper:]' '[:lower:]')"
  local stype="validation"
  if echo "$t" | grep -Eq "happy|success|allowed"; then
    stype="happy-path"
  fi

  echo "Adding generic flow scenario from FA rule: $sid"
  jq --arg id "$sid" --arg title "Business rule: $ruleText" --arg type "$stype" \
    '.scenarios += [{"id":$id,"title":$title,"type":$type}]' \
    "$FLOW" > "$FLOW.tmp" && mv "$FLOW.tmp" "$FLOW"
}

# Apply generic rule -> scenarios
while IFS= read -r rule; do
  [[ -z "$rule" ]] && continue
  add_flow_scenario_generic "$rule"
done < <(extract_additional_business_rules)

# ------------------------------------------------------------
# Existing helpers (kept from your script)
# ------------------------------------------------------------

add_req () {
  local reqId="$1"
  local text="$2"
  local priority="$3"

  if grep -q "\"id\": \"${reqId}\"" "$TA"; then
    return 0
  fi

  echo "Adding requirement ${reqId} to TA"
  jq --arg id "$reqId" --arg text "$text" --arg pr "$priority" \
    '.requirements += [{"id":$id,"text":$text,"priority":$pr}]' \
    "$TA" > "$TA.tmp" && mv "$TA.tmp" "$TA"
}

append_ctx () {
  local marker="$1"
  shift
  local block="$*"

  if grep -q "$marker" "$CTX"; then
    return 0
  fi

  printf "\n%s\n" "$block" >> "$CTX"
}

add_flow_scenario () {
  local sid="$1"
  local title="$2"
  local type="$3"

  if grep -q "\"id\": \"${sid}\"" "$FLOW"; then
    return 0
  fi

  echo "Adding flow scenario ${sid}"
  jq --arg id "$sid" --arg title "$title" --arg type "$type" \
    '.scenarios += [{"id":$id,"title":$title,"type":$type}]' \
    "$FLOW" > "$FLOW.tmp" && mv "$FLOW.tmp" "$FLOW"
}

add_flow_variant () {
  local vtype="$1"
  local vname="$2"
  local action="$3"
  local expected="$4"

  if grep -q "\"name\": \"${vname}\"" "$FLOW"; then
    return 0
  fi

  echo "Adding flow variant: ${vname}"
  jq --arg type "$vtype" --arg name "$vname" --arg action "$action" --arg expected "$expected" \
    '.flows |= (map(if .id=="FLOW-001" then .variants += [{"type":$type,"name":$name,"steps":[{"actor":"system","action":$action,"expected":$expected}]}] else . end))' \
    "$FLOW" > "$FLOW.tmp" && mv "$FLOW.tmp" "$FLOW"
}

# ------------------------------------------------------------
# Your specific mappings (still useful to patch TA + context)
# ------------------------------------------------------------

# RULE 1: max 3 tickets/day
if grep -qiE "only add 3 tickets per day|max 3 tickets per day|3 tickets per day" "$FA"; then
  add_req "REQ-009" "User can create at most 3 tickets per day." "must"

  add_flow_scenario \
    "create_ticket_limit_3_per_day" \
    "Ticket creation fails when user exceeds 3 tickets in one day" \
    "validation"

  add_flow_variant \
    "negative" \
    "User exceeds 3 tickets per day" \
    "Reject ticket creation after 3 tickets created in the same day" \
    "400 ApiError with code LIMIT_EXCEEDED (or similar)"

  append_ctx "REQ-009" "$(cat <<'EOF'
## Daily ticket creation limit (REQ-009)
- A user may create at most 3 tickets per calendar day.
- The 4th attempt must fail (400 or 429 depending on API decision).
- Regression guardrail to prevent burst abuse / accidental spam.
EOF
)"
fi

# RULE 2: HIGH completed before LOW
if grep -qiE "HIGH must always be completed before.*LOW|completed before.*LOW" "$FA"; then
  add_req "REQ-010" "Tickets with priority HIGH must be completed before tickets with priority LOW." "must"

  if ! grep -q "Priority processing order: HIGH > MEDIUM > LOW (REQ-010)" "$TA"; then
    echo "Adding TA assumption: Priority processing order: HIGH > MEDIUM > LOW (REQ-010)"
    jq '.assumptions += ["Priority processing order: HIGH > MEDIUM > LOW (REQ-010)"]' \
      "$TA" > "$TA.tmp" && mv "$TA.tmp" "$TA"
  fi

  add_flow_scenario \
    "ticket_priority_completion_order" \
    "HIGH priority tickets are completed before LOW priority tickets" \
    "validation"

  add_flow_variant \
    "negative" \
    "LOW completed before HIGH is not allowed" \
    "Attempt to complete a LOW ticket while a HIGH ticket is still open" \
    "Operation rejected (409 or 400) with code PRIORITY_ORDER_VIOLATION (or similar)"

  append_ctx "REQ-010" "$(cat <<'EOF'
## Priority completion order (REQ-010)
- If a HIGH priority ticket exists, LOW priority tickets must not be completed before that HIGH ticket is completed.
- Expected behavior: completing LOW while HIGH is still OPEN should be rejected (409/400) with a stable error code.
- This rule is about workflow/processing order (not ticket creation).
EOF
)"
fi

echo "==> FA sync finished."