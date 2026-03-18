#!/bin/bash
# Fetches SonarQube rule details for a given rule key and outputs markdown
# Usage: sonar-rule-details.sh <rule_key>

RULE_KEY="$1"
SONAR_TOKEN="$SONAR_TOKEN"
SONAR_ORG="owennolis"
SONAR_API="https://sonarcloud.io/api/rules/show?key=$RULE_KEY"

if [ -z "$RULE_KEY" ]; then
  echo "No rule key provided" >&2
  exit 1
fi

RULE_JSON=$(curl -s -u "$SONAR_TOKEN:" "$SONAR_API" 2>/dev/null || echo '{}')

# Validate the response is parseable JSON before extracting fields
if ! echo "$RULE_JSON" | jq empty 2>/dev/null; then
  RULE_JSON='{}'
fi

# Extract useful fields (all with fallbacks so jq never errors)
NAME=$(echo "$RULE_JSON" | jq -r '.rule.name // ""' 2>/dev/null || true)
DESC=$(echo "$RULE_JSON" | jq -r '.rule.htmlDesc // .rule.mdDesc // .rule.description // ""' 2>/dev/null \
  | sed 's/<[^>]*>//g' || true)
SEVERITY=$(echo "$RULE_JSON" | jq -r '.rule.severity // ""' 2>/dev/null || true)
LANG=$(echo "$RULE_JSON" | jq -r '.rule.langName // ""' 2>/dev/null || true)
TAGLIST=$(echo "$RULE_JSON" | jq -r '((.rule.sysTags // []) + (.rule.tags // [])) | unique | join(", ")' 2>/dev/null || true)

# Output markdown
cat <<EOF
**Rule:** $RULE_KEY ($NAME)
- **Severity:** $SEVERITY
- **Language:** $LANG
- **Tags:** $TAGLIST

**Description:**
$DESC
EOF
