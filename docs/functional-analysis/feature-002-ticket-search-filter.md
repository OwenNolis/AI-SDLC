# Feature-002: Support tickets zoeken en filteren

## Doel
Als supportmedewerker wil ik support tickets kunnen zoeken en filteren zodat ik sneller relevante tickets kan terugvinden en opvolgen.

## Scope

In scope:
- Overzicht van support tickets tonen
- Zoeken op ticketnummer en subject
- Filteren op status
- Filteren op priority
- Sorteren op aanmaakdatum
- Lege resultaten correct tonen

Out of scope:
- Ticket aanmaken
- Ticket bewerken of sluiten
- Paginatie
- Export van resultaten
- Geavanceerde full-text search

## Requirements
- REQ-001: De gebruiker kan een lijst van support tickets opvragen.
- REQ-002: De gebruiker kan zoeken op ticketnummer of subject.
- REQ-003: De gebruiker kan filteren op status.
- REQ-004: De gebruiker kan filteren op priority.
- REQ-005: De gebruiker kan sorteren op createdAt in oplopende of aflopende volgorde.
- REQ-006: Indien geen tickets gevonden worden, krijgt de gebruiker een duidelijke lege status te zien.
- REQ-007: Ongeldige filterwaarden geven een valide foutmelding terug.

## Business rules
- BR-001: Enkel geldige statussen OPEN en CLOSED zijn toegestaan als filter.
- BR-002: Enkel geldige prioriteiten LOW, MEDIUM en HIGH zijn toegestaan als filter.
- BR-003: De standaard sortering is createdAt dalend.
- BR-004: Zoeken op ticketnummer is een exacte match, zoeken op subject is een gedeeltelijke match.
- BR-005: Combinaties van zoekterm en filters moeten samen toegepast worden.

## Non-functional
- NFR-001: De responstijd voor het ophalen van tickets blijft p95 < 300 ms bij normale datasetgrootte.
- NFR-002: Foutantwoorden volgen het standaard error formaat met correlationId.
- NFR-003: De oplossing moet uitbreidbaar zijn naar paginatie in een latere fase.

## UX notes
- Toon een zoekveld boven de resultatenlijst.
- Gebruik filters voor status en priority als dropdowns.
- Gebruik een sorteerkeuze voor nieuwste/eerst of oudste/eerst.
- Toon een duidelijke melding wanneer geen resultaten gevonden zijn.
- Behoud de actieve zoek- en filterwaarden in de UI.
