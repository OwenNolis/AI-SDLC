#!/usr/bin/env bash
set -euo pipefail

FEATURE="${1:-}"
if [[ -z "$FEATURE" ]]; then
  echo "Usage: ./ai/sync-from-fa.sh <feature-id>"
  exit 1
fi

node ai/sync-flow-scenarios-from-fa.mjs "$FEATURE"

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

# Helper: add requirement if missing
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

# Helper: append test-context section if missing marker
append_ctx () {
  local marker="$1"
  shift
  local block="$*"

  if grep -q "$marker" "$CTX"; then
    return 0
  fi

  printf "\n%s\n" "$block" >> "$CTX"
}

# Helper: add scenario entry if flow has scenarios[] and id missing
add_flow_scenario () {
  local sid="$1"
  local title="$2"
  local type="$3"

  if grep -q "\"id\": \"${sid}\"" "$FLOW"; then
    return 0
  fi

  # if scenarios doesn't exist, create it as empty array (keeps schema valid since it's optional)
  if ! jq -e '.scenarios' "$FLOW" >/dev/null 2>&1; then
    jq '.scenarios = []' "$FLOW" > "$FLOW.tmp" && mv "$FLOW.tmp" "$FLOW"
  fi

  echo "Adding flow scenario ${sid}"
  jq --arg id "$sid" --arg title "$title" --arg type "$type" \
    '.scenarios += [{"id":$id,"title":$title,"type":$type}]' \
    "$FLOW" > "$FLOW.tmp" && mv "$FLOW.tmp" "$FLOW"
}

# Helper: add variant to FLOW-001 if not already present by variant name
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

# -----------------------------
# RULE 1: max 3 tickets/day
# -----------------------------
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

# -----------------------------
# RULE 2: HIGH completed before LOW
# -----------------------------
if grep -qiE "HIGH must always be completed before.*LOW|completed before.*LOW" "$FA"; then
  add_req "REQ-010" "Tickets with priority HIGH must be completed before tickets with priority LOW." "must"

  # Put the intent into TA as a constraint (string-based, schema-safe)
  if ! grep -q "priorityOrder:HIGH>MEDIUM>LOW" "$TA"; then
    echo "Adding TA constraint priorityOrder:HIGH>MEDIUM>LOW (domain-level, schema-safe)"
    # Store as a domain-level note via assumptions (schema-safe + no extra props)
    jq '.assumptions += ["Priority processing order: HIGH > MEDIUM > LOW (REQ-010)"]' \
      "$TA" > "$TA.tmp" && mv "$TA.tmp" "$TA"
  fi

  add_flow_scenario \
    "ticket_priority_completion_order" \
    "HIGH priority tickets are completed before LOW priority tickets" \
    "happy-path"

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