# AI-SDLC Documentation

This document covers all six features of the AI-SDLC pipeline: what each feature does, which files are involved, how to use it, and how to set it up in a new organisation repository.

---

## Table of Contents

1. [Architecture overview](#1-architecture-overview)
2. [LangGraph / AI Analysis Support](#2-langgraph--ai-analysis-support-fa-→-ta-→-tests)
3. [SonarQube AI Fix Flow + AI Code Fixes](#3-sonarqube-ai-fix-flow--ai-code-fixes)
4. [PR Validation](#4-pr-validation)
5. [Dependabot PR + Jira Issues](#5-dependabot-pr--jira-issues)
6. [Docker Deployment & Packages](#6-docker-deployment--packages)
7. [Parent / Child Repository Pattern](#7-parentchild-repository-pattern)
8. [Transferring to a new organisation](#8-transferring-to-a-new-organisation)
9. [Complete secrets & variables reference](#9-complete-secrets--variables-reference)

---

## 1. Architecture overview

`AI-SDLC` is the **parent repository**. It contains all reusable workflow logic, AI scripts, prompts and tooling. Application repos (child repos, e.g. `AI-SDLC-Child`) contain only their own source code, a Functional Analysis (FA) document, a `sonar-project.properties` file, a `docker-compose.yml` and five thin caller-workflows that delegate to the parent.

Every push or PR in a child repo triggers the full AI pipeline via GitHub's reusable workflow mechanism:

```
Child push/PR
  └─► ci.yml (caller)
        └─► AI-SDLC/ci.yml (parent)
              ├─ AI-SDLC flow  (FA → TA → tests → build)
              ├─ SonarCloud scan + quality gate
              ├─ Docker build & push → GHCR
              └─ Docker Compose deploy
  └─► ai-code-fixes.yml  (triggered when CI fails)
  └─► dependabot-jira.yml  (triggered on Dependabot PRs)
  └─► dependabot-automation.yml  (triggered on Dependabot PRs)
```

All AI calls use the **Google Gemini API**. Feature validation additionally uses the `FEATURE_VALIDATION_MODEL` variable (defaults to `gemini-2.5-flash`).

---

## 2. LangGraph / AI Analysis Support (FA → TA → Tests)

### What it does

When a Functional Analysis (FA) markdown file exists at `docs/functional-analysis/<feature-id>.md`, the pipeline automatically:

1. Reads the FA and generates/updates the **Technical Analysis** (`docs/technical-analysis/<feature-id>.ta.json` and `.md`).
2. Generates/updates **flow test scenarios** (`docs/test-scenarios/<feature-id>.flow.json` and `.md`).
3. Generates/updates **test context** (`docs/test-context/<feature-id>.md`).
4. Generates **backend integration tests** (`backend/src/test/java/…/<Feature>GeneratedIT.java`).
5. Generates **frontend component tests** (`frontend/src/ui/__generated__/<Feature>.test.tsx`).
6. Validates all JSON outputs against their schemas.
7. Runs the generated tests to verify correctness.

### Files involved

| File | Purpose |
|------|---------|
| `ai/flow.sh` | Orchestrator — runs all five steps in sequence |
| `ai/sync-from-fa.sh` | Wrapper that calls the Node.js agent |
| `ai/agent/sync-from-fa.mjs` | Gemini-powered agent: reads FA → writes TA JSON, flow JSON, test context |
| `ai/fa-to-ta.sh` | Shell wrapper for the FA→TA step |
| `ai/generate-tests.sh` | Runs backend and frontend test generators |
| `ai/testgen/generate-backend-tests.mjs` | Generates Java `TestRestTemplate` integration tests from the TA |
| `ai/testgen/generate-frontend-tests.mjs` | Generates React Testing Library tests from the TA |
| `ai/testgen/generate-flow-json.mjs` | Generates flow test scenarios from the FA |
| `ai/testgen/utils.mjs` | Shared utilities for all test generators |
| `ai/validator/validate.mjs` | AJV JSON schema validator for TA and flow JSON |
| `ai/schemas/ta.schema.json` | JSON schema for the TA |
| `ai/schemas/flowtests.schema.json` | JSON schema for flow test scenarios |
| `ai/prompts/fa_to_ta.md` | System prompt for FA → TA generation |
| `ai/prompts/ta_to_code_backend.md` | System prompt for backend test generation |
| `ai/prompts/ta_to_code_frontend.md` | System prompt for frontend test generation |
| `ai/prompts/generate_flow_tests.md` | System prompt for flow test generation |
| `ai/prompts/_system.md` | Shared system context injected into all prompts |
| `ai/agent/package.json` | Node.js dependencies for the Gemini agent |
| `.github/workflows/ci.yml` | Triggers this entire flow on every push/PR |

### How it works step by step

```
docs/functional-analysis/<feature-id>.md  (written by developer)
  └─► sync-from-fa.mjs (Gemini)
        ├─► docs/technical-analysis/<feature-id>.ta.json
        ├─► docs/test-scenarios/<feature-id>.flow.json
        └─► docs/test-context/<feature-id>.md
  └─► generate-tests.sh
        ├─► generate-backend-tests.mjs  →  backend/src/test/java/…/GeneratedIT.java
        └─► generate-frontend-tests.mjs →  frontend/src/ui/__generated__/….test.tsx
  └─► mvn test  +  npm test
```

The agent uses `GEMINI_API_KEY` and `GEMINI_MODEL` (defaults to `gemini-2.5-flash-lite`). All outputs are uploaded as workflow artifacts (`ai-sdlc-artifacts`).

### How to use

**In the parent repo**, the flow runs automatically on every `push` or `pull_request`. To run locally:

```bash
export GEMINI_API_KEY="your-key"
export FEATURE_ID="feature-001-support-ticket"
./ai/flow.sh feature-001-support-ticket
```

**In a child repo**, no action is needed — the `ci.yml` caller workflow triggers the parent `ci.yml`, which runs the flow automatically. The child only needs:
- `docs/functional-analysis/<feature-id>.md` — the FA you write.
- `vars.FEATURE_ID` set in the repository variables.

### Writing an FA

Create `docs/functional-analysis/<feature-id>.md`. Minimum required sections:

```markdown
# Feature-001: My Feature

## Requirements
- REQ-001: ...

## Business Rules
- BR-001: ...

## Data
- Entity: MyEntity, fields: id, name, ...

## API notes
- POST /api/my-endpoint
- GET /api/my-endpoint/{id}

## UX notes
- Route: /my-page — description ...
```

---

## 3. SonarQube AI Fix Flow + AI Code Fixes

### What it does

This is a three-layer automated quality loop:

1. **CI layer** (`ci.yml`): builds, tests, scans with SonarCloud. Fails if the Quality Gate fails or HIGH/MEDIUM issues are open.
2. **AI Code Fixes** (`ai-code-fixes.yml`): triggered automatically when CI fails. Sends errors and source code to Gemini, applies the generated fixes, re-scans with SonarCloud, and opens a PR.
3. **Coverage boost**: if coverage is below 80 % or the Quality Gate fails on new code, Gemini generates extra tests until the threshold is met.

### Files involved

| File | Purpose |
|------|---------|
| `.github/workflows/ci.yml` | Main CI: build, test, SonarCloud scan, Docker build/push, deploy |
| `.github/workflows/sonarcloud.yml` | Reusable SonarCloud analysis workflow (called by `ci.yml`) |
| `.github/workflows/ai-code-fixes.yml` | AI fix workflow: error detection → Gemini → PR |
| `.github/scripts/ai-fix-utils.sh` | Shell library: `fetch-sonar-issues`, `apply-ai-fixes`, `boost-coverage`, `get-coverage`, `review-pr` |
| `.github/scripts/sonar-pr-comment.sh` | Posts a detailed SonarQube analysis comment on a PR (uses Gemini for issue explanations) |
| `.github/scripts/sonar-rule-details.sh` | Fetches the rule description for a given SonarQube rule key |
| `.github/ai-fix-config.env` | Configuration for the fix workflow (max attempts, labels, feature flags) |
| `sonar-project.properties` | SonarQube project configuration (sources, tests, coverage paths) |
| `backend/pom.xml` | JaCoCo plugin configured here for Java coverage |

### How it works

```
push / PR → ci.yml
  ├─ mvn verify  (backend build + JaCoCo coverage)
  ├─ jest --coverage  (frontend coverage → lcov.info)
  ├─ SonarCloud scan
  ├─ Wait for SonarCloud processing
  ├─ Post Sonar summary as PR comment  (on PRs)
  └─ Quality Gate check
       ├─ PASS → Docker build & push, deploy
       └─ FAIL → ai-code-fixes.yml triggered

ai-code-fixes.yml
  ├─ Run SDLC flow + detect errors
  ├─ SonarCloud scan
  ├─ fetch-sonar-issues → sonar_issues.json
  ├─ apply-ai-fixes error_analysis.md  (Gemini generates fixes)
  ├─ boost-coverage 80  (Gemini generates tests if needed)
  ├─ Re-scan SonarCloud
  ├─ Open PR with fixes + per-issue explanation
  └─ AI self-review of the fix PR
```

### Quality Gate configuration

Quality Gate fails the CI when:
- The SonarCloud Quality Gate status is not `OK`.
- There are any open `HIGH` or `MEDIUM` severity issues.

Coverage is checked against **80 %**. New/modified files without coverage also trigger the boost.

### How to use

Everything runs automatically. To manually trigger AI fixes:

```bash
# Via GitHub CLI
gh workflow run "AI Code Fixes" --repo <owner>/<repo>
```

Or: **Actions → AI Code Fixes → Run workflow**.

To manually trigger a SonarCloud rescan go to **Actions → CI → Run workflow**.

### sonar-project.properties

Each repo (parent and child) needs this file in its root:

```properties
sonar.organization=your-org
sonar.projectKey=your-org_your-repo
sonar.projectName=Your Project

sonar.sources=backend/src/main/java,frontend/src
sonar.tests=backend/src/test/java,frontend/src
sonar.test.inclusions=frontend/src/**/*.test.ts,frontend/src/**/*.test.tsx

sonar.java.binaries=backend/target/classes
sonar.java.libraries=backend/target/dependency/**/*.jar
sonar.java.source=21

sonar.coverage.jacoco.xmlReportPaths=backend/target/site/jacoco/jacoco.xml
sonar.typescript.lcov.reportPaths=frontend/coverage/lcov.info
sonar.javascript.lcov.reportPaths=frontend/coverage/lcov.info
sonar.sourceEncoding=UTF-8

sonar.exclusions=**/node_modules/**,**/target/**,**/dist/**,**/__generated__/**
```

Set `SONAR_PROJECT_KEY` and `SONAR_ORGANIZATION` as **GitHub Variables** — the workflows override the static values in this file at runtime.

### JaCoCo in pom.xml

Add this to `backend/pom.xml` under `<build><plugins>`:

```xml
<plugin>
  <groupId>org.jacoco</groupId>
  <artifactId>jacoco-maven-plugin</artifactId>
  <version>0.8.14</version>
  <executions>
    <execution>
      <goals><goal>prepare-agent</goal></goals>
    </execution>
    <execution>
      <id>report</id>
      <phase>verify</phase>
      <goals><goal>report</goal></goals>
    </execution>
  </executions>
</plugin>
```

### Jest coverage in package.json

Add to `frontend/package.json`:

```json
"jest": {
  "coverageDirectory": "coverage",
  "coverageReporters": ["lcov", "text", "text-summary"],
  "collectCoverageFrom": [
    "src/**/*.ts",
    "src/**/*.tsx",
    "!src/**/*.test.*",
    "!src/setupTests.ts"
  ]
}
```

---

## 4. PR Validation

### What it does

Triggered by a PR comment starting with `/validate-feature-`. The workflow reads the FA and TA documents for that feature, collects the code context from the PR, and sends everything to Gemini. The result is a structured validation report posted as a PR comment with a **PASS / PARTIAL / FAIL** verdict.

### Files involved

| File | Purpose |
|------|---------|
| `.github/workflows/feature-validation.yml` | The complete validation workflow |
| `docs/functional-analysis/<feature-id>.md` | FA used as source of requirements |
| `docs/technical-analysis/<feature-id>.md` | TA used as source of technical design |
| `vars.FEATURE_VALIDATION_MODEL` | GitHub Variable: the Gemini model to use (defaults to `gemini-2.5-flash`) |

### How to use

On any open Pull Request, post a comment:

```
/validate-feature-011
```

This validates only the files changed in the PR. For a broader check:

```
/validate-feature-011 --full
```

`--full` mode includes the first 150 lines of every Java and TypeScript source file in the repo. Use this for a global implementation overview; use the default (changed-only) for targeted review.

The workflow reacts with a 👍 reaction and posts the report within 1–2 minutes.

### Report sections

The report always contains:

| Section | Source | Checked |
|---------|--------|---------|
| Requirements (REQ-xxx) | FA | Implemented / missing / partial |
| Business Rules (BR-xxx) | FA | Enforced / missing / partial |
| Acceptance Criteria (AC-xxx) | FA | Covered / not covered / partial |
| NFR (NFR-xxx) | FA | Addressed / missing / partial |
| API Contracts | TA | Endpoints, methods, status codes |
| Domain Model & Database | TA | Entities, fields, relations |
| Backend Design | TA | Controller/service/repository structure |
| Frontend Design | TA | Components, routes |
| Summary table | FA + TA | Counts per category |
| **Verdict** | FA + TA + code | **PASS / PARTIAL / FAIL** |

### Where to put FA and TA files

```
docs/
  functional-analysis/
    feature-011-preworkout-website.md   ← file must start with feature-<id>
  technical-analysis/
    feature-011-preworkout-website.md   ← same pattern
```

The workflow searches with a wildcard (`feature-<id>*.md`), so suffixes in the filename are allowed.

### Error messages

| Message in PR comment | Cause |
|-----------------------|-------|
| `FA not found for feature-xxx` | No file matching `feature-xxx*.md` in `docs/functional-analysis/` |
| `TA not found for feature-xxx` | Same for `docs/technical-analysis/` |
| `Gemini API call failed (HTTP 429)` | Quota exceeded — retry the command |

---

## 5. Dependabot PR + Jira Issues

### What it does

Two workflows work together on every Dependabot PR:

**`dependabot-jira.yml`** — Jira integration:
- When a Dependabot PR is opened, creates a Jira Task with the PR title, body and link.
- Posts the Jira issue key as a PR comment (`🎫 Jira task created: [ABC-123](...)`).
- When the PR is closed/merged, finds that comment and transitions the Jira issue to `Closed` or `Done`.
- Idempotent: if a Jira issue already exists for the PR, creation is skipped.

**`dependabot-automation.yml`** — AI review and auto-merge:
- Fetches the PR diff summary.
- Sends PR title, body and diff to Gemini for a risk assessment.
- Posts the review as a PR comment with a `APPROVE` or `HOLD` decision.
- If the decision is `APPROVE`, approves the PR.
- Auto-merges only when: decision = `APPROVE` **AND** all CI checks are green **AND** update type = `semver-patch`.
- MINOR and MAJOR updates always get `HOLD`; they need manual review.

**Fallback** when Gemini API is unavailable: `semver-patch` → `APPROVE`, everything else → `HOLD`.

### Files involved

| File | Purpose |
|------|---------|
| `.github/workflows/dependabot-jira.yml` | Jira issue creation and closure |
| `.github/workflows/dependabot-automation.yml` | Gemini AI review and auto-merge |
| `.github/dependabot.yml` | Dependabot configuration (ecosystems, schedule, grouping) |

### Dependabot configuration

The parent repo monitors:
- `maven` in `/backend` — daily, MINOR+PATCH grouped, MAJOR ignored.
- `npm` in `/frontend` — daily 08:00 CET, MINOR+PATCH grouped, MAJOR ignored.
- `npm` in `/ai/validator` — daily 08:00 CET, MINOR+PATCH grouped.
- `github-actions` in `/` — daily 08:00 CET, MAJOR ignored.

Child repos should copy `docs/child-repo-templates/dependabot.yml` and remove the `/ai/validator` section if that directory does not exist.

### Auto-merge decision table

| Update type | AI decision | CI green | Result |
|-------------|-------------|----------|--------|
| PATCH | APPROVE | Yes | Auto-merged |
| PATCH | APPROVE | No | Approved, waits for CI |
| PATCH | HOLD | — | HOLD comment, no merge |
| MINOR | HOLD (fallback) | — | Manual review required |
| MAJOR | Ignored by Dependabot | — | No PR created |

### How to test manually

Go to **Actions → Dependabot → Jira → Run workflow** and fill in:

| Input | Example |
|-------|---------|
| `pr_title` | `build(deps): bump axios from 1.6.0 to 1.7.4 in /frontend` |
| `pr_url` | `https://github.com/your-org/your-repo/pull/42` |
| `pr_number` | `42` |
| `pr_body` | `Bumps axios from 1.6.0 to 1.7.4` |
| `close_pr_number` | Leave empty for create; enter PR number to close |

---

## 6. Docker Deployment & Packages

### What it does

On every push to `main`, after all tests and the Quality Gate pass, the CI pipeline:

1. Packages the Spring Boot backend as a JAR (`mvn package -DskipTests`).
2. Builds the React frontend for production (`npm run build`).
3. Logs in to GitHub Container Registry (GHCR) using `GITHUB_TOKEN`.
4. Builds and pushes the **backend Docker image** tagged `:latest` and `:<git-sha>`.
5. Builds and pushes the **frontend Docker image** tagged `:latest` and `:<git-sha>`.
6. Runs `docker compose pull && docker compose up -d` to deploy.

### Files involved

| File | Purpose |
|------|---------|
| `backend/Dockerfile` | Builds the Spring Boot JAR into an Alpine JRE image (port 9090) |
| `frontend/Dockerfile` | Copies the Vite build into an nginx-alpine image (port 80) |
| `frontend/nginx.conf` | nginx configuration for SPA routing |
| `docker-compose.yml` | Defines backend + frontend services, reads image names from env vars |
| `.github/workflows/ci.yml` | Contains the Docker build/push and deploy steps |

### Docker images

Images are pushed to:

```
ghcr.io/<DOCKER_OWNER>/<DOCKER_IMAGE_BACKEND>:latest
ghcr.io/<DOCKER_OWNER>/<DOCKER_IMAGE_BACKEND>:<git-sha>
ghcr.io/<DOCKER_OWNER>/<DOCKER_IMAGE_FRONTEND>:latest
ghcr.io/<DOCKER_OWNER>/<DOCKER_IMAGE_FRONTEND>:<git-sha>
```

The `<git-sha>` tag allows rolling back to a specific commit.

### docker-compose.yml

```yaml
services:
  backend:
    image: ${DOCKER_IMAGE_BACKEND:-ghcr.io/your-org/your-backend:latest}
    ports:
      - "9090:9090"
    restart: unless-stopped

  frontend:
    image: ${DOCKER_IMAGE_FRONTEND:-ghcr.io/your-org/your-frontend:latest}
    ports:
      - "80:80"
    depends_on:
      - backend
    restart: unless-stopped
```

The CI workflow injects `DOCKER_IMAGE_BACKEND` and `DOCKER_IMAGE_FRONTEND` as environment variables pointing to the exact git-SHA tagged images before running `docker compose up -d`.

### Required GitHub Variables

| Variable | Example |
|----------|---------|
| `DOCKER_OWNER` | `your-org` |
| `DOCKER_IMAGE_BACKEND` | `my-project-backend` |
| `DOCKER_IMAGE_FRONTEND` | `my-project-frontend` |

No Docker Hub credentials are needed. Authentication uses the automatic `GITHUB_TOKEN`.

---

## 7. Parent/Child Repository Pattern

### Concept

`AI-SDLC` is the **parent** (this repo). It owns all workflow logic, scripts and AI tooling. A **child** repo (e.g. `AI-SDLC-Child`) contains only application code and five thin caller workflows that delegate entirely to the parent.

This means:
- A child repo has **zero pipeline logic** to maintain.
- Improvements to the parent automatically apply to all child repos on the next CI run.
- Setting up a new project takes under 30 minutes.

### What a child repo contains

```
.github/
  workflows/
    ci.yml                      ← 15-line caller
    ai-code-fixes.yml           ← 15-line caller
    dependabot-jira.yml         ← 15-line caller
    dependabot-automation.yml   ← 15-line caller
  dependabot.yml
backend/
  src/
  pom.xml                       ← must include JaCoCo plugin
  Dockerfile
frontend/
  src/
  package.json                  ← must configure Jest coverage
  Dockerfile
  nginx.conf
docs/
  functional-analysis/
    feature-001-my-feature.md   ← written by developer
docker-compose.yml
sonar-project.properties
```

### Caller workflow example

Every caller in the child repo looks like this (exactly):

```yaml
# .github/workflows/ci.yml
name: CI
on:
  pull_request:
  push:
    branches: ["main"]

jobs:
  ci:
    uses: OwenNolis/AI-SDLC/.github/workflows/ci.yml@main
    secrets: inherit
    with:
      FEATURE_ID: ${{ vars.FEATURE_ID }}
      GEMINI_MODEL: ${{ vars.GEMINI_MODEL }}
      SONAR_PROJECT_KEY: ${{ vars.SONAR_PROJECT_KEY }}
      SONAR_ORGANIZATION: ${{ vars.SONAR_ORGANIZATION }}
      DOCKER_OWNER: ${{ vars.DOCKER_OWNER }}
      DOCKER_IMAGE_BACKEND: ${{ vars.DOCKER_IMAGE_BACKEND }}
      DOCKER_IMAGE_FRONTEND: ${{ vars.DOCKER_IMAGE_FRONTEND }}
```

Use `docs/child-repo-templates/` in the parent repo to copy all five template workflows.

### How the parent scripts reach the child

Inside the parent `ci.yml`, a sparse checkout retrieves the AI scripts at runtime:

```yaml
- name: Checkout AI-SDLC tools
  uses: actions/checkout@v6
  with:
    repository: OwenNolis/AI-SDLC
    path: .sdlc-tools
    sparse-checkout: |
      .github/scripts
      .github/ai-fix-config.env
      ai
```

The scripts land in `.sdlc-tools/` in the child's workspace. They are never stored in the child repo itself.

### What is NEVER duplicated in a child repo

- Workflow logic (`ci.yml`, `ai-code-fixes.yml`, `sonarcloud.yml`, etc. — the full versions).
- AI scripts (`ai/flow.sh`, `ai/generate-tests.sh`, `.github/scripts/ai-fix-utils.sh`, …).
- Prompts, schemas, testgen scripts.
- The LangGraph agent.

---

## 8. Transferring to a new organisation

### Prerequisites

| Requirement | Detail |
|-------------|--------|
| GitHub repository | With Actions enabled |
| SonarCloud account | Free at sonarcloud.io; create a project and generate an analysis token |
| Google AI Studio account | Create a Gemini API key at aistudio.google.com |
| Jira Cloud account | For the Dependabot + Jira feature; create or use an existing project |
| Java 21 Spring Boot backend | With Maven and the JaCoCo plugin |
| React/TypeScript frontend | With Jest configured for lcov coverage |

### Step 1 — Copy the five caller workflows

From `docs/child-repo-templates/` in the parent repo, copy to `.github/workflows/` in your new repo:

```
ci.yml
ai-code-fixes.yml
dependabot-jira.yml
dependabot-automation.yml
sonarcloud.yml     ← optional, only if you want a standalone SonarCloud workflow
```

Also copy `.github/dependabot.yml` from `docs/child-repo-templates/dependabot.yml`. Remove the `/ai/validator` entry if your repo does not have that directory.

### Step 2 — Create sonar-project.properties

In the root of your repo (copy and adapt):

```properties
sonar.organization=your-org
sonar.projectKey=your-org_your-repo
sonar.projectName=Your Project

sonar.sources=backend/src/main/java,frontend/src
sonar.tests=backend/src/test/java,frontend/src
sonar.test.inclusions=frontend/src/**/*.test.ts,frontend/src/**/*.test.tsx

sonar.java.binaries=backend/target/classes
sonar.java.libraries=backend/target/dependency/**/*.jar
sonar.java.source=21

sonar.coverage.jacoco.xmlReportPaths=backend/target/site/jacoco/jacoco.xml
sonar.typescript.lcov.reportPaths=frontend/coverage/lcov.info
sonar.javascript.lcov.reportPaths=frontend/coverage/lcov.info
sonar.sourceEncoding=UTF-8
sonar.exclusions=**/node_modules/**,**/target/**,**/dist/**,**/__generated__/**
```

### Step 3 — Create docker-compose.yml

```yaml
services:
  backend:
    image: ${DOCKER_IMAGE_BACKEND:-ghcr.io/your-org/your-backend:latest}
    ports:
      - "9090:9090"
    restart: unless-stopped

  frontend:
    image: ${DOCKER_IMAGE_FRONTEND:-ghcr.io/your-org/your-frontend:latest}
    ports:
      - "80:80"
    depends_on:
      - backend
    restart: unless-stopped
```

### Step 4 — Add JaCoCo to backend pom.xml

Under `<build><plugins>` in `backend/pom.xml`:

```xml
<plugin>
  <groupId>org.jacoco</groupId>
  <artifactId>jacoco-maven-plugin</artifactId>
  <version>0.8.14</version>
  <executions>
    <execution>
      <goals><goal>prepare-agent</goal></goals>
    </execution>
    <execution>
      <id>report</id>
      <phase>verify</phase>
      <goals><goal>report</goal></goals>
    </execution>
  </executions>
</plugin>
```

### Step 5 — Configure Jest coverage in frontend/package.json

```json
"jest": {
  "coverageDirectory": "coverage",
  "coverageReporters": ["lcov", "text", "text-summary"],
  "collectCoverageFrom": [
    "src/**/*.ts",
    "src/**/*.tsx",
    "!src/**/*.test.*",
    "!src/setupTests.ts"
  ]
}
```

### Step 6 — Create a SonarCloud project

1. Go to sonarcloud.io → **My projects → Analyse new project**.
2. Import your GitHub repo.
3. Note the **organisation slug** and **project key**.
4. Generate a token: **Account → Security → Generate Token**.
5. Add it as the `SONAR_TOKEN` secret in your repo.

### Step 7 — Set GitHub Secrets

Go to **Settings → Secrets and variables → Actions → Secrets**:

| Secret | Value |
|--------|-------|
| `GEMINI_API_KEY` | Gemini API key from Google AI Studio |
| `SONAR_TOKEN` | SonarCloud analysis token |
| `JIRA_EMAIL` | Email address of the Jira account |
| `JIRA_API_TOKEN` | Jira API token (Atlassian Account → Security → API Tokens) |

### Step 8 — Set GitHub Variables

Go to **Settings → Secrets and variables → Actions → Variables**:

| Variable | Example | Used by |
|----------|---------|---------|
| `FEATURE_ID` | `feature-001-my-feature` | CI, AI Code Fixes |
| `GEMINI_MODEL` | `gemini-2.5-flash-lite` | CI, AI Code Fixes, Dependabot |
| `FEATURE_VALIDATION_MODEL` | `gemini-2.5-flash` | PR Validation |
| `SONAR_PROJECT_KEY` | `your-org_your-repo` | CI, AI Code Fixes |
| `SONAR_ORGANIZATION` | `your-org` | CI, AI Code Fixes |
| `JIRA_PROJECT_KEY` | `PROJ` | Dependabot → Jira |
| `JIRA_DOMAIN` | `yourcompany.atlassian.net` | Dependabot → Jira |
| `DOCKER_OWNER` | `your-org` | CI (Docker) |
| `DOCKER_IMAGE_BACKEND` | `my-project-backend` | CI (Docker) |
| `DOCKER_IMAGE_FRONTEND` | `my-project-frontend` | CI (Docker) |

### Step 9 — Set Actions permissions

**Settings → Actions → General → Workflow permissions**:
- Select **Read and write permissions**.
- Enable **Allow GitHub Actions to create and approve pull requests**.

### Step 10 — Write the first FA and push

```bash
mkdir -p docs/functional-analysis
# Write your FA (see section 2 for the format)
nano docs/functional-analysis/feature-001-my-feature.md

git add .
git commit -m "feat: initial setup with feature-001"
git push
```

The CI workflow starts automatically. Check the Actions tab.

### Setup checklist

| # | Task | Notes |
|---|------|-------|
| 1 | Copy 5 caller workflows to `.github/workflows/` | From `docs/child-repo-templates/` |
| 2 | Copy `dependabot.yml` to `.github/` | Remove `/ai/validator` if not needed |
| 3 | Create `sonar-project.properties` | Adapt org, project key |
| 4 | Create `docker-compose.yml` | Adapt image names |
| 5 | Add JaCoCo to `backend/pom.xml` | Required for coverage |
| 6 | Configure Jest coverage in `frontend/package.json` | Required for coverage |
| 7 | Create SonarCloud project, generate token | sonarcloud.io |
| 8 | Set GitHub Secrets (4 secrets) | See Step 7 |
| 9 | Set GitHub Variables (10 variables) | See Step 8 |
| 10 | Set Actions permissions (read/write + PR creation) | Settings → Actions |
| 11 | Write first FA and push | Triggers the full pipeline |

---

## 9. Complete secrets & variables reference

### GitHub Secrets

| Secret | Required by | Description |
|--------|------------|-------------|
| `GEMINI_API_KEY` | CI, AI Code Fixes, Dependabot automation, PR Validation | Google AI Studio API key. Get at aistudio.google.com |
| `SONAR_TOKEN` | CI, SonarCloud, AI Code Fixes | SonarCloud analysis token. Generate in SonarCloud under Account → Security → Generate Token |
| `JIRA_EMAIL` | Dependabot → Jira | Email address of the Jira account used for the API |
| `JIRA_API_TOKEN` | Dependabot → Jira | Jira API token. Generate at id.atlassian.com → Security → API Tokens |
| `GITHUB_TOKEN` | All workflows | Automatically provided by GitHub Actions. No configuration needed |

### GitHub Variables

| Variable | Required by | Default | Description |
|----------|------------|---------|-------------|
| `FEATURE_ID` | CI, AI Code Fixes | — | Active feature identifier, e.g. `feature-001-support-ticket`. Must match a file in `docs/functional-analysis/` |
| `GEMINI_MODEL` | CI, AI Code Fixes, Dependabot automation | `gemini-2.5-flash-lite` | Gemini model for the SDLC flow and Dependabot review |
| `FEATURE_VALIDATION_MODEL` | PR Validation | `gemini-2.5-flash` | Gemini model used for PR feature validation (separate from `GEMINI_MODEL` to avoid conflicts) |
| `SONAR_PROJECT_KEY` | CI, SonarCloud, AI Code Fixes | — | SonarCloud project key, e.g. `MyOrg_my-repo`. Overrides the static value in `sonar-project.properties` |
| `SONAR_ORGANIZATION` | CI, SonarCloud, AI Code Fixes | — | SonarCloud organisation slug, e.g. `myorg` |
| `JIRA_PROJECT_KEY` | Dependabot → Jira | — | Jira project key, e.g. `PROJ` |
| `JIRA_DOMAIN` | Dependabot → Jira | — | Jira Cloud domain without `https://`, e.g. `yourcompany.atlassian.net` |
| `DOCKER_OWNER` | CI (Docker) | — | GitHub user or org for GHCR, e.g. `your-org` |
| `DOCKER_IMAGE_BACKEND` | CI (Docker) | — | Docker image name for the backend, e.g. `my-project-backend` |
| `DOCKER_IMAGE_FRONTEND` | CI (Docker) | — | Docker image name for the frontend, e.g. `my-project-frontend` |

### GitHub PATs

No custom PATs are needed for child repos. All authentication uses the automatically provided `GITHUB_TOKEN`.

In the **parent repo** (`AI-SDLC`), the `GITHUB_TOKEN` has read/write permissions on contents and pull-requests, which is sufficient for creating PRs, posting comments, and pushing Docker images to GHCR.

> **Note:** The `GITHUB_TOKEN` is scoped to the repository in which the workflow runs. For the parent repo workflows executing in the context of a child repo (via `workflow_call`), the child repo's `GITHUB_TOKEN` is used automatically through `secrets: inherit`.
