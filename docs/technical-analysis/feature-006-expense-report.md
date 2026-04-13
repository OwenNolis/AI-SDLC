# Feature-006: Onkostennota indienen

## 1. Scope

### In Scope
*   Onkostennota aanmaken met datum, bedrag, categorie en beschrijving.
*   Bonnetje uploaden als bijlage (JPEG, PNG of PDF, max 5MB).
*   Overzicht van eigen ingediende onkostennota's tonen.
*   Status opvolgen (ingediend, goedgekeurd, afgewezen, terugbetaald).
*   Manager kan onkostennota's goedkeuren of afwijzen met commentaar.
*   Totaalbedrag per maand tonen voor de medewerker.

### Out of Scope
*   Automatische terugbetaling via bank.
*   Integratie met boekhoudpakket.
*   Onkostenlimieten per categorie instellen.
*   Groepsonkosten voor meerdere medewerkers.

## 2. Assumptions

*   Er is een bestaand authenticatie- en autorisatiesysteem aanwezig dat de rol van "medewerker" en "manager" kan onderscheiden.
*   De applicatie zal draaien op een omgeving waar bestandsopslag (voor bonnetjes) mogelijk is.
*   De gebruiker heeft een stabiele internetverbinding voor het uploaden van bonnetjes.
*   De categorieën voor onkosten zijn vooraf gedefinieerd en beschikbaar voor selectie.

## 3. Open Questions

