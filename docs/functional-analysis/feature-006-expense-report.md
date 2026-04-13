# Feature-006: Onkostennota indienen

## Doel
Als medewerker wil ik onkosten kunnen indienen zodat ik terugbetaald word voor zakelijke uitgaven.

## Scope

In scope:
- Onkostennota aanmaken met datum, bedrag, categorie en beschrijving
- Bonnetje uploaden als bijlage (JPEG, PNG of PDF, max 5MB)
- Overzicht van eigen ingediende onkostennota's tonen
- Status opvolgen (ingediend, goedgekeurd, afgewezen, terugbetaald)
- Manager kan onkostennota's goedkeuren of afwijzen met commentaar
- Totaalbedrag per maand tonen voor de medewerker

Out of scope:
- Automatische terugbetaling via bank
- Integratie met boekhoudpakket
- Onkostenlimieten per categorie instellen
- Groepsonkosten voor meerdere medewerkers

## Requirements
- REQ-001: De medewerker kan een onkostennota aanmaken met datum, bedrag, categorie en beschrijving.
- REQ-002: De datum mag niet in de toekomst liggen.
- REQ-003: Het bedrag moet positief zijn en maximaal 10.000 euro.
- REQ-004: De categorie moet één van de toegestane waarden zijn: TRAVEL, MEALS, ACCOMMODATION, OFFICE_SUPPLIES, OTHER.
- REQ-005: De medewerker kan een bonnetje uploaden als bijlage (JPEG, PNG of PDF, maximaal 5MB).
- REQ-006: De medewerker kan zijn eigen onkostennota's bekijken met status en bedrag.
- REQ-007: De medewerker ziet het totaalbedrag van goedgekeurde onkosten per kalendermaand.
- REQ-008: De medewerker kan een onkostennota met status "ingediend" verwijderen.
- REQ-009: De manager kan een onkostennota goedkeuren of afwijzen met verplicht commentaar bij afwijzing.
- REQ-010: Bij ongeldige invoer worden veldspecifieke foutmeldingen getoond.

## Business rules
- BR-001: Een onkostennota kan niet gewijzigd worden na indiening.
- BR-002: Alleen de eigen medewerker en zijn directe manager mogen een onkostennota inzien.
- BR-003: Een goedgekeurde of afgewezen onkostennota kan niet meer verwijderd worden.

## Non-functional
- NFR-001: Bijlagen worden opgeslagen in object storage, niet in de database.
- NFR-002: Bedragen worden opgeslagen als gehele getallen in eurocenten om afrondingsfouten te vermijden.
- NFR-003: De API moet binnen 1 seconde reageren voor alle operaties behalve bestandsupload.

## Data
- Entiteit: ExpenseReport, velden: id: Long, employeeId: Long, date: LocalDate, amountInCents: Long, category: String, description: String, status: String, attachmentUrl: String, managerComment: String, createdAt: LocalDateTime
- Constraints: date notNull, amountInCents notNull min:1 max:1000000, category enum:TRAVEL,MEALS,ACCOMMODATION,OFFICE_SUPPLIES,OTHER, status enum:SUBMITTED,APPROVED,REJECTED,REIMBURSED

## API notes
- Endpoint: POST /api/expense-reports — nieuwe onkostennota indienen
- Endpoint: GET /api/expense-reports — eigen onkostennota's ophalen
- Endpoint: DELETE /api/expense-reports/{id} — onkostennota verwijderen
- Endpoint: POST /api/expense-reports/{id}/attachment — bijlage uploaden
- Endpoint: PUT /api/expense-reports/{id}/approve — goedkeuren (manager)
- Endpoint: PUT /api/expense-reports/{id}/reject — afwijzen met commentaar (manager)
- Endpoint: GET /api/expense-reports/monthly-total — totaal per maand ophalen

## UX notes
- `/expenses` route: overzicht van eigen onkostennota's met totaal per maand
- `/expenses/new` route: formulier voor nieuwe onkostennota
- `/manager/expenses` route: overzicht openstaande onkostennota's voor manager
- Componenten:
  - `ExpenseList` — overzicht van onkostennota's met status en bedrag
  - `ExpenseForm` — formulier voor datum, bedrag, categorie, beschrijving
  - `AttachmentUpload` — component voor uploaden van bonnetje
  - `MonthlyTotalCard` — toont totaalbedrag goedgekeurde onkosten per maand
  - `ManagerExpenseList` — lijst van openstaande onkostennota's voor manager
  - `ApproveRejectModal` — modal voor goedkeuren/afwijzen met commentaarveld
  - `StatusBadge` — visuele statusindicator
  - `ErrorDisplay` — veldspecifieke foutmeldingen
