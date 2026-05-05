# Feature-feature-011-preworkout-website: Functionele Analyse - Pre-workout Webshop

## Project

| Project | Full stack webshop voor pre-workout supplementen |
|---|---|
| Primaire actor | Klant |
| Secundaire actor | Admin, betalingsprovider |

## 2. Figma-level UI designs

![Figma-level UI designs](feature-011-preworkout-website/page-2.png)

Deze schermen tonen een realistisch visueel ontwerp voor de belangrijkste klantflows. De stijl gebruikt duidelijke productcards, sterke CTA-knoppen, filterblokken en een checkout met gescheiden invoer en order summary.

### Homepage + shop overzicht

### Productdetailpagina

### Checkout en order summary

## Sequence diagram - checkout en betaling

![Sequence diagram - checkout en betaling](feature-011-preworkout-website/page-4.png)

Deze flow toont hoe klant, frontend, API, database en betalingsprovider samenwerken bij afrekenen.

## Component diagram

![Component diagram](feature-011-preworkout-website/page-5.png)

Dit diagram toont de interactie tussen de frontend, backend API, database, admin dashboard en payment provider.

## Deployment Diagram

![Deployment Diagram](feature-011-preworkout-website/page-6.png)

Dit diagram illustreert de architectuur van de webshop, van de client browser tot de cloud VM en de database.

## Database ERD

![Database ERD](feature-011-preworkout-website/page-7.png)

Het ERD toont de relaties tussen de tabellen User, CartItem, Product, Order en Payment.

## 5. API contracten

Onderstaande contracten geven een concrete basis voor backend implementatie. Alle endpoints onder /admin vereisen de rol Admin. Klantgebonden endpoints vereisen een geldige JWT access token.

| Endpoint | Request | Response |
|---|---|---|
| GET /api/products | Query: search, flavor, minPrice, maxPrice, caffeineMin, caffeineMax, isStock, sort, page, pageSize | 200: Paged list van actieve producten met id, name, price, imageUrl, stockStatus |
| GET /api/products/{id} | Path: product id | 200: productdetail; 404: product niet gevonden |
| POST /api/auth/register | firstName, lastName, email, password | 201: user + token; 409: email bestaat al |
| POST /api/auth/login | email, password | 200: access token + user; 401: ongeldige login |
| GET /api/cart | JWT token | 200: cart items + totals |
| POST /api/cart/items | JWT token, productid, quantity | 201: cart item; 400: quantity ongeldig; 409: onvoldoende voorraad |
| PATCH /api/cart/items/{id} | Path: cart item id, quantity | 200: aangepast item; 404: item niet gevonden |
| DELETE /api/cart/items/{id} | Path: cart item id | 204: verwijderd |
| POST /api/orders | JWT token, shippingAddress, paymentMethod | 201: order pending + paymentUrl; 400: leeg mandje; 409: voorraadprobleem |
| POST /api/payments/webhook | Provider payload met transactionReference en status | 200: webhook verwerkt; update orderstatus |
| POST /api/admin/products | name, description, price, flavor, caffeineMg, servings, stock, imageUrl | 201: nieuw product; 400: validatiefout |
| PUT /api/admin/products/{id} | Volledige productupdate | 200: aangepast product; 404: product niet gevonden |
| PATCH /api/admin/orders/{id} | status | 200: aangepaste bestelling; 400: ongeldige statusovergang |

## 6. Acceptance criteria - kernflows

### REQ-001: Producten bekijken
* AC-001-1: Gegeven actieve producten bestaan, wanneer de klant de shop opent, dan ziet hij producten met naam, prijs, afbeelding en voorraadstatus.
* AC-001-2: Gegeven actieve producten bestaan, wanneer de klant de shop opent, dan verschijnt een lege staat met melding.

### REQ-002: Product toevoegen aan winkelmand
* AC-002-1: Gegeven voldoende voorraad, wanneer de klant een hoeveelheid toevoegt, dan verschijnt het product in het winkelmandje.
* AC-002-2: Gegeven de hoeveelheid groter is dan de voorraad, dan wanneer de klant toevoegt, dan wordt de actie geweigerd met melding.

### REQ-003: Bestelling plaatsen
* AC-003-1: Gegeven een gevuld winkelmandje en geldig adres, wanneer de klant bevestigt, dan wordt een order aangemaakt met status Pending.
* AC-003-2: Gegeven een leeg mandje, wanneer de klant afrekent, dan wordt checkout geblokkeerd.

### REQ-004: Online betaling
* AC-004-1: Gegeven een geldige order, wanneer betaling succesvol is, dan wordt status Paid.
* AC-004-2: Gegeven betaling mislukt, wanneer provider weigert, dan blijft order Pending of wordt Cancelled.

### REQ-005: Admin productbeheer
* AC-005-1: Gegeven adminrechten, wanneer admin product aanmaakt, dan verschijnt het in de catalogus.
* AC-005-2: Gegeven geen adminrechten, wanneer gebruiker admin endpoint aanroept, dan krijgt hij 403 Forbidden.

## 7. Non-functional requirements

* NFR-001: Responsive op desktop, tablet en mobiel.
* NFR-002: Productoverzicht laadt binnen 2 seconden bij normale belasting.
* NFR-003: Wachtwoorden worden gehasht opgeslagen.
* NFR-004: Betalingsgegevens worden niet lokaal opgeslagen; de betaalprovider verklaart of bankgegevens.
* NFR-005: API endpoints gebruiken validatie, authenticatie en autorisatie.
* NFR-006: Database bevat constraints voor unieke e-mailadressen, unieke payment references en geldige hoeveelheden.