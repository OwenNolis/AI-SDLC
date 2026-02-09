## Regression notes

- A previous bug allowed tickets with empty descriptions when submitted via API.
  Always include a regression test for this.

## Non-obvious cases

- Validation errors should always include a correlationId.
- Multiple field errors should be returned in a single response.

## Performance assumptions

- Ticket creation should respond within 500ms under normal load.
## Daily ticket creation limit (REQ-009)
- A user may create at most 3 tickets per calendar day.
- The 4th attempt must fail.
- Prevents abuse and accidental duplicate submissions.

## Priority completion order (REQ-010)
- If a HIGH priority ticket exists, LOW priority tickets must not be completed before that HIGH ticket is completed.
- Expected behavior: completing LOW while HIGH is still OPEN should be rejected (409/400) with a stable error code.
- This rule is about workflow/processing order (not ticket creation).
