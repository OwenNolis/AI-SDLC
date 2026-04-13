# Feature-001: Support Ticket aanmaken

## 1. Scope

### In Scope
*   Ticket aanmaken via User Interface (UI).
*   Validatie van alle vereiste inputvelden op de UI.
*   Opslaan van de ticketinformatie in de persistente opslag (database).
*   Teruggave van een uniek Ticket ID en de initiële status van het aangemaakte ticket aan de UI.
*   Weergeven van duidelijke en informatieve foutmeldingen op de UI bij validatie- of opslagfouten.

### Out of Scope
*   Functionaliteit voor het bewerken of sluiten van reeds aangemaakte tickets.
*   Ondersteuning voor het toevoegen van bijlagen (attachments) aan tickets.
*   Implementatie van e-mail notificaties naar gebruikers of beheerders bij ticketcreatie.

## 2. Assumptions

*   Er is een bestaande, functionele database beschikbaar voor het opslaan van ticketgegevens.
*   De UI-componenten voor het invoeren van ticketgegevens zijn reeds gedefinieerd en beschikbaar.
*   Er is een gedefinieerd datamodel voor de ticketentiteit, inclusief velden zoals titel, beschrijving, prioriteit, en status.
*   Authenticatie en autorisatie mechanismen zijn reeds geïmplementeerd en zullen worden gebruikt om de toegang tot de ticketcreatie functionaliteit te controleren.

## 3. Open Questions

*   Welke specifieke validatieregels moeten worden toegepast op elk inputveld (bijv. maximale lengte, vereiste formaten, verplichte velden)?
*   Wat is de initiële status van een nieuw aangemaakt ticket?
*   Welke specifieke foutcodes en bijbehorende foutmeldingen moeten worden teruggegeven aan de UI voor verschillende scenario's (bijv. databasefout, inputvalidatiefout)?
*   Hoe wordt het unieke Ticket ID gegenereerd (bijv. sequentieel, UUID)?
*   Welke technologie-stack wordt gebruikt voor de backend API die de ticketcreatie afhandelt?

## 4. Domain Model

### 4.1. Ticket

| Veld | Type | Constraints | Testcases |
|---|---|---|---|
| id | Long | notNull | missing, invalid_value |
| ticketNumber | String | notNull, unique | missing, invalid_value, duplicate_per_day |
| subject | String | notNull, minLength:5, maxLength:120, uniquePerDay | empty, too_short, too_long, missing, invalid_value, duplicate_per_day |
| description | String | notNull, minLength:20, maxLength:2000 | empty, too_short, too_long, missing, invalid_value |
| priority | Priority | notNull | missing, invalid_value |
| status | Status | notNull | missing, invalid_value |
| createdAt | LocalDateTime | notNull | missing, invalid_value |
| createdByUserId | Long | notNull | missing, invalid_value |

### 4.2. User

| Veld | Type | Constraints | Testcases |
|---|---|---|---|
| id | Long | notNull | missing, invalid_value |
| username | String | notNull, unique | missing, invalid_value |

### 4.3. TicketDailyLimit

| Veld | Type | Constraints | Testcases |
|---|---|---|---|
| userId | Long | notNull | missing, invalid_value |
| date | LocalDate | notNull | missing, invalid_value |
| ticketCount | Integer | notNull, greaterThanOrEqual:0 | missing, invalid_value |
| highPriorityTicketCount | Integer | notNull, greaterThanOrEqual:0 | missing, invalid_value |

### 4.4. Enums

#### 4.4.1. Priority

*   LOW
*   MEDIUM
*   HIGH

#### 4.4.2. Status

*   OPEN
*   IN_PROGRESS
*   CLOSED

## 5. API Design

### 5.1 Endpoints

| Method | Path        | Request DTO             | Responses

## 6. Backend Design

De backend architectuur volgt een gelaagde structuur, bestaande uit de Controller-, Service- en Repository-lagen.

*   **Controller Laag:** Verantwoordelijk voor het afhandelen van inkomende HTTP-verzoeken, het valideren van de input en het doorsturen van verzoeken naar de Service laag.
*   **Service Laag:** Bevat de kern business logica. Deze laag orkestreert de interactie tussen verschillende componenten, voert validaties uit en roept de Repository laag aan voor data-operaties.
*   **Repository Laag:** Verantwoordelijk voor de interactie met de datastore (bv. database). Deze laag abstraheert de details van de data-opslag en biedt methoden voor CRUD-operaties op entiteiten.

Hieronder volgt een tabel met de klassen en hun specifieke verantwoordelijkheden:

