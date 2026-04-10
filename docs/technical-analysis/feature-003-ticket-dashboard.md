# Technische Analyse: Feature-003: Ticket dashboard

## 1. Scope

### In scope
- Dashboard pagina tonen na inloggen
- Aantal open tickets tonen
- Aantal tickets per prioriteit (LOW, MEDIUM, HIGH) tonen
- Aantal tickets aangemaakt vandaag tonen
- Lijst van de 5 meest recente open tickets tonen
- Automatisch verversen elke 30 seconden

### Out of scope
- Ticket aanmaken vanuit het dashboard
- Grafieken of statistieken over meerdere periodes
- Exporteren van dashboarddata
- Realtime updates via websockets

## 2. Assumptions
- De gebruiker is ingelogd en heeft de benodigde permissies om het dashboard te bekijken.
- De backend API's zijn beschikbaar en responsief.

## 3. Open Questions
- Wat is het exacte formaat van de data die terugkomt van de API's voor de verschillende ticketstatistieken?
- Hoe wordt omgegaan met paginering of scrollen als er meer dan 5 recente tickets zijn? (Hoewel de scope beperkt is tot 5, is het goed om dit te overwegen voor toekomstige uitbreidingen).

## 4. Domain Model

| Entiteit             | Velden                                                              | Constraints
