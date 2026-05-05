# Feature-009: Product Catalog Management

## 1. Scope

### In Scope
*   Aanmaken van nieuwe producten met naam, beschrijving, prijs en initiële voorraad.
*   Bewerken van bestaande productgegevens (naam, beschrijving, prijs).
*   Archiveren van producten (soft delete — product verdwijnt uit catalogus maar blijft in orderhistorie zichtbaar).
*   Voorraad bijwerken via een aparte stock-aanpassing (positief = aanvulling, negatief = correctie).
*   Zoeken en filteren van producten op naam, categorie en beschikbaarheid.
*   Paginering van de productlijst (10 per pagina, gesorteerd op naam).
*   Lage-voorraadwaarschuwing: producten met stock ≤ drempelwaarde worden als "low stock" gemarkeerd.
*   Categorieën beheren: aanmaken, hernoemen, verwijderen (alleen als geen actieve producten gekoppeld).

### Out of Scope
*   Productafbeeldingen uploaden en beheren.
*   Prijshistorie of tijdgebonden kortingen.
*   Bulkimport via CSV of Excel.
*   Integratie met externe ERP- of voorraadsystemen.
*   Klantgerichte productpagina's (dit is uitsluitend een beheerscherm).

## 2. Assumptions

*   Er is een bestaande `Category` entiteit met een `id` van type `UUID` en een `name` veld.
*   De drempelwaarde voor lage voorraad wordt geconfigureerd via een omgevingsvariabele of een configuratiebestand.
*   De initiële voorraad bij het aanmaken van een product is een niet-negatief geheel getal.
*   De stock-aanpassing is een geheel getal (positief voor toevoegen, negatief voor verwijderen).
*   De prijs van een product is een decimaal getal met een geschikte precisie.

## 3. Open Questions

*   Welke specifieke validatieregels moeten worden toegepast op de productnaam, beschrijving en prijs?
*   Hoe wordt de drempelwaarde voor lage voorraad geconfigureerd en beheerd?
*   Moet er een aparte API-endpoint komen voor het beheren van categorieën, of wordt dit geïntegreerd in de product-API?
*   Wat is de verwachte gedrag bij het proberen te verwijderen van een categorie waaraan nog producten gekoppeld zijn?
*   Hoe wordt omgegaan met concurrency-problemen bij het bijwerken van de voorraad?
*   Welke specifieke zoekfilters zijn vereist naast naam, categorie en beschikbaarheid?
*   Moet de "beschikbaarheid" filter onderscheid maken tussen producten die op voorraad zijn en producten die momenteel niet op voorraad zijn maar wel leverbaar?

## 4. Domain Model

### Category

| Veld      | Type              | Constraints                               | Testcases                                                              |
|-----------|-------------------|-------------------------------------------|------------------------------------------------------------------------|
| id        | UUID              | NOT NULL                                  | missing, invalid_value                                                 |
| name      | String            | NOT NULL, maxLength: 100                  | empty, too_long, missing, invalid_value, duplicate_per_day             |
| createdAt | OffsetDateTime    | NOT NULL                                  | missing, invalid_value                                                 |
| updatedAt | OffsetDateTime    | NOT NULL                                  | missing, invalid_value                                                 |

### Product

| Veld                 | Type              | Constraints                               | Testcases                                                              |
|----------------------|-------------------|-------------------------------------------|------------------------------------------------------------------------|
| id                   | UUID              | NOT NULL                                  | missing, invalid_value                                                 |
| name                 | String            | NOT NULL, maxLength: 200                  | empty, too_long, missing, invalid_value, duplicate_per_day             |
| description          | String            |                                           | empty, too_long, invalid_value                                         |
| price                | BigDecimal        | NOT NULL, min: 0.01, max: 99999.99        | empty, missing, invalid_value, too_short, too_long                     |
| stockQuantity        | Integer           | NOT NULL, min: 0, max: 999999             | empty, missing, invalid_value, too_short, too_long                     |
| lowStockThreshold    | Integer           | min: 0                                    | empty, invalid_value, too_short, too_long                              |
| archived             | Boolean           | NOT NULL                                  | missing, invalid_value                                                 |
| category             | Category          | NOT NULL                                  | missing, invalid_value                                                 |
| createdAt            | OffsetDateTime    | NOT NULL                                  | missing, invalid_value                                                 |
| updatedAt            | OffsetDateTime    | NOT NULL                                  | missing, invalid_value                                                 |
| createdByUserId      | UUID              | NOT NULL                                  | missing, invalid_value                                                 |
| lastModifiedByUserId | UUID              | NOT NULL                                  | missing, invalid_value                                                 |

### StockAdjustment

| Veld                 | Type              | Constraints                               | Testcases                                                              |
|----------------------|-------------------|-------------------------------------------|------------------------------------------------------------------------|
| id                   | UUID              | NOT NULL                                  | missing, invalid_value                                                 |
| product              | Product           | NOT NULL                                  | missing, invalid_value                                                 |
| delta                | Integer           | NOT NULL                                  | empty, missing, invalid_value                                          |
| reason               | String            | NOT NULL                                  | empty, missing, invalid_value                                          |
| adjustedByUserId     | UUID              | NOT NULL                                  | missing, invalid_value                                                 |
| adjustmentTimestamp  | OffsetDateTime    | NOT NULL                                  | missing, invalid_value                                                 |
| correlationId        | UUID              | NOT NULL                                  | missing, invalid_value                                                 |

### AuditLog

| Veld          | Type              | Constraints                               | Testcases                                                              |
|---------------|-------------------|-------------------------------------------|------------------------------------------------------------------------|
| id            | UUID              | NOT NULL                                  | missing, invalid_value                                                 |
| operation     | String            | NOT NULL                                  | empty, missing, invalid_value                                          |
| entityType    | String            | NOT NULL                                  | empty, missing, invalid_value                                          |
| entityId      | UUID              | NOT NULL                                  | missing, invalid_value                                                 |
| userId        | UUID              | NOT NULL                                  | missing, invalid_value                                                 |
| logTimestamp  | OffsetDateTime    | NOT NULL                                  | missing, invalid_value                                                 |
| correlationId | UUID              | NOT NULL                                  | missing, invalid_value                                                 |
| details       | String            |                                           | empty, too_long, invalid_value                                         |

### Enums

Er zijn geen enum types gedefinieerd voor dit domein.

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

#### 5.2.1 POST /api/products — Maak een nieuw product aan

| Veld          | Waarde              |
|---------------|---------------------|
| Method        | POST                |
| Path          | /api/products       |
| Auth          | bearer              |
| Request DTO   | CreateProductRequest|

| Status | Body             | Omschrijving                     |
|--------|------------------|----------------------------------|
| 201    | ProductResponse  | Product succesvol aangemaakt     |
| 400    | ApiError         | Validatiefout in request body  |
| 409    | ApiError         | Productnaam niet uniek           |
| 500    | ApiError         | Onverwachte serverfout           |

**Validatieregels:**
*   `name`: verplicht, maximaal 200 tekens, uniek voor niet-gearchiveerde producten
*   `description`: optioneel, maximaal 200 tekens
*   `price`: verplicht, minimaal 0.01, maximaal 99999.99, twee decimalen
*   `stockQuantity`: verplicht, minimaal 0, maximaal 999999
*   `categoryId`: optioneel, moet verwijzen naar een bestaande categorie
*   `lowStockThreshold`: optioneel, minimaal 0

#### 5.2.2 GET /api/products — Haal een gepagineerde lijst met producten op

| Veld          | Waarde              |
|---------------|---------------------|
| Method        | GET                 |
| Path          | /api/products       |
| Auth          | bearer              |
| Request DTO   | N.v.t.              |

| Status | Body                | Omschrijving           |
|--------|---------------------|------------------------|
| 200    | ProductListResponse | Lijst met producten   |
| 500    | ApiError            | Onverwachte serverfout |

**Query Parameters:**
*   `name`: Filter op productnaam (bevat)
*   `categoryId`: Filter op categorie ID
*   `availability`: Filter op beschikbaarheid (active, archived, low_stock)
*   `page`: Paginanummer (standaard 0)
*   `size`: Aantal items per pagina (standaard 10)
*   `sort`: Sortering (bv. name,asc of name,desc)

