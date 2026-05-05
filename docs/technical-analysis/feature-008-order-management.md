# Feature-008: Order Management

## 1. Scope

**In Scope:**

*   Bestelformulier met productoverzicht.
*   Bestellingen plaatsen via UI.
*   Bestellingenhistorie raadplegen.
*   Bestelling annuleren (alleen indien status PENDING).
*   Order button met voldoende breedte en duidelijke feedback (loading, success states).
*   Validatie van bestelgegevens op de frontend.

**Out of Scope:**

*   Betalingsverwerking (integratie met externe payment gateway).
*   Retouren en terugbetalingen.
*   Verzendtracking.
*   Facturatie.

## 2. Assumptions

*   De `sequences` tabel is reeds geconfigureerd met de `entity_type` en `year` kolommen, en de PostgreSQL `nextval('order_seq')` functie is correct ingesteld voor het genereren van unieke `orderNumber` waarden.
*   De bestaande API endpoints `/api/products` en `/api/customers/{id}/address` zijn functioneel en leveren de verwachte data.
*   De frontend Axios instance is beschikbaar op `src/api/client.ts` en is geconfigureerd voor het afhandelen van API calls.
*   De React Query hooks `useQuery` en `useMutation` zijn de enige toegestane mechanismen voor data fetching en state management van server-side data.
*   De bestaande `<Spinner />` component en `<StepIndicator />` component zijn beschikbaar en correct geïmplementeerd in de frontend codebase.
*   De `Customer`, `Order`, `OrderItem`, en `Product` domeinmodellen zijn reeds gedefinieerd en correct gemapt.
*   De `ROLE_USER` en `ROLE_ADMIN` rollen zijn beschikbaar voor autorisatie.

## 3. Open Questions

*   **Besloten:** Gecancelde bestellingen moeten zichtbaar blijven in de bestellingenhistorie, gemarkeerd met een "CANCELLED" badge.
*   **Besloten:** Bij annulering van een bestelling moet de voorraad van de betreffende `OrderItem` producten onmiddellijk worden hersteld.
*   **Besloten:** Een klant kan maximaal 5 adressen opslaan; bij het toevoegen van een zesde adres wordt het oudste adres overschreven.

## 4. Domain Model

### Customer

| Veld  | Type   | Constraints                               | Testcases                                                              |
|-------|--------|-------------------------------------------|------------------------------------------------------------------------|
| id    | UUID   | NOT NULL                                  | missing, invalid_value                                                 |
| name  | String | NOT NULL, minLength:1, maxLength:255    | empty, too_short, too_long, missing, invalid_value                     |
| email | String | NOT NULL, minLength:5, maxLength:255    | empty, too_short, too_long, missing, invalid_value                     |

### Order

| Veld          | Type            | Constraints                                     | Testcases                                                                                             |
|---------------|-----------------|-------------------------------------------------|-------------------------------------------------------------------------------------------------------|
| id            | UUID            | NOT NULL                                        | missing, invalid_value                                                                                |
| orderNumber   | String          | NOT NULL, minLength:13, maxLength:13            | empty, too_short, too_long, missing, invalid_value, duplicate_per_day                                 |
| customer      | Customer        | NOT NULL                                        | missing, invalid_value                                                                                |
| orderItems    | List<OrderItem> | NOT NULL, minLength:1, maxLength:20             | empty, too_short, too_long, missing, invalid_value                                                    |
| deliveryAddress | Address         | NOT NULL                                        | missing, invalid_value                                                                                |
| paymentMethod | PaymentMethod   | NOT NULL                                        | missing, invalid_value                                                                                |
| status        | OrderStatus     | NOT NULL                                        | missing, invalid_value                                                                                |
| totalPrice    | BigDecimal      | NOT NULL, minValue:5.00                         | missing, invalid_value                                                                                |
| createdAt     | LocalDateTime   | NOT NULL                                        | missing, invalid_value                                                                                |
| lastUpdatedAt | LocalDateTime   | NOT NULL                                        | missing, invalid_value                                                                                |
| correlationId | UUID            | NOT NULL                                        | missing, invalid_value                                                                                |

### OrderItem

| Veld      | Type     | Constraints                  | Testcases                               |
|-----------|----------|------------------------------|-----------------------------------------|
| id        | UUID     | NOT NULL                     | missing, invalid_value                  |
| product   | Product  | NOT NULL                     | missing, invalid_value                  |
| quantity  | Integer  | NOT NULL, minValue:1         | missing, invalid_value                  |
| unitPrice | BigDecimal | NOT NULL, minValue:0.00      | missing, invalid_value                  |
| order     | Order    | NOT NULL                     | missing, invalid_value                  |

### Product

| Veld          | Type     | Constraints                               | Testcases                                                              |
|---------------|----------|-------------------------------------------|------------------------------------------------------------------------|
| id            | UUID     | NOT NULL                                  | missing, invalid_value                                                 |
| productId     | String   | NOT NULL, minLength:1, maxLength:255    | empty, too_short, too_long, missing, invalid_value                     |
| name          | String   | NOT NULL, minLength:1, maxLength:255    | empty, too_short, too_long, missing, invalid_value                     |
| currentPrice  | BigDecimal | NOT NULL, minValue:0.00                   | missing, invalid_value                                                 |
| stockQuantity | Integer  | NOT NULL, minValue:0                      | missing, invalid_value                                                 |

