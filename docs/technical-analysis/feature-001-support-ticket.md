# Technische Analyse: Feature-001: Support Ticket aanmaken

## 1. Scope

### In scope
- Ticket aanmaken via UI
- Validatie van input
- Ticket opslaan
- Ticket ID + status teruggeven
- Foutmeldingen op UI

### Out of scope
- Ticket bewerken/sluiten
- Attachments
- Email notificaties

## 2. Assumptions
- Er is een bestaand authenticatie mechanisme dat een bearer token levert.
- De gebruiker die een ticket aanmaakt is geïdentificeerd via het bearer token.
- De database is beschikbaar en geconfigureerd.

## 3. Open Questions
- Hoe wordt de `createdByUserId` verkregen uit het bearer token?
- Wat is de exacte implementatie van de `uniquePerDay` validatie voor het onderwerp? Moet dit op applicatie niveau of database niveau?
- Hoe wordt omgegaan met het genereren van het ticketnummer als de sequentie per jaar gereset wordt? Is er een aparte service of mechanisme voor?
- Wat is de exacte implementatie van de `TicketPriorityCompletionOrderValidator`? Moet dit een aparte service zijn of onderdeel van de TicketService?

## 4. Domain Model

### Entiteiten
| Entiteit | Velden | Constraints |
|----------|--------|-------------|
| Ticket | id: Long | notNull |
| | ticketNumber: String | notNull, unique |
| | subject: String | notNull, minLength:5, maxLength:120, uniquePerDay |
| | description: String | notNull, minLength:20, maxLength:2000 |
| | priority: Priority | notNull |
| | status: Status | notNull |
| | createdAt: LocalDateTime | notNull |
| | createdByUserId: Long | notNull |
| User | id: Long | notNull |
| | username: String | notNull, unique |
| TicketDailyLimit | userId: Long | notNull |
| | date: LocalDate | notNull |
| | ticketCount: Integer | notNull, greaterThanOrEqual:0 |
| | highPriorityTicketCount: Integer | notNull, greaterThanOrEqual:0 |

### Enums
- **Priority**: LOW, MEDIUM, HIGH
- **Status**: OPEN, IN_PROGRESS, CLOSED

## 5. API Design

### Endpoints
| Method | Path | Beschrijving |
|--------|------|--------------|
| POST | /api/tickets | Maak een nieuw ticket aan |
| GET | /api/tickets/{ticketId} | Haal een specifiek ticket op |
| GET | /api/tickets | Haal een lijst van tickets op |
| PUT | /api/tickets/{ticketId}/status | Update de status van een ticket |

### Request DTO
#### CreateTicketRequest
```json
{
  "subject": "string",
  "description": "string",
  "priority": "LOW | MEDIUM | HIGH",
  "createdByUserId": "long"
}
```

#### UpdateTicketStatusRequest
```json
{
  "status": "OPEN | IN_PROGRESS | CLOSED"
}
```

### Response DTO
#### TicketResponse
```json
{
  "ticketId": "long",
  "ticketNumber": "string",
  "subject": "string",
  "description": "string",
  "priority": "LOW | MEDIUM | HIGH",
  "status": "OPEN | IN_PROGRESS | CLOSED",
  "createdAt": "datetime",
  "createdByUserId": "long"
}
```

#### TicketListResponse
```json
{
  "tickets": [
    {
      "ticketId": "long",
      "ticketNumber": "string",
      "subject": "string",
      "priority": "LOW | MEDIUM | HIGH",
      "status": "OPEN | IN_PROGRESS | CLOSED",
      "createdAt": "datetime"
    }
  ]
}
```

### Error formaat
```json
{
  "correlationId": "uuid",
  "code": "ERROR_CODE",
  "message": "Beschrijving",
  "fieldErrors": [
    {
      "field": "fieldName",
      "message": "Error message for field"
    }
  ]
}
```

## 6. Backend Design

### Lagen
- **Controller**: Behandelt inkomende HTTP-requests en valideert basisinput.
- **Service**: Bevat de business logica en orkestreert repositories en validators.
- **Repository**: Verantwoordelijk voor data-access operaties.
- **Validator**: Bevat specifieke validatielogica.