*   Welke specifieke validaties moeten worden toegepast op het "bedrag" veld (bv. alleen numerieke waarden, maximaal aantal decimalen)?
*   Hoe wordt de "datum" van de onkostennota vastgelegd (datum van indienen of datum van de uitgave)?
*   Wat is de exacte workflow voor het toewijzen van een manager aan een medewerker voor goedkeuring?
*   Welke specifieke informatie moet worden opgeslagen voor elke onkostennota (naast de reeds genoemde velden)?
*   Hoe wordt omgegaan met het verwijderen van onkostennota's (indien toegestaan)?
*   Welke notificaties moeten worden verstuurd naar de medewerker en de manager bij statuswijzigingen?
*   Hoe wordt de opslag van de bonnetjes georganiseerd (bv. bestandsnaamconventies, directorystructuur)?
*   Zijn er specifieke eisen met betrekking tot de beveiliging van de geüploade bonnetjes?
*   Welke rapportagemogelijkheden zijn gewenst voor managers (bv. overzicht van alle onkostennota's, per medewerker, per periode)?

## 4. Domain Model

### Employee

| Veld        | Type        | Constraints                               | Testcases                                       |
|-------------|-------------|-------------------------------------------|-------------------------------------------------|
| id          | Long        | notNull                                   | missing, invalid_value                          |
| username    | String      | notNull, minLength:3, maxLength:50        | empty, too_short, too_long, missing, invalid_value |
| managerId   | Long        |                                           | missing, invalid_value                          |

### ExpenseReport

| Veld             | Type            | Constraints                                       | Testcases                                       |
|------------------|-----------------|---------------------------------------------------|-------------------------------------------------|
| id               | Long            | notNull                                           | missing, invalid_value                          |
| employeeId       | Long            | notNull                                           | missing, invalid_value                          |
| date             | LocalDateTime   | notNull                                           | missing, invalid_value, future_date             |
| amountInCents    | Long            | notNull, min:1, max:100000000                      | missing, invalid_value, negative_value, too_large |
| category         | String          | notNull                                           | empty, missing, invalid_value                   |
| description      | String          | maxLength:500                                     | too_long, invalid_value                         |
| status           | String          | notNull                                           | empty, missing, invalid_value                   |
| rejectionReason  | String          | maxLength:500                                     | too_long, invalid_value                         |
| attachmentUrl    | String          |                                                   | invalid_value                                   |
| createdAt        | LocalDateTime   | notNull                                           | missing, invalid_value                          |
| updatedAt        | LocalDateTime   | notNull                                           | missing, invalid_value                          |

### ExpenseReportAttachment

| Veld             | Type            | Constraints                                       | Testcases                                       |
|------------------|-----------------|---------------------------------------------------|-------------------------------------------------|
| id               | Long            | notNull                                           | missing, invalid_value                          |
| expenseReportId  | Long            | notNull                                           | missing, invalid_value                          |
| fileName         | String          | notNull, maxLength:255                            | empty, too_long, missing, invalid_value         |
| fileUrl          | String          | notNull, maxLength:500                            | empty, too_long, missing, invalid_value         |
| fileType         | String          | notNull, maxLength:10                             | empty, too_long, missing, invalid_value         |
| fileSizeInBytes  | Long            | notNull, min:1, max:5242880                       | missing, invalid_value, negative_value, too_large |
| uploadedAt       | LocalDateTime   | notNull                                           | missing, invalid_value                          |

## 5. API Design

### 5.1 Error Formaat

Het standaard error formaat voor API-responses is als volgt gedefinieerd:

```json
{
  "correlationId": "string",
  "code": "string",
  "message": "string",
  "fieldErrors": [
    {
      "field": "string",
      "message": "string"
    }
  ]
}
```

*   `correlationId`: Een unieke identifier voor het verzoek, nuttig voor debugging en logging.
*   `code`: Een specifieke foutcode die de aard van de fout aangeeft.
*   `message`: Een leesbare beschrijving van de fout.
*   `fieldErrors`: Een lijst van specifieke veldfouten, indien van toepassing.

### 5.2 Endpoints

Hieronder volgt een gedetailleerde beschrijving van de API-endpoints voor de "Onkostennota indienen" feature.

| Method | Path                               | Summary                                          | Request DTO/Parameters

## 6. Backend Design

De backend is gestructureerd volgens een gelaagd architectuurpatroon, bestaande uit de Controller-, Service- en Repository-lagen. Dit zorgt voor een duidelijke scheiding van verantwoordelijkheden en bevordert de testbaarheid en onderhoudbaarheid van de code.

*   **Controller Laag:** Verantwoordelijk voor het afhandelen van inkomende HTTP-verzoeken, het valideren van de request body en het doorsturen van de verzoeken naar de Service laag.
*   **Service Laag:** Bevat de kern bedrijfslogica. Deze laag orkestreert de interacties tussen verschillende componenten, voert validaties uit en roept de Repository laag aan voor gegevenspersistentie.
*   **Repository Laag:** Verantwoordelijk voor de directe interactie met de datastore (bijvoorbeeld een relationele database). Deze laag abstraheert de details van de gegevensopslag.

Hieronder volgt een tabel met de belangrijkste klassen en hun verantwoordelijkheden binnen de `expenseReport` en `common` modules:

| Klasse Naam                       | Module   | Verantwoordelijkheid

## 8. Security & Privacy

### 8.1 Authenticatie
Alle API-endpoints die betrekking hebben op onkostennota's vereisen een geldige JWT (JSON Web Token) in de `Authorization: Bearer <token>` header. De token moet een `userId` claim bevatten die de identiteit van de ingelogde medewerker vertegenwoordigt.

### 8.2 Autorisatie
*   **Medewerker (gebruiker met `role: EMPLOYEE`)**:
    *   Kan onkostennota's aanmaken (`POST /api/expense-reports`).
    *   Kan eigen onkostennota's bekijken (`GET /api/expense-reports`, `GET /api/expense-reports/{id}`).
    *   Kan eigen onkostennota's met status "ingediend" verwijderen (`DELETE /api/expense-reports/{id}`).
    *   Kan bijlagen uploaden voor eigen onkostennota's (`POST /api/expense-reports/{id}/attachment`).
    *   Kan maandelijkse totalen van eigen goedgekeurde onkosten bekijken (`GET /api/expense-reports/monthly-total`).
*   **Manager (gebruiker met `role: MANAGER`)**:
    *   Heeft dezelfde rechten als een medewerker voor *eigen* onkostennota's.
    *   Kan onkostennota's van ondergeschikten bekijken (impliciet via de `GET /api/expense-reports` endpoint, waarbij de backend de relatie tussen manager en medewerker controleert).
    *   Kan onkostennota's van ondergeschikten goedkeuren (`POST /api/expense-reports/{id}/approve`).
    *   Kan onkostennota's van ondergeschikten afwijzen (`POST /api/expense-reports/{id}/reject`).

### 8.3 Privacy
*   **Data Minimisatie**: Alleen noodzakelijke gegevens worden verzameld voor het indienen van een onkostennota.
*   **Toegangscontrole**: Toegang tot onkostennota's is strikt beperkt tot de betreffende medewerker en diens directe manager. Dit wordt afgedwongen op de backend.
*   **Bijlagen**: Bijlagen worden opgeslagen in een beveiligde object storage oplossing. Toegang tot deze bijlagen wordt beheerd via de applicatie, met autorisatiecontroles op basis van de gebruiker die de onkostennota inzag. De URL's naar de bijlagen mogen niet publiekelijk toegankelijk zijn.

## 9. Observability

### 9.1 Logging
Alle relevante operaties worden gelogd met een gestructureerd logformaat (bijv. JSON). Elke log entry bevat minimaal:
*   `timestamp`: Tijdstip van de gebeurtenis.
*   `level`: Log level (INFO, WARN, ERROR, DEBUG).
*   `message`: Beschrijving van de gebeurtenis.
*   `correlationId`: Unieke ID om requests over verschillende services heen te traceren.
*   `userId`: ID van de gebruiker die de actie uitvoerde.
*   `endpoint`: De API endpoint die werd aangeroepen.
*   `httpMethod`: De HTTP methode van de request.
*   `statusCode`: De HTTP status code van de response.

**Concrete Voorbeelden van Logging:**

*   **Onkostennota aangemaakt:**
    ```json
    {
      "timestamp": "2023-10-27T10:30:00Z",
      "level": "INFO",
      "message": "Expense report created successfully.",
      "correlationId": "a1b2c3d4-e5f6-7890-1234-567890abcdef",
      "userId": "user-123",
      "endpoint": "/api/expense-reports",
      "httpMethod": "POST",
      "statusCode": 201,
      "expenseReportId": "exp-abc123"
    }
    ```
*   **Poging tot ongeldige datum:**
    ```json
    {
      "timestamp": "2023-10-27T10:31:15Z",
      "level": "WARN",
      "message": "Validation failed for expense report creation.",
      "correlationId": "a1b2c3d4-e5f6-7890-1234-567890abcdef",
      "userId": "user-123",
      "endpoint": "/api/expense-reports",
      "httpMethod": "POST",
      "statusCode": 400,
      "validationErrors": [
        {"field": "date", "message": "Date cannot be in the future."}
      ]
    }
    ```
*   **Onkostennota goedgekeurd:**
    ```json
    {
      "timestamp": "2023-10-27T11:05:45Z",
      "level": "INFO",
      "message": "Expense report approved by manager.",
      "correlationId": "f9e8d7c6-b5a4-3210-fedc-ba9876543210",
      "userId": "manager-456",
      "endpoint": "/api/expense-reports/{id}/approve",
      "httpMethod": "POST",
      "statusCode": 200,
      "expenseReportId": "exp-xyz789"
    }
    ```
*   **Bijlage upload mislukt (grootte):**
    ```json
    {
      "timestamp": "2023-10-27T10:35:00Z",
      "level": "ERROR",
      "message": "Failed to upload attachment: file size exceeds limit.",
      "correlationId": "a1b2c3d4-e5f6-7890-1234-567890abcdef",
      "userId": "user-123",
      "endpoint": "/api/expense-reports/{id}/attachment",
      "httpMethod": "POST",
      "statusCode": 413
    }
    ```

### 9.2 Metrics
*   **Request Counts**: Aantal requests per endpoint en HTTP-methode.
*   **Response Times**: Gemiddelde, P95 en P99 response tijden per endpoint.
*   **Error Rates**: Percentage van requests dat resulteert in een 4xx of 5xx status code, per endpoint.
*   **Database Query Performance**: Gemiddelde en P95 query tijden voor kritieke database-operaties.
*   **File Upload Metrics**: Aantal succesvolle en mislukte uploads, gemiddelde uploadgrootte.

### 9.3 Correlation ID
Een `correlationId` wordt gegenereerd aan het begin van elke inkomende request en wordt meegestuurd in alle interne en externe communicatie die gerelateerd is aan die request. Dit maakt het mogelijk om de volledige levenscyclus van een request te traceren door logs en metrics van verschillende componenten heen. De `correlationId` wordt ook teruggegeven in de response headers (`X-Correlation-ID`) zodat de client deze kan gebruiken voor debugging.

## 10. Performance & Scalability

### 10.1 Performance-eisen
*   **API Respons Tijd**: Alle API-operaties, met uitzondering van bestandsuploads, moeten binnen 1 seconde reageren.
*   **Bestandsupload**: De snelheid van bestandsuploads is afhankelijk van de netwerkbandbreedte van de gebruiker en de object storage service, maar de backend moet efficiënt omgaan met de verwerking en opslag.

### 10.2 Database-indexen
De volgende indexen zijn cruciaal voor de performance van de onkostennota feature:

*   **`expense_reports` tabel**:
    *   `PRIMARY KEY (id)`: Standaard voor unieke identificatie.
    *   `INDEX idx_expense_reports_user_id (user_id)`: Voor het snel ophalen van alle onkostennota's van een specifieke medewerker (`GET /api/expense-reports`).
    *   `INDEX idx_expense_reports_status (status)`: Voor het filteren op status, bijvoorbeeld bij het verwijderen van ingediende nota's.
    *   `INDEX idx_expense_reports_created_at (created_at)`: Voor het sorteren of filteren op aanmaakdatum, relevant voor maandelijkse totalen.
    *   `INDEX idx_expense_reports_user_id_status (user_id, status)`: Gecombineerde index voor efficiënter ophalen van nota's van een gebruiker met een specifieke status.

*   **`expense_report_attachments` tabel** (indien een aparte tabel voor bijlagen wordt gebruikt):
    *   `PRIMARY KEY (id)`
    *   `INDEX idx_expense_report_attachments_expense_report_id (expense_report_id)`: Voor het snel ophalen van bijlagen bij een specifieke onkostennota.

*   **`users` tabel**:
    *   `PRIMARY KEY (id)`
    *   `INDEX idx_users_manager_id (manager_id)`: Voor het efficiënt ophalen van ondergeschikten van een manager.

### 10.3 Schaalbaarheid
*   **Stateless API Services**: De backend API-services moeten stateless worden ontworpen om horizontaal schaalbaar te zijn. Dit betekent dat elke request onafhankelijk kan worden verwerkt door elke beschikbare instantie van de service.
*   **Asynchrone Verwerking**: Voor potentieel tijdrovende taken zoals het verwerken van bijlagen (validatie, optimalisatie) kan een message queue (bijv. RabbitMQ, Kafka) worden gebruikt om deze taken asynchroon af te handelen, waardoor de API-respons sneller blijft.
*   **Database Schaalbaarheid**:
    *   **Read Replicas**: Gebruik maken van read replicas voor de database om de leesbelasting te verdelen, met name voor het ophalen van onkostennota's en maandelijkse totalen.
    *   **Sharding**: Indien de hoeveelheid data extreem groeit, kan sharding van de `expense_reports` tabel op `user_id` een optie zijn om de database verder te schalen.
*   **Object Storage**: Het gebruik van een schaalbare object storage oplossing (bijv. AWS S3, Google Cloud Storage) voor bijlagen zorgt voor een schaalbare en kosteneffectieve opslag.
*   **Caching**: Veelgebruikte data, zoals toegestane categorieën, kan worden gecached om database-hits te verminderen.

Door deze maatregelen kan de applicatie een toenemend aantal gebruikers en onkostennota's verwerken zonder significante prestatievermindering.

## 11. Test Strategy

### Unit Tests

*   **ExpenseList render**: Verifieert dat de `ExpenseList` component correct wordt gerenderd met een lege lijst en met een lijst van onkosten.
*   **ExpenseForm render**: Verifieert dat de `ExpenseForm` component correct wordt gerenderd met alle verwachte inputvelden en knoppen.
*   **AttachmentUpload render**: Verifieert dat de `AttachmentUpload` component correct wordt gerenderd met een knop voor bestandselectie.
*   **MonthlyTotalCard render**: Verifieert dat de `MonthlyTotalCard` component correct wordt gerenderd met een gegeven totaalbedrag.
*   **ManagerExpenseList render**: Verifieert dat de `ManagerExpenseList` component correct wordt gerenderd met een lijst van onkostennota's die actie vereisen.
*   **ApproveRejectModal render**: Verifieert dat de `ApproveRejectModal` component correct wordt gerenderd met de juiste knoppen voor goedkeuren en afwijzen.
*   **StatusBadge render**: Verifieert dat de `StatusBadge` component correct wordt gerenderd voor verschillende statuswaarden (bv. 'Pending', 'Approved', 'Rejected').
*   **ErrorDisplay render**: Verifieert dat de `ErrorDisplay` component correct wordt gerenderd met een gegeven foutmelding.
*   **ExpenseForm handleSubmit**: Test de logica van `handleSubmit` in `ExpenseForm` om te valideren dat de juiste data wordt verzameld en de submit callback wordt aangeroepen.
*   **AttachmentUpload handleFileChange**: Test de logica van `handleFileChange` in `AttachmentUpload` om te valideren dat het geselecteerde bestand correct wordt verwerkt.
*   **ApproveRejectModal handleApprove**: Test de logica van `handleApprove` in `ApproveRejectModal` om te valideren dat de approve callback met de juiste ID wordt aangeroepen.
*   **ApproveRejectModal handleReject**: Test de logica van `handleReject` in `ApproveRejectModal` om te valideren dat de reject callback met de juiste ID en reden wordt aangeroepen.

### Integration Tests

*   **POST /api/expense-reports → 201 Created**: Verifieert dat een nieuwe onkostennota succesvol kan worden aangemaakt met geldige data, resulterend in een `201 Created` statuscode en de correcte response body.
*   **GET /api/expense-reports → 200 OK**: Verifieert dat een lijst van onkostennota's succesvol kan worden opgehaald, resulterend in een `200 OK` statuscode en een correcte response body.
*   **DELETE /api/expense-reports/{id} → 204 No Content**: Verifieert dat een specifieke onkostennota succesvol kan worden verwijderd, resulterend in een `204 No Content` statuscode.
*   **POST /api/expense-reports/{id}/attachment → 201 Created**: Verifieert dat een bijlage succesvol kan worden geüpload naar een specifieke onkostennota, resulterend in een `201 Created` statuscode.
*   **PUT /api/expense-reports/{id}/approve → 200 OK**: Verifieert dat een onkostennota succesvol kan worden goedgekeurd, resulterend in een `200 OK` statuscode.
*   **PUT /api/expense-reports/{id}/reject → 200 OK**: Verifieert dat een onkostennota succesvol kan worden afgewezen met een reden, resulterend in een `200 OK` statuscode.
*   **GET /api/expense-reports/monthly-total → 200 OK**: Verifieert dat het maandelijkse totaal van onkosten succesvol kan worden opgehaald, resulterend in een `200 OK` statuscode.
*   **POST /api/expense-reports → 400 Bad Request (validation error)**: Verifieert dat het aanmaken van een onkostennota met ongeldige data (bv. ontbrekende verplichte velden) resulteert in een `400 Bad Request` statuscode met een duidelijke foutmelding.
*   **POST /api/expense-reports/{id}/attachment → 400 Bad Request (file validation error)**: Verifieert dat het uploaden van een ongeldig bestandstype of te groot bestand resulteert in een `400 Bad Request` statuscode.
*   **PUT /api/expense-reports/{id}/reject → 400 Bad Request (validation error)**: Verifieert dat het afwijzen van een onkostennota zonder opgave van een reden (indien vereist) resulteert in een `400 Bad Request` statuscode.

### End-to-End (E2E) Tests

*   **Medewerker maakt een nieuwe onkostennota aan, vult alle velden in, uploadt een bonnetje en dient deze in.**
    *   Navigeer naar de onkostennota aanmaakpagina.
    *   Vul alle verplichte en optionele velden in (datum, beschrijving, bedrag, categorie).
    *   Upload een geldig bonnetje (bv. JPG, PNG).
    *   Klik op de 'Indienen' knop.
    *   Verifieer dat de onkostennota succesvol is aangemaakt en zichtbaar is in de lijst van de medewerker met de status 'Pending'.
*   **Medewerker bekijkt zijn eigen onkostennota's en ziet de status en het totaal per maand.**
    *   Log in als medewerker.
    *   Navigeer naar de onkostennota overzichtspagina.
    *   Verifieer dat alle ingediende onkostennota's worden weergegeven met hun correcte status.
    *   Verifieer dat de 'MonthlyTotalCard' correct het totaalbedrag voor de huidige maand toont.
*   **Manager bekijkt openstaande onkostennota's, keurt er één goed en wijst een andere af met een reden.**
    *   Log in als manager.
    *   Navigeer naar de manager onkostennota overzichtspagina.
    *   Verifieer dat de lijst van openstaande onkostennota's correct wordt weergegeven.
    *   Selecteer een onkostennota en klik op 'Goedkeuren'. Verifieer dat de status wordt bijgewerkt naar 'Approved'.
    *   Selecteer een andere onkostennota, klik op 'Afwijzen' en voer een reden in. Verifieer dat de status wordt bijgewerkt naar 'Rejected' met de opgegeven reden.
*   **Medewerker probeert een onkostennota te verwijderen die al goedgekeurd is en ziet een foutmelding.**
    *   Log in als medewerker.
    *   Zorg ervoor dat er een goedgekeurde onkostennota bestaat.
    *   Navigeer naar de onkostennota overzichtspagina.
    *   Probeer de goedgekeurde onkostennota te verwijderen.
    *   Verifieer dat er een duidelijke foutmelding wordt getoond die aangeeft dat goedgekeurde onkostennota's niet verwijderd kunnen worden.
*   **Medewerker probeert een onkostennota aan te maken met een datum in de toekomst en ziet een foutmelding.**
    *   Log in als medewerker.
    *   Navigeer naar de onkostennota aanmaakpagina.
    *   Vul alle velden in, maar kies een datum die in de toekomst ligt.
    *   Klik op de 'Indienen' knop.
    *   Verifieer dat er een foutmelding wordt getoond die aangeeft dat de datum niet in de toekomst mag liggen.

## 12. Traceability Matrix

| REQ | Backend | Frontend | Tests |
|-----|---------|----------|-------|
| REQ-001 | ExpenseReportController, ExpenseReportService, ExpenseReportRepository, ExpenseReportEntity, CreateExpenseReportRequestDto, ExpenseReportValidator | ExpenseForm | T; e; s; t; e; n;  ; o; f;  ; e; e; n;  ; o; n; k; o; s; t; e; n; n; o; t; a;  ; s; u; c; c; e; s; v; o; l;  ; k; a; n;  ; w; o; r; d; e; n;  ; a; a; n; g; e; m; a; a; k; t;  ; m; e; t;  ; a; l; l; e;  ; v; e; r; e; i; s; t; e;  ; v; e; l; d; e; n;  ; (; d; a; t; u; m; ,;  ; b; e; d; r; a; g; ,;  ; c; a; t; e; g; o; r; i; e; ,;  ; b; e; s; c; h; r; i; j; v; i; n; g; ); . |
| REQ-002 | ExpenseReportValidator, DateValidator | ExpenseForm | T; e; s; t; e; n;  ; o; f;  ; h; e; t;  ; a; a; n; m; a; k; e; n;  ; v; a; n;  ; e; e; n;  ; o; n; k; o; s; t; e; n; n; o; t; a;  ; m; e; t;  ; e; e; n;  ; d; a; t; u; m;  ; i; n;  ; d; e;  ; t; o; e; k; o; m; s; t;  ; w; o; r; d; t;  ; g; e; w; e; i; g; e; r; d; . |
| REQ-003 | ExpenseReportValidator, AmountValidator | ExpenseForm | T; e; s; t; e; n;  ; o; f;  ; h; e; t;  ; a; a; n; m; a; k; e; n;  ; v; a; n;  ; e; e; n;  ; o; n; k; o; s; t; e; n; n; o; t; a;  ; m; e; t;  ; e; e; n;  ; n; e; g; a; t; i; e; f;  ; b; e; d; r; a; g;  ; o; f;  ; e; e; n;  ; b; e; d; r; a; g;  ; b; o; v; e; n;  ; d; e;  ; 1; 0; .; 0; 0; 0;  ; e; u; r; o;  ; w; o; r; d; t;  ; g; e; w; e; i; g; e; r; d; . |
| REQ-004 | ExpenseReportValidator, CategoryValidator | ExpenseForm | T; e; s; t; e; n;  ; o; f;  ; h; e; t;  ; a; a; n; m; a; k; e; n;  ; v; a; n;  ; e; e; n;  ; o; n; k; o; s; t; e; n; n; o; t; a;  ; m; e; t;  ; e; e; n;  ; o; n; g; e; l; d; i; g; e;  ; c; a; t; e; g; o; r; i; e;  ; w; o; r; d; t;  ; g; e; w; e; i; g; e; r; d; . |
| REQ-005 | ExpenseReportAttachmentController, ExpenseReportAttachmentService, ExpenseReportAttachmentRepository, ExpenseReportAttachmentEntity, AttachmentValidationException, FileStorageService, AmazonS3FileStorageService | AttachmentUpload | T; e; s; t; e; n;  ; o; f;  ; e; e; n;  ; g; e; b; r; u; i; k; e; r;  ; e; e; n;  ; b; o; n; n; e; t; j; e;  ; k; a; n;  ; u; p; l; o; a; d; e; n;  ; m; e; t;  ; d; e;  ; j; u; i; s; t; e;  ; b; e; s; t; a; n; d; s; t; y; p; e; s;  ; (; J; P; E; G; ,;  ; P; N; G; ,;  ; P; D; F; );  ; e; n;  ; b; i; n; n; e; n;  ; d; e;  ; m; a; x; i; m; a; l; e;  ; b; e; s; t; a; n; d; s; g; r; o; o; t; t; e;  ; (; 5; M; B; ); . |
| REQ-006 | ExpenseReportController, ExpenseReportService, ExpenseReportRepository, ExpenseReportResponseDto, ExpenseReportListResponseDto | ExpenseList | T; e; s; t; e; n;  ; o; f;  ; e; e; n;  ; m; e; d; e; w; e; r; k; e; r;  ; e; e; n;  ; l; i; j; s; t;  ; v; a; n;  ; z; i; j; n;  ; e; i; g; e; n;  ; o; n; k; o; s; t; e; n; n; o; t; a; '; s;  ; k; a; n;  ; b; e; k; i; j; k; e; n;  ; m; e; t;  ; d; e;  ; s; t; a; t; u; s;  ; e; n;  ; h; e; t;  ; b; e; d; r; a; g;  ; v; a; n;  ; e; l; k; e;  ; n; o; t; a; . |
| REQ-007 | ExpenseReportController, ExpenseReportService, MonthlyTotalResponseDto | MonthlyTotalCard | T; e; s; t; e; n;  ; o; f;  ; h; e; t;  ; t; o; t; a; a; l; b; e; d; r; a; g;  ; v; a; n;  ; g; o; e; d; g; e; k; e; u; r; d; e;  ; o; n; k; o; s; t; e; n;  ; p; e; r;  ; k; a; l; e; n; d; e; r; m; a; a; n; d;  ; c; o; r; r; e; c; t;  ; w; o; r; d; t;  ; w; e; e; r; g; e; g; e; v; e; n; . |
| REQ-008 | ExpenseReportController, ExpenseReportService, InvalidExpenseReportStatusException | ExpenseList | T; e; s; t; e; n;  ; o; f;  ; e; e; n;  ; o; n; k; o; s; t; e; n; n; o; t; a;  ; m; e; t;  ; d; e;  ; s; t; a; t; u; s;  ; '; i; n; g; e; d; i; e; n; d; ';  ; s; u; c; c; e; s; v; o; l;  ; k; a; n;  ; w; o; r; d; e; n;  ; v; e; r; w; i; j; d; e; r; d; . |
| REQ-009 | ExpenseReportController, ExpenseReportService, RejectExpenseReportRequestDto, InvalidExpenseReportStatusException | ManagerExpenseList, ApproveRejectModal | T; e; s; t; e; n;  ; o; f;  ; e; e; n;  ; m; a; n; a; g; e; r;  ; e; e; n;  ; o; n; k; o; s; t; e; n; n; o; t; a;  ; k; a; n;  ; g; o; e; d; k; e; u; r; e; n;  ; o; f;  ; a; f; w; i; j; z; e; n; ,;  ; e; n;  ; o; f;  ; c; o; m; m; e; n; t; a; a; r;  ; v; e; r; p; l; i; c; h; t;  ; i; s;  ; b; i; j;  ; a; f; w; i; j; z; i; n; g; . |
| REQ-010 | ApiErrorControllerAdvice, ApiError | ErrorDisplay | T; e; s; t; e; n;  ; o; f;  ; e; r;  ; v; e; l; d; s; p; e; c; i; f; i; e; k; e;  ; f; o; u; t; m; e; l; d; i; n; g; e; n;  ; w; o; r; d; e; n;  ; g; e; t; o; o; n; d;  ; b; i; j;  ; o; n; g; e; l; d; i; g; e;  ; i; n; v; o; e; r;  ; i; n;  ; d; e;  ; f; o; r; m; u; l; i; e; r; e; n; . |
| REQ-011 | ExpenseReportController, ExpenseReportService, InvalidExpenseReportStatusException |  | T; e; s; t; e; n;  ; o; f;  ; e; e; n;  ; o; n; k; o; s; t; e; n; n; o; t; a;  ; n; i; e; t;  ; g; e; w; i; j; z; i; g; d;  ; k; a; n;  ; w; o; r; d; e; n;  ; n; a; d; a; t;  ; d; e; z; e;  ; i; s;  ; i; n; g; e; d; i; e; n; d; . |
| REQ-012 | SecurityConfig, JwtTokenProvider, UserPrincipal, EmployeeService | ExpenseList, ManagerExpenseList | T; e; s; t; e; n;  ; o; f;  ; a; l; l; e; e; n;  ; d; e;  ; e; i; g; e; n;  ; m; e; d; e; w; e; r; k; e; r;  ; e; n;  ; z; i; j; n;  ; d; i; r; e; c; t; e;  ; m; a; n; a; g; e; r;  ; t; o; e; g; a; n; g;  ; h; e; b; b; e; n;  ; t; o; t;  ; o; n; k; o; s; t; e; n; n; o; t; a; '; s; . |
| REQ-013 | ExpenseReportController, ExpenseReportService, InvalidExpenseReportStatusException |  | T; e; s; t; e; n;  ; o; f;  ; e; e; n;  ; g; o; e; d; g; e; k; e; u; r; d; e;  ; o; f;  ; a; f; g; e; w; e; z; e; n;  ; o; n; k; o; s; t; e; n; n; o; t; a;  ; n; i; e; t;  ; v; e; r; w; i; j; d; e; r; d;  ; k; a; n;  ; w; o; r; d; e; n; . |
| REQ-014 | ExpenseReportAttachmentService, FileStorageService, AmazonS3FileStorageService |  | T; e; s; t; e; n;  ; o; f;  ; b; i; j; l; a; g; e; n;  ; w; o; r; d; e; n;  ; o; p; g; e; s; l; a; g; e; n;  ; i; n;  ; o; b; j; e; c; t;  ; s; t; o; r; a; g; e;  ; e; n;  ; n; i; e; t;  ; i; n;  ; d; e;  ; d; a; t; a; b; a; s; e; . |
| REQ-015 | ExpenseReportEntity, CreateExpenseReportRequestDto |  | T; e; s; t; e; n;  ; o; f;  ; b; e; d; r; a; g; e; n;  ; w; o; r; d; e; n;  ; o; p; g; e; s; l; a; g; e; n;  ; a; l; s;  ; g; e; h; e; l; e;  ; g; e; t; a; l; l; e; n;  ; i; n;  ; e; u; r; o; c; e; n; t; e; n; . |
| REQ-016 | ExpenseReportController, ExpenseReportService, ExpenseReportAttachmentController, ExpenseReportAttachmentService |  | T; e; s; t; e; n;  ; v; a; n;  ; d; e;  ; r; e; s; p; o; n; s; t; i; j; d;  ; v; a; n;  ; d; e;  ; A; P; I;  ; v; o; o; r;  ; a; l; l; e;  ; o; p; e; r; a; t; i; e; s; ,;  ; m; e; t;  ; u; i; t; z; o; n; d; e; r; i; n; g;  ; v; a; n;  ; b; e; s; t; a; n; d; s; u; p; l; o; a; d; ,;  ; o; m;  ; t; e;  ; v; e; r; i; f; i; ë; r; e; n;  ; d; a; t;  ; d; e; z; e;  ; b; i; n; n; e; n;  ; 1;  ; s; e; c; o; n; d; e;  ; r; e; a; g; e; e; r; t; . |

