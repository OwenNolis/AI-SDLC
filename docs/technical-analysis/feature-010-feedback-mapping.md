# Feature-010: Feedback Mapping Beheer

## 1. Scope

**In Scope:**

*   Beheren van `FeedbackMapping`-regels: aanmaken, bewerken en deactiveren.
*   Koppelen van een `FeedbackMapping` aan een `ServiceNotification` en een `BusinessUnit`.
*   Instellen van `plannerFeedbackType` (enum: `DELIVERY`, `PICKUP`, `CROSSDOCK`, `NOT_CAPTURED`).
*   Instellen van `mainReasonCode` + `mainReasonCodeDescription` en `subReasonCode` + `subReasonCodeDescription`.
*   Filteren en zoeken van `FeedbackMappings` op `servicenotification`, `businessunit` en `activityType`.
*   Validatie: per combinatie van (`ServiceNotification`, `BusinessUnit`, `PlannerFeedbackActivityType`) mag maximaal één actieve `FeedbackMapping` bestaan.

**Out of Scope:**

*   Aanmaken of bewerken van `ServiceNotifications` zelf.
*   Aanmaken of bewerken van `BusinessUnits`.
*   Automatische verwerking van feedbackberichten (dit is uitsluitend beheerconfiguratie).
*   Historische audit van wijzigingen aan `FeedbackMappings`.

## 2. Assumptions

*   De entiteiten `ServiceNotification` en `BusinessUnit` bestaan reeds en hun ID's zijn beschikbaar voor koppeling.
*   De `PlannerFeedbackActivityType` is een bestaand concept dat gebruikt kan worden voor filtering en validatie.
*   De enumeratie `PlannerFeedbackType` is gedefinieerd met de waarden `DELIVERY`, `PICKUP`, `CROSSDOCK`, `NOT_CAPTURED`.

## 3. Open Questions

*   Welke specifieke velden zijn vereist voor de `ServiceNotification` en `BusinessUnit` entiteiten die gekoppeld zullen worden aan `FeedbackMapping`?
*   Is er een bestaande tabel of mechanisme voor het genereren van unieke `mainReasonCode` en `subReasonCode` waarden, of moeten deze vrij gedefinieerd worden binnen de `FeedbackMapping`?
*   Hoe moet de `PlannerFeedbackActivityType` worden geïmplementeerd en welke waarden kan deze aannemen?
*   Is er een specifieke vereiste voor de volgorde van sortering bij het filteren en zoeken van `FeedbackMappings`?
*   Moeten er specifieke permissies worden toegekend voor het beheren van `FeedbackMappings` (bijvoorbeeld `ROLE_ADMIN` vs. `ROLE_USER`)?

## 4. Domain Model

Dit gedeelte beschrijft de domeinmodellen die de kern van de applicatie vormen, inclusief hun velden, typen, beperkingen en testcases.

### ServiceNotification

| Veld | Type | Constraints | Testcases |
|---|---|---|---|
| id | UUID | notNull | |
| serviceType | ShipmentServiceTypeClass | notNull | |
| taskName | String | notNull | |
| active | Boolean | notNull | |

### BusinessUnit

| Veld | Type | Constraints | Testcases |
|---|---|---|---|
| code | String | notNull | |
| lastModifiedBy | String | | |
| active | Boolean | notNull | |

### FeedbackMapping

| Veld | Type | Constraints | Testcases |
|---|---|---|---|
| id | UUID | notNull | |
| serviceNotificationId | UUID | notNull | missing, invalid_value |
| businessUnitCode | String | notNull | missing, invalid_value |
| plannerFeedbackType | PlannerFeedbackActivityType | notNull | missing, invalid_value |
| mainReasonCode | String | notNull, maxLength:50 | empty, too_long, missing |
| mainReasonCodeDescription | String | notNull, maxLength:255 | empty, too_long, missing |
| subReasonCode | String | maxLength:50 | too_long |
| subReasonCodeDescription | String | maxLength:255 | too_long |
| active | Boolean | notNull | |
| createdAt | LocalDateTime | notNull | |
| updatedAt | LocalDateTime | notNull | |
| deletedAt | LocalDateTime | | |

### Enums

De volgende enumeratietypen worden gebruikt binnen de domeinmodellen:

*   **PlannerFeedbackActivityType**:
    *   `DELIVERY`
    *   `PICKUP`
    *   `CROSSDOCK`
    *   `NOT_CAPTURED`

## 5. API Design

### 5.1 Error Formaat

```json
{
  "status": 400,
  "error": "Validation failed",
  "correlationId": "uuid-here",
  "fields": { "fieldName": "error message" }
}
```

### 5.2 Endpoints

#### 5.2.1 POST /api/feedback-mappings — Maak een nieuwe FeedbackMapping aan

| Veld              | Waarde                                     |
| :---------------- | :----------------------------------------- |
| Method            | POST                                       |
| Path              | /api/feedback-mappings                     |
| Auth              | bearer                                     |
| Request DTO       | CreateFeedbackMappingRequest               |

| Status | Body                     | Omschrijving                                                              |
| :----- | :----------------------- | :------------------------------------------------------------------------ |
| 201    | FeedbackMappingResponse  | FeedbackMapping succesvol aangemaakt.                                     |
| 400    | ApiError                 | Validatiefout in de request body.                                         |
| 404    | ApiError                 | ServiceNotification of BusinessUnit niet gevonden of inactief.           |
| 409    | ApiError                 | Een actieve FeedbackMapping met deze combinatie van serviceNotificationId, businessUnitCode en plannerFeedbackType bestaat al. |
| 500    | ApiError                 | Onverwachte serverfout.                                                   |

**Validatieregels:**
*   `serviceNotificationId`: Moet een geldige UUID zijn en verwijzen naar een actieve ServiceNotification.
*   `businessUnitCode`: Moet een geldige String zijn en verwijzen naar een actieve BusinessUnit.
*   `plannerFeedbackType`: Moet een geldige enum waarde zijn (DELIVERY, PICKUP, CROSSDOCK, NOT_CAPTURED).
*   `mainReasonCode`: Moet een verplichte String zijn, maximaal 50 tekens lang.
*   `mainReasonCodeDescription`: Moet een verplichte String zijn, maximaal 255 tekens lang.
*   `subReasonCode`: Optionele String, maximaal 50 tekens lang.
*   `subReasonCodeDescription`: Verplichte String als subReasonCode is opgegeven, maximaal 255 tekens lang.

#### 5.2.2 GET /api/feedback-mappings — Haal een gepagineerde lijst van FeedbackMappings op met filtering

| Veld              | Waarde                                     |
| :---------------- | :----------------------------------------- |
| Method            | GET                                        |
| Path              | /api/feedback-mappings                     |
| Auth              | bearer                                     |
| Request DTO       | N.v.t. (Query Parameters)                  |

| Status | Body                     | Omschrijving                               |
| :----- | :----------------------- | :----------------------------------------- |
| 200    | FeedbackMappingListResponse | Lijst van FeedbackMappings succesvol opgehaald. |
| 400    | ApiError                 | Ongeldige query parameters.                |
| 500    | ApiError                 | Onverwachte serverfout.                    |

**Query Parameters:**
*   `serviceNotificationId` (UUID): Filter op ServiceNotification ID.
*   `businessUnitCode` (String): Filter op Business Unit code.
*   `plannerFeedbackType` (PlannerFeedbackActivityType): Filter op Planner Feedback Activity Type.
*   `active` (Boolean, default: true): Filter op actieve FeedbackMappings.
*   `page` (Integer, default: 0): Paginanummer (startend bij 0).
*   `size` (Integer, default: 10): Aantal items per pagina.
*   `sort` (String, default: 'createdAt,desc'): Sorteerparameter (bv. 'fieldName,asc' of 'fieldName,desc').

#### 5.2.3 GET /api/feedback-mappings/{id} — Haal een specifieke FeedbackMapping op

| Veld              | Waarde                                     |
| :---------------- | :----------------------------------------- |
| Method            | GET                                        |
| Path              | /api/feedback-mappings/{id}                |
| Auth              | bearer                                     |
| Request DTO       | N.v.t. (Path Parameters)                   |

| Status | Body                     | Omschrijving                   |
| :----- | :----------------------- | :----------------------------- |
| 200    | FeedbackMappingResponse  | FeedbackMapping succesvol opgehaald. |
| 404    | ApiError                 | FeedbackMapping niet gevonden. |
| 500    | ApiError                 | Onverwachte serverfout.        |

**Path Parameters:**
*   `id` (UUID): De UUID van de FeedbackMapping.

#### 5.2.4 PUT /api/feedback-mappings/{id} — Bewerk een bestaande FeedbackMapping

| Veld              | Waarde                                     |
| :---------------- | :----------------------------------------- |
| Method            | PUT                                        |
| Path              | /api/feedback-mappings/{id}                |
| Auth              | bearer                                     |
| Request DTO       | UpdateFeedbackMappingRequest               |

