"""
LangGraph FA→TA agent.

Converteert een Functionele Analyse (FA) naar een Technische Analyse (TA)
als zowel Markdown (.md) als JSON (.ta.json).

Gebruik:
  python fa_to_ta.py <feature-id> [--ta-skeleton PATH]

Argumenten:
  feature-id      ID van de feature (bv. feature-001-support-ticket)
  --ta-skeleton   Pad naar TA skelet (standaard: templates/ta_skeleton.md)

Omgevingsvariabelen:
  GEMINI_API_KEY  Verplicht
  GEMINI_MODEL    Optioneel (standaard: gemini-2.5-flash-lite)

Output:
  docs/technical-analysis/<feature-id>.md
  docs/technical-analysis/<feature-id>.ta.json
"""

import argparse
import json
import os
import sys
from pathlib import Path
from typing import TypedDict

from dotenv import load_dotenv
from jsonschema import ValidationError, validate
from langchain_core.messages import HumanMessage
from langchain_google_genai import ChatGoogleGenerativeAI
from langgraph.graph import END, START, StateGraph

# Laad .env vanuit de repo root
load_dotenv(Path(__file__).parent.parent.parent.parent / ".env")


# ── State ──────────────────────────────────────────────────────────────────────

FA_TYPES = ["rest-api", "full-stack", "frontend-only", "event-driven"]

SKELETONS_DIR = Path(__file__).parent / "templates" / "skeletons"


class TAState(TypedDict):
    # Inputs
    feature_id: str
    fa_content: str
    fa_type: str            # rest-api | full-stack | frontend-only | event-driven
    fa_type_manual: str     # handmatig opgegeven type (leeg = auto-detect)
    ta_skeleton: str
    ta_schema: dict
    # Tussenresultaten — gevuld door nodes
    requirements: list      # [{"id": "REQ-001", "text": "...", "priority": "must"}]
    scope: dict             # {"inScope": [...], "outOfScope": [...]}
    assumptions: list       # ["aanname 1", ...]
    open_questions: list    # ["vraag 1", ...]
    domain_model: dict      # {"entities": [...]}
    api_design: dict        # {"errorFormat": {...}, "endpoints": [...]}
    messaging_design: dict  # {"topics": [...], "events": [...], "dlqStrategy": "...", "retryStrategy": "..."}
    backend_design: dict    # {"modules": [...]}
    frontend_design: dict   # {"routes": [...], "components": [...]}
    tests_design: dict      # {"unit": [...], "integration": [...], "e2e": [...]}
    traceability: list      # [{"reqId": "REQ-001", "backendRefs": [...], ...}]
    # Outputs
    ta_json: dict
    ta_markdown: str
    # Beheer
    validation_errors: list
    retry_count: int


# ── LLM ───────────────────────────────────────────────────────────────────────

def get_llm() -> ChatGoogleGenerativeAI:
    api_key = os.environ.get("GEMINI_API_KEY")
    if not api_key:
        print("❌ GEMINI_API_KEY niet ingesteld", file=sys.stderr)
        sys.exit(1)
    return ChatGoogleGenerativeAI(
        model=os.environ.get("GEMINI_MODEL", "gemini-2.5-flash-lite"),
        google_api_key=api_key,
        temperature=0,
    )


def llm_json(prompt: str) -> dict:
    """Stuur een prompt naar Gemini en geef het geparseerde JSON antwoord terug."""
    response = get_llm().invoke([HumanMessage(content=prompt)])
    text = response.content

    # Verwijder markdown code blocks indien aanwezig
    if "```json" in text:
        text = text[text.find("```json") + 7 :]
        text = text[: text.rfind("```")]
    elif "```" in text:
        text = text[text.find("```") + 3 :]
        text = text[: text.rfind("```")]

    start = text.find("{")
    end = text.rfind("}") + 1
    if start == -1 or end == 0:
        raise ValueError(f"Geen JSON gevonden in LLM response:\n{text[:500]}")

    return json.loads(text[start:end])


def llm_text(prompt: str) -> str:
    """Stuur een prompt naar Gemini en geef de ruwe tekst terug."""
    response = get_llm().invoke([HumanMessage(content=prompt)])
    return response.content.strip()


# ── Nodes ──────────────────────────────────────────────────────────────────────

