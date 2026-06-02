# Stageopdracht – AI in de SDLC  
**Van functionele analyse tot release (end-to-end)**

## 📌 Projectdoel

Dit project maakt deel uit van een **12-weekse stageopdracht** en onderzoekt hoe **Artificiële Intelligentie (AI)** concreet ingezet kan worden om het **volledige Software Development Life Cycle (SDLC)** te ondersteunen.

Het doel is het ontwikkelen van een **werkende end-to-end AI-gebaseerde oplossing** die developers ondersteunt bij:

- Functionele analyse  
- Technische analyse  
- Codegeneratie  
- Code-review  
- Testautomatisatie  
- DevOps & dependency management  

De focus ligt op **praktische toepasbaarheid**, **traceerbaarheid** en **kwaliteit**, binnen een realistische enterprise-context.

---

## 🧱 Technologie-stack

Deze opdracht sluit aan bij de standaard stack binnen de organisatie:

- **Backend:** Java 21, Spring Boot 4, Maven  
- **Frontend:** React 19 (TypeScript), Vite  
- **API:** REST, JSON  
- **Testing:** JUnit 5, Spring Boot Test, Jest, React Testing Library, flow-based testing  
- **CI/CD:** GitHub Actions, SonarQube Cloud, Jira, Jenkins  
- **Version control:** GitHub, Dependabot  
- **AI tooling:** Google Gemini API, LangGraph-stijl agenten, prompt-engineering, schema-validatie  
- **Containerisatie:** Docker, Docker Compose, GHCR  

---

## 📂 Repository-structuur

```
AI-SDLC/                         ← parent repository (bevat alle CI-logica en AI-tooling)
├── .github/
│   ├── workflows/               ← alle GitHub Actions workflows
│   │   ├── ci.yml               ← centrale CI: AI-flow, build, test, Sonar, Docker, deploy
│   │   ├── sonarcloud.yml       ← herbruikbare SonarCloud analyse workflow
│   │   ├── ai-code-fixes.yml    ← AI-gedreven foutherstel bij CI-falen
│   │   ├── feature-validation.yml ← PR-validatie via /validate-feature-xxx commentaar
│   │   ├── dependabot-automation.yml ← Gemini AI review + auto-merge van Dependabot PRs
│   │   └── dependabot-jira.yml  ← automatisch Jira-tickets aanmaken/sluiten voor Dependabot PRs
│   ├── scripts/
│   │   ├── ai-fix-utils.sh      ← shell library: Sonar-issues ophalen, AI-fixes toepassen, coverage boosten
│   │   ├── sonar-pr-comment.sh  ← plaatst gedetailleerd Sonar-rapport als PR-commentaar
│   │   └── sonar-rule-details.sh ← haalt regelomschrijving op uit SonarCloud API
│   ├── ai-fix-config.env        ← configuratie voor de AI-fix workflow (labels, drempelwaarden, …)
│   └── dependabot.yml           ← Dependabot configuratie (ecosystemen, schema's, groeperingen)
├── ai/
│   ├── flow.sh                  ← hoofd-orchestrator: voert alle SDLC-stappen sequentieel uit
│   ├── sync-from-fa.sh          ← wrapper die de Gemini-agent aanroept
│   ├── generate-tests.sh        ← genereert backend- en frontendtests uit TA + flow JSON
│   ├── fa-to-ta.sh              ← aparte wrapper voor de FA→TA stap
│   ├── agent/
│   │   └── sync-from-fa.mjs     ← Gemini-agent: FA → TA JSON + flow JSON + test context
│   ├── testgen/
│   │   ├── generate-backend-tests.mjs   ← Java TestRestTemplate integratietests genereren
│   │   ├── generate-frontend-tests.mjs  ← React Testing Library tests genereren
│   │   ├── generate-flow-json.mjs       ← flow testscenario's genereren vanuit FA
│   │   └── utils.mjs                    ← gedeelde hulpfuncties voor alle testgeneratoren
│   ├── validator/
│   │   └── validate.mjs         ← AJV JSON-schema validatie van TA en flow JSON
│   ├── prompts/                 ← systeemprompts voor alle Gemini-aanroepen
│   ├── schemas/
│   │   ├── ta.schema.json       ← JSON-schema voor de Technische Analyse
│   │   └── flowtests.schema.json ← JSON-schema voor flow testscenario's
│   └── rules/                   ← per-feature business-rules voor validatie
├── backend/                     ← Spring Boot applicatie
├── frontend/                    ← React/TypeScript applicatie
├── docs/
│   ├── functional-analysis/     ← FA markdown-bestanden per feature
│   ├── technical-analysis/      ← AI-gegenereerde TA markdown + JSON per feature
│   ├── test-scenarios/          ← AI-gegenereerde flow testscenario's per feature
│   ├── test-context/            ← AI-gegenereerde testcontext per feature
│   └── child-repo-templates/    ← kant-en-klare caller-workflows voor child repos
├── docker-compose.yml
├── sonar-project.properties
└── Documentation.md             ← volledige feature- en transferdocumentatie
```

