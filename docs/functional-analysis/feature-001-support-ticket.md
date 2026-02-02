# Feature-001: Support Ticket aanmaken

## Doel

Als gebruiker wil ik een support ticket kunnen aanmaken zodat ik een probleem kan melden.

## Scope

In scope:

- Ticket aanmaken via UI
- Validatie van input
- Ticket opslaan
- Ticket ID + status teruggeven
- Foutmeldingen op UI

Out of scope:

- Ticket bewerken/sluiten
- Attachments
- Email notificaties

## Requirements

- REQ-001: Gebruiker kan een ticket aanmaken met velden: subject, description, priority.
- REQ-002: Subject is verplicht (min 5, max 120).
- REQ-003: Description is verplicht (min 20, max 2000).
- REQ-004: Priority is verplicht en één van: LOW, MEDIUM, HIGH.
- REQ-005: Na succesvol aanmaken krijgt gebruiker ticketNumber + status = OPEN te zien.
- REQ-006: Bij validatiefouten worden veldspecifieke fouten getoond.
- REQ-007: Server errors tonen generieke foutmelding met correlation id.

## Business rules

- BR-001: ticketNumber is uniek en leesbaar formaat: TCK-YYYY-000001 (sequence per jaar).
- BR-002: status bij creatie is altijd OPEN.

## Non-functional

- NFR-001: API response time p95 < 300ms voor create.
- NFR-002: Logging bevat correlation id.

## UX notes

- Form met 3 velden + submit.
- Submit disabled tot form geldig is.
- Loading indicator tijdens submit.