def classify_fa(state: TAState) -> dict:
    """
    Node 0: Bepaal het FA-type (rest-api / full-stack / frontend-only / event-driven).
    Als fa_type_manual is opgegeven wordt de LLM-call overgeslagen.
    Laadt daarna het bijpassende TA-skelet uit templates/skeletons/<type>/ta_skeleton.md.
    """
    if state.get("fa_type_manual"):
        fa_type = state["fa_type_manual"]
        print(f"🏷️  FA-type (handmatig): {fa_type}")
    else:
        print("🔎 FA-type detecteren...")
        prompt = f"""Je bent een SDLC-classificatie agent.

Lees de onderstaande Functionele Analyse en bepaal het type feature.

FA inhoud:
---
{state["fa_content"]}
---

Kies EXACT één van de volgende types:
- rest-api       : Alleen backend REST endpoints, geen of minimale frontend wijzigingen
- full-stack     : Zowel backend (REST API) als frontend (React/UI) worden gewijzigd
- frontend-only  : Alleen frontend wijzigingen, gebruikt bestaande API endpoints
- event-driven   : Asynchrone verwerking via events, queues of messaging (Kafka, RabbitMQ, etc.)

Geef ALLEEN een JSON object terug:
{{"fa_type": "rest-api"}}

Regels:
- Kies het type dat het beste past op basis van de scope en requirements
- Bij twijfel tussen rest-api en full-stack: kies full-stack als er UI-componenten worden beschreven
"""
        result = llm_json(prompt)
        fa_type = result.get("fa_type", "full-stack")
        if fa_type not in FA_TYPES:
            print(f"  ⚠️  Onbekend type '{fa_type}', fallback naar full-stack")
            fa_type = "full-stack"
        print(f"  ✅ FA-type: {fa_type}")

    # Laad het type-specifieke TA-skelet
    skeleton_path = SKELETONS_DIR / fa_type / "ta_skeleton.md"
    if skeleton_path.exists():
        ta_skeleton = skeleton_path.read_text()
        print(f"  📋 Skelet geladen: skeletons/{fa_type}/ta_skeleton.md")
    else:
        # Fallback naar generiek skelet
        ta_skeleton = state.get("ta_skeleton", "")
        print(f"  ⚠️  Geen skelet gevonden voor {fa_type}, generiek skelet gebruikt")

    return {"fa_type": fa_type, "ta_skeleton": ta_skeleton}


def parse_fa(state: TAState) -> dict:
    """
    Node 1: Parseer de FA en extraheer alle gestructureerde gegevens.
    Haalt requirements, scope, assumptions en open questions op in één call.
    """
    print("🔍 FA parseren...")

    prompt = f"""Je bent een SDLC-analyse agent.

Lees de Functionele Analyse en extraheer alle gestructureerde gegevens.

FA inhoud:
---
{state["fa_content"]}
---

Geef ALLEEN een JSON object terug:
{{
  "requirements": [
    {{"id": "REQ-001", "text": "volledig uitgeschreven requirement tekst", "priority": "must"}}
  ],
  "scope": {{
    "inScope": ["item 1", "item 2"],
    "outOfScope": ["item 1", "item 2"]
  }},
  "assumptions": [
    "aanname die niet expliciet in de FA staat maar wel noodzakelijk is"
  ],
  "openQuestions": [
    "vraag die nog beantwoord moet worden voor implementatie"
  ]
}}

Regels voor requirements:
- IDs voldoen aan patroon REQ-NNN (drie cijfers, bv. REQ-001)
- ALLE items krijgen een REQ-NNN ID: requirements, business rules (BR-xxx), én non-functionals (NFR-xxx)
  Gebruik NOOIT BR- of NFR- als prefix — alles is REQ-
- Nummering is doorlopend: REQ-001, REQ-002, REQ-003, ...
- priority is exact: must, should of could
  * must  = verplicht, kernfunctionaliteit of expliciet vereist
  * should = gewenst maar niet blokkerend
  * could  = nice-to-have
- Schrijf de tekst volledig uit — geen afkortingen

Regels voor scope:
- Haal letterlijk uit de FA scope sectie
- Vul aan met logische implicaties als iets ontbreekt

Regels voor assumptions:
- Minimaal 2, maximaal 6
- Alleen wat NIET in de FA staat maar WEL aangenomen wordt

Regels voor openQuestions:
- Laat leeg ([]) als er geen onduidelijkheden zijn
"""
    result = llm_json(prompt)
    return {
        "requirements": result.get("requirements", []),
        "scope": result.get("scope", {"inScope": [], "outOfScope": []}),
        "assumptions": result.get("openQuestions", []),
        "open_questions": result.get("openQuestions", []),
    }