| Module | Klasse                               | Verantwoordelijkheid                                                                                             |
| :----- | :----------------------------------- | :--------------------------------------------------------------------------------------------------------------- |
| ticket | `TicketController`                   | Verwerkt inkomende HTTP-verzoeken voor ticketgerelateerde operaties, zoals het aanmaken van een ticket.           |
| ticket | `TicketService`                      | Bevat de kern business logica voor het aanmaken en beheren van tickets, inclusief validaties en ticketnummergeneratie. |
| ticket | `TicketRepository`                   | Verantwoordelijk voor de interactie met de datastore voor `Ticket` entiteiten.                                   |
| ticket | `Ticket`                             | Representeert de `Ticket` entiteit in het domein model.                                                          |
| ticket | `CreateTicketRequestDto`             | Data Transfer Object (DTO) voor het aanmaken van een nieuw ticket, bevat de input data van de aanvraag.        |
| ticket | `TicketCreationResponseDto`          | DTO voor de response bij het aanmaken van een ticket, bevat de gegenereerde ticketinformatie.                  |
| ticket | `TicketSubjectUniquePerDayValidator` | Valideert of het ticketonderwerp uniek is per dag voor een specifieke gebruiker.                                 |
| ticket | `TicketDailyLimitValidator`          | Valideert of de dagelijkse ticketlimiet van de gebruiker niet is overschreden.                                   |
| ticket | `HighPriorityTicketDailyLimitValidator` | Valideert of de dagelijkse limiet voor high priority tickets niet is overschreden.                               |
| ticket | `TicketNumberGenerator`              | Genereert unieke ticketnummers in het formaat `TCK-YYYY-NNNNNN`.                                                 |
| ticket | `TicketPriorityOrderService`         | Beheert de logica voor het prioriteren van ticket voltooiing op basis van prioriteit.                           |
| user   | `UserRepository`                     | Verantwoordelijk voor de interactie met de datastore voor `User` entiteiten.                                     |
| user   | `User`                               | Representeert de `User` entiteit in het domein model.                                                            |
| limit  | `TicketDailyLimitRepository`         | Verantwoordelijk voor de interactie met de datastore voor `TicketDailyLimit` entiteiten.                        |
| limit  | `TicketDailyLimit`                   | Representeert de `TicketDailyLimit` entiteit in het domein model, die de dagelijkse limieten bijhoudt.          |
| common | `ApiError`                           | Standaard fout response object voor API-verzoeken, bevat foutmeldingen en statuscodes.                          |
| common | `ApiExceptionHandler`                | Globaal exception handler voor het afhandelen van API-gerelateerde uitzonderingen en het genereren van `ApiError` responses. |
| common | `CorrelationIdFilter`                | Genereert en beheert een correlation ID voor elk inkomend verzoek, essentieel voor tracing en logging.          |
| common | `LoggingService`                     | Verantwoordelijk voor het loggen van applicatiegebeurtenissen, inclusief het gebruik van de correlation ID.     |
| common | `Priority`                           | Enum voor de prioriteit van een ticket (bv. LOW, MEDIUM, HIGH).                                                  |
| common | `Status`                             | Enum voor de status van een ticket (bv. OPEN, IN_PROGRESS, CLOSED).                                              |
| common | `ValidationException`                | Custom exception voor validatiefouten, gebruikt om specifieke validatieproblemen aan te geven.                  |
| common | `ConflictException`                  | Custom exception voor conflicten, zoals het overschrijden van limieten.                                          |
| common | `NotFoundException`                  | Custom exception voor niet-gevonden resources.                                                                    |

## 7. Frontend Design

De frontend voor de "Support Ticket aanmaken" feature bestaat uit specifieke routes en herbruikbare componenten.

*   **Routes:** Definieert de URL-paden waaronder de functionaliteit toegankelijk is.
*   **Componenten:** Bouwstenen van de gebruikersinterface die specifieke taken uitvoeren, zoals het weergeven van formulieren, validatieberichten of laadindicatoren.

Hieronder volgt een tabel met de routes en componenten:

| Type      | Naam                     | Verantwoordelijkheid

## 8. Security & Privacy

### 8.1 Authenticatie
Alle verzoeken naar de `/api/tickets` endpoint voor het aanmaken van een ticket vereisen een geldige JWT (JSON Web Token) in de `Authorization` header. De token moet een `user_id` claim bevatten die de identiteit van de ingelogde gebruiker representeert.