### Klassen
| Klasse | Verantwoordelijkheid |
|--------|---------------------|
| TicketController | Exposeert REST endpoints voor ticket aanmaken en ophalen. |
| TicketService | Implementeert business logica voor ticket aanmaken, inclusief limieten en prioriteitsregels. |
| TicketRepository | Data-access operaties voor tickets. |
| Ticket | JPA entiteit voor tickets. |
| CreateTicketRequest | Request DTO voor ticket aanmaken. |
| TicketResponse | Response DTO voor ticket informatie. |
| TicketListResponse | Response DTO voor een lijst met tickets. |
| UpdateTicketStatusRequest | Request DTO voor het updaten van de ticket status. |
| TicketNotFoundException | Exception bij ontbrekende ticket. |
| TicketSubjectUniquePerDayValidator | Valideert de uniciteit van het onderwerp per dag. |
| TicketDailyLimitValidator | Valideert de dagelijkse ticket limiet per gebruiker. |
| HighPriorityTicketDailyLimitValidator | Valideert de dagelijkse limiet voor high priority tickets per gebruiker. |
| TicketPriorityCompletionOrderValidator | Valideert de voltooiingsvolgorde op basis van prioriteit. |
| TicketNumberGenerator | Genereert unieke ticketnummers. |
| UserRepository | Data-access operaties voor gebruikers. |
| User | JPA entiteit voor gebruikers. |
| TicketDailyLimitRepository | Data-access operaties voor dagelijkse ticket limieten. |
| TicketDailyLimit | JPA entiteit voor dagelijkse ticket limieten. |
| ApiError | Standaard fout response formaat. |
| FieldError | Representeert een specifieke validatiefout. |
| GlobalExceptionHandler | Centrale exception handler. |
| CorrelationIdFilter | Beheert correlation IDs. |
| LoggingAspect | Voegt logging toe aan methodes. |
| EnumConverter | Converteert string representaties naar enum waarden. |

## 7. Frontend Design
- **Routes**:
    - `/tickets/create`: Formulier voor het aanmaken van een nieuw ticket.
    - `/tickets/{ticketId}`: Detailpagina voor een specifiek ticket.
    - `/tickets`: Lijstweergave van tickets.
- **Componenten**:
    - `TicketCreateForm`: Component voor het invoeren van ticketgegevens.
    - `TicketDetails`: Component voor het weergeven van ticketinformatie.
    - `TicketList`: Component voor het weergeven van een lijst met tickets.
    - `ErrorMessage`: Component voor het tonen van foutmeldingen.

## 8. Security & Privacy
- Endpoints vereisen authenticatie (bearer token).
- Input validatie aan de backend om data-integriteit te waarborgen.
- Gevoelige informatie (zoals user ID) wordt alleen verwerkt waar nodig en niet onnodig blootgesteld.

## 9. Observability
- **Logging**:
    - Inkomende requests (met correlationId).
    - Business events (bv. ticket aangemaakt, limiet overschreden).
    - Fouten (met correlationId en stack trace).
    - Gebruik van `LoggingAspect` voor consistente logging.
- **Metrics**:
    - Request count per endpoint.
    - Response tijd (p95) per endpoint.
    - Error rate per endpoint.
    - Aantal aangemaakte tickets per gebruiker per dag.
    - Aantal high priority tickets per gebruiker per dag.
- **Correlation ID**: Wordt gegenereerd door `CorrelationIdFilter` en meegestuurd in alle logs en responses.

## 10. Performance & Scalability
- Database indexen op veelgebruikte filtervelden (bv. `status`, `priority`, `createdByUserId`).
- p95 responstijd voor `/api/tickets` POST endpoint < 300ms (REQ-010).
- Optimalisatie van database queries voor het ophalen van lijsten met tickets.
- Schaalbaarheid van de applicatie door stateless services en horizontale schaalbaarheid.

## 11. Test Strategy

### Unit tests
- Validators (bv. `TicketSubjectUniquePerDayValidator`, `TicketDailyLimitValidator`).
- Business logica in de `TicketService`.
- `TicketNumberGenerator`.
- Data-access methodes in de repositories (mocked).

### Integration tests
- Volledige request/response cyclus voor `/api/tickets` (POST).
- Testen van successcenario's (201 Created) en foutscenario's (400 Bad Request, 409 Conflict, 500 Internal Server Error).
- Testen van de interactie tussen Controller, Service en Repository.