#### 5.2.3 GET /api/products/{id} — Haal de details van een specifiek product op

| Veld          | Waarde              |
|---------------|---------------------|
| Method        | GET                 |
| Path          | /api/products/{id}  |
| Auth          | bearer              |
| Request DTO   | N.v.t.              |

| Status | Body             | Omschrijving         |
|--------|------------------|----------------------|
| 200    | ProductResponse  | Productdetails       |
| 404    | ApiError         | Product niet gevonden|
| 500    | ApiError         | Onverwachte serverfout|

#### 5.2.4 PUT /api/products/{id} — Bewerk een bestaand product

| Veld          | Waarde              |
|---------------|---------------------|
| Method        | PUT                 |
| Path          | /api/products/{id}  |
| Auth          | bearer              |
| Request DTO   | UpdateProductRequest|

| Status | Body             | Omschrijving                     |
|--------|------------------|----------------------------------|
| 200    | ProductResponse  | Product succesvol bijgewerkt     |
| 400    | ApiError         | Validatiefout in request body  |
| 404    | ApiError         | Product niet gevonden            |
| 409    | ApiError         | Productnaam niet uniek           |
| 500    | ApiError         | Onverwachte serverfout           |

**Validatieregels:**
*   `name`: verplicht, maximaal 200 tekens, uniek voor niet-gearchiveerde producten
*   `description`: optioneel, maximaal 200 tekens
*   `price`: verplicht, minimaal 0.01, maximaal 99999.99, twee decimalen
*   `categoryId`: optioneel, moet verwijzen naar een bestaande categorie
*   `lowStockThreshold`: optioneel, minimaal 0

#### 5.2.5 DELETE /api/products/{id} — Archiveer een product (soft delete)

| Veld          | Waarde              |
|---------------|---------------------|
| Method        | DELETE              |
| Path          | /api/products/{id}  |
| Auth          | bearer              |
| Request DTO   | N.v.t.              |

| Status | Body             | Omschrijving                                                                                                                            |
|--------|------------------|-----------------------------------------------------------------------------------------------------------------------------------------|
| 204    | N.v.t.           | Product succesvol gearchiveerd                                                                                                          |
| 404    | ApiError         | Product niet gevonden                                                                                                                   |
| 409    | ApiError         | Product kan niet worden gearchiveerd omdat het nog gekoppeld is aan actieve orders (aanname, FA specificeert dit niet expliciet) |
| 500    | ApiError         | Onverwachte serverfout                                                                                                                  |

#### 5.2.6 PATCH /api/products/{id}/stock — Pas de voorraad van een product aan

| Veld          | Waarde              |
|---------------|---------------------|
| Method        | PATCH               |
| Path          | /api/products/{id}/stock |
| Auth          | bearer              |
| Request DTO   | StockAdjustmentRequest |

| Status | Body             | Omschrijving                                     |
|--------|------------------|--------------------------------------------------|
| 200    | ProductResponse  | Voorraad succesvol aangepast                     |
| 400    | ApiError         | Validatiefout of negatieve voorraad              |
| 404    | ApiError         | Product niet gevonden                            |
| 500    | ApiError         | Onverwachte serverfout                           |

**Validatieregels:**
*   `delta`: verplicht
*   `reason`: verplicht, moet een van de toegestane redenen zijn (RESTOCK, CORRECTION, DAMAGE, RETURN)

#### 5.2.7 GET /api/products/{id}/stock-adjustments — Haal de auditlog van stock-aanpassingen voor een product op

| Veld          | Waarde              |
|---------------|---------------------|
| Method        | GET                 |
| Path          | /api/products/{id}/stock-adjustments |
| Auth          | bearer              |
| Request DTO   | N.v.t.              |

| Status | Body                      | Omschrijving                 |
|--------|---------------------------|------------------------------|
| 200    | StockAdjustmentListResponse | Lijst met stock-aanpassingen |
| 404    | ApiError                  | Product niet gevonden        |
| 500    | ApiError                  | Onverwachte serverfout       |

#### 5.2.8 POST /api/categories — Maak een nieuwe categorie aan

| Veld          | Waarde              |
|---------------|---------------------|
| Method        | POST                |
| Path          | /api/categories     |
| Auth          | bearer              |
| Request DTO   | CreateCategoryRequest|

| Status | Body             | Omschrijving                     |
|--------|------------------|----------------------------------|
| 201    | CategoryResponse | Categorie succesvol aangemaakt   |
| 400    | ApiError         | Validatiefout in request body  |
| 409    | ApiError         | Categorie naam is al in gebruik  |
| 500    | ApiError         | Onverwachte serverfout           |

**Validatieregels:**
*   `name`: verplicht, maximaal 100 tekens, uniek

#### 5.2.9 GET /api/categories — Haal alle categorieën op

| Veld          | Waarde              |
|---------------|---------------------|
| Method        | GET                 |
| Path          | /api/categories     |
| Auth          | bearer              |
| Request DTO   | N.v.t.              |

| Status | Body                | Omschrijving           |
|--------|---------------------|------------------------|
| 200    | CategoryListResponse| Lijst met categorieën  |
| 500    | ApiError            | Onverwachte serverfout |

#### 5.2.10 PUT /api/categories/{id} — Hernoem een categorie

| Veld          | Waarde              |
|---------------|---------------------|
| Method        | PUT                 |
| Path          | /api/categories/{id}|
| Auth          | bearer              |
| Request DTO   | UpdateCategoryRequest|

| Status | Body             | Omschrijving                     |
|--------|------------------|----------------------------------|
| 200    | CategoryResponse | Categorie succesvol hernoemd     |
| 400    | ApiError         | Validatiefout in request body  |
| 404    | ApiError         | Categorie niet gevonden          |
| 409    | ApiError         | Nieuwe categorie naam is al in gebruik |
| 500    | ApiError         | Onverwachte serverfout           |

**Validatieregels:**
*   `name`: verplicht, maximaal 100 tekens, uniek

#### 5.2.11 DELETE /api/categories/{id} — Verwijder een categorie

| Veld          | Waarde              |
|---------------|---------------------|
| Method        | DELETE              |
| Path          | /api/categories/{id}|
| Auth          | bearer              |
| Request DTO   | N.v.t.              |

| Status | Body             | Omschrijving                                                              |
|--------|------------------|---------------------------------------------------------------------------|
| 204    | N.v.t.           | Categorie succesvol verwijderd                                             |
| 404    | ApiError         | Categorie niet gevonden                                                   |
| 409    | ApiError         | Categorie kan niet worden verwijderd omdat er nog actieve producten aan gekoppeld zijn |
| 500    | ApiError         | Onverwachte serverfout                                                    |

## 6. Backend Design

De backend volgt een gelaagde architectuur: Controller → Service → Repository. Controllers zijn verantwoordelijk voor het afhandelen van HTTP-verzoeken en delegeren de businesslogica naar de Service-laag. De Service-laag bevat de kern businesslogica en beheert transacties. Repositories, geïmplementeerd met Spring Data JPA, zijn verantwoordelijk voor de interactie met de database. Domain modellen zijn pure Java-objecten zonder Spring-annotaties. DTO's worden geïmplementeerd als Java records en de mapping tussen entiteiten en DTO's wordt afgehandeld door MapStruct.

### Product Module

