# Feature-005: Gebruikersprofiel beheren

## Doel
Als ingelogde gebruiker wil ik mijn profielgegevens kunnen bekijken en aanpassen zodat mijn account altijd up-to-date is.

## Scope

In scope:
- Profielpagina tonen met huidige gegevens
- Weergavenaam aanpassen
- E-mailadres aanpassen
- Wachtwoord wijzigen
- Profielfoto uploaden en verwijderen
- Validatiefeedback tonen bij ongeldige invoer

Out of scope:
- Account verwijderen
- Two-factor authenticatie instellen
- Notificatievoorkeuren beheren
- Gekoppelde externe accounts (Google, GitHub)

## Requirements
- REQ-001: De gebruiker kan zijn huidige profielgegevens (weergavenaam, e-mailadres, profielfoto) bekijken.
- REQ-002: De gebruiker kan zijn weergavenaam aanpassen (minimaal 2, maximaal 50 tekens).
- REQ-003: De gebruiker kan zijn e-mailadres aanpassen. Het nieuwe e-mailadres moet uniek zijn in het systeem.
- REQ-004: De gebruiker kan zijn wachtwoord wijzigen door het huidige wachtwoord en tweemaal het nieuwe wachtwoord in te voeren.
- REQ-005: Het nieuwe wachtwoord moet minimaal 8 tekens bevatten, met minstens één hoofdletter en één cijfer.
- REQ-006: De gebruiker kan een profielfoto uploaden (JPEG of PNG, maximaal 2MB).
- REQ-007: De gebruiker kan zijn profielfoto verwijderen, waarna een standaard avatar getoond wordt.
- REQ-008: Bij ongeldige invoer worden veldspecifieke foutmeldingen getoond.
- REQ-009: Na een succesvolle wijziging wordt een bevestigingsmelding getoond.

## Business rules
- BR-001: Een e-mailadres mag slechts aan één account gekoppeld zijn.
- BR-002: Het huidige wachtwoord moet correct zijn voordat een nieuw wachtwoord ingesteld kan worden.
- BR-003: Het nieuwe wachtwoord mag niet gelijk zijn aan het huidige wachtwoord.

## Non-functional
- NFR-001: Wachtwoorden worden opgeslagen als bcrypt hash, nooit als plaintext.
- NFR-002: Profielfoto's worden opgeslagen in object storage (S3-compatibel).
- NFR-003: De profielpagina moet binnen 1 seconde geladen zijn.

## UX notes
- `/profile` route: profielpagina met bewerkbare velden
- Componenten:
  - `ProfilePage` — hoofdcontainer voor de profielpagina
  - `ProfileForm` — formulier voor weergavenaam en e-mailadres
  - `PasswordChangeForm` — apart formulier voor wachtwoordwijziging
  - `AvatarUpload` — component voor uploaden en verwijderen van profielfoto
  - `SuccessBanner` — bevestigingsmelding na succesvolle wijziging
  - `ErrorDisplay` — veldspecifieke foutmeldingen