def generate_domain_model(state: TAState) -> dict:
    """Node 2: Genereer domain model op basis van requirements."""
    print("🏗️  Domain model genereren...")

    prompt = f"""Je bent een SDLC-analyse agent.

Genereer een domain model voor een Java/Spring Boot applicatie.

Requirements:
{json.dumps(state["requirements"], indent=2)}

Geef ALLEEN een JSON object terug:
{{
  "entities": [
    {{
      "name": "EntityName",
      "fields": [
        {{
          "name": "fieldName",
          "type": "String|Long|Integer|Boolean|LocalDateTime|UUID",
          "constraints": ["notNull", "minLength:5", "maxLength:255"],
          "testCases": ["empty", "too_short", "too_long", "missing", "invalid_value"]
        }}
      ]
    }}
  ]
}}

Regels:
- testCases ALLEEN uit: empty, too_short, too_long, missing, invalid_value, duplicate_per_day
- Geen extra velden buiten name, type, constraints en testCases
- Stack: Java/Spring Boot (JPA entiteiten)
- Voeg technische velden toe die logisch voortvloeien uit de requirements
  (bv. id, createdAt, userId als dat relevant is)
"""
    return {"domain_model": llm_json(prompt)}


def generate_api_design(state: TAState) -> dict:
    """Node 3: Genereer REST API design op basis van domain model en requirements."""
    print("🔌 API design genereren...")

    prompt = f"""Je bent een SDLC-analyse agent.

Genereer het REST API design voor een Spring Boot applicatie.

Domain model:
{json.dumps(state["domain_model"], indent=2)}

Requirements:
{json.dumps(state["requirements"], indent=2)}

Geef ALLEEN een JSON object terug:
{{
  "errorFormat": {{
    "type": "ApiError",
    "fields": ["correlationId", "code", "message", "fieldErrors"]
  }},
  "endpoints": [
    {{
      "method": "POST",
      "path": "/api/resource",
      "summary": "Korte beschrijving",
      "request": {{
        "bodySchemaRef": "CreateResourceRequest",
        "validationRules": [
          "veldNaam: beschrijving van de validatieregel"
        ]
      }},
      "responses": [
        {{"status": 201, "bodySchemaRef": "ResourceResponse", "notes": "succesvol aangemaakt"}},
        {{"status": 400, "bodySchemaRef": "ApiError", "notes": "validatiefout in request body"}},
        {{"status": 500, "bodySchemaRef": "ApiError", "notes": "onverwachte serverfout"}}
      ],
      "auth": "none|bearer"
    }}
  ]
}}

Regels:
- method EXACT: GET, POST, PUT, PATCH of DELETE
- Elke requirement die input of output vereist krijgt een endpoint
- Validatieregels beschrijven per veld wat gecontroleerd wordt
- Geen extra velden
"""
    return {"api_design": llm_json(prompt)}


def generate_messaging_design(state: TAState) -> dict:
    """Node 3b (event-driven): Genereer messaging design (topics, events, DLQ/retry strategie)."""
    print("📨 Messaging design genereren...")

    prompt = f"""Je bent een SDLC-analyse agent.

Genereer het messaging design voor een event-driven Spring Boot applicatie.

Domain model:
{json.dumps(state["domain_model"], indent=2)}

Requirements:
{json.dumps(state["requirements"], indent=2)}

Geef ALLEEN een JSON object terug:
{{
  "topics": [
    {{
      "name": "topic-naam",
      "producer": "ProducerServiceNaam",
      "consumer": "ConsumerServiceNaam",
      "description": "Wat er op dit topic gepubliceerd wordt"
    }}
  ],
  "events": [
    {{
      "name": "EventNaam",
      "trigger": "wanneer dit event getriggerd wordt",
      "payloadFields": ["veld1", "veld2", "veld3"]
    }}
  ],
  "dlqStrategy": "beschrijving van de dead letter queue aanpak",
  "retryStrategy": "beschrijving van de retry strategie (bv. exponential backoff, max 3 pogingen)"
}}

Regels:
- topic namen: kebab-case (bv. ticket-created)
- event namen: PascalCase (bv. TicketCreatedEvent)
- payloadFields: lijst van veldnamen die in de event payload zitten
- dlqStrategy en retryStrategy: concrete beschrijving, geen vage termen
- Geen extra velden
"""
    return {"messaging_design": llm_json(prompt)}


