# Feature-003: Ticket dashboard

## Doel
Als supportmedewerker wil ik een overzichtsdashboard zien bij het inloggen zodat ik in één oogopslag de status van alle tickets kan beoordelen.

## Scope

In scope:
- Dashboard pagina tonen na inloggen
- Aantal open tickets tonen
- Aantal tickets per prioriteit (LOW, MEDIUM, HIGH) tonen
- Aantal tickets aangemaakt vandaag tonen
- Lijst van de 5 meest recente open tickets tonen
- Automatisch verversen elke 30 seconden

Out of scope:
- Ticket aanmaken vanuit het dashboard
- Grafieken of statistieken over meerdere periodes
- Exporteren van dashboarddata
- Realtime updates via websockets

## Requirements
- REQ-001: Het dashboard toont het totaal aantal open tickets.
- REQ-002: Het dashboard toont het aantal tickets per prioriteit (LOW, MEDIUM, HIGH).
- REQ-003: Het dashboard toont het aantal tickets aangemaakt op de huidige dag.
- REQ-004: Het dashboard toont de 5 meest recente open tickets met ticketnummer, subject en prioriteit.
- REQ-005: Het dashboard ververst automatisch elke 30 seconden zonder pagina reload.
- REQ-006: Bij het laden van het dashboard wordt een laadstatus getoond.
- REQ-007: Als de API niet bereikbaar is, wordt een foutmelding getoond met de mogelijkheid om handmatig te verversen.
- REQ-008: Als er geen open tickets zijn, wordt een lege staat getoond met een positieve boodschap.

## Non-functional
- NFR-001: Het dashboard moet binnen 2 seconden volledig geladen zijn.
- NFR-002: De automatische refresh mag de gebruiker niet onderbreken bij het bekijken van de pagina.

## UX notes
- `/dashboard` route: hoofdpagina na inloggen
- Componenten:
  - `DashboardPage` — hoofdcontainer, beheert polling logica
  - `StatCard` — toont één statistiek (label + waarde)
  - `RecentTicketsList` — toont de 5 meest recente tickets
  - `RecentTicketRow` — toont één ticket in de lijst
  - `LoadingSpinner` — laadanimatie tijdens ophalen
  - `ErrorBanner` — foutmelding met retry knop
  - `EmptyState` — lege staat als er geen open tickets zijn
- States: loading, success, error, empty

## Bestaande API
- Endpoint: GET /api/support-tickets — haalt lijst van support tickets op (filter/sortering via query params)
- Endpoint: GET /api/support-tickets?status=OPEN&sortBy=createdAt&sortOrder=DESC — voor recente open tickets