### Address

| Veld          | Type     | Constraints                               | Testcases                                                              |
|---------------|----------|-------------------------------------------|------------------------------------------------------------------------|
| id            | UUID     | NOT NULL                                  | missing, invalid_value                                                 |
| street        | String   | NOT NULL, minLength:1, maxLength:255    | empty, too_short, too_long, missing, invalid_value                     |
| houseNumber   | String   | NOT NULL, minLength:1, maxLength:20     | empty, too_short, too_long, missing, invalid_value                     |
| postalCode    | String   | NOT NULL, minLength:4, maxLength:10     | empty, too_short, too_long, missing, invalid_value                     |
| city          | String   | NOT NULL, minLength:1, maxLength:255    | empty, too_short, too_long, missing, invalid_value                     |
| country       | String   | NOT NULL, minLength:2, maxLength:100    | empty, too_short, too_long, missing, invalid_value                     |

### Enums

#### PaymentMethod

| Waarde           |
|------------------|
| CREDIT_CARD      |
| BANK_TRANSFER    |
| IDEAL            |

#### OrderStatus

| Waarde    |
|-----------|
| PENDING   |
| CONFIRMED |
| SHIPPED   |
| DELIVERED |
| CANCELLED |

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

#### 5.2.1 POST /api/orders — Plaats een nieuwe bestelling

| Veld          | Waarde

## 6. Backend Design

De backend volgt de strikte gelaagde architectuur: Controller → Service → Repository. Het domeinmodel is puur Java, zonder Spring-annotaties. DTO's worden geïmplementeerd als Java records en MapStruct wordt gebruikt voor mapping.

### Order Module

| Klasse                      | Verantwoordelijkheid                                                                                             |
| :-------------------------- | :--------------------------------------------------------------------------------------------------------------- |
| `OrderController`           | Verwerkt inkomende HTTP-verzoeken voor orders en stuurt ze door naar de `OrderService`.                         |
| `OrderService`              | Bevat de kernlogica voor het aanmaken, ophalen en annuleren van bestellingen.                                   |
| `OrderRepository`           | Verantwoordelijk voor de interactie met de database voor `Order`-entiteiten.                                    |
| `OrderEntity`               | Representeert de `Order`-entiteit in de database.                                                                |
| `CreateOrderRequestDto`     | Data Transfer Object voor het aanmaken van een nieuwe bestelling.                                                |
| `OrderResponseDto`          | Data Transfer Object voor het retourneren van bestelgegevens.                                                     |
| `PaginatedOrderResponseDto` | Data Transfer Object voor gepagineerde lijsten met bestellingen.                                                 |
| `OrderItemEntity`           | Representeert de `OrderItem`-entiteit in de database.                                                            |
| `OrderItemDto`              | Data Transfer Object voor individuele order items.                                                               |
| `OrderNotFoundException`    | Exception die wordt gegooid wanneer een bestelling niet wordt gevonden.                                           |
| `OrderAlreadyCancelledException` | Exception die wordt gegooid wanneer een bestelling al geannuleerd is.                                            |
| `OrderCancellationTimeExpiredException` | Exception die wordt gegooid wanneer de annuleringstermijn voor een bestelling is verstreken.                 |
| `InsufficientStockException` | Exception die wordt gegooid wanneer er onvoldoende voorraad is voor een product.                                 |
| `OrderNumberGenerator`      | Genereert unieke ordernummers volgens het gespecificeerde formaat, gebruikmakend van de `sequences` tabel.       |
| `OrderValidationService`    | Valideert de input voor het aanmaken van een bestelling volgens de business rules.                               |
| `OrderCancellationService`  | Behandelt de logica voor het annuleren van bestellingen, inclusief het herstellen van voorraad.                 |
| `OrderPersistenceService`   | Verantwoordelijk voor het opslaan en ophalen van ordergegevens met een lange bewaartermijn (indien van toepassing). |

### Product Module

| Klasse                      | Verantwoordelijkheid                                                              |
| :-------------------------- | :-------------------------------------------------------------------------------- |
| `ProductController`         | Verwerkt inkomende HTTP-verzoeken voor producten.                                |
| `ProductService`            | Bevat de logica voor het ophalen van productinformatie.                         |
| `ProductRepository`         | Verantwoordelijk voor de interactie met de database voor `Product`-entiteiten.   |
| `ProductEntity`             | Representeert de `Product`-entiteit in de database.                              |
| `ProductResponseDto`        | Data Transfer Object voor productinformatie.                                     |
| `PaginatedProductResponseDto` | Data Transfer Object voor gepagineerde lijsten met producten.                   |
| `StockService`              | Beheert de voorraad van producten en voert controles uit.                       |

### Customer Module

