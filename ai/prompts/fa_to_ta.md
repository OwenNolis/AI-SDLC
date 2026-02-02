# Task: Functionele Analyse → Technische Analyse

Input:

- FA: {{FA_MARKDOWN}}
- Extra context folder: /docs/context/**
- (Optioneel) eerdere TA's: /docs/technical-analysis/**
- TA schema: {{TA_SCHEMA_JSON}}

Doel:
Genereer:

1) TA als Markdown volgens secties hieronder
2) TA als JSON die valideert tegen het schema

TA secties (Markdown):

1. Scope
2. Assumptions
3. Open Questions
4. Domain Model
5. API Design (endpoints, DTOs, validation, errors)
6. Backend Design (layers, classes, responsibilities)
7. Frontend Design (pages/components, state, API client)
8. Security & Privacy
9. Observability (logging, metrics)
10. Performance & Scalability notes
11. Test Strategy (unit/integration/e2e)
12. Traceability matrix (REQ → endpoint/UI → tests → modules)

Constraints:

- Respecteer conventions uit /docs/context/**
- Gebruik consistente naming.
- Iedere requirement uit FA moet in traceability terugkomen.
- Geef expliciete foutcodes + error response format.
- Geen implementation details die conflicteren met stack (Spring Boot/React).

Output:

- `## TA (Markdown)` gevolgd door TA
- `## TA (JSON)` gevolgd door JSON (in codeblock)
