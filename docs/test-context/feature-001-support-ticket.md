## Regression notes

- A previous bug allowed tickets with empty descriptions when submitted via API. Always include a regression test for this.
- Ensure tests cover the daily limits for total tickets and high-priority tickets.
- Verify that attempting to create a ticket with a duplicate subject on the same day results in an appropriate error.

## Non-obvious cases

- Validation errors should always include a correlationId.
- Multiple field errors should be returned in a single response.
- The system must correctly track and enforce daily limits for both total tickets and high-priority tickets per user.
- The completion order logic (HIGH before LOW/MEDIUM) should be robust and handle edge cases.

## Performance assumptions

- Ticket creation should respond within 500ms under normal load.
- API responses for validation errors (e.g., duplicate subject, limits exceeded) should be fast.

## Daily ticket creation limit (REQ-009, REQ-016, REQ-022, REQ-027, REQ-032)

- A user may create at most 3 tickets per calendar day.
- The 4th attempt to create any ticket on the same day must fail.
- This prevents abuse and accidental duplicate submissions.

## High priority ticket creation limit (REQ-013, REQ-017, REQ-023, REQ-028, REQ-033)

- A user may create at most 2 HIGH priority tickets per calendar day.
- The 3rd attempt to create a HIGH priority ticket on the same day must fail.

## Priority completion order (REQ-010, REQ-018, REQ-024, REQ-012, REQ-019, REQ-025, REQ-029, REQ-030)

- If a HIGH priority ticket exists and is still OPEN, LOW and MEDIUM priority tickets must not be completed before that HIGH ticket is completed.
- Expected behavior: attempting to complete a LOW or MEDIUM ticket while a HIGH ticket is still OPEN should be rejected (e.g., 409 Conflict or 400 Bad Request) with a stable error code.
- This rule pertains to the workflow/processing order of tickets, not the creation order.

## Immediate visibility of HIGH priority tickets (REQ-011, REQ-014, REQ-020, REQ-031, REQ-034)

- Tickets with HIGH priority should be immediately visible to the user upon creation, without requiring a page refresh or explicit search.
- This implies the frontend should update its state promptly upon successful creation.

## Unique subject per day (REQ-008, REQ-015, REQ-021, REQ-026)

- Ticket subjects must be unique within the same calendar day for a given user.
- Attempting to create a ticket with a duplicate subject on the same day should result in an error (e.g., 409 Conflict or 400 Bad Request).