| Klasse                   | Verantwoordelijkheid                                                              |
| :----------------------- | :-------------------------------------------------------------------------------- |
| `CustomerController`     | Verwerkt inkomende HTTP-verzoeken voor klantgerelateerde data.                    |
| `CustomerService`        | Bevat de logica voor het ophalen van klantgegevens.                              |
| `CustomerRepository`     | Verantwoordelijk voor de interactie met de database voor `Customer`-entiteiten.  |
| `CustomerEntity`         | Representeert de `Customer`-entiteit in de database.                             |
| `AddressService`         | Bevat de logica voor het ophalen van adressen van een klant.                     |
| `AddressRepository`      | Verantwoordelijk voor de interactie met de database voor `Address`-entiteiten.   |
| `AddressEntity`          | Representeert de `Address`-entiteit in de database.                              |
| `ListAddressResponseDto` | Data Transfer Object voor een lijst met adressen.                                |

### Common Module

| Klasse              | Verantwoordelijkheid                                                                 |
| :------------------ | :----------------------------------------------------------------------------------- |
| `ApiError`          | Standaard foutmelding object voor API responses, conform de gespecificeerde structuur. |
| `ApiExceptionHandler` | Globale exception handler voor het afhandelen van API-gerelateerde exceptions.       |
| `CorrelationIdFilter` | Filter om een `correlationId` te genereren en toe te voegen aan elke request.        |
| `LoggingAspect`     | Aspect voor het loggen van requests en responses met `correlationId`.                |
| `ValidationConfig`  | Configuratie voor bean validation.                                                   |
| `SecurityConfig`    | Configuratie voor authenticatie en autorisatie (JWT Bearer token).                   |
| `PaymentMethodEnum` | Enum voor de verschillende betaalmethoden.                                          |
| `OrderStatusEnum`   | Enum voor de verschillende orderstatussen (`PENDING`, `PROCESSING`, `SHIPPED`, `DELIVERED`, `CANCELLED`). |

## 7. Frontend Design

### /checkout

| Component | Verantwoordelijkheid |
|---|---|
| CheckoutPage | Hoofdcomponent voor het bestelproces, beheert de stappen van de wizard. |
| StepIndicator | Visuele indicator voor de huidige stap in het bestelproces (bestaand component). |
| ProductSelectionStep | Component voor het selecteren van producten, inclusief weergave van producten en voorraad. |
| AddressSelectionStep | Component voor het selecteren of invoeren van het leveringsadres. |
| PaymentMethodSelectionStep | Component voor het selecteren van de betaalmethode. |
| OrderSummaryStep | Component voor het tonen van de bestel samenvatting en de definitieve order button. |
| OrderButton | Knop voor het plaatsen van de bestelling, met loading state en minimale breedte van 200px. |
| FormErrorDisplay | Component voor het tonen van veldspecifieke foutmeldingen en een algemene foutbanner. |
| LoadingSpinner | Component voor het tonen van een laadindicator (bestaand component). |

### /order-history

| Component | Verantwoordelijkheid |
|---|---|
| OrderHistoryPage | Pagina voor het weergeven van de bestelgeschiedenis van de klant. |
| OrderList | Component dat de lijst met bestellingen weergeeft, inclusief paginering en sortering. |
| OrderItem | Component voor het weergeven van een individuele bestelling in de lijst. |
| CancelOrderButton | Knop om een bestelling te annuleren, alleen zichtbaar voor PENDING orders binnen 30 minuten. |
| OrderStatusBadge | Component voor het tonen van de status van een bestelling (bv. PENDING, CONFIRMED, CANCELLED). |

## 8. Security & Privacy

### 8.1 Authenticatie en Autorisatie

*   **Authenticatie:** Alle API-endpoints gerelateerd aan orders vereisen een geldige JWT Bearer token voor authenticatie. De authenticatie is stateless.
*   **Autorisatie:**
    *   Gebruikers met de rol `ROLE_USER` kunnen hun eigen bestellingen plaatsen, bekijken en annuleren.
    *   Gebruikers met de rol `ROLE_ADMIN` hebben volledige toegang tot alle orderbeheerfunctionaliteiten, inclusief het bekijken van alle bestellingen en het beheren van de status (indien toekomstige functionaliteit dit vereist).
    *   Toegang tot `/api/orders/{id}` is beperkt tot de eigenaar van de bestelling of een `ROLE_ADMIN`.
    *   Het annuleren van een bestelling (`DELETE /api/orders/{id}`) is alleen toegestaan voor de eigenaar van de bestelling en alleen indien de bestelling de status `PENDING` heeft en binnen 30 minuten na creatie is.

### 8.2 Data Privacy

*   **Gevoelige Data:** Persoonlijke gegevens zoals e-mailadressen, namen en adressen worden opgeslagen in de `customers` en `orders` tabellen. Deze data wordt alleen gebruikt voor het verwerken van bestellingen en wordt niet gelogd in platte tekst.
*   **Logboekbeveiliging:** Conform de architectuurregels worden geen gevoelige gegevens (zoals creditcardnummers, tokens of volledige adressen) gelogd. Enkel de `correlationId` en relevante niet-gevoelige orderinformatie worden gelogd.
*   **Adresbeheer:** Klanten kunnen maximaal 5 adressen opslaan. Bij het toevoegen van een zesde adres wordt het oudste adres automatisch overschreven. Dit wordt beheerd op applicatieniveau in de service layer.