| Status | Body                     | Omschrijving                                                                                                                            |
| :----- | :----------------------- | :-------------------------------------------------------------------------------------------------------------------------------------- |
| 200    | FeedbackMappingResponse  | FeedbackMapping succesvol bijgewerkt.                                                                                                   |
| 400    | ApiError                 | Validatiefout in de request body.                                                                                                       |
| 404    | ApiError                 | FeedbackMapping niet gevonden.                                                                                                          |
| 409    | ApiError                 | Een andere actieve FeedbackMapping met deze combinatie van serviceNotificationId, businessUnitCode en plannerFeedbackType bestaat al na de wijziging. |
| 500    | ApiError                 | Onverwachte serverfout.                                                                                                                 |

**Path Parameters:**
*   `id` (UUID): De UUID van de FeedbackMapping die bewerkt moet worden.

**Validatieregels:**
*   `mainReasonCode`: Moet een verplichte String zijn, maximaal 50 tekens lang.
*   `mainReasonCodeDescription`: Moet een verplichte String zijn, maximaal 255 tekens lang.
*   `subReasonCode`: Optionele String, maximaal 50 tekens lang.
*   `subReasonCodeDescription`: Verplichte String als subReasonCode is opgegeven, maximaal 255 tekens lang.
*   `active`: Boolean, bepaalt of de mapping actief is.

#### 5.2.5 DELETE /api/feedback-mappings/{id} — Deactiveer een FeedbackMapping (soft delete)

| Veld              | Waarde                                     |
| :---------------- | :----------------------------------------- |
| Method            | DELETE                                     |
| Path              | /api/feedback-mappings/{id}                |
| Auth              | bearer                                     |
| Request DTO       | N.v.t. (Path Parameters)                   |

| Status | Body                     | Omschrijving                   |
| :----- | :----------------------- | :----------------------------- |
| 204    | N.v.t.                   | FeedbackMapping succesvol gedeactiveerd. |
| 404    | ApiError                 | FeedbackMapping niet gevonden. |
| 500    | ApiError                 | Onverwachte serverfout.        |

**Path Parameters:**
*   `id` (UUID): De UUID van de FeedbackMapping die gedeactiveerd moet worden.

## 6. Backend Design

De backend volgt een gelaagde architectuur: Controller → Service → Repository. Dit zorgt voor een duidelijke scheiding van verantwoordelijkheden en bevordert testbaarheid en onderhoudbaarheid.