Deze repository fungeert tegelijk als **parent repository** in een parent-child patroon: alle workflow-logica, AI-scripts en tooling leven hier centraal. Child-repositories (bv. `AI-SDLC-Child`) bevatten enkel applicatiecode en vijf dunne caller-workflows die delegeren naar deze parent.

---

## 🗺️ Milestones

### 🧩 Milestone 1 – Basis & infrastructuur

**Doel:**  
Een stabiele basis creëren om AI-ondersteuning te demonstreren binnen een gecontroleerde omgeving.

**Opgeleverd:**
- Projectstructuur met backend (Spring Boot) en frontend (React/TypeScript)
- Vastgelegde conventies en standaarden
- Eerste AI-instructiesets (prompts en workflows)
- GitHub Actions basisopzet

---

### 🔍 Milestone 2 – Analyse-ondersteuning

**Doel:**  
AI inzetten om analysewerk te versnellen en te structureren.

**Wat het doet:**

Een developer schrijft een **Functionele Analyse** als markdown-bestand in `docs/functional-analysis/<feature-id>.md`. Zodra dit bestand bestaat, verwerkt de Gemini-agent (`ai/agent/sync-from-fa.mjs`) het volledig automatisch:

- De FA wordt gelezen en omgezet naar een gestructureerde **Technische Analyse** (`docs/technical-analysis/<feature-id>.ta.json`).
- Tegelijk worden **flow testscenario's** gegenereerd (`docs/test-scenarios/<feature-id>.flow.json`) die beschrijven welke stappen een gebruiker doorloopt en welke responses worden verwacht.
- Een **testcontext** (`docs/test-context/<feature-id>.md`) wordt aangemaakt met aanvullende achtergrond voor de testgeneratoren.
- Alle JSON-output wordt gevalideerd tegen stricte JSON-schema's (`ai/schemas/`) via AJV.

De agent gebruikt temperatuur 0 voor deterministische, consistente output en past bestaande documenten incrementeel bij zonder bestaande requirements te verwijderen.

**Traceerbaarheid:** `FA.md → ta.json + flow.json + context.md → tests`

---

### 🧠 Milestone 3 – Codegeneratie

**Doel:**  
AI inzetten om van analyse naar **werkende code** te gaan.

**Wat het doet:**

Vanuit de gegenereerde TA en flow testscenario's genereert de pipeline automatisch:

- **Backend integratietests** (`backend/src/test/java/…/<Feature>GeneratedIT.java`) — volwaardige Spring Boot `TestRestTemplate`-tests die de REST-endpoints van de feature valideren. De tests respecteren de bestaande controller/service/repository-architectuur en de naming conventions van het project.
- **Frontend component-tests** (`frontend/src/ui/__generated__/<Feature>.test.tsx`) — React Testing Library-tests die de UI-componenten van de feature testen op basis van de TA en flow-scenario's.