def generate_backend_design(state: TAState) -> dict:
    """Node 4: Genereer backend modules en klassen."""
    print("⚙️  Backend design genereren...")

    fa_type = state.get("fa_type", "full-stack")
    design_context = (
        f"Messaging design:\n{json.dumps(state['messaging_design'], indent=2)}"
        if fa_type == "event-driven"
        else f"API design:\n{json.dumps(state['api_design'], indent=2)}"
    )

    prompt = f"""Je bent een SDLC-analyse agent.

Genereer de backend architectuur voor een Spring Boot applicatie.

{design_context}

Domain model:
{json.dumps(state["domain_model"], indent=2)}

Requirements:
{json.dumps(state["requirements"], indent=2)}

Geef ALLEEN een JSON object terug:
{{
  "modules": [
    {{
      "name": "modulenaam (bv. ticket, user, common)",
      "classes": [
        {{
          "name": "KlasseNaam",
          "responsibility": "Wat deze klasse doet in één zin"
        }}
      ]
    }}
  ]
}}

Regels:
- Gebruik Spring Boot lagen: Controller, Service, Repository, Entity, DTO, Exception
- Elke business rule uit de requirements krijgt een eigen Service of Validator klasse
- Naamgeving: PascalCase klassen, camelCase modules
- Geen extra velden buiten name en responsibility
"""
    return {"backend_design": llm_json(prompt)}


def generate_frontend_design(state: TAState) -> dict:
    """Node 5: Genereer frontend routes en componenten."""
    print("🖥️  Frontend design genereren...")

    fa_type = state.get("fa_type", "full-stack")

    if fa_type == "frontend-only":
        prompt = f"""Je bent een SDLC-analyse agent.

Genereer de frontend structuur voor een React/TypeScript applicatie.
Dit is een FRONTEND-ONLY feature — er zijn geen nieuwe backend endpoints, alleen bestaande API wordt geconsumeerd.

Requirements:
{json.dumps(state["requirements"], indent=2)}

Geef ALLEEN een JSON object terug:
{{
  "routes": ["/pad/naar/pagina"],
  "components": ["ComponentNaam"],
  "consumedEndpoints": [
    {{"method": "GET", "path": "/api/bestaand-endpoint", "description": "Waarvoor gebruikt"}}
  ],
  "tests": {{
    "unit": ["ComponentNaam rendering in alle UI states (loading, empty, error, success)"],
    "integration": ["GET /api/endpoint → 200 OK (gemockt)"],
    "e2e": ["Beschrijving van volledige gebruikersflow"]
  }}
}}

Regels:
- Routes: paden die de gebruiker kan navigeren
- Componenten: PascalCase, inclusief loading/error/empty state componenten
- consumedEndpoints: bestaande API endpoints die geconsumeerd worden
- Stack: React 18, TypeScript
- tests.unit: alle UI states (loading, empty, error, success) testen
- tests.integration: met gemockte API responses
- tests.e2e: gebruikersflow in gewone taal
"""
    else:
        prompt = f"""Je bent een SDLC-analyse agent.

Genereer de frontend structuur voor een React/TypeScript applicatie.

API endpoints:
{json.dumps(state["api_design"].get("endpoints", []), indent=2)}

Requirements:
{json.dumps(state["requirements"], indent=2)}

Geef ALLEEN een JSON object terug:
{{
  "routes": ["/pad/naar/pagina"],
  "components": ["ComponentNaam"],
  "tests": {{
    "unit": ["KlasseNaam.methodeNaam of ComponentNaam render"],
    "integration": ["POST /api/endpoint → 201 Created"],
    "e2e": ["Beschrijving van volledige gebruikersflow"]
  }}
}}

Regels:
- Routes: paden die de gebruiker kan navigeren
- Componenten: PascalCase, alleen wat relevant is voor de FA scope
- Stack: React 18, TypeScript
- tests.unit: klassenamen of component render tests
- tests.integration: endpoint + verwacht statuscode
- tests.e2e: gebruikersflow in gewone taal
"""
    result = llm_json(prompt)
    return {
        "frontend_design": {
            "routes": result.get("routes", []),
            "components": result.get("components", []),
        },
        "tests_design": result.get("tests", {"unit": [], "integration": [], "e2e": []}),
    }