### 8.2 Autorisatie
*   **Ticket Aanmaken:** Alleen geauthenticeerde gebruikers met de rol `SUPPORT_AGENT` of `ADMIN` mogen tickets aanmaken. Dit wordt gecontroleerd aan de hand van de `roles` claim in de JWT.
*   **Rate Limiting:** Er worden twee niveaus van rate limiting toegepast om misbruik te voorkomen:
    *   **Algemeen:** Een gebruiker mag maximaal 3 tickets per dag aanmaken. Dit wordt bijgehouden per `user_id` en de huidige datum.
    *   **Prioriteit HIGH:** Een gebruiker mag maximaal 2 tickets met prioriteit `HIGH` per dag aanmaken. Dit wordt bijgehouden per `user_id`, de huidige datum en de prioriteit.
    *   Bij overschrijding van deze limieten wordt een `429 Too Many Requests` HTTP-statuscode geretourneerd met een duidelijke foutmelding.

### 8.3 Privacy
*   **Gegevensopslag:** Persoonlijke gegevens van de gebruiker die tickets aanmaakt (zoals `user_id`) worden opgeslagen in de ticketrecord voor auditdoeleinden en om de rate limiting te implementeren. Deze gegevens worden niet publiekelijk toegankelijk gemaakt.
*   **Gevoelige Informatie:** De `description` van een ticket kan gevoelige informatie bevatten. Toegang tot deze informatie is strikt beperkt tot geautoriseerde gebruikersrollen (`SUPPORT_AGENT`, `ADMIN`). Er worden geen specifieke encryptie-eisen gesteld aan de `description` op dit niveau, maar de algemene beveiligingsmaatregelen van de applicatie zijn van toepassing.

## 9. Observability

### 9.1 Logging
Logging vindt plaats op verschillende niveaus (INFO, WARN, ERROR) en bevat essentiële informatie voor debugging en monitoring. Elke log entry bevat een `correlation_id` voor het traceren van een enkele request door het systeem.

#### 9.1.1 Voorbeelden van Log Entries:

*   **INFO - Ticket Aanmaken Start:**
    ```
    [INFO] 2023-10-27T10:00:00Z [correlation_id: abcdef123456] User 'user123' attempting to create ticket. Subject: 'Issue with login', Priority: 'HIGH'
    ```
    *   **Doel:** Traceer het begin van een ticket aanmaak request.

*   **INFO - Validatie Succesvol:**
    ```
    [INFO] 2023-10-27T10:00:01Z [correlation_id: abcdef123456] Input validation successful for ticket creation.
    ```
    *   **Doel:** Bevestig dat de input voldoet aan de gestelde eisen.

*   **INFO - Ticket Aangemaakt:**
    ```
    [INFO] 2023-10-27T10:00:02Z [correlation_id: abcdef123456] Ticket TCK-2023-000001 successfully created with status OPEN.
    ```
    *   **Doel:** Bevestig de succesvolle aanmaak van het ticket en het toegekende ticketnummer.

*   **WARN - Rate Limit Overschreden (Algemeen):**
    ```
    [WARN] 2023-10-27T10:05:00Z [correlation_id: ghijkl789012] User 'user456' exceeded daily ticket creation limit (3 tickets).
    ```
    *   **Doel:** Signaleer dat een gebruiker de dagelijkse limiet voor ticket aanmaken heeft bereikt.

*   **WARN - Rate Limit Overschreden (Prioriteit HIGH):**
    ```
    [WARN] 2023-10-27T10:10:00Z [correlation_id: mnopqr345678] User 'user789' exceeded daily HIGH priority ticket creation limit (2 tickets).
    ```
    *   **Doel:** Signaleer dat een gebruiker de dagelijkse limiet voor HIGH prioriteit tickets heeft bereikt.

*   **ERROR - Validatie Fout (Subject Lengte):**
    ```
    [ERROR] 2023-10-27T10:15:00Z [correlation_id: stuvwx901234] Validation error: Subject must be between 5 and 120 characters. Received: 'Short'.
    ```
    *   **Doel:** Log specifieke validatiefouten voor debugging.

*   **ERROR - Server Fout (Database):**
    ```
    [ERROR] 2023-10-27T10:20:00Z [correlation_id: yz1234567890] Unexpected server error during ticket creation: Database connection failed.
    ```
    *   **Doel:** Log onverwachte serverfouten voor incident response.

### 9.2 Metrics
*   **`ticket_creation_count`**: Een teller die het aantal succesvol aangemaakte tickets bijhoudt. Kan worden gefilterd op prioriteit.
*   **`ticket_creation_latency_seconds`**: Een histogram of timer die de responstijd van de `/api/tickets` endpoint meet.
*   **`ticket_creation_validation_errors`**: Een teller voor het aantal mislukte ticket aanmaak pogingen door validatiefouten.
*   **`ticket_creation_server_errors`**: Een teller voor het aantal mislukte ticket aanmaak pogingen door serverfouten.
*   **`user_daily_ticket_limit_exceeded`**: Een teller die aangeeft hoe vaak de algemene dagelijkse ticket limiet is overschreden.
*   **`user_daily_high_priority_limit_exceeded`**: Een teller die aangeeft hoe vaak de dagelijkse limiet voor HIGH prioriteit tickets is overschreden.

