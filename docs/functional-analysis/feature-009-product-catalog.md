# Feature-009: Product Catalog Management

## Doel

Als beheerder wil ik producten kunnen aanmaken, beheren en archiveren zodat het assortiment altijd actueel is en klanten correcte productinformatie zien bij het plaatsen van bestellingen.

## Scope

In scope:

- Aanmaken van nieuwe producten met naam, beschrijving, prijs en initiële voorraad
- Bewerken van bestaande productgegevens (naam, beschrijving, prijs)
- Archiveren van producten (soft delete — product verdwijnt uit catalogus maar blijft in orderhistorie zichtbaar)
- Voorraad bijwerken via een aparte stock-aanpassing (positief = aanvulling, negatief = correctie)
- Zoeken en filteren van producten op naam, categorie en beschikbaarheid
- Paginering van de productlijst (10 per pagina, gesorteerd op naam)
- Lage-voorraadwaarschuwing: producten met stock ≤ drempelwaarde worden als "low stock" gemarkeerd
- Categorieën beheren: aanmaken, hernoemen, verwijderen (alleen als geen actieve producten gekoppeld)

Out of scope:

- Productafbeeldingen uploaden en beheren
- Prijshistorie of tijdgebonden kortingen
- Bulkimport via CSV of Excel
- Integratie met externe ERP- of voorraadsystemen
- Klantgerichte productpagina's (dit is uitsluitend een beheerscherm)

## Requirements

- REQ-001: Een beheerder kan een nieuw product aanmaken met: naam, beschrijving, prijs (€), categorie en initiële voorraad.
- REQ-002: Naam is verplicht, uniek binnen de catalogus en maximaal 200 tekens lang.
- REQ-003: Prijs is verplicht, minimaal €0,01 en maximaal €99.999,99, opgeslagen met twee decimalen.
- REQ-004: Initiële voorraad is verplicht, minimaal 0 (uitverkocht) en maximaal 999.999 stuks.
- REQ-005: Een beheerder kan naam, beschrijving, prijs en categorie van een bestaand product bewerken; de voorraad wordt uitsluitend via een stock-aanpassing gewijzigd.
- REQ-006: Een beheerder kan een product archiveren; het product verdwijnt uit het actieve assortiment maar blijft gekoppeld aan historische orderregels.
- REQ-007: Een beheerder kan de voorraad van een product aanpassen met een delta (bv. +50 of -10) en een verplichte reden (RESTOCK, CORRECTION, DAMAGE, RETURN).
- REQ-008: Een negatieve stock-aanpassing mag de voorraad niet onder 0 brengen; dit wordt afgewezen met een foutmelding.
- REQ-009: Producten met een voorraad ≤ de ingestelde drempelwaarde (standaard 10) worden in de lijst gemarkeerd als "low stock".
- REQ-010: De productlijst ondersteunt paginering (10 per pagina), sortering op naam (standaard oplopend) en filteren op naam (bevat), categorie en beschikbaarheid (actief / gearchiveerd / low stock).
- REQ-011: Een categorie kan alleen worden verwijderd als er geen actieve producten aan gekoppeld zijn.
- REQ-012: Een beheerder kan een nieuwe categorie aanmaken met een unieke naam van maximaal 100 tekens.

## Business rules

- BR-001: Productnaam moet uniek zijn binnen alle niet-gearchiveerde producten; gearchiveerde producten tellen niet mee bij de uniekheidscontrole.
- BR-002: Een gearchiveerd product kan niet meer worden bewerkt of opnieuw gearchiveerd, maar kan wel worden hersteld (unarchive) als beheerder.
- BR-003: Stock-aanpassingen worden gelogd in een audit-tabel (product_id, delta, reden, beheerder-id, tijdstip); dit log is niet verwijderbaar.
- BR-004: De lage-voorraaddrempel is configureerbaar per product (optioneel); als geen drempel is ingesteld, geldt de globale standaard van 10 stuks.
- BR-005: Prijswijzigingen hebben geen terugwerkende kracht op bestaande orders.
- BR-006: Een product kan niet worden aangemaakt met dezelfde naam als een gearchiveerd product zonder expliciete bevestiging (conflict-melding met optie om het gearchiveerde product te herstellen).

## Non-functional

- NFR-001: API response time p95 < 300ms voor het ophalen van de productlijst (inclusief filters en paginering).
- NFR-002: API response time p95 < 500ms voor aanmaken en bewerken van producten.
- NFR-003: Alle schrijfoperaties (aanmaken, bewerken, archiveren, stock-aanpassing) worden gelogd met correlationId en beheerder-id.
- NFR-004: Toegang tot alle product-endpoints is beperkt tot gebruikers met de rol ROLE_ADMIN.
- NFR-005: De productlijst ondersteunt minimaal 10.000 actieve producten zonder merkbare prestatiedegradatie.

## Data

- Entiteit: Product, velden: id: UUID, name: String, description: String, price: BigDecimal, stock: Integer, lowStockThreshold: Integer (nullable), categoryId: UUID (nullable), archivedAt: LocalDateTime (nullable), createdAt: LocalDateTime, updatedAt: LocalDateTime
- Entiteit: Category, velden: id: UUID, name: String, createdAt: LocalDateTime, updatedAt: LocalDateTime
- Entiteit: StockAdjustment, velden: id: UUID, productId: UUID, delta: Integer, reason: Enum (RESTOCK, CORRECTION, DAMAGE, RETURN), adminId: UUID, createdAt: LocalDateTime
- Constraints: Product.name max 200 tekens, uniek onder niet-gearchiveerde producten; Product.price tussen 0.01 en 99999.99; Product.stock ≥ 0; Category.name max 100 tekens, uniek

## API notes