def generate_traceability(state: TAState) -> dict:
    """
    Node 6: Genereer de traceability matrix.
    Koppelt elke requirement specifiek aan de juiste klassen, componenten en tests.
    """
    print("🔗 Traceability matrix genereren...")

    all_classes = [
        cls["name"]
        for module in state["backend_design"].get("modules", [])
        for cls in module.get("classes", [])
    ]
    all_components = state["frontend_design"].get("components", [])

    prompt = f"""Je bent een SDLC-analyse agent.

Maak een traceability matrix die elke requirement koppelt aan de juiste implementatie.

Requirements:
{json.dumps(state["requirements"], indent=2)}

Beschikbare backend klassen:
{json.dumps(all_classes, indent=2)}

Beschikbare frontend componenten:
{json.dumps(all_components, indent=2)}

Geef ALLEEN een JSON object terug:
{{
  "traceability": [
    {{
      "reqId": "REQ-001",
      "backendRefs": ["KlasseNaam", "AndereKlasse"],
      "frontendRefs": ["ComponentNaam"],
      "testRefs": ["beschrijving van de test die deze requirement valideert"]
    }}
  ]
}}

Regels:
- Elk REQ-item uit de requirements lijst moet voorkomen
- backendRefs: ALLEEN klassen uit de beschikbare backend klassen
- frontendRefs: ALLEEN componenten uit de beschikbare frontend componenten
- testRefs: beschrijf concreet wat getest wordt voor deze requirement
- Een requirement die puur backend is heeft een lege frontendRefs lijst
- Een requirement die puur UI is heeft een lege backendRefs lijst
"""
    result = llm_json(prompt)
    return {"traceability": result.get("traceability", [])}


def assemble_ta_json(state: TAState) -> dict:
    """
    Node 7: Assembleer de volledige TA JSON uit alle losse delen.
    Geen LLM-call — puur samenvoegen.
    """
    print("📦 TA JSON assembleren...")

    # Alleen toegestane velden doorlaten (schema staat geen extras toe)
    raw_api = state["api_design"]

    # Strip onbekende velden uit endpoints en hun request objecten
    clean_endpoints = []
    for ep in raw_api.get("endpoints", []):
        raw_req = ep.get("request", {})
        clean_responses = []
        for r in ep.get("responses", []):
            clean_r = {k: v for k, v in r.items() if k in ("status", "bodySchemaRef", "notes")}
            if "bodySchemaRef" not in clean_r:
                clean_r["bodySchemaRef"] = "EmptyResponse"
            clean_responses.append(clean_r)

        clean_endpoints.append({
            "method":   ep["method"],
            "path":     ep["path"],
            "summary":  ep.get("summary", ""),
            "request": {
                k: v for k, v in raw_req.items()
                if k in ("bodySchemaRef", "validationRules")
            },
            "responses": clean_responses,
            **({"auth": ep["auth"]} if "auth" in ep else {}),
        })

    api = {
        "errorFormat": raw_api.get("errorFormat", {"type": "ApiError", "fields": []}),
        "endpoints":   clean_endpoints,
    }

    raw_domain = state["domain_model"]
    clean_entities = []
    for ent in raw_domain.get("entities", []):
        clean_fields = []
        for f in ent.get("fields", []):
            clean_f = {k: v for k, v in f.items() if k in ("name", "type", "constraints", "testCases")}
            clean_fields.append(clean_f)
        clean_entities.append({"name": ent["name"], "fields": clean_fields})
    domain = {"entities": clean_entities}

    # Strip extra fields from requirements (schema: id, text, priority only)
    clean_requirements = [
        {k: v for k, v in r.items() if k in ("id", "text", "priority")}
        for r in state["requirements"]
        if isinstance(r.get("id"), str) and r["id"].startswith("REQ-")
    ]

    # Strip extra fields from backend classes (schema: name, responsibility only)
    clean_modules = []
    for mod in state["backend_design"].get("modules", []):
        clean_classes = [
            {k: v for k, v in cls.items() if k in ("name", "responsibility")}
            for cls in mod.get("classes", [])
        ]
        clean_modules.append({"name": mod["name"], "classes": clean_classes})

    fa_type = state.get("fa_type", "full-stack")

    # Veilige defaults voor secties die overgeslagen zijn voor dit FA-type
    frontend = {
        "routes":     state["frontend_design"].get("routes", []),
        "components": state["frontend_design"].get("components", []),
    }
    tests = {
        "unit":        state["tests_design"].get("unit", []),
        "integration": state["tests_design"].get("integration", []),
        "e2e":         state["tests_design"].get("e2e", []),
    }

    ta = {
        "meta": {
            "featureId": state["feature_id"],
            "title": _extract_title(state["fa_content"], state["feature_id"]),
            "version": "1.0.0",
        },
        "scope": state["scope"],
        "assumptions": state["assumptions"],
        "openQuestions": state["open_questions"],
        "requirements": clean_requirements,
        "domain": domain,
        "api": api,
        "backend": {"modules": clean_modules},
        "frontend": frontend,
        "tests": tests,
        # Voeg messaging sectie toe voor event-driven features
        **({"messaging": _clean_messaging(state["messaging_design"])} if fa_type == "event-driven" else {}),
        # Filter traceability: alleen geldige REQ-NNN IDs, testRefs altijd als array
        "traceability": [
            {
                **t,
                "testRefs": (
                    t["testRefs"] if isinstance(t.get("testRefs"), list)
                    else [t["testRefs"]] if isinstance(t.get("testRefs"), str)
                    else []
                ),
            }
            for t in state["traceability"]
            if isinstance(t.get("reqId"), str) and t["reqId"].startswith("REQ-")
        ],
    }

    return {"ta_json": ta}