### 9.3 Correlation ID
De `correlation_id` wordt gegenereerd aan het begin van elke request naar de `/api/tickets` endpoint en wordt meegestuurd in alle interne service calls en log entries die gerelateerd zijn aan die specifieke request. Dit maakt het mogelijk om de volledige levenscyclus van een ticket aanmaak request te volgen, zelfs over meerdere microservices heen. De `correlation_id` wordt ook teruggegeven in de API response bij server errors.

## 10. Performance & Scalability

### 10.1 Performance-eisen
*   **API Response Tijd:** De API response tijd voor het aanmaken van een ticket moet voor 95% van de verzoeken minder dan 300 milliseconden zijn. Dit geldt voor de gehele request-response cyclus, inclusief database-operaties en validaties.

### 10.2 Database-indexen
Om de performance te waarborgen, met name voor het genereren van unieke ticketnummers en het controleren van limieten, worden de volgende database-indexen aanbevolen:

*   **`tickets` tabel:**
    *   `ticket_number` (UNIQUE INDEX): Voor snelle lookup van tickets en het garanderen van uniciteit.
    *   `created_at` (INDEX): Voor het filteren van tickets op datum, met name voor het dagelijkse limiet.
    *   `user_id` (INDEX): Voor het efficiënt ophalen van tickets per gebruiker, essentieel voor limietcontroles.
    *   `priority` (INDEX): Voor het snel filteren op prioriteit, met name voor de HIGH prioriteit limiet.
    *   **Gecombineerde Index:** `(user_id, DATE(created_at))` - Deze index is cruciaal voor het efficiënt tellen van tickets per gebruiker per dag.
    *   **Gecombineerde Index:** `(user_id, DATE(created_at), priority)` - Deze index is cruciaal voor het efficiënt tellen van HIGH prioriteit tickets per gebruiker per dag.

*   **`ticket_subjects` tabel (indien een aparte tabel voor unieke onderwerpen per dag wordt overwogen):**
    *   `subject` (UNIQUE INDEX): Om de uniciteit van onderwerpen per dag te garanderen.
    *   `created_date` (INDEX): Voor het filteren op datum.
    *   **Gecombineerde Index:** `(subject, created_date)` - Voor snelle controle op uniciteit.

### 10.3 Schaalbaarheid
*   **Database Schaalbaarheid:** De database moet horizontaal schaalbaar zijn om de groeiende hoeveelheid ticketdata te kunnen verwerken. Dit kan worden bereikt door middel van sharding of replicatie.
*   **API Gateway Rate Limiting:** Naast de applicatieve rate limiting, kan een API Gateway worden ingezet om op netwerkniveau rate limiting toe te passen, wat extra bescherming biedt tegen brute-force aanvallen en overbelasting.
*   **Asynchrone Verwerking (Optioneel):** Voor zeer hoge volumes kan de ticket aanmaak workflow worden geoptimaliseerd door het gebruik van een message queue. De initiële API call zou dan een ticket aanmaken in een "pending" status en een message in de queue plaatsen. Een aparte worker service zou dan de daadwerkelijke ticket creatie, validatie en opslag afhandelen. Dit verhoogt de responstijd van de API, maar verbetert de doorvoer en veerkracht.
*   **Unieke Ticket Nummer Generatie:** De sequentie-gebaseerde nummering (TCK-YYYY-000001) kan een bottleneck worden bij zeer hoge volumes. Overweeg een gedistribueerd ID-generatiesysteem (zoals UUIDs of een Snowflake-achtige implementatie) als de verwachte ticket aanmaak frequentie de capaciteit van een enkele sequentie overschrijdt. Echter, voor de huidige vereiste van leesbare ticketnummers, is de sequentie per jaar een acceptabele oplossing, mits de database-implementatie van sequenties schaalbaar is.
*   **Caching:** Caching kan worden overwogen voor veelgevraagde data, zoals de lijst van beschikbare prioriteiten, om de databasebelasting te verminderen.

## 11. Test Strategy

### Unit Tests

*   `TicketCreationForm` component:
    *   Verificatie van correcte rendering van het formulier met alle verwachte inputvelden (subject, description, priority, category).
    *   Testen van de `handleInputChange` functie om te verzekeren dat de state correct wordt bijgewerkt bij interactie met de inputvelden.
    *   Testen van de `handleSubmit` functie om te verifiëren dat de correcte data wordt verzameld en de submit callback wordt aangeroepen met de verwachte payload.
