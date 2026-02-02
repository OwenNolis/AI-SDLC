# Task: AI Code Review (PR)

Input:

- Diff: {{GIT_DIFF}}
- TA: {{TA_MARKDOWN}}
- Conventions: /docs/context/**

Doel:

1) Check alignment met TA & requirements
2) Style/pattern compliance
3) Test coverage gaps
4) Security checks (input validation, authz, injection, secrets)
5) Performance pitfalls
6) Geef concrete, actiegerichte comments

Output:

- Summary (2-5 bullets)
- Must Fix (lijst)
- Should Fix
- Nice to have
- Test suggestions
