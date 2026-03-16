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

RULE_JSON=$(curl -s -u "$SONAR_TOKEN:" "$SONAR_API")

# Extract useful fields
NAME=$(echo "$RULE_JSON" | jq -r '.rule.name // ""')
DESC=$(echo "$RULE_JSON" | jq -r '.rule.htmlDesc // .rule.description // ""' | sed 's/<[^>]*>//g')
SEVERITY=$(echo "$RULE_JSON" | jq -r '.rule.severity // ""')
LANG=$(echo "$RULE_JSON" | jq -r '.rule.langName // ""')
TAGLIST=$(echo "$RULE_JSON" | jq -r '.rule.tags | join(", ")')

# Output markdown
cat <<EOF
**Rule:** $RULE_KEY ($NAME)
- **Severity:** $SEVERITY
- **Language:** $LANG
- **Tags:** $TAGLIST

**Description:**
$DESC
EOF