*   **Controllers**: Verantwoordelijk voor het afhandelen van inkomende HTTP-verzoeken, het valideren van de request body (via DTO's en validators) en het delegeren van de business logica naar de Service laag. Ze retourneren de response DTO's.
*   **Services**: Bevatten de kern business logica. Ze orkestreren operaties, voeren transacties uit (`@Transactional`), en interageren met de Repository laag voor data-persistentie. Ze kunnen ook custom exceptions thrown.
*   **Repositories**: Gebruiken Spring Data JPA interfaces om data-access operaties uit te voeren op de database. Ze zijn verantwoordelijk voor CRUD-operaties en het ophalen van entiteiten.
*   **Domain Models**: Pure Java-klassen die de datastructuur van de applicatie representeren, zonder Spring-specifieke annotaties.
*   **DTOs**: Java records worden gebruikt voor Request en Response objecten om de data-uitwisseling tussen de lagen te definiëren.
*   **Mappers**: MapStruct wordt gebruikt voor het efficiënt mappen tussen DTO's en Domain Models.
*   **Validators**: Zodra de input is gevalideerd, worden specifieke validators gebruikt om de business regels te controleren.

### Feedback Mapping Module

| Klasse                         | Verantwoordelijkheid                                                                                             |
| :----------------------------- | :--------------------------------------------------------------------------------------------------------------- |
| `FeedbackMappingController`    | Exposeert de REST API endpoints voor FeedbackMappings.                                                           |
| `FeedbackMappingService`       | Orchestreert de business logica voor FeedbackMappings.                                                           |
| `FeedbackMappingRepository`    | Verantwoordelijk voor data-access operaties op FeedbackMappings.                                                 |
| `FeedbackMapping`              | Representeert de FeedbackMapping entiteit in de database.                                                        |
| `CreateFeedbackMappingRequest` | DTO voor het aanmaken van een nieuwe FeedbackMapping.                                                            |
| `UpdateFeedbackMappingRequest` | DTO voor het bijwerken van een bestaande FeedbackMapping.                                                        |
| `FeedbackMappingResponse`      | DTO voor het retourneren van een FeedbackMapping.                                                                 |
| `FeedbackMappingListResponse`  | DTO voor het retourneren van een lijst van FeedbackMappings.                                                      |
| `FeedbackMappingNotFoundException` | Exception die wordt gegooid wanneer een FeedbackMapping niet wordt gevonden.                                     |
| `FeedbackMappingConflictException` | Exception die wordt gegooid wanneer een duplicaat FeedbackMapping wordt gedetecteerd.                           |
| `ServiceNotificationNotFoundException` | Exception die wordt gegooid wanneer een ServiceNotification niet wordt gevonden.                               |
| `BusinessUnitNotFoundException` | Exception die wordt gegooid wanneer een BusinessUnit niet wordt gevonden.                                        |
| `FeedbackMappingCreateValidator` | Valideert de input voor het aanmaken van een FeedbackMapping.                                                    |
| `FeedbackMappingUpdateValidator` | Valideert de input voor het bijwerken van een FeedbackMapping.                                                   |
| `FeedbackMappingExistsService` | Controleert op de uniekheid van FeedbackMappings op basis van `serviceNotificationId`, `businessUnitCode` en `plannerFeedbackType`. |
| `ActiveServiceNotificationValidator` | Valideert of een ServiceNotification actief is.                                                                 |
| `ActiveBusinessUnitValidator`  | Valideert of een BusinessUnit actief is.                                                                        |
| `PlannerFeedbackTypeValidator` | Valideert of de `plannerFeedbackType` een geldige enum waarde is.                                               |
| `FeedbackMappingLoggingService`| Logt schrijfoperaties op FeedbackMappings met correlationId en gebruiker-id.                                     |

### Common Module

| Klasse                     | Verantwoordelijkheid                                                              |
| :------------------------- | :-------------------------------------------------------------------------------- |
| `ApiError`                 | Standaard foutformaat voor API responses.                                         |
| `ApiErrorResponse`         | DTO voor het retourneren van API fouten.                                           |
| `PageableRequest`          | DTO voor paginering en sortering parameters.                                      |
| `SecurityConfig`           | Configureert beveiligingsinstellingen, inclusief rolgebaseerde toegang.           |
| `JwtAuthenticationFilter`  | Filter voor het valideren van JWT tokens voor authenticatie.                      |
| `CustomUserDetailsService` | Laadt gebruikersdetails voor authenticatie.                                       |
| `AuthenticationFacade`     | Biedt toegang tot de huidige geauthenticeerde gebruiker.                          |

### Domain Module

| Klasse                     | Verantwoordelijkheid                                                              |
| :------------------------- | :-------------------------------------------------------------------------------- |
| `ServiceNotification`      | Representeert de ServiceNotification entiteit.                                    |
| `BusinessUnit`             | Representeert de BusinessUnit entiteit.                                           |
| `PlannerFeedbackActivityType` | Enum voor de mogelijke waarden van `plannerFeedbackType`.                        |
| `ShipmentServiceTypeClass` | Enum voor de mogelijke waarden van `serviceType`.                                |

## 7. Frontend Design

Dit gedeelte beschrijft de frontend architectuur en componenten voor de Feedback Mapping Beheer feature. De frontend zal worden gebouwd met React 18, TypeScript en Vite, conform de projectregels. State management wordt afgehandeld door React Query voor server state en `useState` voor lokale UI state. Formulieren worden beheerd met React Hook Form en Zod voor validatie. API-calls worden gedaan via Axios met interceptors voor authenticatie en correlatie-ID's.

### /admin/feedback-mappings

| Component | Verantwoordelijkheid |
|---|---|
| FeedbackMappingTable | Toont een tabel van feedback mappings met filtermogelijkheden en navigatie naar detail/edit pagina's. |
| FeedbackMappingFilterBar | Biedt filteropties voor ServiceNotification, BusinessUnit en PlannerFeedbackActivityType. |
| LoadingSpinner | Toont een laadindicator tijdens het ophalen van data. |
| ErrorDisplay | Toont foutmeldingen aan de gebruiker. |

### /admin/feedback-mappings/new

| Component | Verantwoordelijkheid |
|---|---|
| FeedbackMappingForm | Formulier voor het aanmaken van een nieuwe feedback mapping. |
| ServiceNotificationSelect | Dropdown voor het selecteren van een actieve ServiceNotification. |
| BusinessUnitSelect | Dropdown voor het selecteren van een actieve BusinessUnit. |
| ActivityTypeSelect | Dropdown voor het selecteren van een PlannerFeedbackActivityType. |
| ReasonCodeFields | Inputvelden voor mainReasonCode, mainReasonCodeDescription, subReasonCode en subReasonCodeDescription. |
| LoadingSpinner | Toont een laadindicator tijdens het ophalen van dropdown data of bij het indienen van het formulier. |
| ErrorDisplay | Toont foutmeldingen aan de gebruiker, inclusief validatiefouten. |

### /admin/feedback-mappings/:id/edit

| Component | Verantwoordelijkheid |
|---|---|
| FeedbackMappingForm | Formulier voor het bewerken van een bestaande feedback mapping (met readonly sleutelcombinatie). |
| ServiceNotificationSelect | Dropdown voor het selecteren van een actieve ServiceNotification (readonly). |
| BusinessUnitSelect | Dropdown voor het selecteren van een actieve BusinessUnit (readonly). |
| ActivityTypeSelect | Dropdown voor het selecteren van een PlannerFeedbackActivityType (readonly). |
| ReasonCodeFields | Inputvelden voor mainReasonCode, mainReasonCodeDescription, subReasonCode en subReasonCodeDescription. |
| LoadingSpinner | Toont een laadindicator tijdens het ophalen van data of bij het indienen van het formulier. |
| ErrorDisplay | Toont foutmeldingen aan de gebruiker, inclusief validatiefouten. |

## 8. Security & Privacy

Alle endpoints gerelateerd aan `FeedbackMapping` zijn beveiligd en vereisen de `ROLE_ADMIN` autorisatie. Dit wordt geïmplementeerd via Spring Security's `HttpSecurity` configuratie, waarbij specifieke paden (`/api/feedback-mappings/**`) worden gematcht en geautoriseerd voor de `ROLE_ADMIN`.

*   **Authenticatie**: Stateless JWT Bearer token authenticatie is in gebruik, conform de architectuurregels.
*   **Autorisatie**: Toegang tot alle CRUD-operaties en het ophalen van de lijst van `FeedbackMapping` is strikt beperkt tot gebruikers met de `ROLE_ADMIN`.
*   **Privacy**: Gevoelige data zoals e-mailadressen, wachtwoorden of creditcardnummers worden niet opgeslagen in de `FeedbackMapping` entiteit of in logs. De velden `serviceNotificationId`, `businessUnitCode`, `plannerFeedbackType`, `mainReasonCode`, `mainReasonCodeDescription`, `subReasonCode`, en `subReasonCodeDescription` bevatten geen persoonsgegevens.

## 9. Observability

### Logging

Logging wordt afgehandeld door SLF4J met Logback, met gestructureerde JSON-logs in productie. Alle logberichten bevatten de `correlationId` en, waar relevant, de `userId` van de actor.

**Voorbeelden van logberichten:**

*   **Aanmaken FeedbackMapping (Success):**
    ```json
    {
      "timestamp": "2023-10-27T10:30:00.123Z",
      "level": "INFO",
      "thread": "http-nio-8080-exec-1",
      "logger": "com.example.feedback.controller.FeedbackMappingController",
      "message": "FeedbackMapping created successfully.",
      "correlationId": "a1b2c3d4-e5f6-7890-1234-567890abcdef",
      "userId": "admin-user-id",
      "details": {
        "feedbackMappingId": "f1e2d3c4-b5a6-7890-1234-567890abcdef",
        "serviceNotificationId": "sn-12345",
        "businessUnitCode": "BU001",
        "plannerFeedbackType": "DELIVERY"
      }
    }
    ```
*   **Aanmaken FeedbackMapping (Conflict - Duplicate Key):**
    ```json
    {
      "timestamp": "2023-10-27T10:31:00.456Z",
      "level": "WARN",
      "thread": "http-nio-8080-exec-2",
      "logger": "com.example.feedback.service.FeedbackMappingService",
      "message": "Attempted to create duplicate FeedbackMapping.",
      "correlationId": "b2c3d4e5-f6a7-8901-2345-67890abcdef1",
      "userId": "admin-user-id",
      "details": {
        "serviceNotificationId": "sn-12345",
        "businessUnitCode": "BU001",
        "plannerFeedbackType": "DELIVERY"
      }
    }
    ```
*   **Bewerken FeedbackMapping (Validation Error):**
    ```json
    {
      "timestamp": "2023-10-27T10:32:00.789Z",
      "level": "ERROR",
      "thread": "http-nio-8080-exec-3",
      "logger": "com.example.feedback.controller.FeedbackMappingController",
      "message": "Validation failed for FeedbackMapping update.",
      "correlationId": "c3d4e5f6-a7b8-9012-3456-7890abcdef12",
      "userId": "admin-user-id",
      "error": "Validation failed",
      "status": 400,
      "fields": {
        "mainReasonCode": "mainReasonCode cannot be empty if subReasonCode is provided."
      }
    }
    ```
*   **Ophalen Lijst (Performance Warning):**
    ```json
    {
      "timestamp": "2023-10-27T10:33:00.012Z",
      "level": "WARN",
      "thread": "http-nio-8080-exec-4",
      "logger": "com.example.feedback.controller.FeedbackMappingController",
      "message": "API response time for GET /api/feedback-mappings exceeded threshold.",
      "correlationId": "d4e5f6a7-b8c9-0123-4567-890abcdef123",
      "durationMs": 350
    }
    ```

### Metrics

*   Aantal aanmaak-, update- en deactivatie-operaties per `FeedbackMapping`.
*   Aantal verzoeken naar de lijst-endpoint, met succesvolle en mislukte responsen.
*   Responstijden voor alle `FeedbackMapping` gerelateerde endpoints, met name de lijst-endpoint (p95 < 300ms).

### Correlation ID

Elke inkomende HTTP-request naar de `/api/feedback-mappings` endpoints wordt voorzien van een unieke `correlationId` door de `CorrelationIdFilter`. Deze `correlationId` wordt:
*   Toegevoegd aan de MDC (Mapped Diagnostic Context) voor gebruik in alle logberichten binnen de request-scope.
*   Meegestuurd in de response headers van foutmeldingen.
*   Gebruikt om gerelateerde logberichten van verschillende services (indien van toepassing in de toekomst) aan elkaar te koppelen.

## 10. Performance & Scalability

### Performance Eisen

*   **API Response Time (p95 < 300ms)**: Het ophalen van de gefilterde lijst van `FeedbackMapping` moet voldoen aan de p95 responstijd eis van minder dan 300 milliseconden. Dit vereist efficiënte databasequeries en indexering.
*   **Schrijfacties**: Aanmaak-, bewerkings- en deactivatie-operaties moeten snel en responsief zijn, idealiter binnen 100ms.

### Database Indexen

Om de performance van de lijst-endpoint te optimaliseren, met name voor filtering op `serviceNotificationId`, `businessUnitCode` en `plannerFeedbackType`, worden de volgende database-indexen aangemaakt op de `feedback_mappings` tabel:

*   Een samengestelde index op `(service_notification_id, business_unit_code, planner_feedback_type, active)`:
    ```sql
    CREATE INDEX idx_feedback_mappings_filter ON feedback_mappings (service_notification_id, business_unit_code, planner_feedback_type, active);
    ```
    Deze index is cruciaal voor de filter- en uniekheidscontroles. De `active` kolom wordt toegevoegd om ook de prestaties van het filteren op actieve mappings te verbeteren.

*   Een index op `created_at` en `updated_at` kan nuttig zijn voor het sorteren of het vinden van recente mappings, hoewel dit momenteel geen expliciete eis is.

### Schaalbaarheid

*   **Stateless Backend**: De Spring Boot applicatie is stateless, wat horizontale schaalbaarheid via load balancing mogelijk maakt.
*   **Database Schaalbaarheid**: PostgreSQL 16 biedt robuuste schaalbaarheidsopties, waaronder replicatie en sharding, mocht de datavolume significant toenemen. De gekozen indexen dragen bij aan de efficiëntie van de databaseoperaties onder belasting.
*   **Soft Deletes**: Het gebruik van soft deletes (`deleted_at`) in plaats van hard deletes zorgt ervoor dat historische data behouden blijft voor auditdoeleinden zonder de prestaties van leesoperaties significant te beïnvloeden, aangezien de query's standaard alleen actieve records ophalen.
*   **Paginering**: De implementatie van paginering op de lijst-endpoint voorkomt het ophalen van grote datasets in één keer, wat de geheugenbelasting op zowel de applicatie als de database vermindert en de responstijden verbetert.

## 11. Test Strategie

Deze sectie beschrijft de teststrategie voor de Feedback Mapping Beheer feature, conform de gedefinieerde testconventies.

### Unit Tests

Unit tests zullen worden uitgevoerd op de individuele componenten en functies om hun correcte werking te valideren. De focus ligt op het isoleren van de kleinste testbare eenheden.

*   `FeedbackMappingTable.render`: Verifieert de correcte rendering van de tabelcomponent.
*   `FeedbackMappingFilterBar.render`: Verifieert de correcte rendering van de filterbalkcomponent.
*   `FeedbackMappingForm.render`: Verifieert de correcte rendering van het feedback mapping formulier.
*   `ServiceNotificationSelect.render`: Verifieert de correcte rendering van de selectiecomponent voor service notificaties.
*   `BusinessUnitSelect.render`: Verifieert de correcte rendering van de selectiecomponent voor business units.
*   `ActivityTypeSelect.render`: Verifieert de correcte rendering van de selectiecomponent voor activiteitstypes.
*   `ReasonCodeFields.render`: Verifieert de correcte rendering van de velden voor redenen.
*   `FeedbackMappingTable.fetchData`: Test de logica voor het ophalen van data voor de tabel.
*   `FeedbackMappingForm.submit`: Test de logica voor het indienen van het formulier.
*   `FeedbackMappingForm.validation`: Test de validatielogica van het formulier.

### Integratie Tests

Integratie tests zullen de interactie tussen verschillende componenten en de backend API valideren. Deze tests zullen de gedefinieerde API endpoints en hun verwachte responses testen.

*   `POST /api/feedback-mappings` → `201 Created`: Verifieert succesvolle creatie van een feedback mapping.
*   `GET /api/feedback-mappings` → `200 OK`: Verifieert succesvolle retrieval van een lijst met feedback mappings.
*   `GET /api/feedback-mappings/{id}` → `200 OK`: Verifieert succesvolle retrieval van een specifieke feedback mapping.
*   `PUT /api/feedback-mappings/{id}` → `200 OK`: Verifieert succesvolle update van een feedback mapping.
*   `DELETE /api/feedback-mappings/{id}` → `204 No Content`: Verifieert succesvolle verwijdering van een feedback mapping.
*   `POST /api/feedback-mappings` → `400 Bad Request (validation error)`: Test de validatie bij het aanmaken van een mapping met ongeldige data.
*   `POST /api/feedback-mappings` → `404 Not Found (ServiceNotification/BusinessUnit)`: Test de foutafhandeling wanneer ServiceNotification of BusinessUnit niet gevonden wordt.
*   `POST /api/feedback-mappings` → `409 Conflict (duplicate mapping)`: Test de foutafhandeling bij het aanmaken van een dubbele mapping.
*   `PUT /api/feedback-mappings/{id}` → `404 Not Found`: Test de foutafhandeling bij het updaten van een niet-bestaande mapping.
*   `PUT /api/feedback-mappings/{id}` → `409 Conflict (duplicate mapping after update)`: Test de foutafhandeling bij het updaten naar een dubbele mapping.
*   `DELETE /api/feedback-mappings/{id}` → `404 Not Found`: Test de foutafhandeling bij het verwijderen van een niet-bestaande mapping.

### End-to-End (E2E) Tests

E2E tests zullen de volledige gebruikerservaring simuleren, van de frontend interactie tot de backend verwerking en database wijzigingen.

*   Als beheerder navigeer naar `/admin/feedback-mappings`, maak een nieuwe mapping aan, controleer of deze in de lijst verschijnt, bewerk de mapping, deactiveer de mapping en controleer of deze niet meer actief is in de lijst.

## 12. Acceptance Criteria

| AC-ID | REQ | Gegeven | Wanneer | Dan | Testtype |
|-------|-----|---------|---------|-----|----------|
| AC-001-1 | REQ-001 | Een ingelogde beheerder met ROLE_ADMIN en een geldige ServiceNotification met id 'SN123' en actieve BusinessUnit met code 'BU456'. | De beheerder maakt een nieuwe FeedbackMapping aan via POST /api/feedback-mappings met de volgende data: serviceNotificationId='SN123', businessUnitCode='BU456', plannerFeedbackType='DELIVERY', mainReasonCode='MR001', mainReasonCodeDescription='Main reason description', subReasonCode='SR001', subReasonCodeDescription='Sub reason description'. | De API retourneert HTTP 201 Created met een FeedbackMappingResponse body die de aangemaakte mapping bevat, inclusief een gegenereerd uniek id, serviceNotificationId='SN123', businessUnitCode='BU456', plannerFeedbackType='DELIVERY', mainReasonCode='MR001', mainReasonCodeDescription='Main reason description', subReasonCode='SR001', subReasonCodeDescription='Sub reason description', en active=true. | integration |
| AC-001-2 | REQ-001 | Een ingelogde beheerder met ROLE_ADMIN en een geldige ServiceNotification met id 'SN123' en actieve BusinessUnit met code 'BU456'. | De beheerder probeert een nieuwe FeedbackMapping aan te maken via POST /api/feedback-mappings zonder het verplichte veld 'mainReasonCode'. | De API retourneert HTTP 400 Bad Request met een ApiError body die een veldspecifieke foutmelding bevat voor 'mainReasonCode'. | integration |
| AC-001-3 | REQ-001 | Een ingelogde beheerder met ROLE_ADMIN en een geldige ServiceNotification met id 'SN123' en actieve BusinessUnit met code 'BU456'. | De beheerder probeert een nieuwe FeedbackMapping aan te maken via POST /api/feedback-mappings met een lege string voor 'mainReasonCodeDescription' terwijl 'mainReasonCode' wel is ingevuld. | De API retourneert HTTP 400 Bad Request met een ApiError body die een veldspecifieke foutmelding bevat voor 'mainReasonCodeDescription'. | integration |
| AC-001-4 | REQ-001 | Een ingelogde beheerder met ROLE_ADMIN en een geldige ServiceNotification met id 'SN123' en actieve BusinessUnit met code 'BU456'. | De beheerder probeert een nieuwe FeedbackMapping aan te maken via POST /api/feedback-mappings met een 'subReasonCode' maar zonder 'subReasonCodeDescription'. | De API retourneert HTTP 400 Bad Request met een ApiError body die een veldspecifieke foutmelding bevat voor 'subReasonCodeDescription'. | integration |
| AC-001-5 | REQ-001 | Een ingelogde beheerder met ROLE_ADMIN en een geldige ServiceNotification met id 'SN123' en actieve BusinessUnit met code 'BU456'. | De beheerder probeert een nieuwe FeedbackMapping aan te maken via POST /api/feedback-mappings met een 'mainReasonCode' van 51 tekens. | De API retourneert HTTP 400 Bad Request met een ApiError body die een veldspecifieke foutmelding bevat voor 'mainReasonCode' (lengte). | integration |
| AC-001-6 | REQ-001 | Een ingelogde beheerder met ROLE_ADMIN en een geldige ServiceNotification met id 'SN123' en actieve BusinessUnit met code 'BU456'. | De beheerder probeert een nieuwe FeedbackMapping aan te maken via POST /api/feedback-mappings met een 'subReasonCode' van 51 tekens. | De API retourneert HTTP 400 Bad Request met een ApiError body die een veldspecifieke foutmelding bevat voor 'subReasonCode' (lengte). | integration |
| AC-001-7 | REQ-001 | Een ingelogde beheerder met ROLE_ADMIN en een geldige ServiceNotification met id 'SN123' en actieve BusinessUnit met code 'BU456'. | De beheerder probeert een nieuwe FeedbackMapping aan te maken via POST /api/feedback-mappings met een 'mainReasonCodeDescription' van 256 tekens. | De API retourneert HTTP 400 Bad Request met een ApiError body die een veldspecifieke foutmelding bevat voor 'mainReasonCodeDescription' (lengte). | integration |
| AC-001-8 | REQ-001 | Een ingelogde beheerder met ROLE_ADMIN en een geldige ServiceNotification met id 'SN123' en actieve BusinessUnit met code 'BU456'. | De beheerder probeert een nieuwe FeedbackMapping aan te maken via POST /api/feedback-mappings met een 'subReasonCodeDescription' van 256 tekens. | De API retourneert HTTP 400 Bad Request met een ApiError body die een veldspecifieke foutmelding bevat voor 'subReasonCodeDescription' (lengte). | integration |
| AC-002-1 | REQ-002 | Een ingelogde beheerder met ROLE_ADMIN en een geldige ServiceNotification met id 'SN123' en actieve BusinessUnit met code 'BU456'. | De beheerder probeert een nieuwe FeedbackMapping aan te maken via POST /api/feedback-mappings met 'plannerFeedbackType' ingesteld op 'INVALID_TYPE'. | De API retourneert HTTP 400 Bad Request met een ApiError body die een veldspecifieke foutmelding bevat voor 'plannerFeedbackType' met de melding dat de waarde ongeldig is. | integration |
| AC-002-2 | REQ-002 | Een ingelogde beheerder met ROLE_ADMIN en een geldige ServiceNotification met id 'SN123' en actieve BusinessUnit met code 'BU456'. | De beheerder probeert een nieuwe FeedbackMapping aan te maken via POST /api/feedback-mappings met 'plannerFeedbackType' ingesteld op 'DELIVERY'. | De API accepteert de aanvraag en retourneert HTTP 201 Created. | integration |
| AC-002-3 | REQ-002 | Een ingelogde beheerder met ROLE_ADMIN en een geldige ServiceNotification met id 'SN123' en actieve BusinessUnit met code 'BU456'. | De beheerder probeert een nieuwe FeedbackMapping aan te maken via POST /api/feedback-mappings met 'plannerFeedbackType' ingesteld op 'PICKUP'. | De API accepteert de aanvraag en retourneert HTTP 201 Created. | integration |
| AC-002-4 | REQ-002 | Een ingelogde beheerder met ROLE_ADMIN en een geldige ServiceNotification met id 'SN123' en actieve BusinessUnit met code 'BU456'. | De beheerder probeert een nieuwe FeedbackMapping aan te maken via POST /api/feedback-mappings met 'plannerFeedbackType' ingesteld op 'CROSSDOCK'. | De API accepteert de aanvraag en retourneert HTTP 201 Created. | integration |
| AC-002-5 | REQ-002 | Een ingelogde beheerder met ROLE_ADMIN en een geldige ServiceNotification met id 'SN123' en actieve BusinessUnit met code 'BU456'. | De beheerder probeert een nieuwe FeedbackMapping aan te maken via POST /api/feedback-mappings met 'plannerFeedbackType' ingesteld op 'NOT_CAPTURED'. | De API accepteert de aanvraag en retourneert HTTP 201 Created. | integration |
| AC-003-1 | REQ-003 | Een ingelogde beheerder met ROLE_ADMIN en een geldige ServiceNotification met id 'SN123' en actieve BusinessUnit met code 'BU456'. | De beheerder probeert een nieuwe FeedbackMapping aan te maken via POST /api/feedback-mappings met een 'mainReasonCode' van 50 tekens. | De API accepteert de aanvraag en retourneert HTTP 201 Created. | integration |
| AC-003-2 | REQ-003 | Een ingelogde beheerder met ROLE_ADMIN en een geldige ServiceNotification met id 'SN123' en actieve BusinessUnit met code 'BU456'. | De beheerder probeert een nieuwe FeedbackMapping aan te maken via POST /api/feedback-mappings met een 'mainReasonCode' van 51 tekens. | De API retourneert HTTP 400 Bad Request met een ApiError body die een veldspecifieke foutmelding bevat voor 'mainReasonCode' (lengte). | integration |
| AC-004-1 | REQ-004 | Een ingelogde beheerder met ROLE_ADMIN en een geldige ServiceNotification met id 'SN123' en actieve BusinessUnit met code 'BU456'. | De beheerder probeert een nieuwe FeedbackMapping aan te maken via POST /api/feedback-mappings met 'subReasonCode' ingesteld op 'SR002' en 'subReasonCodeDescription' ingesteld op 'Sub reason description 2'. | De API accepteert de aanvraag en retourneert HTTP 201 Created. | integration |
| AC-004-2 | REQ-004 | Een ingelogde beheerder met ROLE_ADMIN en een geldige ServiceNotification met id 'SN123' en actieve BusinessUnit met code 'BU456'. | De beheerder probeert een nieuwe FeedbackMapping aan te maken via POST /api/feedback-mappings met 'subReasonCode' ingesteld op 'SR003' maar zonder 'subReasonCodeDescription'. | De API retourneert HTTP 400 Bad Request met een ApiError body die een veldspecifieke foutmelding bevat voor 'subReasonCodeDescription'. | integration |
| AC-004-3 | REQ-004 | Een ingelogde beheerder met ROLE_ADMIN en een geldige ServiceNotification met id 'SN123' en actieve BusinessUnit met code 'BU456'. | De beheerder probeert een nieuwe FeedbackMapping aan te maken via POST /api/feedback-mappings met 'subReasonCode' ingesteld op 'SR004' en 'subReasonCodeDescription' ingesteld op een lege string. | De API retourneert HTTP 400 Bad Request met een ApiError body die een veldspecifieke foutmelding bevat voor 'subReasonCodeDescription'. | integration |
| AC-004-4 | REQ-004 | Een ingelogde beheerder met ROLE_ADMIN en een geldige ServiceNotification met id 'SN123' en actieve BusinessUnit met code 'BU456'. | De beheerder probeert een nieuwe FeedbackMapping aan te maken via POST /api/feedback-mappings zonder 'subReasonCode' en zonder 'subReasonCodeDescription'. | De API accepteert de aanvraag en retourneert HTTP 201 Created. | integration |
| AC-004-5 | REQ-004 | Een ingelogde beheerder met ROLE_ADMIN en een geldige ServiceNotification met id 'SN123' en actieve BusinessUnit met code 'BU456'. | De beheerder probeert een nieuwe FeedbackMapping aan te maken via POST /api/feedback-mappings met 'subReasonCode' ingesteld op een string van 50 tekens en 'subReasonCodeDescription' ingesteld op 'Valid description'. | De API accepteert de aanvraag en retourneert HTTP 201 Created. | integration |
| AC-004-6 | REQ-004 | Een ingelogde beheerder met ROLE_ADMIN en een geldige ServiceNotification met id 'SN123' en actieve BusinessUnit met code 'BU456'. | De beheerder probeert een nieuwe FeedbackMapping aan te maken via POST /api/feedback-mappings met 'subReasonCode' ingesteld op een string van 51 tekens en 'subReasonCodeDescription' ingesteld op 'Valid description'. | De API retourneert HTTP 400 Bad Request met een ApiError body die een veldspecifieke foutmelding bevat voor 'subReasonCode' (lengte). | integration |
| AC-005-1 | REQ-005 | Een ingelogde beheerder met ROLE_ADMIN, een actieve FeedbackMapping met id 'FM1' voor (serviceNotificationId='SN123', businessUnitCode='BU456', plannerFeedbackType='DELIVERY'). | De beheerder probeert een nieuwe FeedbackMapping aan te maken via POST /api/feedback-mappings met dezelfde combinatie: serviceNotificationId='SN123', businessUnitCode='BU456', plannerFeedbackType='DELIVERY', mainReasonCode='MR002', mainReasonCodeDescription='Another reason'. | De API retourneert HTTP 409 Conflict met een ApiError body die aangeeft dat de combinatie al bestaat. | integration |
| AC-005-2 | REQ-005 | Een ingelogde beheerder met ROLE_ADMIN, een gedeactiveerde FeedbackMapping met id 'FM2' voor (serviceNotificationId='SN123', businessUnitCode='BU456', plannerFeedbackType='DELIVERY'). | De beheerder probeert een nieuwe FeedbackMapping aan te maken via POST /api/feedback-mappings met dezelfde combinatie: serviceNotificationId='SN123', businessUnitCode='BU456', plannerFeedbackType='DELIVERY', mainReasonCode='MR003', mainReasonCodeDescription='Yet another reason'. | De API retourneert HTTP 409 Conflict met een ApiError body die aangeeft dat de combinatie al bestaat en niet opnieuw geactiveerd kan worden zolang de oude mapping niet definitief verwijderd is. | integration |
| AC-006-1 | REQ-006 | Een ingelogde beheerder met ROLE_ADMIN en een bestaande FeedbackMapping met id 'FM1' (serviceNotificationId='SN123', businessUnitCode='BU456', plannerFeedbackType='DELIVERY', mainReasonCode='MR001', mainReasonCodeDescription='Original description'). | De beheerder bewerkt de FeedbackMapping met id 'FM1' via PUT /api/feedback-mappings/FM1 met de volgende data: mainReasonCodeDescription='Updated description', subReasonCode='SR005', subReasonCodeDescription='Updated sub description'. | De API retourneert HTTP 200 OK met een FeedbackMappingResponse body die de bijgewerkte mapping bevat met id 'FM1', serviceNotificationId='SN123', businessUnitCode='BU456', plannerFeedbackType='DELIVERY', mainReasonCode='MR001', mainReasonCodeDescription='Updated description', subReasonCode='SR005', subReasonCodeDescription='Updated sub description', en active=true. | integration |
| AC-006-2 | REQ-006 | Een ingelogde beheerder met ROLE_ADMIN en een bestaande FeedbackMapping met id 'FM1' (serviceNotificationId='SN123', businessUnitCode='BU456', plannerFeedbackType='DELIVERY'). | De beheerder probeert de FeedbackMapping met id 'FM1' te bewerken via PUT /api/feedback-mappings/FM1 door de 'serviceNotificationId' te wijzigen naar 'SN456'. | De API retourneert HTTP 400 Bad Request met een ApiError body die aangeeft dat de sleutelcombinatie niet gewijzigd kan worden. | integration |
| AC-006-3 | REQ-006 | Een ingelogde beheerder met ROLE_ADMIN en een bestaande FeedbackMapping met id 'FM1' (serviceNotificationId='SN123', businessUnitCode='BU456', plannerFeedbackType='DELIVERY'). | De beheerder probeert de FeedbackMapping met id 'FM1' te bewerken via PUT /api/feedback-mappings/FM1 door de 'businessUnitCode' te wijzigen naar 'BU789'. | De API retourneert HTTP 400 Bad Request met een ApiError body die aangeeft dat de sleutelcombinatie niet gewijzigd kan worden. | integration |
| AC-006-4 | REQ-006 | Een ingelogde beheerder met ROLE_ADMIN en een bestaande FeedbackMapping met id 'FM1' (serviceNotificationId='SN123', businessUnitCode='BU456', plannerFeedbackType='DELIVERY'). | De beheerder probeert de FeedbackMapping met id 'FM1' te bewerken via PUT /api/feedback-mappings/FM1 door de 'plannerFeedbackType' te wijzigen naar 'PICKUP'. | De API retourneert HTTP 400 Bad Request met een ApiError body die aangeeft dat de sleutelcombinatie niet gewijzigd kan worden. | integration |
| AC-006-5 | REQ-006 | Een ingelogde beheerder met ROLE_ADMIN en een bestaande FeedbackMapping met id 'FM1'. | De beheerder probeert de FeedbackMapping met id 'FM1' te bewerken via PUT /api/feedback-mappings/FM1 met een ongeldige 'plannerFeedbackType' waarde 'INVALID_TYPE'. | De API retourneert HTTP 400 Bad Request met een ApiError body die een veldspecifieke foutmelding bevat voor 'plannerFeedbackType'. | integration |
| AC-007-1 | REQ-007 | Een ingelogde beheerder met ROLE_ADMIN en een actieve FeedbackMapping met id 'FM1'. | De beheerder deactiveert de FeedbackMapping met id 'FM1' via DELETE /api/feedback-mappings/FM1 (aannemende dat DELETE soft delete implementeert). | De API retourneert HTTP 204 No Content. De FeedbackMapping met id 'FM1' heeft nu active=false. Bij het opvragen van de lijst met GET /api/feedback-mappings (standaard filter op active=true) is de mapping niet zichtbaar. | integration |
| AC-007-2 | REQ-007 | Een ingelogde beheerder met ROLE_ADMIN en een gedeactiveerde FeedbackMapping met id 'FM1'. | De beheerder vraagt de lijst van FeedbackMappings op met GET /api/feedback-mappings?active=false. | De FeedbackMapping met id 'FM1' is zichtbaar in de response. | integration |
| AC-007-3 | REQ-007 | Een ingelogde beheerder met ROLE_ADMIN en een actieve FeedbackMapping met id 'FM1'. | De beheerder probeert de FeedbackMapping met id 'FM1' te deactiveren via DELETE /api/feedback-mappings/FM1, maar de mapping bestaat niet. | De API retourneert HTTP 404 Not Found met een ApiError body. | integration |
| AC-008-1 | REQ-008 | Een ingelogde beheerder met ROLE_ADMIN en minimaal 15 FeedbackMappings, waarvan er 7 met serviceNotificationId='SN123', 5 met businessUnitCode='BU456', en 3 met plannerFeedbackType='DELIVERY'. | De beheerder vraagt de lijst van FeedbackMappings op met GET /api/feedback-mappings?serviceNotificationId=SN123&businessUnitCode=BU456&plannerFeedbackType=DELIVERY&page=1&size=10. | De API retourneert HTTP 200 OK met een FeedbackMappingListResponse body die maximaal 10 FeedbackMappings bevat, gefilterd op de opgegeven criteria. De response bevat ook informatie over het totaal aantal resultaten en het aantal pagina's. | integration |
| AC-008-2 | REQ-008 | Een ingelogde beheerder met ROLE_ADMIN en minimaal 15 FeedbackMappings. | De beheerder vraagt de lijst van FeedbackMappings op met GET /api/feedback-mappings?page=2&size=10. | De API retourneert HTTP 200 OK met een FeedbackMappingListResponse body die de volgende 10 FeedbackMappings bevat (resultaten 11-20). | integration |
| AC-008-3 | REQ-008 | Een ingelogde beheerder met ROLE_ADMIN en minimaal 15 FeedbackMappings. | De beheerder vraagt de lijst van FeedbackMappings op met GET /api/feedback-mappings?page=3&size=10. | De API retourneert HTTP 200 OK met een FeedbackMappingListResponse body die de resterende FeedbackMappings bevat (resultaten 21-30, of minder indien er minder dan 30 zijn). | integration |
| AC-008-4 | REQ-008 | Een ingelogde beheerder met ROLE_ADMIN. | De beheerder vraagt de lijst van FeedbackMappings op met GET /api/feedback-mappings?page=1&size=5. | De API retourneert HTTP 200 OK met een FeedbackMappingListResponse body die maximaal 5 FeedbackMappings bevat. | integration |
| AC-008-5 | REQ-008 | Een ingelogde beheerder met ROLE_ADMIN. | De beheerder vraagt de lijst van FeedbackMappings op met GET /api/feedback-mappings?page=1&size=100. | De API retourneert HTTP 200 OK met een FeedbackMappingListResponse body die maximaal 100 FeedbackMappings bevat. | integration |
| AC-008-6 | REQ-008 | Een ingelogde beheerder met ROLE_ADMIN. | De beheerder vraagt de lijst van FeedbackMappings op met GET /api/feedback-mappings?page=0&size=10. | De API retourneert HTTP 400 Bad Request met een ApiError body die een veldspecifieke foutmelding bevat voor 'page' (pagina mag niet 0 zijn). | integration |
| AC-008-7 | REQ-008 | Een ingelogde beheerder met ROLE_ADMIN. | De beheerder vraagt de lijst van FeedbackMappings op met GET /api/feedback-mappings?page=1&size=0. | De API retourneert HTTP 400 Bad Request met een ApiError body die een veldspecifieke foutmelding bevat voor 'size' (aantal per pagina mag niet 0 zijn). | integration |
| AC-009-1 | REQ-009 | Een ingelogde beheerder met ROLE_ADMIN en een BusinessUnit met code 'BU456'. | De beheerder probeert een nieuwe FeedbackMapping aan te maken via POST /api/feedback-mappings met serviceNotificationId='SN_NON_EXISTENT' en businessUnitCode='BU456'. | De API retourneert HTTP 404 Not Found met een ApiError body die aangeeft dat de ServiceNotification niet bestaat. | integration |
| AC-009-2 | REQ-009 | Een ingelogde beheerder met ROLE_ADMIN en een BusinessUnit met code 'BU456'. | De beheerder probeert een nieuwe FeedbackMapping aan te maken via POST /api/feedback-mappings met serviceNotificationId='SN_INACTIVE' (een ServiceNotification die niet actief is) en businessUnitCode='BU456'. | De API retourneert HTTP 400 Bad Request met een ApiError body die aangeeft dat de opgegeven ServiceNotification niet actief is. | integration |
| AC-009-3 | REQ-009 | Een ingelogde beheerder met ROLE_ADMIN en een actieve ServiceNotification met id 'SN123' en een BusinessUnit met code 'BU456'. | De beheerder probeert een nieuwe FeedbackMapping aan te maken via POST /api/feedback-mappings met serviceNotificationId='SN123' en businessUnitCode='BU456'. | De API accepteert de aanvraag en retourneert HTTP 201 Created. | integration |
| AC-010-1 | REQ-010 | Een ingelogde beheerder met ROLE_ADMIN en een actieve ServiceNotification met id 'SN123'. | De beheerder probeert een nieuwe FeedbackMapping aan te maken via POST /api/feedback-mappings met serviceNotificationId='SN123' en businessUnitCode='BU_NON_EXISTENT'. | De API retourneert HTTP 404 Not Found met een ApiError body die aangeeft dat de BusinessUnit niet bestaat. | integration |
| AC-010-2 | REQ-010 | Een ingelogde beheerder met ROLE_ADMIN en een actieve ServiceNotification met id 'SN123'. | De beheerder probeert een nieuwe FeedbackMapping aan te maken via POST /api/feedback-mappings met serviceNotificationId='SN123' en businessUnitCode='BU_INACTIVE' (een BusinessUnit die niet actief is). | De API retourneert HTTP 400 Bad Request met een ApiError body die aangeeft dat de opgegeven BusinessUnit niet actief is. | integration |
| AC-010-3 | REQ-010 | Een ingelogde beheerder met ROLE_ADMIN en een actieve ServiceNotification met id 'SN123' en een actieve BusinessUnit met code 'BU456'. | De beheerder probeert een nieuwe FeedbackMapping aan te maken via POST /api/feedback-mappings met serviceNotificationId='SN123' en businessUnitCode='BU456'. | De API accepteert de aanvraag en retourneert HTTP 201 Created. | integration |
| AC-011-1 | REQ-011 | Er zijn actieve FeedbackMappings (active=true) en gedeactiveerde FeedbackMappings (active=false) in het systeem. | Een proces dat feedback verwerkt, vraagt de beschikbare FeedbackMappings op. | Alleen de FeedbackMappings met active=true worden doorgegeven aan het feedbackverwerkingsproces. | integration |
| AC-011-2 | REQ-011 | Een gedeactiveerde FeedbackMapping met id 'FM1' bestaat in het systeem. | De gedeactiveerde FeedbackMapping met id 'FM1' wordt opgevraagd via GET /api/feedback-mappings/FM1. | De mapping wordt succesvol opgehaald en de 'active' vlag is false. | integration |
| AC-012-1 | REQ-012 | Een ingelogde beheerder met ROLE_ADMIN en een gedeactiveerde FeedbackMapping met id 'FM1' voor (serviceNotificationId='SN123', businessUnitCode='BU456', plannerFeedbackType='DELIVERY'). | De beheerder bewerkt de FeedbackMapping met id 'FM1' via PUT /api/feedback-mappings/FM1 om deze opnieuw te activeren (active=true) met dezelfde sleutelcombinatie. | De API retourneert HTTP 200 OK met de bijgewerkte mapping (active=true). | integration |
| AC-012-2 | REQ-012 | Een ingelogde beheerder met ROLE_ADMIN, een gedeactiveerde FeedbackMapping met id 'FM1' voor (serviceNotificationId='SN123', businessUnitCode='BU456', plannerFeedbackType='DELIVERY') en een actieve FeedbackMapping met id 'FM2' voor dezelfde combinatie. | De beheerder probeert de gedeactiveerde FeedbackMapping met id 'FM1' via PUT /api/feedback-mappings/FM1 opnieuw te activeren (active=true). | De API retourneert HTTP 409 Conflict met een ApiError body die aangeeft dat de combinatie al bestaat en actief is. | integration |
| AC-013-1 | REQ-013 | Een ingelogde beheerder met ROLE_ADMIN en een geldige ServiceNotification met id 'SN123' en actieve BusinessUnit met code 'BU456'. | De beheerder probeert een nieuwe FeedbackMapping aan te maken via POST /api/feedback-mappings met mainReasonCode='MR001' en mainReasonCodeDescription='Valid description'. | De API accepteert de aanvraag en retourneert HTTP 201 Created. | integration |
| AC-013-2 | REQ-013 | Een ingelogde beheerder met ROLE_ADMIN en een geldige ServiceNotification met id 'SN123' en actieve BusinessUnit met code 'BU456'. | De beheerder probeert een nieuwe FeedbackMapping aan te maken via POST /api/feedback-mappings met mainReasonCode='MR001' maar zonder mainReasonCodeDescription. | De API retourneert HTTP 400 Bad Request met een ApiError body die een veldspecifieke foutmelding bevat voor 'mainReasonCodeDescription'. | integration |
| AC-013-3 | REQ-013 | Een ingelogde beheerder met ROLE_ADMIN en een geldige ServiceNotification met id 'SN123' en actieve BusinessUnit met code 'BU456'. | De beheerder probeert een nieuwe FeedbackMapping aan te maken via POST /api/feedback-mappings met mainReasonCode='MR001' en mainReasonCodeDescription=''. | De API retourneert HTTP 400 Bad Request met een ApiError body die een veldspecifieke foutmelding bevat voor 'mainReasonCodeDescription'. | integration |
| AC-013-4 | REQ-013 | Een ingelogde beheerder met ROLE_ADMIN en een geldige ServiceNotification met id 'SN123' en actieve BusinessUnit met code 'BU456'. | De beheerder probeert een nieuwe FeedbackMapping aan te maken via POST /api/feedback-mappings met mainReasonCode='MR001' en mainReasonCodeDescription met 255 tekens. | De API accepteert de aanvraag en retourneert HTTP 201 Created. | integration |
| AC-013-5 | REQ-013 | Een ingelogde beheerder met ROLE_ADMIN en een geldige ServiceNotification met id 'SN123' en actieve BusinessUnit met code 'BU456'. | De beheerder probeert een nieuwe FeedbackMapping aan te maken via POST /api/feedback-mappings met mainReasonCode='MR001' en mainReasonCodeDescription met 256 tekens. | De API retourneert HTTP 400 Bad Request met een ApiError body die een veldspecifieke foutmelding bevat voor 'mainReasonCodeDescription' (lengte). | integration |
| AC-014-1 | REQ-014 | Een ingelogde beheerder met ROLE_ADMIN en een geldige ServiceNotification met id 'SN123' en actieve BusinessUnit met code 'BU456'. | De beheerder probeert een nieuwe FeedbackMapping aan te maken via POST /api/feedback-mappings met subReasonCode='SR001' en subReasonCodeDescription='Valid sub description'. | De API accepteert de aanvraag en retourneert HTTP 201 Created. | integration |
| AC-014-2 | REQ-014 | Een ingelogde beheerder met ROLE_ADMIN en een geldige ServiceNotification met id 'SN123' en actieve BusinessUnit met code 'BU456'. | De beheerder probeert een nieuwe FeedbackMapping aan te maken via POST /api/feedback-mappings met subReasonCode='SR002' maar zonder subReasonCodeDescription. | De API retourneert HTTP 400 Bad Request met een ApiError body die een veldspecifieke foutmelding bevat voor 'subReasonCodeDescription'. | integration |
| AC-014-3 | REQ-014 | Een ingelogde beheerder met ROLE_ADMIN en een geldige ServiceNotification met id 'SN123' en actieve BusinessUnit met code 'BU456'. | De beheerder probeert een nieuwe FeedbackMapping aan te maken via POST /api/feedback-mappings met subReasonCode='SR003' en subReasonCodeDescription=''. | De API retourneert HTTP 400 Bad Request met een ApiError body die een veldspecifieke foutmelding bevat voor 'subReasonCodeDescription'. | integration |
| AC-014-4 | REQ-014 | Een ingelogde beheerder met ROLE_ADMIN en een geldige ServiceNotification met id 'SN123' en actieve BusinessUnit met code 'BU456'. | De beheerder probeert een nieuwe FeedbackMapping aan te maken via POST /api/feedback-mappings met subReasonCode='SR004' en subReasonCodeDescription met 255 tekens. | De API accepteert de aanvraag en retourneert HTTP 201 Created. | integration |
| AC-014-5 | REQ-014 | Een ingelogde beheerder met ROLE_ADMIN en een geldige ServiceNotification met id 'SN123' en actieve BusinessUnit met code 'BU456'. | De beheerder probeert een nieuwe FeedbackMapping aan te maken via POST /api/feedback-mappings met subReasonCode='SR005' en subReasonCodeDescription met 256 tekens. | De API retourneert HTTP 400 Bad Request met een ApiError body die een veldspecifieke foutmelding bevat voor 'subReasonCodeDescription' (lengte). | integration |
| AC-015-1 | REQ-015 | Een ingelogde beheerder met ROLE_ADMIN en een dataset van 1000 FeedbackMappings. | De beheerder vraagt de lijst van FeedbackMappings op met GET /api/feedback-mappings?page=1&size=10. | De response tijd van de API is p95 < 300ms. | integration |
| AC-015-2 | REQ-015 | Een ingelogde beheerder met ROLE_ADMIN en een dataset van 1000 FeedbackMappings met specifieke filters toegepast. | De beheerder vraagt de lijst van FeedbackMappings op met GET /api/feedback-mappings?serviceNotificationId=SN123&businessUnitCode=BU456&page=1&size=10. | De response tijd van de API is p95 < 300ms. | integration |
| AC-016-1 | REQ-016 | Een ingelogde beheerder met ROLE_ADMIN en een geldige correlationId 'corr-123'. | De beheerder maakt een nieuwe FeedbackMapping aan via POST /api/feedback-mappings met alle verplichte velden correct ingevuld. | De schrijfoperatie wordt gelogd met de correlationId 'corr-123' en de id van de ingelogde beheerder. | integration |
| AC-016-2 | REQ-016 | Een ingelogde beheerder met ROLE_ADMIN en een geldige correlationId 'corr-456'. | De beheerder bewerkt een bestaande FeedbackMapping via PUT /api/feedback-mappings/{id}. | De schrijfoperatie wordt gelogd met de correlationId 'corr-456' en de id van de ingelogde beheerder. | integration |
| AC-016-3 | REQ-016 | Een ingelogde beheerder met ROLE_ADMIN en een geldige correlationId 'corr-789'. | De beheerder deactiveert een FeedbackMapping via DELETE /api/feedback-mappings/{id}. | De schrijfoperatie wordt gelogd met de correlationId 'corr-789' en de id van de ingelogde beheerder. | integration |
| AC-017-1 | REQ-017 | Een gebruiker met de rol ROLE_USER (niet ROLE_ADMIN). | De gebruiker probeert een nieuwe FeedbackMapping aan te maken via POST /api/feedback-mappings. | De API retourneert HTTP 403 Forbidden. | integration |
| AC-017-2 | REQ-017 | Een gebruiker met de rol ROLE_USER (niet ROLE_ADMIN). | De gebruiker probeert een bestaande FeedbackMapping te bewerken via PUT /api/feedback-mappings/{id}. | De API retourneert HTTP 403 Forbidden. | integration |
| AC-017-3 | REQ-017 | Een gebruiker met de rol ROLE_USER (niet ROLE_ADMIN). | De gebruiker probeert een FeedbackMapping te deactiveren via DELETE /api/feedback-mappings/{id}. | De API retourneert HTTP 403 Forbidden. | integration |
| AC-017-4 | REQ-017 | Een gebruiker met de rol ROLE_USER (niet ROLE_ADMIN). | De gebruiker probeert de lijst van FeedbackMappings op te vragen met GET /api/feedback-mappings. | De API retourneert HTTP 200 OK (lezen is toegestaan voor alle ingelogde gebruikers). | integration |
| AC-017-5 | REQ-017 | Een ingelogde beheerder met ROLE_ADMIN. | De beheerder probeert een nieuwe FeedbackMapping aan te maken via POST /api/feedback-mappings. | De API accepteert de aanvraag en retourneert HTTP 201 Created. | integration |


## 13. Traceability Matrix

| REQ | Backend | Frontend | Tests |
|-----|---------|----------|-------|
| REQ-001 | FeedbackMappingController, FeedbackMappingService, FeedbackMappingRepository, CreateFeedbackMappingRequest | FeedbackMappingForm, ServiceNotificationSelect, BusinessUnitSelect, ActivityTypeSelect, ReasonCodeFields | Test the creation of a new FeedbackMapping with all specified fields.; Verify that a new FeedbackMapping is persisted in the database. |
| REQ-002 | FeedbackMappingController, FeedbackMappingService, FeedbackMappingCreateValidator, PlannerFeedbackTypeValidator | ActivityTypeSelect | Test creating a FeedbackMapping with a valid plannerFeedbackType.; Test creating a FeedbackMapping with an invalid plannerFeedbackType and verify error.; Test creating a FeedbackMapping without a plannerFeedbackType and verify error. |
| REQ-003 | FeedbackMappingController, FeedbackMappingService, FeedbackMappingCreateValidator | ReasonCodeFields | Test creating a FeedbackMapping with a mainReasonCode of exactly 50 characters.; Test creating a FeedbackMapping with a mainReasonCode longer than 50 characters and verify error.; Test creating a FeedbackMapping without a mainReasonCode and verify error. |
| REQ-004 | FeedbackMappingController, FeedbackMappingService, FeedbackMappingCreateValidator | ReasonCodeFields | Test creating a FeedbackMapping with subReasonCode and subReasonCodeDescription.; Test creating a FeedbackMapping with only subReasonCode and verify error.; Test creating a FeedbackMapping with only subReasonCodeDescription and verify it's ignored.; Test creating a FeedbackMapping with a subReasonCode of exactly 50 characters.; Test creating a FeedbackMapping with a subReasonCode longer than 50 characters and verify error. |
| REQ-005 | FeedbackMappingController, FeedbackMappingService, FeedbackMappingRepository, FeedbackMappingExistsService, FeedbackMappingConflictException |  | Test creating a FeedbackMapping with a unique key combination.; Test creating a FeedbackMapping with an existing key combination and verify HTTP 409 Conflict.; Test creating a FeedbackMapping with an existing key combination but different case and verify it's treated as duplicate. |
| REQ-006 | FeedbackMappingController, FeedbackMappingService, FeedbackMappingRepository, UpdateFeedbackMappingRequest, FeedbackMappingUpdateValidator | FeedbackMappingForm | Test updating all editable fields of an existing FeedbackMapping.; Test attempting to update the key combination of a FeedbackMapping and verify it's not allowed.; Test updating a FeedbackMapping with valid data.; Test updating a FeedbackMapping with invalid data and verify error. |
| REQ-007 | FeedbackMappingController, FeedbackMappingService, FeedbackMappingRepository | FeedbackMappingTable | Test deactivating an active FeedbackMapping.; Verify that a deactivated FeedbackMapping is no longer considered active.; Verify that deactivated FeedbackMappings are still retrievable for audit. |
| REQ-008 | FeedbackMappingController, FeedbackMappingService, FeedbackMappingRepository, PageableRequest | FeedbackMappingTable, FeedbackMappingFilterBar | Test filtering FeedbackMappings by serviceNotificationId.; Test filtering FeedbackMappings by businessUnitCode.; Test filtering FeedbackMappings by plannerFeedbackType.; Test filtering FeedbackMappings by a combination of fields.; Test paginating through the list of FeedbackMappings (10 per page).; Test requesting a page beyond the available results. |
| REQ-009 | FeedbackMappingController, FeedbackMappingService, FeedbackMappingCreateValidator, ActiveServiceNotificationValidator, ServiceNotificationNotFoundException | ServiceNotificationSelect | Test creating a FeedbackMapping with an existing and active ServiceNotification.; Test creating a FeedbackMapping with a non-existent ServiceNotification and verify error.; Test creating a FeedbackMapping with a deactivated ServiceNotification and verify error. |
| REQ-010 | FeedbackMappingController, FeedbackMappingService, FeedbackMappingCreateValidator, ActiveBusinessUnitValidator, BusinessUnitNotFoundException | BusinessUnitSelect | Test creating a FeedbackMapping with an existing and active BusinessUnit.; Test creating a FeedbackMapping with a non-existent BusinessUnit and verify error.; Test creating a FeedbackMapping with a deactivated BusinessUnit and verify error. |
| REQ-011 | FeedbackMappingService, FeedbackMappingRepository |  | Verify that only active FeedbackMappings are used in feedback processing.; Verify that deactivated FeedbackMappings are not used in feedback processing.; Verify that deactivated FeedbackMappings are retained for audit purposes. |
| REQ-012 | FeedbackMappingController, FeedbackMappingService, FeedbackMappingRepository, FeedbackMappingExistsService, FeedbackMappingConflictException | FeedbackMappingTable | Test reactivating a deactivated FeedbackMapping.; Test reactivating a deactivated FeedbackMapping with a unique key combination.; Test reactivating a deactivated FeedbackMapping with a key combination that already exists (active or inactive) and verify HTTP 409 Conflict. |
| REQ-013 | FeedbackMappingController, FeedbackMappingService, FeedbackMappingCreateValidator, FeedbackMappingUpdateValidator | ReasonCodeFields | Test creating a FeedbackMapping with mainReasonCode and mainReasonCodeDescription.; Test creating a FeedbackMapping with only mainReasonCode and verify error.; Test creating a FeedbackMapping with mainReasonCode and an empty mainReasonCodeDescription and verify error.; Test creating a FeedbackMapping with mainReasonCode of 255 characters.; Test creating a FeedbackMapping with mainReasonCodeDescription longer than 255 characters and verify error. |
| REQ-014 | FeedbackMappingController, FeedbackMappingService, FeedbackMappingCreateValidator, FeedbackMappingUpdateValidator | ReasonCodeFields | Test creating a FeedbackMapping with subReasonCode and subReasonCodeDescription.; Test creating a FeedbackMapping with only subReasonCode and verify error.; Test creating a FeedbackMapping with subReasonCode and an empty subReasonCodeDescription and verify error.; Test creating a FeedbackMapping with subReasonCodeDescription of 255 characters.; Test creating a FeedbackMapping with subReasonCodeDescription longer than 255 characters and verify error. |
| REQ-015 | FeedbackMappingController, FeedbackMappingService | FeedbackMappingTable, FeedbackMappingFilterBar | Measure the response time for fetching the filtered FeedbackMapping list under load.; Ensure p95 response time is below 300ms for the filtered list endpoint. |
| REQ-016 | FeedbackMappingController, FeedbackMappingService, FeedbackMappingRepository, FeedbackMappingLoggingService |  | Verify that create operations are logged with correlationId and userId.; Verify that update operations are logged with correlationId and userId.; Verify that delete/deactivate operations are logged with correlationId and userId. |
| REQ-017 | FeedbackMappingController, SecurityConfig, JwtAuthenticationFilter, CustomUserDetailsService, AuthenticationFacade |  | Test access to all FeedbackMapping endpoints with a user having ROLE_ADMIN.; Test access to all FeedbackMapping endpoints with a user not having ROLE_ADMIN and verify unauthorized access. |

