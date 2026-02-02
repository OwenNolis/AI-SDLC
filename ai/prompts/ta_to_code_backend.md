# Task: TA → Backend Implementation (Spring Boot)

Input:

- TA (Markdown): {{TA_MARKDOWN}}
- Repo conventions: /docs/context/**
- Bestaande codebase snapshot (optioneel): {{CODE_INDEX}}

Doel:
Genereer werkende backend code:

- Controller + service + repository + entity + DTOs
- Validation annotations
- Exception handling (ControllerAdvice)
- Logging
- OpenAPI annotations (indien conventie)
- Tests: unit + integration

Constraints:

- Maven project, Java 17, Spring Boot 3
- Gebruik package structuur volgens coding-standards.
- Geen business logic in controller.
- Return consistente error responses.

Output:
Voor elk bestand:

- `### File: <path>`
- codeblock met inhoud