| Klasse                         | Verantwoordelijkheid                                                              |
| :----------------------------- | :-------------------------------------------------------------------------------- |
| `ProductController`            | Exposeert REST endpoints voor productbeheer.                                      |
| `ProductService`               | Orchestreert de businesslogica voor productoperaties.                             |
| `ProductRepository`            | Verantwoordelijk voor datatoegang tot productgegevens.                            |
| `ProductEntity`                | Representeert de productentiteit in de database.                                  |
| `CreateProductRequestDto`      | Data Transfer Object voor het aanmaken van een product.                          |
| `UpdateProductRequestDto`      | Data Transfer Object voor het bijwerken van een product.                         |
| `ProductResponseDto`           | Data Transfer Object voor het retourneren van productgegevens.                    |
| `ProductListResponseDto`       | Data Transfer Object voor het retourneren van een lijst met producten.            |
| `ProductCreateValidator`       | Valideert de input voor het aanmaken van een product.                            |
| `ProductUpdateValidator`       | Valideert de input voor het bijwerken van een product.                           |
| `ProductNameUniquenessValidator` | Controleert de uniekheid van productnamen voor niet-gearchiveerde producten.     |
| `ProductArchivedStatusValidator` | Valideert de status van een product bij archiveringspogingen.                   |
| `ProductNotFoundException`     | Exception voor wanneer een product niet wordt gevonden.                          |
| `ProductNameConflictException` | Exception voor wanneer een productnaam al in gebruik is.                         |
| `ProductCannotArchiveException`| Exception voor wanneer een product niet gearchiveerd kan worden.                  |

### Stock Module

| Klasse                           | Verantwoordelijkheid                                                              |
| :------------------------------- | :-------------------------------------------------------------------------------- |
| `StockController`                | Exposeert REST endpoints voor voorraadbeheer.                                     |
| `StockService`                   | Orchestreert de businesslogica voor voorraadoperaties.                            |
| `StockAdjustmentRepository`      | Verantwoordelijk voor datatoegang tot stock-aanpassingen.                         |
| `StockAdjustmentEntity`          | Representeert een stock-aanpassing in de database.                                |
| `StockAdjustmentRequestDto`      | Data Transfer Object voor het aanpassen van voorraad.                             |
| `StockAdjustmentListResponseDto` | Data Transfer Object voor het retourneren van een lijst met stock-aanpassingen.    |
| `StockAdjustmentValidator`       | Valideert de input voor stock-aanpassingen.                                       |
| `NegativeStockPreventionValidator` | Voorkomt dat voorraad negatief wordt door stock-aanpassingen.                   |
| `StockAdjustmentReasonEnum`      | Definieert de toegestane redenen voor stock-aanpassingen.                         |

### Category Module

| Klasse                       | Verantwoordelijkheid                                                              |
| :--------------------------- | :-------------------------------------------------------------------------------- |
| `CategoryController`         | Exposeert REST endpoints voor categoribeheer.                                     |
| `CategoryService`            | Orchestreert de businesslogica voor categorieoperaties.                           |
| `CategoryRepository`         | Verantwoordelijk voor datatoegang tot categorigegevens.                           |
| `CategoryEntity`             | Representeert de categorieentiteit in de database.                              |
| `CreateCategoryRequestDto`   | Data Transfer Object voor het aanmaken van een categorie.                         |
| `UpdateCategoryRequestDto`   | Data Transfer Object voor het bijwerken van een categorie.                        |
| `CategoryResponseDto`        | Data Transfer Object voor het retourneren van categorigegevens.                   |
| `CategoryListResponseDto`    | Data Transfer Object voor het retourneren van een lijst met categorieën.          |
| `CategoryNameUniquenessValidator` | Controleert de uniekheid van categorienamen.                                      |
| `CategoryCannotDeleteException`| Exception voor wanneer een categorie niet verwijderd kan worden.                 |
| `CategoryNotFoundException`  | Exception voor wanneer een categorie niet wordt gevonden.                        |
| `CategoryNameConflictException`| Exception voor wanneer een categorienaam al in gebruik is.                       |

### Audit Module

| Klasse               | Verantwoordelijkheid                                      |
| :------------------- | :-------------------------------------------------------- |
| `AuditLogService`    | Verantwoordelijk voor het loggen van audit-gebeurtenissen. |
| `AuditLogRepository` | Verantwoordelijk voor datatoegang tot auditloggegevens.   |
| `AuditLogEntity`     | Representeert een auditlog-entry in de database.          |
| `AuditOperationEnum` | Definieert de mogelijke audit-operaties.                  |
| `AuditEntityTypeEnum`| Definieert de mogelijke entiteitstypes voor auditlogs.    |

### Common Module

| Klasse                   | Verantwoordelijkheid                                                              |
| :----------------------- | :-------------------------------------------------------------------------------- |
| `ApiErrorDto`            | Standaard formaat voor API-foutmeldingen.                                         |
| `GlobalExceptionHandler` | Centrale afhandeling van exceptions voor de API.                                  |
| `SecurityConfig`         | Configureert beveiligingsinstellingen, inclusief authenticatie en autorisatie.    |
| `JwtAuthenticationFilter`| Filter voor het verwerken van JWT-tokens voor authenticatie.                      |
| `PageableRequestDto`     | Data Transfer Object voor paginering- en sorteerparameters.                     |
| `PageableResponseDto`    | Data Transfer Object voor gepagineerde antwoorden.                                |
| `UuidGenerator`          | Utility voor het genereren van UUID's.                                            |
| `BigDecimalConfig`       | Configuratie voor BigDecimal precisie en schaal.                                  |

## 7. Frontend Design

Dit gedeelte beschrijft de frontend architectuur en componenten voor de Product Catalog Management feature, conform de gedefinieerde conventies.

### /admin/products

| Component           | Verantwoordelijkheid                                                                                                                            |
| :------------------ | :---------------------------------------------------------------------------------------------------------------------------------------------- |
| ProductListPage       | Hoofdcomponent voor de productlijstpagina. Bevat de ProductTable, zoek-/filterfunctionaliteit en de toggle voor gearchiveerde producten.        |
| ProductTable        | Toont de lijst met producten in een tabel. Bevat kolommen voor Naam, Categorie, Prijs, Voorraad (met low-stock badge), Status en acties (Bewerken, Archiveren). |
| LowStockBadge       | Visuele indicator (badge) voor producten met lage voorraad.                                                                                     |
| Pagination          | Component voor het navigeren door de productlijst.                                                                                              |
| ConfirmDialog       | Generiek dialoogvenster voor bevestigingsvragen (bv. bij archiveren of verwijderen categorie).                                                    |

### /admin/products/new

| Component           | Verantwoordelijkheid                                                                                                                            |
| :------------------ | :---------------------------------------------------------------------------------------------------------------------------------------------- |
| ProductFormPage     | Pagina voor het aanmaken van een nieuw product. Bevat het ProductForm.                                                                            |
| ProductForm         | Formulier voor het invoeren van productgegevens (naam, beschrijving, prijs, categorie, initiële voorraad, lage-voorraaddrempel). Inclusief inline veldvalidatie. |
| CategoryDropdown    | Dropdown component om een categorie te selecteren.                                                                                              |

### /admin/products/:id/edit

| Component               | Verantwoordelijkheid                                                                                                                            |
| :---------------------- | :---------------------------------------------------------------------------------------------------------------------------------------------- |
| ProductEditPage         | Pagina voor het bewerken van een bestaand product. Bevat het ProductForm en de knop om de voorraad aan te passen.                                 |
| ProductForm             | Formulier voor het bewerken van productgegevens (naam, beschrijving, prijs, categorie, lage-voorraaddrempel). Voorraadveld is niet direct bewerkbaar. |
| StockAdjustmentButton   | Knop die de StockAdjustmentModal opent.                                                                                                         |
| StockAdjustmentModal    | Modal voor het aanpassen van de voorraad (delta, reden). Toont huidige en nieuwe voorraad live.                                                |
| CategoryDropdown        | Dropdown component om een categorie te selecteren.                                                                                              |

### /admin/categories

| Component           | Verantwoordelijkheid                                                                                                                            |
| :------------------ | :---------------------------------------------------------------------------------------------------------------------------------------------- |
| CategoryManagerPage | Pagina voor het beheren van categorieën. Bevat de CategoryManager.                                                                               |
| CategoryManager     | Toont een lijst met categorieën en biedt functionaliteit voor inline bewerken en verwijderen.                                                   |
| ConfirmDialog       | Generiek dialoogvenster voor bevestigingsvragen (bv. bij verwijderen categorie).                                                                 |

## 8. Security & Privacy

