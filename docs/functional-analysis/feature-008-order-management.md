# Feature-008: Order Management

## Doel

Als klant wil ik mijn bestellingen kunnen plaatsen, bekijken en annuleren zodat ik volledige controle heb over mijn aankopen.

## Scope

In scope:

- Bestelformulier met productoverzicht
- Bestellingen plaatsen via UI
- Bestellingenhistorie raadplegen
- Bestelling annuleren (alleen indien status PENDING)
- Order button met voldoende breedte en duidelijke feedback
- Validatie van bestelgegevens

Out of scope:

- Betalingsverwerking (externe payment gateway)
- Retouren en terugbetalingen
- Verzendtracking
- Facturatie

## Requirements

- REQ-001: Klant kan een bestelling plaatsen met: producten (lijst), leveringsadres, betaalmethode.
- REQ-002: Elk product in de bestelling heeft: productId, naam, aantal, eenheidsprijs.
- REQ-003: Leveringsadres is verplicht met velden: straat, huisnummer, postcode, stad, land.
- REQ-004: Betaalmethode is verplicht en één van: CREDIT_CARD, BANK_TRANSFER, IDEAL.
- REQ-005: Na plaatsen krijgt klant een orderNumber + status PENDING te zien.
- REQ-006: Klant kan bestellingen bekijken gefilterd op status: PENDING, CONFIRMED, SHIPPED, DELIVERED, CANCELLED.
- REQ-007: Klant kan een bestelling annuleren indien status = PENDING.
- REQ-008: Order button moet minimaal 200px breed zijn voor duidelijke zichtbaarheid.
- REQ-009: Order button toont loading state tijdens het plaatsen van de bestelling.
- REQ-010: Bij validatiefouten worden veldspecifieke foutmeldingen getoond.
- REQ-011: Server errors tonen generieke foutmelding met correlationId.

## Business rules

- BR-001: orderNumber is uniek: ORD-YYYY-000001 (sequence per jaar).
- BR-002: Status bij aanmaken is altijd PENDING.
- BR-003: Annuleren is alleen mogelijk binnen 30 minuten na plaatsen.
- BR-004: Minimale bestelwaarde is €5,00.
- BR-005: Maximaal 20 verschillende producten per bestelling.
- BR-006: Voorraadcontrole vindt plaats bij het plaatsen (niet bij toevoegen aan winkelwagen).

## Non-functional

- NFR-001: API response time p95 < 500ms voor order plaatsen.
- NFR-002: API response time p95 < 200ms voor bestellingenlijst ophalen.
- NFR-003: Logging bevat correlationId voor elke order transactie.
- NFR-004: Order data wordt minimaal 7 jaar bewaard (wettelijke verplichting).

## UX notes

- Order button: minimaal 200px breed, primaire kleur, disabled tijdens loading.
- Bestelformulier: stap-voor-stap wizard (producten → adres → betaling → bevestiging).
- Bestellingenhistorie: gesorteerd op datum aflopend, paginering per 10.
- Annuleerknop alleen zichtbaar indien status = PENDING en binnen 30 minuten.
- States per component: loading, empty, error, success.

## Bestaande API

- Endpoint: GET /api/products — ophalen van beschikbare producten
- Endpoint: GET /api/customers/{id} — klantgegevens ophalen voor pre-fill adres