## 9. Observability

### 9.1 Logging

Alle backend logging zal gestructureerd zijn in JSON-formaat voor productieomgevingen, gebruikmakend van SLF4J en Logback. De `correlationId` wordt in elke log entry opgenomen.

**Voorbeelden van te loggen gebeurtenissen:**

*   **Order Creatie (Succesvol):**
    ```json
    {
      "timestamp": "2024-03-15T10:30:00.123Z",
      "level": "INFO",
      "thread": "http-nio-8080-exec-1",
      "logger": "com.example.orders.service.OrderService",
      "message": "Order created successfully",
      "correlationId": "a1b2c3d4-e5f6-7890-1234-567890abcdef",
      "orderId": "f0e1d2c3-b4a5-6789-0123-456789abcdef",
      "orderNumber": "ORD-2024-000001",
      "customerId": "123e4567-e89b-12d3-a456-426614174000",
      "totalAmount": 55.75
    }
    ```
*   **Order Creatie (Validatiefout):**
    ```json
    {
      "timestamp": "2024-03-15T10:31:00.456Z",
      "level": "WARN",
      "thread": "http-nio-8080-exec-2",
      "logger": "com.example.orders.controller.OrderController",
      "message": "Validation failed for order creation",
      "correlationId": "b2c3d4e5-f6a7-8901-2345-67890abcdef0",
      "orderValidationErrors": {
        "items[0].quantity": "Quantity must be at least 1",
        "deliveryAddress.postalCode": "Invalid postal code format"
      }
    }
    ```
*   **Order Annulering (Succesvol):**
    ```json
    {
      "timestamp": "2024-03-15T11:00:00.789Z",
      "level": "INFO",
      "thread": "http-nio-8080-exec-3",
      "logger": "com.example.orders.service.OrderService",
      "message": "Order cancelled successfully",
      "correlationId": "c3d4e5f6-a7b8-9012-3456-7890abcdef01",
      "orderId": "f0e1d2c3-b4a5-6789-0123-456789abcdef",
      "orderNumber": "ORD-2024-000001",
      "customerId": "123e4567-e89b-12d3-a456-426614174000"
    }
    ```
*   **Order Annulering (Niet toegestaan - te laat):**
    ```json
    {
      "timestamp": "2024-03-15T11:05:00.901Z",
      "level": "WARN",
      "thread": "http-nio-8080-exec-4",
      "logger": "com.example.orders.service.OrderService",
      "message": "Order cancellation failed: Order is too old to be cancelled",
      "correlationId": "d4e5f6a7-b8c9-0123-4567-890abcdef012",
      "orderId": "f0e1d2c3-b4a5-6789-0123-456789abcdef",
      "orderNumber": "ORD-2024-000001"
    }
    ```
*   **Interne Serverfout:**
    ```json
    {
      "timestamp": "2024-03-15T11:15:00.111Z",
      "level": "ERROR",
      "thread": "http-nio-8080-exec-5",
      "logger": "com.example.orders.controller.OrderController",
      "message": "An unexpected error occurred during order processing",
      "correlationId": "e5f6a7b8-c9d0-1234-5678-90abcdef0123",
      "exception": "com.example.orders.exception.OrderProcessingException: Failed to update stock for product XYZ",
      "stacktrace": "..."
    }
    ```

### 9.2 Metrics

*   **Request Latency:** Metrieken voor de responstijd van de `/api/orders` (POST) en `/api/orders?customerId={id}` (GET) endpoints worden verzameld. Doel is p95 < 500ms voor order creatie en p95 < 200ms voor de orderlijst.
*   **Error Rates:** Aantal 4xx en 5xx responses per endpoint worden gemonitord.
*   **Order Status Counts:** Aantal orders per status (PENDING, CONFIRMED, SHIPPED, DELIVERED, CANCELLED) kan worden gemonitord voor operationele inzichten.

### 9.3 Correlation ID

*   Een `correlationId` (UUID) wordt gegenereerd aan het begin van elke inkomende HTTP-request door de `CorrelationIdFilter` in de backend.
*   Deze `correlationId` wordt toegevoegd aan de MDC (Mapped Diagnostic Context) van Logback en opgenomen in elke log entry.
*   De `correlationId` wordt ook teruggegeven in de response body van alle foutmeldingen, conform het gespecificeerde formaat.
*   De frontend zal de `correlationId` niet genereren, maar deze ontvangen in de foutreacties en eventueel doorgeven in verdere requests indien nodig (hoewel dit voor deze feature niet expliciet vereist is).

## 10. Performance & Scalability

### 10.1 Performance Eisen