Alle endpoints gerelateerd aan productcatalogusbeheer (`/api/products`, `/api/categories`) vereisen de `ROLE_ADMIN` autorisatie. Dit wordt afgedwongen middels Spring Security configuratie, waarbij de `SecurityFilterChain` de endpoints beveiligt en de `AccessDeniedHandler` wordt geconfigureerd om een `403 Forbidden` response te retourneren bij ongeautoriseerde toegang.

**Authenticatie:** Gebruikers authenticeren zich via JWT Bearer tokens. De `JwtAuthenticationFilter` valideert de token en stelt de `Authentication` principal in voor de thread.

**Autorisatie:** De `ROLE_ADMIN` rol wordt gecontroleerd op de controller-laag met behulp van de `@PreAuthorize("hasRole('ROLE_ADMIN')")` annotatie.

**Privacy:**
*   Gevoelige data zoals wachtwoorden of creditcardinformatie wordt niet opgeslagen of verwerkt binnen deze feature.
*   Productnamen, beschrijvingen en prijzen worden als publieke catalogusinformatie beschouwd en mogen worden weergegeven.
*   Audit logs voor stock-aanpassingen bevatten de `beheerder-id`, maar geen persoonlijke identificeerbare informatie van de beheerder anders dan de ID.
*   De `correlationId` wordt meegenomen in alle logboekvermeldingen, inclusief audit logs, om traceerbaarheid te waarborgen zonder privacygevoelige informatie bloot te leggen.

## 9. Observability

### Logging

Logging wordt geïmplementeerd met SLF4J en Logback, met gestructureerde JSON output in productie.

**Backend Logging:**

*   **Controller Layer:** Log inkomende requests en de uitgaande responses. Log validatiefouten met de `correlationId` en de specifieke velden die falen.
    *   **Voorbeeld (Request):**
        ```json
        {
          "timestamp": "2023-10-27T10:00:00.123Z",
          "level": "INFO",
          "thread": "http-nio-8080-exec-1",
          "logger": "com.example.product.controller.ProductController",
          "message": "Received POST request for /api/products",
          "mdc": {
            "correlationId": "a1b2c3d4-e5f6-7890-1234-567890abcdef"
          },
          "request": {
            "method": "POST",
            "uri": "/api/products",
            "headers": { ... }
          }
        }
        ```
    *   **Voorbeeld (Validation Error):**
        ```json
        {
          "timestamp": "2023-10-27T10:00:01.456Z",
          "level": "WARN",
          "thread": "http-nio-8080-exec-1",
          "logger": "com.example.product.controller.ProductController",
          "message": "Validation failed for product creation",
          "mdc": {
            "correlationId": "a1b2c3d4-e5f6-7890-1234-567890abcdef"
          },
          "errorResponse": {
            "status": 400,
            "error": "Validation failed",
            "correlationId": "a1b2c3d4-e5f6-7890-1234-567890abcdef",
            "fields": {
              "name": "Product name cannot be empty",
              "price": "Price must be between 0.01 and 99999.99"
            }
          }
        }
        ```
*   **Service Layer:** Log belangrijke business logic stappen, transactiebegin en -einde, en eventuele fouten die optreden tijdens de verwerking.
    *   **Voorbeeld (Service Logic):**
        ```json
        {
          "timestamp": "2023-10-27T10:00:02.789Z",
          "level": "INFO",
          "thread": "application-executor-1",
          "logger": "com.example.product.service.ProductService",
          "message": "Attempting to create product with name: 'Awesome Gadget'",
          "mdc": {
            "correlationId": "a1b2c3d4-e5f6-7890-1234-567890abcdef"
          }
        }
        ```
*   **Repository Layer:** Log database-operaties (CRUD) indien nodig voor debugging, maar vermijd het loggen van gevoelige data.
*   **Audit Logging:** Stock-aanpassingen worden gelogd in een aparte `audit_log` tabel met `product_id`, `delta`, `reason`, `admin_id`, en `timestamp`. Deze logs zijn niet verwijderbaar.

**Frontend Logging:**
*   Gebruik `console.error` uitsluitend voor fouten.
*   Logging naar de console wordt onderdrukt in productieomgevingen.
*   Fouten die via Axios worden opgevangen, worden gelogd met de `correlationId` indien beschikbaar.

### Correlation ID

*   Een `correlationId` (UUID) wordt gegenereerd door de `CorrelationIdFilter` in de backend voor elke inkomende request.
*   Deze `correlationId` wordt toegevoegd aan de MDC (Mapped Diagnostic Context) van Logback en opgenomen in alle logboekvermeldingen.
*   De `correlationId` wordt teruggegeven in alle foutresponses conform het gespecificeerde formaat.
*   De frontend genereert **geen** `correlationId`. De `correlationId` van de backend wordt meegestuurd in de response headers en kan door de frontend worden gebruikt om te loggen of te tonen aan de gebruiker.

### Metrics

*   Basis metrics zoals request count, response times (gemiddeld en p95) per endpoint worden verzameld.
*   Specifieke metrics voor productcatalogusbeheer:
    *   Aantal aangemaakte producten.
    *   Aantal bewerkte producten.
    *   Aantal gearchiveerde producten.
    *   Aantal stock-aanpassingen (per reden).
    *   Aantal productlijst-verzoeken met filters.

## 10. Performance & Scalability

### Performance Eisen

*   **API Response Time (p95):**
    *   Ophalen productlijst (inclusief filters en paginering): `< 300ms`.
    *   Aanmaken en bewerken van producten: `< 500ms`.
*   **Database:** De productlijst moet minimaal 10.000 actieve producten kunnen ondersteunen zonder merkbare prestatiedegradatie.

### Database Optimalisaties

*   **Product Tabel (`products`):**
    *   `id`: `UUID` (Primaire sleutel).
    *   `name`: `VARCHAR(200)`, `NOT NULL`, `UNIQUE` (voor actieve producten). Index op `name`.
    *   `description`: `TEXT`.
    *   `price`: `DECIMAL(7, 2)`, `NOT NULL`.
    *   `category_id`: `UUID`, `FOREIGN KEY` naar `categories(id)`. Index op `category_id`.
    *   `stock_quantity`: `INTEGER`, `NOT NULL`, `DEFAULT 0`.
    *   `low_stock_threshold`: `INTEGER`, `NULLABLE`.
    *   `is_archived`: `BOOLEAN`, `NOT NULL`, `DEFAULT FALSE`. Index op `is_archived`.
    *   `created_at`, `updated_at`: `TIMESTAMP WITH TIME ZONE`, `NOT NULL`.
    *   `deleted_at`: `TIMESTAMP WITH TIME ZONE`, `NULLABLE` (voor soft delete).
*   **Categorie Tabel (`categories`):**
    *   `id`: `UUID` (Primaire sleutel).
    *   `name`: `VARCHAR(100)`, `NOT NULL`, `UNIQUE`. Index op `name`.
    *   `created_at`, `updated_at`: `TIMESTAMP WITH TIME ZONE`, `NOT NULL`.
    *   `deleted_at`: `TIMESTAMP WITH TIME ZONE`, `NULLABLE`.
*   **Audit Log Tabel (`stock_adjustments_audit`):**
    *   `id`: `UUID` (Primaire sleutel).
    *   `product_id`: `UUID`, `NOT NULL`, `FOREIGN KEY` naar `products(id)`. Index op `product_id`.
    *   `delta`: `INTEGER`, `NOT NULL`.
    *   `reason`: `VARCHAR(50)`, `NOT NULL` (enum: RESTOCK, CORRECTION, DAMAGE, RETURN).
    *   `admin_id`: `UUID`, `NOT NULL`.
    *   `timestamp`: `TIMESTAMP WITH TIME ZONE`, `NOT NULL`.
*   **Sequences Tabel:** Gebruikt voor het genereren van eventuele sequentiële ID's indien nodig, hoewel UUID's de voorkeur hebben voor entiteits-ID's.

**Indexen:**
*   `products`: `name`, `category_id`, `is_archived`, `deleted_at`.
*   `categories`: `name`, `deleted_at`.
*   `stock_adjustments_audit`: `product_id`, `timestamp`.

