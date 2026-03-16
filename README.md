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

- **Backend:** Java 17, Spring Boot 3, Maven  
- **Frontend:** React 18 (TypeScript)  
- **API:** REST, JSON, OpenAPI  
- **Testing:** JUnit 5, Spring Boot Test, Flow-based testing  
- **CI/CD:** GitHub Actions, SonarQube Cloud, Jira, Jenkins  
- **Version control:** GitHub, Dependabot  
- **AI tooling:** LLMs, agent-based workflows, prompt-engineering  

---

## 📂 Repository-doel

Deze repository fungeert als **test- en demo-omgeving** waarin alle AI-ondersteunde stappen van de SDLC:

- geïntegreerd  
- gedemonstreerd  
- reproduceerbaar  
- traceerbaar  

zijn geïmplementeerd.

AI-output moet **bestaande architectuur, patronen en coding standards respecteren**.

---

## 🗺️ Milestones

### 🧩 Milestone 1 – Basis & infrastructuur

**Doel:**  
Een stabiele basis creëren om AI-ondersteuning te demonstreren binnen een gecontroleerde omgeving.

**Opgeleverd:**
- Test repository (zelf opgezet of aangeleverd)
- Basis projectstructuur (backend + frontend)
- Vastgelegde conventies en standaarden
- Eerste AI-instructiesets (prompts / workflows)

---

### 🔍 Milestone 2 – Analyse-ondersteuning

**Doel:**  
AI inzetten om analysewerk te versnellen en te structureren.

**Functionaliteiten:**
- Inlezen van een **Functionele Analyse (FA)**
- Automatisch genereren van een **Technische Analyse (TA)**
- Toevoegen van extra context (bv. map met eerdere analyses)
- Genereren van **functionele testscenario’s**
  - Flow-based testing

**Focus:**
- Consistente structuur
- Traceerbaarheid FA → TA → tests
- Expliciete open vragen en assumpties

---

### 🧠 Milestone 3 – Codegeneratie

**Doel:**  
AI inzetten om van analyse naar **werkende code** te gaan.

**Functionaliteiten:**
- Verwerken van Technische Analyse
- Genereren van:
  - Spring Boot backend code
  - React frontend code
- Respecteren van:
  - architectuurlagen
  - naming conventions
  - validatie- en error-handling patterns

---

### 🧪 Milestone 4 – Testautomatisatie

**Doel:**  
Automatisch kwaliteitscontrole toevoegen via AI.

**Functionaliteiten:**
- Genereren van testen op basis van:
  - Functionele Analyse
  - Technische Analyse
  - Gegenereerde code
- Ondersteuning voor:
  - unit tests
  - integratietests
  - flow-based functionele tests

---

### 🚀 Milestone 5 – DevOps & dependency management

**Doel:**  
AI integreren in het build- en deliveryproces.

**Functionaliteiten:**
- Configuratie van **Dependabot**, **SonarQube Cloud** en **Jira** in combinatie met AI
- Onderzoek naar:
  - GitHub Actions
  - Workflows
  - Dependabot
  - SonarQube Cloud
  - Jira
  - Jenkins
- Automatische:
  - build
  - error fixing
  - issue assignment
  - packaging
  - (optioneel) deployment

---

### 🤖 Milestone 6 – Agentic AI (extra)

**Doel:**  
Een volledig geautomatiseerde SDLC-flow aantonen.

**Implementatie:**
- Keten van agentic bots die automatisch uitvoeren:

**Focus:**
- End-to-end automatisatie
- Beperk menselijke interventie
- Duidelijke logging en controlepunten

---

## 🚫 Niet in scope

- Volledige organisatorische implementatie van AI binnen het bedrijf
- Change management of HR-impact
- Juridische of compliance-uitwerking op bedrijfsniveau

---

## 📄 Enablementplan

Naast de technische implementatie wordt een **enablementplan (± 1 A4)** opgeleverd met:

- Aanbevelingen voor AI-adoptie
- Mogelijke risico’s
- Governance en kwaliteitsbewaking
- Tooling-keuzes en randvoorwaarden

Dit document dient als **adviesdocument** voor toekomstige adoptie.

---

## ▶️ How to run

### 🔧 Vereisten

- Java 17+
- Node.js 18+
- Maven
- Git
- (Optioneel) Docker

### 🖥️ Backend starten

```bash
cd backend
mvn clean spring-boot:run
http://localhost:9090
```

### 🖥️ Frontend starten

```
cd frontend
npm install
npm run dev
http://localhost:5173
```

---

## 🥽 Tests creëren

### 🖥️ Backend
```
node ai/testgen/generate-backend-tests.mjs feature-001-support-ticket
```

### 🖥️ Frontend
```
stgen/generate-frontend-tests.mjs feature-001-support-ticket

```

---

## 🧪 Tests uitvoeren

### 🖥️ Backend
```
cd backend
mvn test
```

### 🖥️ Frontend
```
cd frontend
npm test
```

### 🔬 Volledige testen

#### Flow only testing
```
./ai/flow.sh feature-001-support-ticket
```

#### Flow + TA matrix testing
```
BACKEND_MATRIX=1 ./ai/flow.sh feature-001-support-ticket
```

---

## 🚀 CI/CD & DevOps

Dit project maakt gebruik van een hybride CI/CD-aanpak waarbij **GitHub Actions**, **Jenkins** en **AI-ondersteuning** gecombineerd worden om het volledige Software Development Life Cycle (SDLC) proces te automatiseren.

### Continuous Integration (CI) – GitHub Actions

Voor continue integratie wordt **GitHub Actions** gebruikt.  
Bij elke `push` of `pull request` naar de `main` branch wordt automatisch de volledige AI-gedreven flow uitgevoerd:

1. Synchronisatie van Functional Analysis (FA) naar:
   - Technical Analysis (TA)
   - Flow test scenario’s
   - Test context
2. JSON-validatie van TA en Flow via schema’s
3. Automatische generatie van backend- en frontendtests
4. Uitvoeren van:
   - Backend tests (Maven)
   - Frontend tests (Jest)

Deze flow wordt uitgevoerd via één centraal script:

```
./ai/flow.sh <feature-id>
```

---

## ✅ Verwachte resultaten
	•	Werkende end-to-end AI-flow
	•	Volledige traceerbaarheid:
	•	requirement → analyse → code → tests
	•	Herbruikbare instructiesets
	•	Praktische demo’s
	•	Onderbouwd enablementplan

⸻

## 👤 Auteur

- Stageopdracht – AI in de SDLC
- Student: Owen Nolis
- Opleiding: Toegepaste Informatica / AP Hogeschool
- Periode: 02/02/26 - 29/05/26