*   **Order Creatie:** De API response time voor het plaatsen van een bestelling (`POST /api/orders`) moet p95 < 500ms zijn.
*   **Order Lijst Ophalen:** De API response time voor het ophalen van de bestellingenlijst (`GET /api/orders?customerId={id}`) moet p95 < 200ms zijn.

### 10.2 Database Optimalisatie

*   **Tabellen:**
    *   `orders`: `id UUID PK`, `customer_id UUID FK`, `order_number VARCHAR`, `status VARCHAR`, `total_amount DECIMAL`, `created_at TIMESTAMP WITH TIME ZONE`, `updated_at TIMESTAMP WITH TIME ZONE`, `deleted_at TIMESTAMP WITH TIME ZONE`.
    *   `order_items`: `id UUID PK`, `order_id UUID FK`, `product_id UUID FK`, `quantity INT`, `unit_price DECIMAL`.
    *   `customers`: `id UUID PK`, `email VARCHAR UNIQUE`, `name VARCHAR`.
    *   `products`: `id UUID PK`, `name VARCHAR`, `stock INT`, `price DECIMAL`.
    *   `sequences`: `entity_type VARCHAR`, `year INT`, `current_value BIGINT`.
*   **Indexen:**
    *   `orders`:
        *   `idx_orders_customer_id`: Op `customer_id` voor snelle retrieval van bestellingen per klant.
        *   `idx_orders_created_at`: Op `created_at` voor sortering en filtering op datum.
        *   `idx_orders_status`: Op `status` voor filtering op bestelstatus.
    *   `order_items`:
        *   `idx_order_items_order_id`: Op `order_id` voor snelle retrieval van items per bestelling.
        *   `idx_order_items_product_id`: Op `product_id` voor het identificeren van bestellingen met specifieke producten.
    *   `customers`:
        *   `idx_customers_email`: Op `email` voor snelle lookup van klanten.
    *   `sequences`:
        *   `pk_sequences`: Op `entity_type` en `year` voor efficiënte lookup van sequenties.
*   **Sequence Generatie:** De `order_number` wordt gegenereerd met behulp van de bestaande `sequences` tabel en de PostgreSQL `nextval('order_seq')` functie, gecombineerd met het jaar. Dit zorgt voor unieke en chronologisch geordende ordernummers per jaar (formaat: `ORD-YYYY-NNNNNN`).

### 10.3 Schaalbaarheid

*   **Stateless Backend:** De Spring Boot applicatie is stateless, wat horizontale schaalbaarheid via load balancing mogelijk maakt.
*   **Database Connecties:** De Spring Data JPA configuratie zal worden geoptimaliseerd voor een efficiënt gebruik van database connecties (bijv. via een connection pool zoals HikariCP).
*   **Asynchrone Operaties (Toekomst):** Hoewel messaging momenteel niet in gebruik is, kan voor toekomstige uitbreidingen (bijv. voorraadupdates na annulering) een message queue overwogen worden om de responstijd van de ordercreatie te minimaliseren en de verwerking te ontkoppelen.
*   **Caching:** Overwegen van caching voor veelgevraagde, statische data zoals productinformatie om de databasebelasting te verminderen.
*   **Data Retentie:** Order data wordt minimaal 7 jaar bewaard conform wettelijke verplichtingen. Dit vereist een strategie voor data-archivering of -opschoning op lange termijn om de database beheersbaar te houden.

## 11. Test Strategy

### Unit Tests

*   `ProductSelectionStep.render`
*   `AddressSelectionStep.render`
*   `PaymentMethodSelectionStep.render`
*   `OrderSummaryStep.render`
*   `OrderButton.render`
*   `FormErrorDisplay.render`
*   `OrderHistoryPage.render`
*   `OrderList.render`
*   `OrderItem.render`
*   `CancelOrderButton.render`
*   `OrderStatusBadge.render`
*   `useProductSelectionForm` hook
*   `useAddressForm` hook
*   `usePaymentForm` hook
*   `useOrderMutation` hook

### Integration Tests

*   `POST /api/orders` should return `201 Created` upon successful order creation.
*   `GET /api/orders?customerId={id}` should return `200 OK` with the customer's order history.
*   `GET /api/products` should return `200 OK` with a paginated product list.
*   `GET /api/customers/{id}/address` should return `200 OK` with the customer's saved addresses.
*   `DELETE /api/orders/{id}` should return `204 No Content` upon successful order cancellation.
*   `POST /api/orders` should return `400 Bad Request` for validation errors.
*   `POST /api/orders` should return `409 Conflict` when there is insufficient stock for an order item.

### End-to-End (E2E) Tests

*   User navigates to checkout, selects products, fills in address and payment method, places an order, and sees the confirmation page.
*   User navigates to order history, views the list of orders, and cancels a `PENDING` order within the allowed timeframe.

## 12. Acceptance Criteria