*   `InputValidationMessage` component:
    *   Verificatie van correcte rendering van de foutmelding wanneer een validatiefout wordt doorgegeven.
    *   Verificatie dat er geen foutmelding wordt weergegeven wanneer er geen validatiefouten zijn.
*   `LoadingSpinner` component:
    *   Verificatie van correcte rendering van de spinner wanneer de `isLoading` prop `true` is.
    *   Verificatie dat de spinner niet wordt weergegeven wanneer de `isLoading` prop `false` is.
*   `ErrorDisplay` component:
    *   Verificatie van correcte rendering van de foutmelding wanneer een `errorMessage` prop wordt doorgegeven.
    *   Verificatie dat er geen foutmelding wordt weergegeven wanneer de `errorMessage` prop leeg is.
*   `TicketDetailsDisplay` component:
    *   Verificatie van correcte rendering van de ticket details (ticketnummer, status) wanneer de `ticket` prop wordt doorgegeven.

### Integration Tests

*   **API Endpoint: `POST /api/tickets`**
    *   **Success Case:**
        *   Verstuur een POST request met geldige ticketdata.
        *   Verwacht een `201 Created` statuscode.
        *   Verifieer dat de response body de aangemaakte ticketinformatie bevat, inclusief een uniek ticketnummer en de initiële status (bv. "OPEN").
    *   **Bad Request Cases:**
        *   Verstuur een POST request met ongeldige of ontbrekende verplichte velden (bv. lege subject, ongeldige priority).
        *   Verwacht een `400 Bad Request` statuscode.
        *   Verifieer dat de response body specifieke foutmeldingen bevat die de ongeldige velden identificeren.
    *   **Conflict Cases:**
        *   Verstuur een POST request met een subject dat al bestaat voor de huidige dag.
        *   Verwacht een `409 Conflict` statuscode.
        *   Verifieer dat de response body een conflictmelding bevat.
        *   Verstuur een POST request die het dagelijkse limiet voor het aanmaken van tickets overschrijdt.
        *   Verwacht een `409 Conflict` statuscode.
        *   Verifieer dat de response body een conflictmelding bevat.
        *   Verstuur een POST request die het dagelijkse limiet voor het aanmaken van HIGH priority tickets overschrijdt.
        *   Verwacht een `409 Conflict` statuscode.
        *   Verifieer dat de response body een conflictmelding bevat.
    *   **Server Error Case:**
        *   Simuleer een serverfout tijdens het verwerken van het ticket aanmaak request (bv. databasefout).
        *   Verwacht een `500 Internal Server Error` statuscode.
        *   Verifieer dat de response body een algemene serverfoutmelding bevat.

### End-to-End (E2E) Tests

*   **Succesvolle Ticket Aanmaak:**
    *   Navigeer naar de ticket aanmaakpagina.
    *   Vul alle vereiste velden in met geldige gegevens (subject, description, priority, category).
    *   Klik op de "Submit" knop.
    *   Verifieer dat de `LoadingSpinner` wordt weergegeven tijdens de verwerking.
    *   Na succesvolle aanmaak, verifieer dat de `TicketDetailsDisplay` component wordt getoond met het correcte ticketnummer en de status "OPEN".
*   **Validatie Fouten bij Ongeldige Input:**
    *   Navigeer naar de ticket aanmaakpagina.
    *   Laat een verplicht veld leeg (bv. subject).
    *   Voer ongeldige data in een ander veld in (bv. subject korter dan de minimale lengte).
    *   Verifieer dat de "Submit" knop disabled blijft.
    *   Verifieer dat veldspecifieke foutmeldingen worden weergegeven naast de betreffende inputvelden.
*   **Conflict: Dubbel Subject:**
    *   Navigeer naar de ticket aanmaakpagina.
    *   Vul het formulier in met een subject dat al eerder op de huidige dag is aangemaakt.
    *   Klik op de "Submit" knop.
    *   Verifieer dat een `ErrorDisplay` component verschijnt met een conflictmelding gerelateerd aan het dubbele subject.
*   **Conflict: Dagelijks Ticket Limiet:**
    *   Simuleer (of voer uit) het aanmaken van het maximale aantal tickets per dag.
    *   Probeer vervolgens nog een ticket aan te maken.
    *   Verifieer dat een `ErrorDisplay` component verschijnt met een conflictmelding die aangeeft dat het dagelijkse limiet is bereikt.