De gegenereerde code is geen tijdelijke scaffold maar directe, uitvoerbare testcode die onmiddellijk door `mvn test` en `npm test` wordt opgepikt.

---

### 🧪 Milestone 4 – Testautomatisatie

**Doel:**  
Automatisch kwaliteitscontrole toevoegen via AI.

**Wat het doet:**

De testautomatisatie werkt op twee niveaus:

**Generatie (bij elke CI-run):**
- `ai/generate-tests.sh` genereert backend- en frontendtests vanuit de actuele TA en flow JSON.
- Tests worden gegenereerd op basis van de requirements (REQ), business rules (BR), acceptance criteria (AC) en API-contracts uit de TA.

**Coverage-bewaking (AI Code Fixes workflow):**
- Na elke SonarCloud-scan controleert de pipeline of de code coverage boven 80 % ligt.
- Als de coverage te laag is, of als de SonarCloud Quality Gate faalt op nieuwe code, vraagt de pipeline Gemini om extra tests te genereren voor de niet-gedekte code.
- De pipeline herhaalt de SonarCloud-scan na het toevoegen van de extra tests.

**Flow-based testing:**  
De `flow.json`-bestanden beschrijven gebruikersstromen als een reeks stappen met HTTP-methode, endpoint, request-body en verwachte response. Deze worden gebruikt als basis voor zowel de backend integratietests als de frontend component-tests.

---

### 🚀 Milestone 5 – DevOps & dependency management

**Doel:**  
AI integreren in het build- en deliveryproces.

**Wat het doet:**

**SonarQube AI Fix Flow:**  
Na elke push of PR voert de CI-pipeline een volledige SonarCloud-analyse uit. Als de Quality Gate faalt of als er open `HIGH`/`MEDIUM`-issues zijn, triggert de `ai-code-fixes.yml` workflow automatisch:
- Foutlogs en broncode worden naar Gemini gestuurd.
- Gemini analyseert de issues en genereert concrete code-fixes.
- De fixes worden toegepast op `backend/src/` en `frontend/src/`.
- Er wordt automatisch een Pull Request geopend met een gedetailleerde beschrijving per issue (inclusief de SonarQube-regelomschrijving, wat er fout was en hoe het is opgelost).
- Gemini doet een self-review van het gegenereerde PR.

**Dependabot + AI + Jira:**  
- Dependabot detecteert dagelijks verouderde afhankelijkheden (Maven, npm, GitHub Actions) en opent automatisch PRs.
- Bij elke Dependabot-PR maakt de `dependabot-jira.yml` workflow automatisch een Jira Task aan met de PR-titel, body en link. Wanneer de PR wordt gemerged of gesloten, wordt het Jira-ticket automatisch gesloten.
- De `dependabot-automation.yml` workflow laat Gemini een risico-analyse uitvoeren op de dependency-update. Het resultaat is een gestructureerde review met risicoclassificatie (`LOW`/`MEDIUM`/`HIGH`), een inschatting van breaking changes en een aanbeveling: `APPROVE` of `HOLD`.
- `semver-patch`-updates met groene CI en een `APPROVE`-beslissing worden **automatisch gemerged**. `MINOR`- en `MAJOR`-updates krijgen altijd een `HOLD` en vereisen handmatige review.

**Docker & deployment:**  
Bij elke push naar `main` na een geslaagde Quality Gate bouwt en publiceert de CI-pipeline automatisch Docker-images naar GHCR (GitHub Container Registry) — getagd met `:latest` en `:<git-sha>` — en deployt via `docker compose up -d`.

---

### 🤖 Milestone 6 – Agentic AI (extra)

**Doel:**  
Een volledig geautomatiseerde SDLC-flow aantonen.

**Wat het doet:**

De volledige pipeline is een keten van gespecialiseerde AI-agents die elk een afgebakende SDLC-stap uitvoeren:

| Stap | Agent / Tool | Output |
|------|-------------|--------|
| FA inlezen | `sync-from-fa.mjs` (Gemini) | TA JSON, flow JSON, testcontext |
| Schema-validatie | `validate.mjs` (AJV) | Validatierapport |
| Testgeneratie | `generate-backend-tests.mjs` / `generate-frontend-tests.mjs` (Gemini) | Java IT-tests, TSX tests |
| Build & test | Maven + Jest | Testresultaten, coverage |
| Kwaliteitsanalyse | SonarCloud + `sonar-pr-comment.sh` (Gemini) | PR-commentaar met uitleg |
| AI-foutherstel | `ai-fix-utils.sh` (Gemini) | Code-fixes, fix-PR |
| PR-validatie | `feature-validation.yml` (Gemini) | PASS/PARTIAL/FAIL rapport |
| Dependency review | `dependabot-automation.yml` (Gemini) | APPROVE/HOLD beslissing |
| Jira-koppeling | `dependabot-jira.yml` (Jira REST API) | Jira Task aanmaak/sluiting |
| Docker & deploy | Docker, GHCR, Compose | Draaiende containers |

De enige twee handmatige stappen in de volledige flow zijn: (1) een FA schrijven en pushen, en (2) een gegenereerde fix-PR reviewen en mergen. Alles daartussenin is volledig geautomatiseerd.

---

## 🔍 Feature-uitleg

### Feature 1 — LangGraph / Analyse-ondersteuning (FA → TA → Tests)

De kern van het systeem. Een Functionele Analyse-bestand triggert een Gemini-agent die de volledige documentatieketen opbouwt en testcode genereert. De agent gebruikt structurele JSON-schema's om te garanderen dat de output consistent en machineleesbaar is. Bestaande documenten worden incrementeel bijgewerkt zonder bestaande requirements te verwijderen.

De traceerbaarheid loopt van `FA.md` → `ta.json` → `flow.json` → `context.md` → `GeneratedIT.java` → `*.test.tsx`.

---

### Feature 2 — SonarQube AI Fix Flow + AI Code Fixes

Een drielaagse kwaliteitsloop die volledig zonder menselijke interventie draait:

1. **CI-laag:** bouwt, test en scant met SonarCloud. Blokkeert bij `HIGH`/`MEDIUM`-issues of een gefaalde Quality Gate.
2. **AI Fix-laag:** analyseert de foutlogs en broncode met Gemini, past fixes toe, opent een PR met per-issue uitleg.
3. **Coverage-laag:** als de coverage onder 80 % valt of nieuwe code ongedekt is, genereert Gemini extra tests en herscant SonarCloud.

---

### Feature 3 — PR Validatie

Via een PR-commentaar (`/validate-feature-011`) valideert de workflow automatisch of de code in de PR de requirements uit de FA en TA correct implementeert. Het rapport bevat per REQ/BR/AC/NFR een status (✅/⚠️/❌) en een eindverdict `PASS`, `PARTIAL` of `FAIL`.

---

### Feature 4 — Dependabot PR + Jira Issues

Koppelt het dependency-update-proces aan Jira en AI-review. Dependabot-PRs krijgen automatisch een Jira-ticket dat na het mergen automatisch wordt gesloten. Gemini beoordeelt elke update op risico en breaking changes. Veilige patch-updates worden automatisch gemerged.

---

### Feature 5 — Docker Deployment & Packages

Na een geslaagde Quality Gate bouwt de CI-pipeline automatisch Docker-images voor backend en frontend en publiceert deze naar GHCR. Elke push naar `main` resulteert in een verse deploymentmet zowel een `:latest` als een specifiek `:git-sha` tag voor rollbacks.

---

### Feature 6 — Parent/Child Repository Patroon

Deze repository is de **parent**: alle workflow-logica, scripts en AI-tooling leven hier centraal. Child-repositories bevatten alleen applicatiecode en vijf dunne caller-workflows (elk 10-15 regels) die via GitHub's reusable workflows-mechanisme volledig delegeren aan de parent. Scripts worden bij elke run automatisch opgehaald via sparse-checkout. Updates aan de parent gelden automatisch voor alle child-repos.