def generate_ta_markdown(state: TAState) -> dict:
    """
    Node 8: Genereer de volledige TA als Markdown document.
    Gebruikt het TA skelet als structuurreferentie.
    """
    print("📄 TA Markdown genereren...")

    skeleton_hint = (
        f"\nTA skelet (gebruik deze structuur en sectieopbouw):\n---\n{state['ta_skeleton']}\n---"
        if state.get("ta_skeleton")
        else ""
    )

    prompt = f"""Je bent een SDLC-analyse agent.

Genereer een volledige Technische Analyse als Markdown document.
{skeleton_hint}

Gebruik de volgende gegevens als inhoud:

Feature ID: {state["feature_id"]}
Titel: {state["ta_json"]["meta"]["title"]}

Scope:
{json.dumps(state["scope"], indent=2)}

Assumptions:
{json.dumps(state["assumptions"], indent=2)}

Open questions:
{json.dumps(state["open_questions"], indent=2)}

Requirements:
{json.dumps(state["requirements"], indent=2)}

Domain model:
{json.dumps(state["domain_model"], indent=2)}

API design:
{json.dumps(state["api_design"], indent=2)}

Backend design:
{json.dumps(state["backend_design"], indent=2)}

Frontend design:
{json.dumps(state["frontend_design"], indent=2)}

Test strategy:
{json.dumps(state["tests_design"], indent=2)}

Traceability:
{json.dumps(state["traceability"], indent=2)}

Schrijf de volledige TA als Markdown met deze 12 secties:
1. Scope
2. Assumptions
3. Open Questions
4. Domain Model (tabel met entiteiten, velden, constraints)
5. API Design (endpoints, request/response DTOs, validatieregels, error formaat)
6. Backend Design (lagen, klassen, verantwoordelijkheden)
7. Frontend Design (routes, componenten)
8. Security & Privacy
9. Observability (logging, metrics, correlation id)
10. Performance & Scalability
11. Test Strategy (unit, integration, e2e)
12. Traceability Matrix (REQ → backend → frontend → tests)

Regels:
- Schrijf in het Nederlands
- Gebruik Markdown tabellen voor domain model en traceability
- Elke sectie heeft een duidelijke ## heading
- Wees concreet en technisch — geen vage omschrijvingen
- Geef ALLEEN de Markdown tekst terug, geen JSON
"""
    markdown = llm_text(prompt)

    # Verwijder eventuele markdown code block wrapper
    if markdown.startswith("```markdown"):
        markdown = markdown[len("```markdown"):].lstrip()
        if markdown.endswith("```"):
            markdown = markdown[:-3].rstrip()
    elif markdown.startswith("```"):
        markdown = markdown[3:].lstrip()
        if markdown.endswith("```"):
            markdown = markdown[:-3].rstrip()

    return {"ta_markdown": markdown}


def validate_schema(state: TAState) -> dict:
    """Node 9: Valideer de TA JSON tegen ta.schema.json."""
    print("✅ Schema valideren...")

    errors: list[str] = []
    try:
        validate(instance=state["ta_json"], schema=state["ta_schema"])
        print("  ✅ Schema valide")
    except ValidationError as e:
        errors.append(f"{e.json_path}: {e.message}")
        print(f"  ❌ Validatiefout: {errors[-1]}")

    return {
        "validation_errors": errors,
        "retry_count": state.get("retry_count", 0),
    }


