## Regression notes

- A previous bug allowed tickets with empty descriptions when submitted via API.
  Always include a regression test for this.

## Non-obvious cases

- Validation errors should always include a correlationId.
- Multiple field errors should be returned in a single response.

## Performance assumptions

- Ticket creation should respond within 500ms under normal load.
