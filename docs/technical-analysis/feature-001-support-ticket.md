# Technical Analysis — Feature-001 Support Ticket

## 1. Scope

### In scope

- POST /api/tickets
- Persist SupportTicket entity
- UI form + success screen

### Out of scope

- Update/close
- Attachments
- Email

## 2. Assumptions

- Auth is aanwezig op platformniveau; endpoint vereist ingelogde user (placeholder: "authenticated").
- We gebruiken JPA + relationele DB.
- Correlation ID komt uit header `X-Correlation-Id` (of wordt gegenereerd server-side).

## 3. Open Questions

- Moet ticket gekoppeld worden aan userId? (nu: optioneel / TODO)
- Is er een bestaande globale ErrorResponse standard? (we definiëren hieronder eentje)

## 4. Domain Model

Entity: SupportTicket

- id: UUID
- ticketNumber: string (TCK-YYYY-000001)
- subject: string
- description: string
- priority: enum {LOW, MEDIUM, HIGH}
- status: enum {OPEN}
- createdAt: Instant

## 5. API Design

### Error format

`ApiError`

- correlationId: string
- code: string
- message: string
- fieldErrors?: [{field, message}]

### Endpoint

**POST /api/tickets**
Auth: authenticated

Request body: CreateTicketRequest

- subject: string (min 5, max 120)
- description: string (min 20, max 2000)
- priority: "LOW"|"MEDIUM"|"HIGH"

Responses:

- 201: CreateTicketResponse { ticketNumber, status }
- 400: ApiError met fieldErrors
- 500: ApiError met correlationId

## 6. Backend Design

Packages:

- controller: TicketController
- service: TicketService, TicketNumberGenerator
- repository: SupportTicketRepository
- domain: SupportTicket, Priority, TicketStatus
- dto: CreateTicketRequest, CreateTicketResponse, ApiError, FieldError
- error: GlobalExceptionHandler

TicketNumberGenerator:

- format: TCK-{year}-{seq}
- seq per year via DB table `ticket_sequence` of simpler: DB sequence + year prefix (demo: database sequence zonder reset, maar year in string)

## 7. Frontend Design

Route: /tickets/new
Components:

- TicketCreatePage
- TicketForm
API client:
- POST /api/tickets

UX:

- Client-side validation (zelfde regels)
- Disable submit if invalid
- Show field errors from 400
- Show generic error with correlation id for 500

## 8. Security & Privacy

- Geen gevoelige data.
- Server-side validation blijft verplicht.
- Auth placeholder.

## 9. Observability

- Log ticket created: ticketNumber, priority, correlationId
- Voeg correlationId toe aan responses bij errors

## 10. Performance

- Eén insert + ticketNumber generation; target p95 < 300ms haalbaar.
- TicketNumber generator moet concurrency safe zijn (in demo: DB sequence).

## 11. Test Strategy

Unit:

- TicketService creates OPEN status
- Validation rules via Bean Validation
Integration:
- POST /api/tickets returns 201 + correct payload
- Invalid request returns 400 + fieldErrors
Frontend:
- Form validation
- Submit success path
- Field error rendering

## 12. Traceability

| REQ | Backend | Frontend | Tests |
| --- | --- | --- | --- |
| REQ-001 | TicketController, TicketService | TicketForm | TicketControllerIT, TicketForm.test |
| REQ-002 | CreateTicketRequest validation | TicketForm validation | TicketValidationTest |
| REQ-003 | CreateTicketRequest validation | TicketForm validation | TicketValidationTest |
| REQ-004 | Priority enum validation | priority select | TicketValidationTest |
| REQ-005 | CreateTicketResponse | success message | TicketControllerIT |
| REQ-006 | GlobalExceptionHandler 400 mapping | error UI | TicketControllerIT, TicketForm.test |
| REQ-007 | ApiError correlationId | generic error UI | TicketControllerIT |