def self_correct(state: TAState) -> dict:
    """Node 10: Herstel validatiefouten via gerichte Gemini-call."""
    retry = state.get("retry_count", 0) + 1
    print(f"🔧 Zelfcorrectie (poging {retry}/3)...")

    prompt = f"""Je bent een SDLC-analyse agent die JSON schema validatiefouten herstelt.

Huidige TA JSON (bevat fouten):
{json.dumps(state["ta_json"], indent=2)}

Validatiefouten:
{json.dumps(state["validation_errors"], indent=2)}

TA Schema (waartegen gevalideerd wordt):
{json.dumps(state["ta_schema"], indent=2)}

Herstel ALLEEN de velden die de validatiefouten veroorzaken.
Geef de VOLLEDIGE gecorrigeerde TA JSON terug als object.
Verander niets wat al valide is.
"""
    return {
        "ta_json": llm_json(prompt),
        "retry_count": retry,
    }


# ── Conditionele edges ─────────────────────────────────────────────────────────

def route_after_parse_fa(state: TAState) -> str:
    """frontend-only slaat domain/api/backend over en gaat direct naar frontend."""
    if state["fa_type"] == "frontend-only":
        return "generate_frontend_design"
    return "generate_domain_model"


def route_after_domain_model(state: TAState) -> str:
    """event-driven gaat naar messaging design, andere types naar API design."""
    if state["fa_type"] == "event-driven":
        return "generate_messaging_design"
    return "generate_api_design"


def route_after_backend_design(state: TAState) -> str:
    """full-stack heeft ook een frontend; rest-api en event-driven slaan frontend over."""
    if state["fa_type"] == "full-stack":
        return "generate_frontend_design"
    return "generate_traceability"


def after_validation(state: TAState) -> str:
    if state["validation_errors"] and state.get("retry_count", 0) < 3:
        return "self_correct"
    return "generate_ta_markdown"




# ── Hulpfuncties ───────────────────────────────────────────────────────────────

def _clean_messaging(raw: dict) -> dict:
    """Strip niet-schema velden uit messaging_design."""
    return {
        "topics": [
            {k: v for k, v in t.items() if k in ("name", "producer", "consumer", "description")}
            for t in raw.get("topics", [])
        ],
        "events": [
            {k: v for k, v in e.items() if k in ("name", "trigger", "payloadFields")}
            for e in raw.get("events", [])
        ],
        **({k: raw[k] for k in ("dlqStrategy", "retryStrategy") if k in raw}),
    }


def _extract_title(fa_content: str, fallback: str) -> str:
    """Haal de feature titel uit de eerste # heading van de FA."""
    for line in fa_content.splitlines():
        if line.startswith("# "):
            return line[2:].strip()
    return fallback


# ── Graph ──────────────────────────────────────────────────────────────────────

def build_graph():
    graph = StateGraph(TAState)

    graph.add_node("classify_fa",               classify_fa)
    graph.add_node("parse_fa",                  parse_fa)
    graph.add_node("generate_domain_model",     generate_domain_model)
    graph.add_node("generate_api_design",       generate_api_design)
    graph.add_node("generate_messaging_design", generate_messaging_design)
    graph.add_node("generate_backend_design",   generate_backend_design)
    graph.add_node("generate_frontend_design",  generate_frontend_design)
    graph.add_node("generate_traceability",     generate_traceability)
    graph.add_node("assemble_ta_json",          assemble_ta_json)
    graph.add_node("validate_schema",           validate_schema)
    graph.add_node("self_correct",              self_correct)
    graph.add_node("generate_ta_markdown",      generate_ta_markdown)

    graph.add_edge(START,                        "classify_fa")
    graph.add_edge("classify_fa",                "parse_fa")

    # Na parse_fa: frontend-only → frontend, andere types → domain model
    graph.add_conditional_edges("parse_fa", route_after_parse_fa)

    # Na domain model: event-driven → messaging, andere types → api design
    graph.add_conditional_edges("generate_domain_model", route_after_domain_model)

    # REST API design → backend
    graph.add_edge("generate_api_design",        "generate_backend_design")

    # Messaging design (event-driven) → backend
    graph.add_edge("generate_messaging_design",  "generate_backend_design")

    # Na backend: full-stack → frontend, rest-api/event-driven → traceability
    graph.add_conditional_edges("generate_backend_design", route_after_backend_design)

    # Frontend (altijd) → traceability
    graph.add_edge("generate_frontend_design",   "generate_traceability")

    graph.add_edge("generate_traceability",      "assemble_ta_json")
    graph.add_edge("assemble_ta_json",           "validate_schema")
    graph.add_conditional_edges("validate_schema", after_validation)
    graph.add_edge("self_correct",               "validate_schema")
    graph.add_edge("generate_ta_markdown",       END)

    return graph.compile()