### Scalability

*   **Database:** PostgreSQL 16 is gekozen voor zijn robuustheid, schaalbaarheid en geavanceerde functies zoals JSONB-ondersteuning (indien nodig voor toekomstige uitbreidingen) en efficiënte indexering. Partitionering kan worden overwogen voor de `stock_adjustments_audit` tabel als deze extreem groot wordt.
*   **Backend:**
    *   Spring Boot 3 met Java 21 biedt uitstekende prestaties en geheugenbeheer.
    *   Asynchrone verwerking kan worden overwogen voor langdurige operaties, hoewel de huidige requirements dit niet direct vereisen.
    *   Stateless architectuur door JWT zorgt voor horizontale schaalbaarheid van de applicatieservers.
*   **Containerisatie:** Docker en Docker Compose maken het eenvoudig om de applicatie te deployen en te schalen in verschillende omgevingen.
*   **Caching:** Caching kan worden geïmplementeerd voor veelgevraagde, statische productdata (bijvoorbeeld categorieën) om de databasebelasting te verminderen en de response tijden te verbeteren. Dit kan worden gedaan met Spring Cache.
*   **API Gateway:** Voor grotere systemen kan een API Gateway worden ingezet voor taken als rate limiting, load balancing en authenticatie, wat de schaalbaarheid verder ten goede komt.

## 11. Test Strategie

De teststrategie voor de Product Catalog Management feature is opgebouwd uit drie lagen: Unit Tests, Integratie Tests en End-to-End (E2E) Tests. Deze gelaagde aanpak zorgt voor een robuuste kwaliteitsborging door de functionaliteit op verschillende niveaus te valideren.

### Unit Tests

Unit tests richten zich op het isoleren en testen van de kleinste testbare eenheden van de applicatie, zoals individuele functies, methoden of componenten. De volgende eenheden zullen worden getest:

*   **Frontend Componenten:**
    *   `ProductListPage.render`: Verifieert de correcte rendering van de productlijstpagina.
    *   `ProductTable.render`: Valideert de weergave van de producttabel.
    *   `LowStockBadge.render`: Test de weergave van de badge voor lage voorraad.
    *   `Pagination.render`: Controleert de functionaliteit en weergave van de paginatiecomponent.
    *   `ConfirmDialog.render`: Verifieert de correcte weergave en werking van het bevestigingsdialoogvenster.
    *   `ProductFormPage.render`: Test de rendering van de productformulierenpagina.
    *   `ProductForm.render`: Valideert de weergave en initiële staat van het productformulier.
    *   `CategoryDropdown.render`: Controleert de correcte weergave en functionaliteit van de categorie-dropdown.
    *   `ProductEditPage.render`: Test de rendering van de productbewerkingspagina.
    *   `StockAdjustmentButton.render`: Verifieert de weergave van de knop voor voorraadcorrectie.
    *   `StockAdjustmentModal.render`: Test de weergave en initiële staat van het modal voor voorraadcorrectie.
    *   `CategoryManagerPage.render`: Valideert de rendering van de categoriebeheerpagina.
    *   `CategoryManager.render`: Controleert de weergave en functionaliteit van de categoriebeheercomponent.

*   **Backend API Client Functies:**
    *   `api/products.createProduct`: Test de correcte aanroep en verwachte respons van de API voor het aanmaken van een product.
    *   `api/products.getProducts`: Valideert de aanroep en respons voor het ophalen van producten.
    *   `api/products.getProductById`: Test de aanroep en respons voor het ophalen van een specifiek product.
    *   `api/products.updateProduct`: Verifieert de aanroep en respons voor het bijwerken van een product.
    *   `api/products.archiveProduct`: Test de aanroep en respons voor het archiveren van een product.
    *   `api/products.adjustStock`: Valideert de aanroep en respons voor het aanpassen van de voorraad.
    *   `api/categories.createCategory`: Test de aanroep en respons voor het aanmaken van een categorie.
    *   `api/categories.getCategories`: Verifieert de aanroep en respons voor het ophalen van categorieën.
    *   `api/categories.updateCategory`: Test de aanroep en respons voor het bijwerken van een categorie.
    *   `api/categories.deleteCategory`: Valideert de aanroep en respons voor het verwijderen van een categorie.

### Integratie Tests

Integratie tests valideren de interactie tussen verschillende componenten of services. Voor deze feature zullen de volgende API-endpoints worden getest om de correcte werking van de backend te waarborgen:

*   `POST /api/products` → `201 Created`: Verifieert dat het aanmaken van een nieuw product succesvol is en de juiste statuscode retourneert.
*   `GET /api/products` → `200 OK`: Test het ophalen van de lijst met producten en controleert op een succesvolle respons.
*   `GET /api/products/{id}` → `200 OK`: Valideert het ophalen van een specifiek product op basis van zijn ID.
*   `PUT /api/products/{id}` → `200 OK`: Test het bijwerken van een bestaand product.
*   `DELETE /api/products/{id}` → `204 No Content`: Verifieert dat het verwijderen van een product correct wordt afgehandeld met de verwachte statuscode.
*   `PATCH /api/products/{id}/stock` → `200 OK`: Test de correcte werking van de API voor het aanpassen van de voorraad van een product.
*   `POST /api/categories` → `201 Created`: Verifieert dat het aanmaken van een nieuwe categorie succesvol is.
*   `GET /api/categories` → `200 OK`: Test het ophalen van de lijst met categorieën.
*   `PUT /api/categories/{id}` → `200 OK`: Valideert het bijwerken van een bestaande categorie.
*   `DELETE /api/categories/{id}` → `204 No Content`: Test het correct verwijderen van een categorie.

### End-to-End (E2E) Tests

E2E tests simuleren realistische gebruikersscenario's door de gehele applicatiestroom te testen, van de frontend tot de backend en de database. De volgende scenario's zullen worden gevalideerd:

*   **Product Management Scenario:**
    *   Als beheerder wil ik een nieuw product kunnen aanmaken, de lijst met producten kunnen bekijken met filters en sortering, een product kunnen bewerken, een product kunnen archiveren en de voorraad van een product kunnen aanpassen.

*   **Category Management Scenario:**
    *   Als beheerder wil ik categorieën kunnen aanmaken, hernoemen en verwijderen (mits er geen producten aan gekoppeld zijn).

## 12. Acceptance Criteria

