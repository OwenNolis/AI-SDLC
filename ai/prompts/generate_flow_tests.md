# Task: TA/FA → Flow-based Test Scenarios

Input:

- FA: {{FA_MARKDOWN}}
- TA: {{TA_MARKDOWN}}
- Flow schema: {{FLOW_SCHEMA_JSON}}

Doel:
Genereer flow-based test scenarios voor functionele testing:

- Happy flow + alternatieve flows + negatieve flows
- Data varianten
- Verwachte API calls + UI state updates
- Observability checks

Output:

1) Markdown: leesbaar voor analisten/testers
2) JSON: valideert tegen flow schema

Output format:

- `## Flow Tests (Markdown)`
- `## Flow Tests (JSON)` in codeblock