# ── Main ───────────────────────────────────────────────────────────────────────

def parse_args():
    parser = argparse.ArgumentParser(description="LangGraph FA→TA agent")
    parser.add_argument("feature_id", help="Feature ID (bv. feature-001-support-ticket)")
    parser.add_argument(
        "--fa-type",
        choices=FA_TYPES,
        default="",
        help=f"FA-type handmatig opgeven ({', '.join(FA_TYPES)}). Standaard: auto-detect.",
    )
    parser.add_argument(
        "--ta-skeleton",
        default="",
        help="Pad naar TA skelet (overschrijft het type-specifieke skelet).",
    )
    return parser.parse_args()


def main():
    args = parse_args()
    feature_id = args.feature_id

    base        = Path(__file__).parent.parent.parent.parent
    fa_path     = base / "docs" / "functional-analysis" / f"{feature_id}.md"
    ta_md_path  = base / "docs" / "technical-analysis"  / f"{feature_id}.md"
    ta_json_path= base / "docs" / "technical-analysis"  / f"{feature_id}.ta.json"
    schema_path = base / "ai"   / "schemas"             / "ta.schema.json"

    print("==============================================")
    print("AI-SDLC — LangGraph FA→TA agent")
    print(f"Feature : {feature_id}")
    print("==============================================\n")

    if not fa_path.exists():
        print(f"❌ FA niet gevonden: {fa_path}", file=sys.stderr)
        sys.exit(1)
    if not schema_path.exists():
        print(f"❌ Schema niet gevonden: {schema_path}", file=sys.stderr)
        sys.exit(1)

    # Handmatig ta-skeleton overschrijft het type-specifieke skelet
    ta_skeleton = ""
    if args.ta_skeleton:
        ta_skeleton_path = Path(args.ta_skeleton)
        if ta_skeleton_path.exists():
            ta_skeleton = ta_skeleton_path.read_text()
            print(f"📋 TA skelet (handmatig): {ta_skeleton_path.name}\n")

    if args.fa_type:
        print(f"🏷️  FA-type (handmatig opgegeven): {args.fa_type}\n")

    app = build_graph()
    final = app.invoke({
        "feature_id":       feature_id,
        "fa_content":       fa_path.read_text(),
        "fa_type":          "",
        "fa_type_manual":   args.fa_type,
        "ta_skeleton":      ta_skeleton,
        "ta_schema":        json.loads(schema_path.read_text()),
        "requirements":     [],
        "scope":            {"inScope": [], "outOfScope": []},
        "assumptions":      [],
        "open_questions":   [],
        "domain_model":     {},
        "api_design":       {},
        "messaging_design": {},
        "backend_design":   {},
        "frontend_design":  {},
        "tests_design":     {"unit": [], "integration": [], "e2e": []},
        "traceability":     [],
        "ta_json":          {},
        "ta_markdown":      "",
        "validation_errors": [],
        "retry_count":      0,
    })

    # Schrijf outputs
    ta_md_path.parent.mkdir(parents=True, exist_ok=True)
    ta_json_path.parent.mkdir(parents=True, exist_ok=True)

    ta_json_path.write_text(json.dumps(final["ta_json"], indent=2) + "\n")
    ta_md_path.write_text(final["ta_markdown"] + "\n")

    print(f"\n✅ Klaar!")
    print(f"   🏷️  FA-type  : {final['fa_type']}")
    print(f"   📄 {ta_md_path.relative_to(base)}")
    print(f"   📋 {ta_json_path.relative_to(base)}")

    if final["validation_errors"]:
        print(f"\n⚠️  {len(final['validation_errors'])} onopgeloste validatiefout(en):")
        for err in final["validation_errors"]:
            print(f"   - {err}")
        sys.exit(1)


if __name__ == "__main__":
    main()