| AC-ID | REQ | Gegeven | Wanneer | Dan | Testtype |
|-------|-----|---------|---------|-----|----------|
| AC-001-1 | REQ-001 | Een beheerder is ingelogd en bevindt zich op de productbeheerpagina | De beheerder vult de velden 'naam', 'beschrijving', 'prijs', 'categorie' en 'initiële voorraad' in en klikt op 'Product aanmaken' | Het nieuwe product wordt succesvol aangemaakt en is zichtbaar in de productlijst | integration |
| AC-002-1 | REQ-002 | Een beheerder probeert een nieuw product aan te maken | Het veld 'naam' wordt leeg gelaten en de beheerder klikt op 'Product aanmaken' | Er wordt een foutmelding getoond dat de naam verplicht is en het product wordt niet aangemaakt | integration |
| AC-002-2 | REQ-002 | Een beheerder probeert een nieuw product aan te maken met een naam langer dan 200 tekens | De beheerder vult een naam in die 201 tekens lang is en klikt op 'Product aanmaken' | Er wordt een foutmelding getoond dat de naam maximaal 200 tekens lang mag zijn en het product wordt niet aangemaakt | integration |
| AC-002-3 | REQ-002 | Er bestaat al een product met de naam 'Test Product' | Een beheerder probeert een nieuw product aan te maken met de naam 'Test Product' | Er wordt een foutmelding getoond dat de naam al in gebruik is en het product wordt niet aangemaakt | integration |
| AC-003-1 | REQ-003 | Een beheerder probeert een nieuw product aan te maken | Het veld 'prijs' wordt leeg gelaten en de beheerder klikt op 'Product aanmaken' | Er wordt een foutmelding getoond dat de prijs verplicht is en het product wordt niet aangemaakt | integration |
| AC-003-2 | REQ-003 | Een beheerder probeert een nieuw product aan te maken met een prijs van €0,00 | De beheerder vult de prijs in als '0.00' en klikt op 'Product aanmaken' | Er wordt een foutmelding getoond dat de prijs minimaal €0,01 moet zijn en het product wordt niet aangemaakt | integration |
| AC-003-3 | REQ-003 | Een beheerder probeert een nieuw product aan te maken met een prijs van €100.000,00 | De beheerder vult de prijs in als '100000.00' en klikt op 'Product aanmaken' | Er wordt een foutmelding getoond dat de prijs maximaal €99.999,99 mag zijn en het product wordt niet aangemaakt | integration |
| AC-003-4 | REQ-003 | Een beheerder probeert een nieuw product aan te maken met een prijs van €123,456 | De beheerder vult de prijs in als '123.456' en klikt op 'Product aanmaken' | De prijs wordt correct opgeslagen als €123,46 (afgerond op twee decimalen) en het product wordt aangemaakt | integration |
| AC-004-1 | REQ-004 | Een beheerder probeert een nieuw product aan te maken | Het veld 'initiële voorraad' wordt leeg gelaten en de beheerder klikt op 'Product aanmaken' | Er wordt een foutmelding getoond dat de initiële voorraad verplicht is en het product wordt niet aangemaakt | integration |
| AC-004-2 | REQ-004 | Een beheerder probeert een nieuw product aan te maken met een initiële voorraad van -1 | De beheerder vult de initiële voorraad in als '-1' en klikt op 'Product aanmaken' | Er wordt een foutmelding getoond dat de initiële voorraad minimaal 0 moet zijn en het product wordt niet aangemaakt | integration |
| AC-004-3 | REQ-004 | Een beheerder probeert een nieuw product aan te maken met een initiële voorraad van 1.000.000 | De beheerder vult de initiële voorraad in als '1000000' en klikt op 'Product aanmaken' | Er wordt een foutmelding getoond dat de initiële voorraad maximaal 999.999 mag zijn en het product wordt niet aangemaakt | integration |
| AC-005-1 | REQ-005 | Een beheerder is ingelogd en heeft een bestaand product geselecteerd voor bewerking | De beheerder wijzigt de 'naam', 'beschrijving', 'prijs' en 'categorie' en klikt op 'Opslaan' | De wijzigingen aan naam, beschrijving, prijs en categorie worden succesvol opgeslagen en het product wordt bijgewerkt | integration |
| AC-005-2 | REQ-005 | Een beheerder is ingelogd en heeft een bestaand product geselecteerd voor bewerking | De beheerder probeert de 'initiële voorraad' direct te wijzigen via het productbewerkingsformulier en klikt op 'Opslaan' | De wijziging aan de voorraad wordt genegeerd en er wordt een melding getoond dat de voorraad alleen via stock-aanpassing gewijzigd kan worden | integration |
| AC-006-1 | REQ-006 | Een beheerder is ingelogd en heeft een actief product geselecteerd | De beheerder klikt op de 'Archiveer' knop voor het product | Het product wordt succesvol gearchiveerd, verdwijnt uit het actieve assortiment en is niet meer zichtbaar in de standaard productlijst | integration |
| AC-006-2 | REQ-006 | Een product is gearchiveerd en er zijn historische orderregels aan gekoppeld | De beheerder bekijkt de historische orderregels | Het gearchiveerde product blijft correct gekoppeld aan de historische orderregels | integration |
| AC-007-1 | REQ-007 | Een beheerder is ingelogd en heeft een product geselecteerd waarvan de voorraad aangepast moet worden | De beheerder voert een delta van '+50' in, selecteert de reden 'RESTOCK' en klikt op 'Voorraad aanpassen' | De voorraad van het product wordt met 50 verhoogd en de aanpassing wordt gelogd | integration |
| AC-007-2 | REQ-007 | Een beheerder is ingelogd en heeft een product geselecteerd waarvan de voorraad aangepast moet worden | De beheerder voert een delta van '-10' in, selecteert de reden 'DAMAGE' en klikt op 'Voorraad aanpassen' | De voorraad van het product wordt met 10 verlaagd en de aanpassing wordt gelogd | integration |
| AC-007-3 | REQ-007 | Een beheerder probeert de voorraad van een product aan te passen | De beheerder laat het veld 'reden' leeg en klikt op 'Voorraad aanpassen' | Er wordt een foutmelding getoond dat de reden verplicht is en de voorraad wordt niet aangepast | integration |
| AC-008-1 | REQ-008 | Een product heeft een huidige voorraad van 5 stuks | Een beheerder probeert de voorraad aan te passen met een delta van '-10' en de reden 'CORRECTION' | De voorraad wordt niet onder 0 gebracht; de aanpassing wordt afgewezen met een foutmelding dat de voorraad niet negatief mag worden | integration |
| AC-008-2 | REQ-008 | Een product heeft een huidige voorraad van 0 stuks | Een beheerder probeert de voorraad aan te passen met een delta van '-5' en de reden 'RETURN' | De voorraad wordt niet onder 0 gebracht; de aanpassing wordt afgewezen met een foutmelding dat de voorraad niet negatief mag worden | integration |
| AC-009-1 | REQ-009 | Een product heeft een voorraad van 8 stuks en de drempelwaarde is ingesteld op 10 | De productlijst wordt geladen | Het product wordt gemarkeerd als 'low stock' | integration |
| AC-009-2 | REQ-009 | Een product heeft een voorraad van 12 stuks en de drempelwaarde is ingesteld op 10 | De productlijst wordt geladen | Het product wordt niet gemarkeerd als 'low stock' | integration |
| AC-009-3 | REQ-009 | Een product heeft een voorraad van 10 stuks en de drempelwaarde is ingesteld op 10 | De productlijst wordt geladen | Het product wordt niet gemarkeerd als 'low stock' | integration |
| AC-010-1 | REQ-010 | Er zijn 25 producten in de catalogus | De beheerder navigeert naar de productlijst | De productlijst toont de eerste 10 producten, met navigatieknoppen voor de volgende pagina's | integration |
| AC-010-2 | REQ-010 | De productlijst is geopend | De beheerder klikt op de sorteerknop voor 'naam' oplopend | De producten worden alfabetisch gesorteerd op naam (A-Z) | integration |
| AC-010-3 | REQ-010 | De productlijst is geopend | De beheerder voert 'Laptop' in het filterveld 'naam' in | Alleen producten waarvan de naam 'Laptop' bevat worden getoond | integration |
| AC-010-4 | REQ-010 | De productlijst is geopend | De beheerder selecteert de categorie 'Elektronica' uit het filter | Alleen producten in de categorie 'Elektronica' worden getoond | integration |
| AC-010-5 | REQ-010 | De productlijst is geopend | De beheerder selecteert de filter 'beschikbaarheid: gearchiveerd' | Alleen gearchiveerde producten worden getoond | integration |
| AC-010-6 | REQ-010 | De productlijst is geopend | De beheerder selecteert de filter 'beschikbaarheid: low stock' | Alleen producten met een voorraad lager dan of gelijk aan de drempelwaarde worden getoond | integration |
| AC-011-1 | REQ-011 | Er zijn geen actieve producten gekoppeld aan de categorie 'Oude Gadgets' | Een beheerder probeert de categorie 'Oude Gadgets' te verwijderen | De categorie 'Oude Gadgets' wordt succesvol verwijderd | integration |
| AC-011-2 | REQ-011 | Er is een actief product gekoppeld aan de categorie 'Elektronica' | Een beheerder probeert de categorie 'Elektronica' te verwijderen | De categorie 'Elektronica' kan niet worden verwijderd en er wordt een foutmelding getoond dat er nog actieve producten aan gekoppeld zijn | integration |
| AC-012-1 | REQ-012 | Een beheerder is ingelogd op de categoriebeheerpagina | De beheerder vult de naam 'Nieuwe Categorie' in en klikt op 'Categorie aanmaken' | De nieuwe categorie 'Nieuwe Categorie' wordt succesvol aangemaakt en is zichtbaar in de lijst | integration |
| AC-012-2 | REQ-012 | Een beheerder probeert een nieuwe categorie aan te maken met een naam langer dan 100 tekens | De beheerder vult een naam in die 101 tekens lang is en klikt op 'Categorie aanmaken' | Er wordt een foutmelding getoond dat de naam maximaal 100 tekens lang mag zijn en de categorie wordt niet aangemaakt | integration |
| AC-012-3 | REQ-012 | Er bestaat al een categorie met de naam 'Bestaande Categorie' | Een beheerder probeert een nieuwe categorie aan te maken met de naam 'Bestaande Categorie' | Er wordt een foutmelding getoond dat de naam van de categorie al in gebruik is en de categorie wordt niet aangemaakt | integration |
| AC-013-1 | REQ-013 | Er bestaat een actief product met de naam 'Uniek Product' | Een beheerder probeert een nieuw product aan te maken met de naam 'Uniek Product' | Er wordt een foutmelding getoond dat de productnaam al in gebruik is voor een actief product en het product wordt niet aangemaakt | integration |
| AC-013-2 | REQ-013 | Er bestaat een gearchiveerd product met de naam 'Gearchiveerd Product' | Een beheerder probeert een nieuw product aan te maken met de naam 'Gearchiveerd Product' | Het product wordt succesvol aangemaakt, omdat de uniekheidscontrole niet geldt voor gearchiveerde producten | integration |
| AC-014-1 | REQ-014 | Een product is gearchiveerd | Een beheerder probeert het gearchiveerde product te bewerken | De bewerkingsopties voor het product zijn uitgeschakeld of niet beschikbaar, en er wordt een melding getoond dat gearchiveerde producten niet bewerkt kunnen worden | integration |
| AC-014-2 | REQ-014 | Een product is gearchiveerd | Een beheerder probeert het gearchiveerde product opnieuw te archiveren | Er gebeurt niets en er wordt een melding getoond dat het product al gearchiveerd is | integration |
| AC-014-3 | REQ-014 | Een product is gearchiveerd | Een beheerder klikt op de 'Herstel' (unarchive) knop voor het product | Het product wordt succesvol hersteld, wordt weer actief in het assortiment en is zichtbaar in de productlijst | integration |
| AC-015-1 | REQ-015 | Een beheerder past de voorraad van een product aan | De stock-aanpassing is voltooid | Er wordt een record aangemaakt in de audit-tabel met de product_id, delta, reden, beheerder-id en het tijdstip van de aanpassing | integration |
| AC-015-2 | REQ-015 | De audit-tabel bevat stock-aanpassingen | Een beheerder probeert de records in de audit-tabel te verwijderen | De verwijderoperatie wordt geweigerd en er wordt een foutmelding getoond dat de audit-log niet verwijderbaar is | integration |
| AC-016-1 | REQ-016 | Een product heeft geen specifieke lage-voorraaddrempel ingesteld | De voorraad van dit product daalt tot 8 stuks en de globale drempel is 10 | Het product wordt gemarkeerd als 'low stock' | integration |
| AC-016-2 | REQ-016 | Een product heeft een specifieke lage-voorraaddrempel ingesteld op 5 | De voorraad van dit product daalt tot 4 stuks | Het product wordt gemarkeerd als 'low stock' | integration |
| AC-016-3 | REQ-016 | Een product heeft een specifieke lage-voorraaddrempel ingesteld op 5 | De voorraad van dit product daalt tot 6 stuks | Het product wordt niet gemarkeerd als 'low stock' | integration |
| AC-017-1 | REQ-017 | Er is een order geplaatst met product X tegen prijs Y | De beheerder wijzigt de prijs van product X naar Z | De prijs van product X in de bestaande order blijft Y en wordt niet beïnvloed door de prijswijziging | integration |
| AC-018-1 | REQ-018 | Er bestaat een gearchiveerd product met de naam 'Conflict Product' | Een beheerder probeert een nieuw product aan te maken met de naam 'Conflict Product' zonder expliciete bevestiging | Er wordt een conflict-melding getoond met de optie om het gearchiveerde product te herstellen, en het nieuwe product wordt niet aangemaakt | integration |
| AC-018-2 | REQ-018 | Er bestaat een gearchiveerd product met de naam 'Conflict Product' | Een beheerder probeert een nieuw product aan te maken met de naam 'Conflict Product' en bevestigt de conflict-melding om het gearchiveerde product te herstellen | Het gearchiveerde product wordt hersteld en het nieuwe product wordt niet aangemaakt | integration |
| AC-019-1 | REQ-019 | De productcatalogus bevat 10.000 producten | Een beheerder vraagt de productlijst op met standaard filters en paginering | De API response time voor het ophalen van de productlijst is p95 < 300ms | integration |
| AC-020-1 | REQ-020 | Een beheerder wil een nieuw product aanmaken | De beheerder verstuurt de aanvraag om een nieuw product aan te maken | De API response time voor het aanmaken van het product is p95 < 500ms | integration |
| AC-020-2 | REQ-020 | Een beheerder wil een bestaand product bewerken | De beheerder verstuurt de aanvraag om een bestaand product te bewerken | De API response time voor het bewerken van het product is p95 < 500ms | integration |
| AC-021-1 | REQ-021 | Een beheerder maakt een nieuw product aan | De aanmaakoperatie is voltooid | De aanmaakoperatie wordt gelogd met de correlationId en de beheerder-id | integration |
| AC-021-2 | REQ-021 | Een beheerder bewerkt een bestaand product | De bewerkingsoperatie is voltooid | De bewerkingsoperatie wordt gelogd met de correlationId en de beheerder-id | integration |
| AC-021-3 | REQ-021 | Een beheerder archiveert een product | De archiveringsoperatie is voltooid | De archiveringsoperatie wordt gelogd met de correlationId en de beheerder-id | integration |
| AC-021-4 | REQ-021 | Een beheerder past de voorraad van een product aan | De stock-aanpassing is voltooid | De stock-aanpassing wordt gelogd met de correlationId en de beheerder-id | integration |
| AC-022-1 | REQ-022 | Een gebruiker met de rol 'ROLE_USER' probeert toegang te krijgen tot de productaanmaak-endpoint | De gebruiker stuurt een POST-verzoek naar de productaanmaak-endpoint | De API retourneert een HTTP 403 Forbidden foutmelding | integration |
| AC-022-2 | REQ-022 | Een gebruiker met de rol 'ROLE_ADMIN' probeert toegang te krijgen tot de productaanmaak-endpoint | De gebruiker stuurt een POST-verzoek naar de productaanmaak-endpoint | De API retourneert een HTTP 201 Created statuscode | integration |
| AC-023-1 | REQ-023 | De productcatalogus bevat 10.000 actieve producten | Een beheerder laadt de productlijst met alle filters en sorteringen uitgeschakeld | De laadtijd van de productlijst is acceptabel en er is geen merkbare prestatiedegradatie | integration |