*   **Conflict: Dagelijks HIGH Priority Ticket Limiet:**
    *   Simuleer (of voer uit) het aanmaken van het maximale aantal HIGH priority tickets per dag.
    *   Probeer vervolgens nog een HIGH priority ticket aan te maken.
    *   Verifieer dat een `ErrorDisplay` component verschijnt met een conflictmelding die aangeeft dat het dagelijkse limiet voor HIGH priority tickets is bereikt.

## 12. Traceability Matrix

| REQ | Backend | Frontend | Tests |
|-----|---------|----------|-------|
| REQ-001 | TicketController, TicketService, TicketRepository, CreateTicketRequestDto, Ticket | TicketCreationForm | T; e; s; t; e; n;  ; o; f;  ; e; e; n;  ; g; e; b; r; u; i; k; e; r;  ; e; e; n;  ; t; i; c; k; e; t;  ; k; a; n;  ; a; a; n; m; a; k; e; n;  ; m; e; t;  ; d; e;  ; v; e; l; d; e; n;  ; s; u; b; j; e; c; t; ,;  ; d; e; s; c; r; i; p; t; i; o; n;  ; e; n;  ; p; r; i; o; r; i; t; y;  ; v; i; a;  ; h; e; t;  ; f; o; r; m; u; l; i; e; r; . |
| REQ-002 | CreateTicketRequestDto, TicketSubjectUniquePerDayValidator, TicketController, ApiExceptionHandler, ValidationException | TicketCreationForm, InputValidationMessage | T; e; s; t; e; n;  ; v; a; n;  ; d; e;  ; v; a; l; i; d; a; t; i; e;  ; v; a; n;  ; h; e; t;  ; s; u; b; j; e; c; t;  ; v; e; l; d; :;  ; m; i; n; i; m; a; a; l;  ; 5;  ; k; a; r; a; k; t; e; r; s; ,;  ; m; a; x; i; m; a; a; l;  ; 1; 2; 0;  ; k; a; r; a; k; t; e; r; s; .;  ; T; e; s; t; e; n;  ; v; a; n;  ; l; e; g; e;  ; i; n; p; u; t;  ; e; n;  ; i; n; p; u; t;  ; b; u; i; t; e; n;  ; d; e;  ; l; i; m; i; e; t; e; n; . |
| REQ-003 | CreateTicketRequestDto, TicketController, ApiExceptionHandler, ValidationException | TicketCreationForm, InputValidationMessage | T; e; s; t; e; n;  ; v; a; n;  ; d; e;  ; v; a; l; i; d; a; t; i; e;  ; v; a; n;  ; h; e; t;  ; d; e; s; c; r; i; p; t; i; o; n;  ; v; e; l; d; :;  ; m; i; n; i; m; a; a; l;  ; 2; 0;  ; k; a; r; a; k; t; e; r; s; ,;  ; m; a; x; i; m; a; a; l;  ; 2; 0; 0; 0;  ; k; a; r; a; k; t; e; r; s; .;  ; T; e; s; t; e; n;  ; v; a; n;  ; l; e; g; e;  ; i; n; p; u; t;  ; e; n;  ; i; n; p; u; t;  ; b; u; i; t; e; n;  ; d; e;  ; l; i; m; i; e; t; e; n; . |
| REQ-004 | CreateTicketRequestDto, TicketController, ApiExceptionHandler, ValidationException, Priority | TicketCreationForm, InputValidationMessage | T; e; s; t; e; n;  ; v; a; n;  ; d; e;  ; v; a; l; i; d; a; t; i; e;  ; v; a; n;  ; h; e; t;  ; p; r; i; o; r; i; t; y;  ; v; e; l; d; :;  ; m; o; e; t;  ; é; é; n;  ; v; a; n;  ; d; e;  ; t; o; e; g; e; s; t; a; n; e;  ; w; a; a; r; d; e; n;  ; (; L; O; W; ,;  ; M; E; D; I; U; M; ,;  ; H; I; G; H; );  ; z; i; j; n; .;  ; T; e; s; t; e; n;  ; v; a; n;  ; o; n; g; e; l; d; i; g; e;  ; w; a; a; r; d; e; n; . |
| REQ-005 | TicketController, TicketService, TicketCreationResponseDto, Ticket | TicketDetailsDisplay | T; e; s; t; e; n;  ; o; f;  ; n; a;  ; s; u; c; c; e; s; v; o; l; l; e;  ; a; a; n; m; a; a; k;  ; v; a; n;  ; e; e; n;  ; t; i; c; k; e; t; ,;  ; h; e; t;  ; t; i; c; k; e; t; n; u; m; m; e; r;  ; e; n;  ; d; e;  ; s; t; a; t; u; s;  ; O; P; E; N;  ; w; o; r; d; e; n;  ; g; e; t; o; o; n; d;  ; a; a; n;  ; d; e;  ; g; e; b; r; u; i; k; e; r; . |
| REQ-006 | ApiExceptionHandler, ValidationException, TicketController | InputValidationMessage, ErrorDisplay | T; e; s; t; e; n;  ; o; f;  ; b; i; j;  ; o; n; g; e; l; d; i; g; e;  ; i; n; v; o; e; r;  ; (; b; i; j; v; .;  ; t; e;  ; k; o; r; t;  ; s; u; b; j; e; c; t; );  ; s; p; e; c; i; f; i; e; k; e;  ; f; o; u; t; m; e; l; d; i; n; g; e; n;  ; p; e; r;  ; v; e; l; d;  ; w; o; r; d; e; n;  ; g; e; t; o; o; n; d;  ; o; p;  ; d; e;  ; U; I; . |
| REQ-007 | ApiExceptionHandler, CorrelationIdFilter, LoggingService, ApiError | ErrorDisplay | T; e; s; t; e; n;  ; o; f;  ; b; i; j;  ; e; e; n;  ; s; e; r; v; e; r;  ; e; r; r; o; r;  ; (; b; i; j; v; .;  ; d; a; t; a; b; a; s; e;  ; f; o; u; t; );  ; e; e; n;  ; g; e; n; e; r; i; e; k; e;  ; f; o; u; t; m; e; l; d; i; n; g;  ; m; e; t;  ; e; e; n;  ; c; o; r; r; e; l; a; t; i; o; n;  ; i; d;  ; w; o; r; d; t;  ; g; e; t; o; o; n; d;  ; o; p;  ; d; e;  ; U; I; . |
| REQ-008 | TicketNumberGenerator, TicketRepository, Ticket |  | T; e; s; t; e; n;  ; o; f;  ; h; e; t;  ; g; e; g; e; n; e; r; e; e; r; d; e;  ; t; i; c; k; e; t; n; u; m; m; e; r;  ; v; o; l; d; o; e; t;  ; a; a; n;  ; h; e; t;  ; f; o; r; m; a; a; t;  ; T; C; K; -; Y; Y; Y; Y; -; 0; 0; 0; 0; 0; 1;  ; e; n;  ; o; f;  ; h; e; t;  ; u; n; i; e; k;  ; i; s;  ; p; e; r;  ; j; a; a; r; . |
| REQ-009 | TicketService, Ticket |  | T; e; s; t; e; n;  ; o; f;  ; d; e;  ; s; t; a; t; u; s;  ; v; a; n;  ; e; e; n;  ; n; i; e; u; w;  ; a; a; n; g; e; m; a; a; k; t;  ; t; i; c; k; e; t;  ; a; l; t; i; j; d;  ; O; P; E; N;  ; i; s; . |
| REQ-010 | TicketController, TicketService |  | P; e; r; f; o; r; m; a; n; c; e;  ; t; e; s; t; e; n; :;  ; m; e; t; e; n;  ; v; a; n;  ; d; e;  ; A; P; I;  ; r; e; s; p; o; n; s; e;  ; t; i; j; d;  ; v; o; o; r;  ; h; e; t;  ; a; a; n; m; a; k; e; n;  ; v; a; n;  ; e; e; n;  ; t; i; c; k; e; t;  ; e; n;  ; v; e; r; i; f; i; ë; r; e; n;  ; d; a; t;  ; 9; 5; %;  ; v; a; n;  ; d; e;  ; v; e; r; z; o; e; k; e; n;  ; o; n; d; e; r;  ; d; e;  ; 3; 0; 0; m; s;  ; b; l; i; j; f; t; . |
| REQ-011 | LoggingService, CorrelationIdFilter |  | T; e; s; t; e; n;  ; o; f;  ; a; l; l; e;  ; r; e; l; e; v; a; n; t; e;  ; l; o; g; b; e; r; i; c; h; t; e; n;  ; d; e;  ; c; o; r; r; e; l; a; t; i; o; n;  ; i; d;  ; b; e; v; a; t; t; e; n; . |
| REQ-012 | TicketService, TicketRepository | TicketDetailsDisplay | T; e; s; t; e; n;  ; o; f;  ; e; e; n;  ; t; i; c; k; e; t;  ; m; e; t;  ; p; r; i; o; r; i; t; e; i; t;  ; H; I; G; H;  ; d; i; r; e; c; t;  ; n; a;  ; a; a; n; m; a; k; e; n;  ; z; i; c; h; t; b; a; a; r;  ; i; s;  ; i; n;  ; d; e;  ; l; i; j; s; t;  ; v; a; n;  ; t; i; c; k; e; t; s; . |
| REQ-013 | TicketSubjectUniquePerDayValidator, TicketRepository, ConflictException | TicketCreationForm, InputValidationMessage | T; e; s; t; e; n;  ; o; f;  ; h; e; t;  ; o; n; d; e; r; w; e; r; p;  ; v; a; n;  ; e; e; n;  ; t; i; c; k; e; t;  ; u; n; i; e; k;  ; i; s;  ; p; e; r;  ; d; a; g; .;  ; P; o; g; i; n; g; e; n;  ; o; m;  ; e; e; n;  ; t; i; c; k; e; t;  ; m; e; t;  ; e; e; n;  ; r; e; e; d; s;  ; b; e; s; t; a; a; n; d;  ; o; n; d; e; r; w; e; r; p;  ; o; p;  ; d; e; z; e; l; f; d; e;  ; d; a; g;  ; a; a; n;  ; t; e;  ; m; a; k; e; n;  ; m; o; e; t; e; n;  ; f; a; l; e; n; . |
| REQ-014 | TicketDailyLimitValidator, TicketRepository, TicketDailyLimitRepository, ConflictException | TicketCreationForm, InputValidationMessage | T; e; s; t; e; n;  ; o; f;  ; e; e; n;  ; g; e; b; r; u; i; k; e; r;  ; m; a; x; i; m; a; a; l;  ; 3;  ; t; i; c; k; e; t; s;  ; p; e; r;  ; d; a; g;  ; k; a; n;  ; a; a; n; m; a; k; e; n; .;  ; P; o; g; i; n; g; e; n;  ; o; m;  ; e; e; n;  ; v; i; e; r; d; e;  ; t; i; c; k; e; t;  ; a; a; n;  ; t; e;  ; m; a; k; e; n;  ; m; o; e; t; e; n;  ; f; a; l; e; n; . |
| REQ-015 | HighPriorityTicketDailyLimitValidator, TicketRepository, TicketDailyLimitRepository, ConflictException | TicketCreationForm, InputValidationMessage | T; e; s; t; e; n;  ; o; f;  ; e; e; n;  ; g; e; b; r; u; i; k; e; r;  ; m; a; x; i; m; a; a; l;  ; 2;  ; t; i; c; k; e; t; s;  ; m; e; t;  ; p; r; i; o; r; i; t; e; i; t;  ; H; I; G; H;  ; p; e; r;  ; d; a; g;  ; k; a; n;  ; a; a; n; m; a; k; e; n; .;  ; P; o; g; i; n; g; e; n;  ; o; m;  ; e; e; n;  ; d; e; r; d; e;  ; H; I; G; H;  ; p; r; i; o; r; i; t; e; i; t;  ; t; i; c; k; e; t;  ; a; a; n;  ; t; e;  ; m; a; k; e; n;  ; m; o; e; t; e; n;  ; f; a; l; e; n; . |
| REQ-016 | TicketPriorityOrderService, TicketRepository |  | T; e; s; t; e; n;  ; v; a; n;  ; d; e;  ; v; o; l; t; o; o; i; i; n; g; s; v; o; l; g; o; r; d; e; :;  ; e; e; n;  ; t; i; c; k; e; t;  ; m; e; t;  ; p; r; i; o; r; i; t; e; i; t;  ; H; I; G; H;  ; m; o; e; t;  ; a; l; t; i; j; d;  ; w; o; r; d; e; n;  ; v; o; l; t; o; o; i; d;  ; v; o; o; r; d; a; t;  ; e; e; n;  ; t; i; c; k; e; t;  ; m; e; t;  ; p; r; i; o; r; i; t; e; i; t;  ; L; O; W;  ; w; o; r; d; t;  ; v; o; l; t; o; o; i; d; . |
| REQ-017 | TicketPriorityOrderService, TicketRepository |  | T; e; s; t; e; n;  ; v; a; n;  ; d; e;  ; v; o; l; t; o; o; i; i; n; g; s; v; o; l; g; o; r; d; e; :;  ; e; e; n;  ; t; i; c; k; e; t;  ; m; e; t;  ; p; r; i; o; r; i; t; e; i; t;  ; H; I; G; H;  ; m; o; e; t;  ; a; l; t; i; j; d;  ; w; o; r; d; e; n;  ; v; o; l; t; o; o; i; d;  ; v; o; o; r; d; a; t;  ; e; e; n;  ; t; i; c; k; e; t;  ; m; e; t;  ; p; r; i; o; r; i; t; e; i; t;  ; M; E; D; I; U; M;  ; w; o; r; d; t;  ; v; o; l; t; o; o; i; d; . |