### E2E tests
- Gebruikersflow van het invullen van het ticket aanmaakformulier tot het succesvol aanmaken en weergeven van het ticket.
- Testen van de validatie feedback op de UI.
- Testen van de foutmeldingen op de UI bij server errors.

## 12. Traceability Matrix

| REQ | Backend | Tests |
|-----|---------|-------|
| REQ-001 | TicketController, TicketService, TicketRepository, CreateTicketRequest, Ticket | Unit tests voor TicketService, Integration tests voor /api/tickets POST, E2E tests voor ticket aanmaken. |
| REQ-002 | TicketController, TicketService, CreateTicketRequest, TicketSubjectUniquePerDayValidator | Unit tests voor TicketSubjectUniquePerDayValidator, Integration tests voor /api/tickets POST met ongeldige subject lengtes, E2E tests voor subject validatie op UI. |
| REQ-003 | TicketController, TicketService, CreateTicketRequest | Unit tests voor TicketService (description validatie), Integration tests voor /api/tickets POST met ongeldige description lengtes, E2E tests voor description validatie op UI. |
| REQ-004 | TicketController, TicketService, CreateTicketRequest, EnumConverter | Unit tests voor EnumConverter, Integration tests voor /api/tickets POST met ongeldige priority waarden, E2E tests voor priority validatie op UI. |
| REQ-005 | TicketController, TicketService, TicketResponse | Integration tests voor /api/tickets POST (201 response), E2E tests voor het controleren van de geretourneerde ticketnummer en status. |
| REQ-006 | TicketController, GlobalExceptionHandler, FieldError | Integration tests voor /api/tickets POST met validatiefouten, E2E tests voor het tonen van veldspecifieke foutmeldingen op UI. |
| REQ-007 | GlobalExceptionHandler, ApiError, CorrelationIdFilter | Integration tests voor server errors, E2E tests voor het tonen van generieke foutmeldingen met correlation ID op UI. |
| REQ-008 | TicketNumberGenerator, TicketRepository, Ticket | Unit tests voor TicketNumberGenerator, Integration tests voor /api/tickets POST (controleren ticketnummer formaat en uniciteit), E2E tests voor het controleren van het ticketnummer. |
| REQ-009 | TicketService, Ticket | Unit tests voor TicketService (status initialisatie), Integration tests voor /api/tickets POST (controleren status). |
| REQ-010 | TicketController, TicketService | Performance tests (load testing) op /api/tickets POST endpoint. |
| REQ-011 | LoggingAspect, CorrelationIdFilter | Unit tests voor LoggingAspect, Integration tests (controleren logs), E2E tests (controleren logs). |
| REQ-012 | TicketController, TicketService, TicketListResponse | Integration tests voor /api/tickets POST en GET (controleren zichtbaarheid HIGH priority ticket), E2E tests voor het tonen van HIGH priority tickets. |
| REQ-013 | TicketSubjectUniquePerDayValidator, TicketRepository | Unit tests voor TicketSubjectUniquePerDayValidator, Integration tests voor /api/tickets POST met dubbel onderwerp op dezelfde dag. |
| REQ-014 | TicketDailyLimitValidator, TicketDailyLimitRepository, TicketDailyLimit | Unit tests voor TicketDailyLimitValidator, Integration tests voor /api/tickets POST (controleren dagelijkse limiet), E2E tests voor dagelijkse limiet op UI. |
| REQ-015 | HighPriorityTicketDailyLimitValidator, TicketDailyLimitRepository, TicketDailyLimit | Unit tests voor HighPriorityTicketDailyLimitValidator, Integration tests voor /api/tickets POST (controleren dagelijkse HIGH priority limiet), E2E tests voor dagelijkse HIGH priority limiet op UI. |
| REQ-016 | TicketPriorityCompletionOrderValidator, TicketRepository | Unit tests voor TicketPriorityCompletionOrderValidator, Integration tests voor PUT /api/tickets/{ticketId}/status (controleren voltooiingsvolgorde). |
| REQ-017 | TicketPriorityCompletionOrderValidator, TicketRepository | Unit tests voor TicketPriorityCompletionOrderValidator, Integration tests voor PUT /api/tickets/{ticketId}/status (controleren voltooiingsvolgorde). |