| AC-ID | REQ | Gegeven | Wanneer | Dan | Testtype |
|-------|-----|---------|---------|-----|----------|
| AC-001-1 | REQ-001 | Een klant heeft producten toegevoegd aan zijn winkelwagen en is op de checkout pagina. | De klant vult een geldig leveringsadres in, selecteert een betaalmethode en klikt op 'Bestelling plaatsen'. | De bestelling wordt succesvol geplaatst en de klant ziet een ordernummer en de status PENDING. | e2e |
| AC-001-2 | REQ-001 | Een klant heeft producten toegevoegd aan zijn winkelwagen en is op de checkout pagina. | De klant probeert een bestelling te plaatsen zonder een leveringsadres in te vullen. | Er wordt een foutmelding getoond dat het leveringsadres verplicht is en de bestelling wordt niet geplaatst. | e2e |
| AC-002-1 | REQ-002 | Een bestelling is succesvol geplaatst. | De orderdetails worden opgevraagd. | Elk product in de bestelling bevat een productId, naam, aantal en eenheidsprijs. | integration |
| AC-003-1 | REQ-003 | Een klant probeert een bestelling te plaatsen. | Het leveringsadres veld 'straat' is leeg. | Er wordt een veldspecifieke foutmelding getoond voor het 'straat' veld en de bestelling wordt niet geplaatst. | e2e |
| AC-003-2 | REQ-003 | Een klant probeert een bestelling te plaatsen. | Het leveringsadres veld 'postcode' is ongeldig (bv. te kort). | Er wordt een veldspecifieke foutmelding getoond voor het 'postcode' veld en de bestelling wordt niet geplaatst. | e2e |
| AC-004-1 | REQ-004 | Een klant probeert een bestelling te plaatsen. | De betaalmethode is ingesteld op 'CREDIT_CARD'. | De bestelling wordt succesvol geplaatst. | integration |
| AC-004-2 | REQ-004 | Een klant probeert een bestelling te plaatsen. | De betaalmethode is ingesteld op een ongeldige waarde (bv. 'PAYPAL'). | Er wordt een veldspecifieke foutmelding getoond voor de betaalmethode en de bestelling wordt niet geplaatst. | e2e |
| AC-005-1 | REQ-005 | Een klant heeft succesvol een bestelling geplaatst. | De bestelling is verwerkt door het systeem. | De klant ziet een uniek orderNumber en de status PENDING op de orderbevestigingspagina. | e2e |
| AC-006-1 | REQ-006 | Een klant heeft meerdere bestellingen met verschillende statussen (PENDING, CONFIRMED, SHIPPED). | De klant navigeert naar de bestellingenpagina en filtert op 'CONFIRMED'. | Alleen bestellingen met de status CONFIRMED worden getoond. | e2e |
| AC-006-2 | REQ-006 | Een klant heeft meerdere bestellingen met verschillende statussen. | De klant navigeert naar de bestellingenpagina en filtert op 'CANCELLED'. | Alleen bestellingen met de status CANCELLED worden getoond. | e2e |
| AC-007-1 | REQ-007 | Een klant heeft een bestelling met status PENDING. | De klant klikt op de 'Annuleer bestelling' knop voor deze bestelling. | De bestelling wordt geannuleerd en de status wordt bijgewerkt naar CANCELLED. | e2e |
| AC-007-2 | REQ-007 | Een klant heeft een bestelling met status CONFIRMED. | De klant probeert de bestelling te annuleren. | De 'Annuleer bestelling' knop is niet zichtbaar of niet klikbaar, en de bestelling blijft CONFIRMED. | e2e |
| AC-008-1 | REQ-008 | De 'Bestellen' knop is zichtbaar op de checkout pagina. | De pagina wordt geladen. | De 'Bestellen' knop heeft een breedte van minimaal 200px. | e2e |
| AC-009-1 | REQ-009 | Een klant heeft alle benodigde informatie ingevuld voor een bestelling. | De klant klikt op de 'Bestellen' knop. | De 'Bestellen' knop toont een loading indicator (bv. spinner) en is niet klikbaar totdat de bestelling is geplaatst of een fout optreedt. | e2e |
| AC-010-1 | REQ-010 | Een klant probeert een bestelling te plaatsen met een ongeldig huisnummer. | De klant klikt op 'Bestellen'. | Er wordt een veldspecifieke foutmelding getoond onder het huisnummer veld, bv. 'Ongeldig huisnummer formaat'. | e2e |
| AC-010-2 | REQ-010 | Een klant probeert een bestelling te plaatsen met een lege stad. | De klant klikt op 'Bestellen'. | Er wordt een veldspecifieke foutmelding getoond onder het stad veld, bv. 'Stad is verplicht'. | e2e |
| AC-011-1 | REQ-011 | Er treedt een onverwachte server error op tijdens het plaatsen van een bestelling. | De API response wordt ontvangen. | De gebruiker ziet een generieke foutmelding, bv. 'Er is een onverwachte fout opgetreden. Probeer het later opnieuw.', en een correlationId wordt gelogd. | integration |
| AC-012-1 | REQ-012 | Een bestelling wordt succesvol geplaatst in het huidige jaar (bv. 2023). | Het ordernummer wordt gegenereerd. | Het ordernummer volgt het formaat ORD-2023-000001. | unit |
| AC-012-2 | REQ-012 | De eerste bestelling van het volgende jaar (bv. 2024) wordt geplaatst. | Het ordernummer wordt gegenereerd. | Het ordernummer volgt het formaat ORD-2024-000001. | unit |
| AC-013-1 | REQ-013 | Een klant plaatst een nieuwe bestelling. | De bestelling wordt aangemaakt in het systeem. | De status van de nieuw aangemaakte bestelling is PENDING. | integration |
| AC-014-1 | REQ-014 | Een klant heeft een bestelling geplaatst 20 minuten geleden en de status is PENDING. | De klant probeert de bestelling te annuleren. | De bestelling wordt succesvol geannuleerd. | e2e |
| AC-014-2 | REQ-014 | Een klant heeft een bestelling geplaatst 40 minuten geleden en de status is PENDING. | De klant probeert de bestelling te annuleren. | De bestelling kan niet geannuleerd worden omdat de tijdslimiet van 30 minuten is verstreken. | e2e |
| AC-015-1 | REQ-015 | Een klant voegt producten toe aan de winkelwagen met een totale waarde van €6,00. | De klant probeert de bestelling te plaatsen. | De bestelling wordt succesvol geplaatst. | e2e |
| AC-015-2 | REQ-015 | Een klant voegt producten toe aan de winkelwagen met een totale waarde van €4,50. | De klant probeert de bestelling te plaatsen. | Er wordt een foutmelding getoond dat de minimale bestelwaarde van €5,00 niet is bereikt en de bestelling wordt niet geplaatst. | e2e |
| AC-016-1 | REQ-016 | Een klant voegt 15 verschillende producten toe aan de winkelwagen. | De klant probeert de bestelling te plaatsen. | De bestelling wordt succesvol geplaatst. | e2e |
| AC-016-2 | REQ-016 | Een klant voegt 21 verschillende producten toe aan de winkelwagen. | De klant probeert de bestelling te plaatsen. | Er wordt een foutmelding getoond dat het maximaal aantal verschillende producten (20) is overschreden en de bestelling wordt niet geplaatst. | e2e |
| AC-017-1 | REQ-017 | Een product heeft een beperkte voorraad van 5 stuks. | Een klant voegt 3 stuks van dit product toe aan de winkelwagen en probeert de bestelling te plaatsen. | De bestelling wordt succesvol geplaatst en de voorraad wordt verminderd met 3. | integration |
| AC-017-2 | REQ-017 | Een product heeft een beperkte voorraad van 5 stuks. | Een klant voegt 6 stuks van dit product toe aan de winkelwagen en probeert de bestelling te plaatsen. | Er wordt een foutmelding getoond dat het product niet op voorraad is in de gevraagde hoeveelheid en de bestelling wordt niet geplaatst. | integration |
| AC-018-1 | REQ-018 | Een klant plaatst een bestelling met een gemiddelde complexiteit. | De API request voor het plaatsen van de bestelling wordt uitgevoerd. | De response time van de API is p95 < 500ms. | integration |
| AC-019-1 | REQ-019 | Een klant heeft meerdere bestellingen. | De API request voor het ophalen van de bestellingenlijst wordt uitgevoerd. | De response time van de API is p95 < 200ms. | integration |
| AC-020-1 | REQ-020 | Een klant plaatst een bestelling. | De bestellingstransactie wordt verwerkt door de server. | De logboeken bevatten een uniek correlationId voor deze specifieke order transactie. | integration |
| AC-021-1 | REQ-021 | Een bestelling is geplaatst en verwerkt. | De data van de bestelling wordt opgevraagd na 7 jaar. | De bestelgegevens zijn nog steeds beschikbaar in het systeem. | integration |