## 13. Traceability Matrix

| REQ | Backend | Frontend | Tests |
|-----|---------|----------|-------|
| REQ-001 | ProductController, ProductService, ProductRepository, ProductEntity, CreateProductRequestDto, ProductCreateValidator | ProductFormPage, ProductForm | Test het aanmaken van een nieuw product met alle verplichte velden (naam, beschrijving, prijs, categorie, initiële voorraad).; Test het aanmaken van een nieuw product met optionele velden (indien van toepassing).; Test het aanmaken van een nieuw product met ongeldige invoer voor elk veld om validatie te triggeren. |
| REQ-002 | ProductCreateValidator, ProductUpdateValidator, ProductNameUniquenessValidator, ProductRepository | ProductForm | Test het aanmaken van een product met een lege naam.; Test het aanmaken van een product met een naam langer dan 200 tekens.; Test het aanmaken van een product met een naam die al bestaat voor een actief product.; Test het aanmaken van een product met een naam die al bestaat voor een gearchiveerd product (moet toegestaan zijn).; Test het aanmaken van een product met een unieke naam. |
| REQ-003 | ProductCreateValidator, ProductUpdateValidator, BigDecimalConfig | ProductForm | Test het aanmaken/bewerken van een product met een prijs van 0.00.; Test het aanmaken/bewerken van een product met een prijs van 0.01.; Test het aanmaken/bewerken van een product met een prijs van 99999.99.; Test het aanmaken/bewerken van een product met een prijs lager dan 0.01.; Test het aanmaken/bewerken van een product met een prijs hoger dan 99999.99.; Test het aanmaken/bewerken van een product met een prijs met meer dan twee decimalen. |
| REQ-004 | ProductCreateValidator, ProductUpdateValidator | ProductForm | Test het aanmaken/bewerken van een product met een initiële voorraad van 0.; Test het aanmaken/bewerken van een product met een initiële voorraad van 999999.; Test het aanmaken/bewerken van een product met een initiële voorraad lager dan 0.; Test het aanmaken/bewerken van een product met een initiële voorraad hoger dan 999999. |
| REQ-005 | ProductController, ProductService, UpdateProductRequestDto, ProductUpdateValidator | ProductEditPage, ProductForm | Test het bewerken van de naam, beschrijving, prijs en categorie van een bestaand product.; Test dat de voorraad niet bewerkt kan worden via deze endpoint.; Test het bewerken van een product dat gearchiveerd is (moet niet kunnen). |
| REQ-006 | ProductController, ProductService, ProductRepository, ProductArchivedStatusValidator | ProductTable, ConfirmDialog | Test het archiveren van een actief product.; Test dat een gearchiveerd product niet meer zichtbaar is in de actieve productlijst.; Test dat een gearchiveerd product nog steeds gekoppeld is aan historische orderregels (vereist integratietest met ordersysteem). |
| REQ-007 | StockController, StockService, StockAdjustmentRepository, StockAdjustmentEntity, StockAdjustmentRequestDto, StockAdjustmentValidator, StockAdjustmentReasonEnum | StockAdjustmentButton, StockAdjustmentModal | Test het aanpassen van de voorraad met een positieve delta (bv. +50) met geldige redenen.; Test het aanpassen van de voorraad met een negatieve delta (bv. -10) met geldige redenen.; Test het aanpassen van de voorraad met een ongeldige reden.; Test het aanpassen van de voorraad van een gearchiveerd product (moet niet kunnen). |
| REQ-008 | StockService, NegativeStockPreventionValidator, ProductRepository | StockAdjustmentModal | Test het aanpassen van de voorraad met een negatieve delta die de voorraad onder 0 zou brengen.; Test dat de voorraad niet onder 0 kan komen en dat er een foutmelding wordt getoond. |
| REQ-009 | ProductService, ProductRepository | ProductTable, LowStockBadge | Test dat producten met een voorraad gelijk aan de drempelwaarde (standaard 10) gemarkeerd worden als 'low stock'.; Test dat producten met een voorraad net boven de drempelwaarde niet gemarkeerd worden.; Test dat producten met een voorraad net onder de drempelwaarde wel gemarkeerd worden. |
| REQ-010 | ProductController, ProductService, PageableRequestDto, PageableResponseDto | ProductListPage, ProductTable, Pagination | Test paginering van de productlijst (10 per pagina).; Test sortering op naam (oplopend en aflopend).; Test filteren op naam (bevat).; Test filteren op categorie.; Test filteren op beschikbaarheid (actief).; Test filteren op beschikbaarheid (gearchiveerd).; Test filteren op beschikbaarheid (low stock). |
| REQ-011 | CategoryService, CategoryRepository, ProductRepository, CategoryCannotDeleteException | CategoryManagerPage, CategoryManager, ConfirmDialog | Test het verwijderen van een categorie waaraan nog actieve producten gekoppeld zijn (moet geweigerd worden).; Test het verwijderen van een categorie waaraan geen actieve producten gekoppeld zijn. |
| REQ-012 | CategoryController, CategoryService, CategoryRepository, CreateCategoryRequestDto, CategoryNameUniquenessValidator | CategoryManagerPage, CategoryManager | Test het aanmaken van een nieuwe categorie met een unieke naam (max 100 tekens).; Test het aanmaken van een categorie met een naam langer dan 100 tekens.; Test het aanmaken van een categorie met een naam die al bestaat. |
| REQ-013 | ProductNameUniquenessValidator, ProductRepository |  | Test het aanmaken van een product met een naam die al bestaat voor een ander actief product.; Test het aanmaken van een product met een naam die al bestaat voor een gearchiveerd product (moet toegestaan zijn).; Test het aanmaken van een product met een naam die al bestaat voor een ander gearchiveerd product (moet toegestaan zijn). |
| REQ-014 | ProductController, ProductService, ProductRepository, ProductArchivedStatusValidator | ProductTable, ProductEditPage | Test dat een gearchiveerd product niet bewerkt kan worden.; Test dat een gearchiveerd product niet opnieuw gearchiveerd kan worden.; Test het herstellen (unarchive) van een gearchiveerd product. |
| REQ-015 | StockService, AuditLogService, AuditLogRepository, AuditLogEntity, AuditOperationEnum, AuditEntityTypeEnum |  | Test dat elke stock-aanpassing wordt gelogd in de audit-tabel met de juiste velden (product_id, delta, reden, beheerder-id, tijdstip).; Test dat de audit-log niet verwijderbaar is. |
| REQ-016 | ProductService, ProductRepository | ProductForm | Test het instellen van een specifieke lage-voorraaddrempel voor een product.; Test dat de ingestelde drempelwaarde wordt gebruikt voor de 'low stock' markering.; Test dat de globale standaard drempelwaarde (10) wordt gebruikt als er geen specifieke drempel is ingesteld voor een product. |
| REQ-017 | ProductService, ProductRepository |  | Test dat een prijswijziging van een product geen invloed heeft op reeds geplaatste orders die dat product bevatten. |
| REQ-018 | ProductCreateValidator, ProductNameConflictException, ProductRepository | ProductForm, ConfirmDialog | Test het aanmaken van een product met dezelfde naam als een gearchiveerd product zonder expliciete bevestiging (moet conflict geven).; Test het aanmaken van een product met dezelfde naam als een gearchiveerd product met expliciete bevestiging (moet toegestaan zijn).; Test de conflict-melding met de optie om het gearchiveerde product te herstellen. |
| REQ-019 | ProductController, ProductService, PageableRequestDto, PageableResponseDto | ProductListPage | Meet de response time van de API voor het ophalen van de productlijst met verschillende filters en paginering.; Valideer dat de p95 response time onder de 300ms blijft voor minimaal 10.000 producten. |
| REQ-020 | ProductController, ProductService, CreateProductRequestDto, UpdateProductRequestDto | ProductFormPage, ProductEditPage | Meet de response time van de API voor het aanmaken van een product.; Meet de response time van de API voor het bewerken van een product.; Valideer dat de p95 response time onder de 500ms blijft. |
| REQ-021 | AuditLogService, AuditLogRepository, AuditLogEntity, AuditOperationEnum, AuditEntityTypeEnum, UuidGenerator |  | Test dat alle schrijfoperaties (aanmaken, bewerken, archiveren, stock-aanpassing) worden gelogd met een correlationId en beheerder-id.; Controleer de audit logs voor de aanwezigheid van correlationId en beheerder-id bij elke relevante operatie. |
| REQ-022 | SecurityConfig, JwtAuthenticationFilter, ProductController, StockController, CategoryController |  | Test toegang tot alle product-gerelateerde API endpoints met een gebruiker zonder ROLE_ADMIN.; Test toegang tot alle product-gerelateerde API endpoints met een gebruiker met ROLE_ADMIN.; Test dat niet-geautoriseerde verzoeken worden afgewezen met een 403 Forbidden status. |
| REQ-023 | ProductController, ProductService, PageableRequestDto, PageableResponseDto | ProductListPage, ProductTable | Simuleer een database met minimaal 10.000 actieve producten.; Test het laden van de productlijst en controleer op merkbare prestatiedegradatie.; Test de laadtijd van de productlijstpagina en de producttabel. |

