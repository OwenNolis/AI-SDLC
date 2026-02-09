#!/usr/bin/env bash
set -euo pipefail

FEATURE="$1"

FA="docs/functional-analysis/${FEATURE}.md"
TA="docs/technical-analysis/${FEATURE}.ta.json"
FLOW="docs/test-scenarios/${FEATURE}.flow.json"
CTX="docs/test-context/${FEATURE}.md"

if ! grep -qi "3 tickets per day" "$FA"; then
  echo "No new FA rules detected (3 tickets/day)"
  exit 0
fi

echo "Detected FA rule: max 3 tickets per day"

# --- Patch TA (idempotent) ---
if ! grep -q "REQ-009" "$TA"; then
  echo "Adding REQ-009 to TA"
  jq '.requirements += [{
    "id":"REQ-009",
    "text":"User can create at most 3 tickets per day.",
    "priority":"must"
  }]' "$TA" > "$TA.tmp" && mv "$TA.tmp" "$TA"
fi

# --- Patch Flow ---
if ! grep -q "create_ticket_limit_3_per_day" "$FLOW"; then
  echo "Adding flow scenario for daily ticket limit"
  jq '.flows[0].variants += [{
    "type":"negative",
    "name":"User exceeds 3 tickets per day",
    "steps":[{
      "actor":"system",
      "action":"Reject ticket creation after 3 tickets created in same day",
      "expected":"400 with field error on limit"
    }]
  }]' "$FLOW" > "$FLOW.tmp" && mv "$FLOW.tmp" "$FLOW"
fi

# --- Patch test-context ---
if ! grep -q "REQ-009" "$CTX"; then
  echo "Updating test-context"
  cat >> "$CTX" <<EOF

## Daily ticket creation limit (REQ-009)
- A user may create at most 3 tickets per calendar day.
- The 4th attempt must fail.
- Prevents abuse and accidental duplicate submissions.
EOF
fi

echo "FA sync completed."