## 13. Traceability Matrix

| REQ | Backend | Frontend | Tests |
|-----|---------|----------|-------|
| REQ-001 | OrderController, OrderService, CreateOrderRequestDto | CheckoutPage, ProductSelectionStep, AddressSelectionStep, PaymentMethodSelectionStep | Test het plaatsen van een bestelling met een geldige lijst van producten, leveringsadres en betaalmethode.; Test het plaatsen van een bestelling met een lege lijst van producten.; Test het plaatsen van een bestelling zonder leveringsadres.; Test het plaatsen van een bestelling zonder betaalmethode. |
| REQ-002 | CreateOrderRequestDto, OrderItemDto | ProductSelectionStep, OrderSummaryStep | Valideer dat elk product in de bestelling de juiste productId, naam, aantal en eenheidsprijs bevat.; Test met een product met een negatief aantal.; Test met een product met een negatieve eenheidsprijs. |
| REQ-003 | CreateOrderRequestDto, AddressEntity, AddressService, OrderValidationService | AddressSelectionStep | Test het invoeren van een volledig en geldig leveringsadres.; Test het invoeren van een leveringsadres met ontbrekende verplichte velden (straat, huisnummer, postcode, stad, land).; Test met ongeldige postcode formaten. |
| REQ-004 | CreateOrderRequestDto, PaymentMethodEnum, OrderValidationService | PaymentMethodSelectionStep | Test het selecteren van elke geldige betaalmethode (CREDIT_CARD, BANK_TRANSFER, IDEAL).; Test het plaatsen van een bestelling zonder een betaalmethode te selecteren.; Test met een ongeldige betaalmethode. |
| REQ-005 | OrderController, OrderService, OrderResponseDto | CheckoutPage, OrderSummaryStep | Valideer dat na succesvol plaatsen van een bestelling, de response een orderNumber en status PENDING bevat.; Controleer de structuur van de orderNumber en de initiële status. |
| REQ-006 | OrderController, OrderService, PaginatedOrderResponseDto | OrderHistoryPage, OrderList, OrderStatusBadge | Test het ophalen van bestellingen en filteren op PENDING.; Test het ophalen van bestellingen en filteren op CONFIRMED.; Test het ophalen van bestellingen en filteren op SHIPPED.; Test het ophalen van bestellingen en filteren op DELIVERED.; Test het ophalen van bestellingen en filteren op CANCELLED.; Test het ophalen van bestellingen zonder filter (alle statussen).; Test met een ongeldige status filter. |
| REQ-007 | OrderController, OrderService, OrderCancellationService | OrderHistoryPage, CancelOrderButton | Test het annuleren van een bestelling met status PENDING.; Test het proberen te annuleren van een bestelling met een andere status dan PENDING. |
| REQ-008 |  | OrderButton | Controleer visueel of de 'Order' knop minimaal 200px breed is op verschillende schermresoluties. |
| REQ-009 |  | OrderButton, LoadingSpinner | Test of de 'Order' knop een loading state toont tijdens het plaatsen van de bestelling.; Controleer of de loading state verdwijnt na succesvolle of mislukte order plaatsing. |
| REQ-010 | ApiExceptionHandler, OrderValidationService | FormErrorDisplay | Test het indienen van een formulier met ongeldige invoer (bijv. ongeldig adres, ongeldig aantal producten) en controleer of veldspecifieke foutmeldingen worden getoond.; Test verschillende validatiefouten op verschillende velden. |
| REQ-011 | ApiExceptionHandler, ApiError |  | Simuleer een server error tijdens het plaatsen van een bestelling en controleer of een generieke foutmelding met een correlationId wordt geretourneerd.; Controleer de structuur van de foutmelding. |
| REQ-012 | OrderNumberGenerator, OrderRepository, OrderEntity |  | Valideer dat gegenereerde orderNumbers uniek zijn en het formaat ORD-YYYY-000001 volgen.; Test de sequentie van orderNumbers binnen een jaar.; Test de overgang naar een nieuw jaar en de reset van de sequentie. |
| REQ-013 | OrderService, OrderEntity, OrderStatusEnum |  | Controleer dat de status van een nieuw aangemaakte bestelling altijd PENDING is. |
| REQ-014 | OrderCancellationService, OrderCancellationTimeExpiredException | CancelOrderButton | Test het annuleren van een bestelling binnen 30 minuten na het plaatsen.; Test het proberen te annuleren van een bestelling na 30 minuten na het plaatsen.; Test het annuleren van een bestelling die al langer dan 30 minuten geleden is geplaatst. |
| REQ-015 | OrderValidationService | OrderSummaryStep | Test het plaatsen van een bestelling met een totale waarde van precies €5,00.; Test het plaatsen van een bestelling met een totale waarde van minder dan €5,00.; Test het plaatsen van een bestelling met een totale waarde van meer dan €5,00. |
| REQ-016 | OrderValidationService | ProductSelectionStep, OrderSummaryStep | Test het toevoegen van maximaal 20 verschillende producten aan een bestelling.; Test het proberen toe te voegen van het 21e verschillende product aan een bestelling. |
| REQ-017 | StockService, OrderService |  | Test dat voorraadcontrole plaatsvindt bij het definitief plaatsen van de bestelling, niet bij het toevoegen aan de winkelwagen.; Simuleer een situatie waarbij een product niet op voorraad is en controleer de foutmelding bij het plaatsen van de bestelling. |
| REQ-018 | OrderController, OrderService |  | Meet de response time van de API voor het plaatsen van een bestelling en valideer dat p95 < 500ms is onder normale belasting.; Voer performance tests uit om de p95 response time te valideren. |
| REQ-019 | OrderController, OrderService, PaginatedOrderResponseDto |  | Meet de response time van de API voor het ophalen van de bestellingenlijst en valideer dat p95 < 200ms is onder normale belasting.; Voer performance tests uit om de p95 response time te valideren. |
| REQ-020 | CorrelationIdFilter, LoggingAspect, OrderService |  | Controleer de logs voor order transacties en valideer dat elke log entry een correlationId bevat.; Traceer een order transactie door de logs om de aanwezigheid van correlationId te verifiëren. |
| REQ-021 | OrderRepository, OrderPersistenceService |  | Valideer dat order data na 7 jaar nog steeds beschikbaar is in de database.; Simuleer het verstrijken van 7 jaar en controleer de data retentie. |

