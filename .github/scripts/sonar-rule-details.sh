#!/bin/bash
# Fetches SonarQube rule details for a given rule key and outputs markdown
# Usage: sonar-rule-details.sh <rule_key>

RULE_KEY="$1"
SONAR_TOKEN="$SONAR_TOKEN"
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

# Name
NAME=$(echo "$RULE_JSON" | jq -r '.rule.name // ""' 2>/dev/null || true)

# Type (CODE_SMELL, BUG, VULNERABILITY, SECURITY_HOTSPOT)
TYPE=$(echo "$RULE_JSON" | jq -r '.rule.type // ""' 2>/dev/null || true)

# Severity — new API uses impacts[].severity (HIGH/MEDIUM/LOW),
# fall back to legacy .rule.severity (BLOCKER/CRITICAL/MAJOR/MINOR/INFO)
SEVERITY=$(echo "$RULE_JSON" | jq -r '
  (.rule.impacts // []) | map(.severity) | first // .rule.severity // ""
' 2>/dev/null || true)
# If impacts array was present but first returned nothing, fall back to legacy
if [ -z "$SEVERITY" ]; then
  SEVERITY=$(echo "$RULE_JSON" | jq -r '.rule.severity // ""' 2>/dev/null || true)
fi

# Language
LANG=$(echo "$RULE_JSON" | jq -r '.rule.langName // ""' 2>/dev/null || true)

# Tags — combine system tags and user tags
TAGLIST=$(echo "$RULE_JSON" | \
  jq -r '((.rule.sysTags // []) + (.rule.tags // [])) | unique | join(", ")' \
  2>/dev/null || true)

# Description — try descriptionSections (new API), then htmlDesc/mdDesc (legacy)
DESC=$(echo "$RULE_JSON" | jq -r '
  (
    (.rule.descriptionSections // [])
    | map(select(.key == "root_cause" or .key == "introduction" or .key == "default"))
    | .[0].content
  ) //
  .rule.htmlDesc //
  .rule.mdDesc //
  ""
' 2>/dev/null | python3 -c "
import sys, re
txt = sys.stdin.read()
txt = re.sub(r'<[^>]+>', '', txt)   # strip HTML tags
txt = re.sub(r'\s+', ' ', txt).strip()
print(txt[:400])
" 2>/dev/null || true)

# Output markdown
{
  echo "**Rule:** $RULE_KEY${NAME:+ — ${NAME}}"
  [ -n "$TYPE" ]     && echo "- **Type:** $TYPE"
  [ -n "$SEVERITY" ] && echo "- **Severity:** $SEVERITY"
  [ -n "$LANG" ]     && echo "- **Language:** $LANG"
  [ -n "$TAGLIST" ]  && echo "- **Tags:** $TAGLIST"
  [ -n "$DESC" ]     && echo "- **Description:** $DESC"
}
