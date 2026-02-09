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