- Endpoint: GET /api/products — pagineerde lijst met filters: ?name=, ?categoryId=, ?availability=active|archived|low_stock, ?page=0&size=10&sort=name,asc
- Endpoint: POST /api/products — nieuw product aanmaken
- Endpoint: GET /api/products/{id} — product detail ophalen
- Endpoint: PUT /api/products/{id} — product bewerken (naam, beschrijving, prijs, categorie)
- Endpoint: DELETE /api/products/{id} — product archiveren (soft delete, zet archivedAt)
- Endpoint: PATCH /api/products/{id}/stock — stock-aanpassing met delta en reden
- Endpoint: GET /api/products/{id}/stock-adjustments — auditlog van stock-aanpassingen voor een product
- Endpoint: POST /api/categories — categorie aanmaken
- Endpoint: GET /api/categories — alle categorieën ophalen
- Endpoint: PUT /api/categories/{id} — categorie hernoemen
- Endpoint: DELETE /api/categories/{id} — categorie verwijderen (alleen als geen actieve producten)
- Request POST /api/products: { name, description, price, stock, categoryId (optioneel), lowStockThreshold (optioneel) }
- Request PATCH /api/products/{id}/stock: { delta, reason }
- Response: { id, name, description, price, stock, lowStock (boolean), category, archivedAt, createdAt, updatedAt }

## Acceptance Criteria

### REQ-001: Een beheerder kan een nieuw product aanmaken

- **AC-001-1**: Gegeven een ingelogde beheerder op het productbeheerscherm, wanneer de beheerder alle verplichte velden invult (naam, prijs, initiële voorraad) en op 'Opslaan' klikt, dan wordt het product aangemaakt en verschijnt het in de productlijst met de ingevoerde gegevens.
- **AC-001-2**: Gegeven een ingelogde beheerder op het aanmaakformulier, wanneer de beheerder het formulier indient zonder een naam in te vullen, dan wordt het formulier niet verstuurd en wordt een veldspecifieke foutmelding getoond bij het naamveld.

### REQ-002: Naam is verplicht, uniek en maximaal 200 tekens

- **AC-002-1**: Gegeven een bestaand actief product met naam "Laptop Pro", wanneer een beheerder een nieuw product probeert aan te maken met dezelfde naam, dan retourneert de API HTTP 409 Conflict met een foutmelding dat de naam al in gebruik is.
- **AC-002-2**: Gegeven een beheerder die een product aanmaakt, wanneer de naam exact 200 tekens lang is, dan wordt het product succesvol aangemaakt.

### REQ-006: Product archiveren

- **AC-006-1**: Gegeven een actief product, wanneer een beheerder op 'Archiveren' klikt en de actie bevestigt, dan wordt archivedAt ingesteld op de huidige tijdstip en verdwijnt het product uit de actieve productlijst.
- **AC-006-2**: Gegeven een gearchiveerd product dat gekoppeld is aan een bestaande orderregel, wanneer de orderhistorie wordt bekeken, dan is de productnaam nog steeds zichtbaar in de orderregel.

### REQ-007: Stock-aanpassing met delta en reden

- **AC-007-1**: Gegeven een product met voorraad 50, wanneer een beheerder een stock-aanpassing van +100 met reden RESTOCK indient, dan wordt de nieuwe voorraad 150 en verschijnt de aanpassing in het stock-auditlog.
- **AC-007-2**: Gegeven een product met voorraad 5, wanneer een beheerder een stock-aanpassing van -10 indient, dan retourneert de API HTTP 400 Bad Request met een foutmelding dat de voorraad niet negatief kan worden.

### REQ-009: Lage-voorraadmarkering

- **AC-009-1**: Gegeven een product met een lage-voorraaddrempel van 10 en een huidige voorraad van 8, wanneer de productlijst wordt geladen, dan wordt het product gemarkeerd als "low stock" (lowStock: true).
- **AC-009-2**: Gegeven een product met voorraad 11 en drempel 10, wanneer de productlijst wordt geladen, dan is lowStock: false.

### REQ-011: Categorie verwijderen

- **AC-011-1**: Gegeven een categorie zonder gekoppelde actieve producten, wanneer een beheerder de categorie verwijdert, dan wordt de categorie succesvol verwijderd en verdwijnt uit de categorielijst.
- **AC-011-2**: Gegeven een categorie met minstens één actief gekoppeld product, wanneer een beheerder de categorie probeert te verwijderen, dan retourneert de API HTTP 409 Conflict met een foutmelding.

## UX notes

- /admin/products: overzichtstabel met kolommen Naam, Categorie, Prijs, Voorraad (met low-stock badge), Status (Actief/Gearchiveerd) en acties (Bewerken, Archiveren)
- /admin/products/new: aanmaakformulier met velden Naam, Beschrijving (textarea), Prijs, Categorie (dropdown), Initiële voorraad, Lage-voorraaddrempel (optioneel); inline veldvalidatie
- /admin/products/{id}/edit: bewerkformulier, identiek aan aanmaak maar zonder voorraadveld; aparte "Voorraad aanpassen" knop opent een modal
- Modal "Voorraad aanpassen": invoerveld voor delta (positief of negatief getal), dropdown voor reden, bevestigingsknop; toont huidige en nieuwe voorraad live
- Low-stock badge: oranje badge "Laag" naast het voorraadcijfer in de tabel en het detailscherm
- Gearchiveerde producten: grijs weergegeven in de tabel met badge "Gearchiveerd"; standaard verborgen achter toggle "Toon gearchiveerde producten"
- Categorieën: beheerd via /admin/categories; eenvoudige lijst met inline bewerken en verwijderknop (met bevestigingsdialoog)
- Componenten: ProductTable, ProductForm, StockAdjustmentModal, LowStockBadge, CategoryManager, ConfirmDialog, Pagination