---

## 🚫 Niet in scope

- Volledige organisatorische implementatie van AI binnen het bedrijf
- Change management of HR-impact
- Juridische of compliance-uitwerking op bedrijfsniveau

---

## 📄 Enablementplan

Naast de technische implementatie wordt een **enablementplan (± 1 A4)** opgeleverd met:

- Aanbevelingen voor AI-adoptie
- Mogelijke risico's
- Governance en kwaliteitsbewaking
- Tooling-keuzes en randvoorwaarden

Dit document dient als **adviesdocument** voor toekomstige adoptie.

---

## ▶️ How to run

### 🔧 Vereisten

- Java 21+
- Node.js 22+
- Maven
- Git
- (Optioneel) Docker

### 🖥️ Backend starten

```bash
cd backend
mvn clean spring-boot:run
# http://localhost:9090
```

### 🖥️ Frontend starten

```bash
cd frontend
npm install
npm run dev
# http://localhost:5173
```

---

## 🥽 Tests creëren

### 🖥️ Backend
```bash
node ai/testgen/generate-backend-tests.mjs feature-001-support-ticket
```

### 🖥️ Frontend
```bash
node ai/testgen/generate-frontend-tests.mjs feature-001-support-ticket
```

---

## 🧪 Tests uitvoeren

### 🖥️ Backend
```bash
cd backend
mvn test
```

### 🖥️ Frontend
```bash
cd frontend
npm test
```

### 🔬 Volledige SDLC-flow

#### Flow only testing
```bash
./ai/flow.sh feature-001-support-ticket
```

#### Flow + TA matrix testing
```bash
BACKEND_MATRIX=1 ./ai/flow.sh feature-001-support-ticket
```

---

## 🚀 CI/CD & DevOps

Dit project maakt gebruik van een hybride CI/CD-aanpak waarbij **GitHub Actions** en **AI-ondersteuning** gecombineerd worden om het volledige SDLC-proces te automatiseren.

### Volledige pipeline

```
push / PR → ci.yml
  ├─ AI-SDLC flow (FA → TA → tests → build → mvn test → npm test)
  ├─ SonarCloud analyse + Quality Gate
  ├─ PR-commentaar met Sonar-rapport (op PRs)
  ├─ Docker build & push → GHCR           (alleen main push)
  └─ Docker Compose deploy                (alleen main push)

CI gefaald → ai-code-fixes.yml
  ├─ Foutdetectie (compilatie, tests, Sonar-issues)
  ├─ Gemini genereert fixes
  ├─ Coverage boost indien nodig
  ├─ Herhaalde SonarCloud-scan
  └─ Fix-PR aanmaken

Dependabot PR → dependabot-automation.yml + dependabot-jira.yml
  ├─ Gemini AI review (APPROVE / HOLD)
  ├─ Auto-merge bij PATCH + CI groen + APPROVE
  ├─ Jira Task aanmaken
  └─ Jira Task sluiten na merge

PR-commentaar /validate-feature-xxx → feature-validation.yml
  └─ FA + TA + code → Gemini → PASS/PARTIAL/FAIL rapport
```

---

## ✅ Verwachte resultaten

- Werkende end-to-end AI-flow
- Volledige traceerbaarheid: requirement → analyse → code → tests
- Herbruikbare instructiesets
- Praktische demo's
- Onderbouwd enablementplan

---

## 📚 Verdere documentatie

Zie [Documentation.md](Documentation.md) voor een volledige Engelstalige feature-documentatie, inclusief transfergids en compleet secrets/variables overzicht.

---

## 👤 Auteur

- Stageopdracht – AI in de SDLC
- Student: Owen Nolis
- Opleiding: Toegepaste Informatica / AP Hogeschool
- Periode: 02/02/26 - 29/05/26
