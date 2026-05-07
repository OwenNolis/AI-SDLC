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

Optionele argumenten:
  --context       Inline tekst als extra context
  --context-dir   Map met .md/.txt bestanden — alle bestanden worden geladen
  --context-files Specifieke bestanden als extra context (meerdere mogelijk)

Output:
  docs/technical-analysis/<feature-id>.md
  docs/technical-analysis/<feature-id>.ta.json
"""

import argparse
import base64
import json
import os
import sys
from concurrent.futures import ThreadPoolExecutor
from pathlib import Path
from typing import TypedDict

from dotenv import load_dotenv
from jsonschema import ValidationError, validate
from langchain_core.messages import HumanMessage
from langchain_google_genai import ChatGoogleGenerativeAI
from langgraph.graph import END, START, StateGraph
try:
    from pypdf import PdfReader as _PdfReader
    _PYPDF_AVAILABLE = True
except Exception:
    _PdfReader = None  # type: ignore[assignment,misc]
    _PYPDF_AVAILABLE = False

# Laad .env vanuit de repo root
load_dotenv(Path(__file__).parent.parent.parent.parent / ".env")


CONTEXT_EXTENSIONS = (".md", ".txt", ".pdf", ".png", ".jpg", ".jpeg", ".gif", ".webp")

IMAGE_EXTENSIONS = {".png", ".jpg", ".jpeg", ".gif", ".webp"}

_IMAGE_MIME: dict[str, str] = {
    ".png":  "image/png",
    ".jpg":  "image/jpeg",
    ".jpeg": "image/jpeg",
    ".gif":  "image/gif",
    ".webp": "image/webp",
}


def _describe_image(f: Path) -> str:
    """Stuur een afbeelding naar het LLM en geef een gestructureerde tekstbeschrijving terug."""
    mime = _IMAGE_MIME[f.suffix.lower()]
    data = base64.standard_b64encode(f.read_bytes()).decode()
    message = HumanMessage(content=[
        {
            "type": "image_url",
            "image_url": {"url": f"data:{mime};base64,{data}"},
        },
        {
            "type": "text",
            "text": (
                "Je bent een SDLC-documentatie assistent. "
                "Analyseer deze afbeelding en beschrijf de inhoud VOLLEDIG en EXACT in gestructureerde tekst "
                "zodat het bruikbaar is als context voor het genereren van een Technische Analyse.\n\n"
                "KRITIEKE REGELS — deze gelden altijd, zonder uitzondering:\n"
                "- Neem ELK zichtbaar veld op van ELKE entiteit, klasse of component in de afbeelding\n"
                "- Noteer bij elk veld: de exacte naam, het exacte type en eventuele zichtbare constraints\n"
                "- Noteer alle relaties met hun multipliciteit (bv. 1, 0..*, 1..*, 0..1)\n"
                "- Noteer ALLE enum-waarden exact zoals ze in de afbeelding staan\n"
                "- Sla NOOIT een veld over omdat de entiteit 'al bestaat' of 'read-only' is — "
                "alle zichtbare velden MOETEN worden opgenomen, ongeacht eventuele notities zoals 'all should already exist'\n"
                "- Kopieer namen exact (hoofdletters, camelCase, underscores) — verzin geen synoniemen\n"
                "- Als een veld of type onduidelijk leesbaar is, schrijf dan op wat zichtbaar is en markeer het met (onzeker)\n\n"
                "Diagramtype-specifieke instructies:\n"
                "- Domeinmodel / ERD / UML class diagram: per entiteit een ### heading, "
                "daarna een bullet-lijst met alle velden (naam: type — constraints), "
                "gevolgd door een 'Relaties' subsectie\n"
                "- Sequentiediagram: actoren en stappen op volgorde\n"
                "- Flowchart / procesdiagram: elke stap en beslissing\n"
                "- UI-mockup / wireframe: schermopbouw, componenten en interacties\n"
                "- Architectuurdiagram: services, verbindingen en verantwoordelijkheden\n\n"
                "Geef de beschrijving in het Nederlands. Gebruik koppen en bullet-lijsten."
            ),
        },
    ])
    llm = get_llm()
    response = llm.invoke([message])
    return response.content.strip()


def read_context_file(f: Path) -> str:
    """Lees een context bestand (.md, .txt, .pdf of afbeelding) en geef de tekst terug."""
    suffix = f.suffix.lower()
    if suffix in IMAGE_EXTENSIONS:
        return _describe_image(f)
    if suffix == ".pdf":
        if not _PYPDF_AVAILABLE:
            return f"[PDF: {f.name} — pypdf niet beschikbaar op Python 3.14]"
        reader = _PdfReader(f)
        return "\n".join(page.extract_text() or "" for page in reader.pages)
    return f.read_text(encoding="utf-8")


# ── State ──────────────────────────────────────────────────────────────────────

FA_TYPES = ["rest-api", "full-stack", "frontend-only", "event-driven"]

SKELETONS_DIR = Path(__file__).parent / "templates" / "skeletons"


class TAState(TypedDict):
    # Inputs
    feature_id: str
    fa_content: str
    fa_path_str: str   # Absoluut pad naar de FA .md (voor afbeeldingen)
    ta_path_str: str   # Absoluut pad naar de TA .md (voor relatieve afbeeldingspaden)
    fa_type: str            # rest-api | full-stack | frontend-only | event-driven
    fa_type_manual: str     # handmatig opgegeven type (leeg = auto-detect)
    extra_context: str      # optionele extra context meegegeven via --context
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
    acceptance_criteria: list  # [{"acId": "AC-001-1", "reqId": "REQ-001", "given": "...", "when": "...", "then": "...", "testType": "integration"}]
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
    import re as _re
    print("🔍 FA parseren...")

    # Count items already labelled in the FA to give the LLM a hard upper bound
    fa_text = state["fa_content"]
    fa_req_ids  = _re.findall(r'\bREQ-\d+\b', fa_text)
    fa_br_ids   = _re.findall(r'\bBR-\d+\b',  fa_text)
    fa_nfr_ids  = _re.findall(r'\bNFR-\d+\b', fa_text)
    n_reqs  = len(set(fa_req_ids))
    n_brs   = len(set(fa_br_ids))
    n_nfrs  = len(set(fa_nfr_ids))
    total_items = n_reqs + n_brs + n_nfrs
    max_req_id  = f"REQ-{total_items:03d}" if total_items > 0 else "REQ-999"

    count_hint = (
        f"\n⚠️ De FA bevat EXACT {total_items} items "
        f"({n_reqs} requirements, {n_brs} business rules, {n_nfrs} NFRs). "
        f"Genereer EXACT {total_items} REQ-NNN entries — niet meer, niet minder. "
        f"Laatste ID mag maximaal {max_req_id} zijn.\n"
        if total_items > 0 else ""
    )

    prompt = f"""Je bent een SDLC-analyse agent.

Lees de Functionele Analyse en extraheer alle gestructureerde gegevens.
{count_hint}
FA inhoud:
---
{fa_text}
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
    raw_reqs = result.get("requirements", [])

    # Hard cap: strip any invented requirements beyond the FA item count
    if total_items > 0 and len(raw_reqs) > total_items:
        print(f"  ⚠️  LLM genereerde {len(raw_reqs)} requirements; ingekort tot {total_items}")
        raw_reqs = raw_reqs[:total_items]

    return {
        "requirements": raw_reqs,
        "scope": result.get("scope", {"inScope": [], "outOfScope": []}),
        "assumptions": result.get("openQuestions", []),
        "open_questions": result.get("openQuestions", []),
    }


def generate_domain_model(state: TAState) -> dict:
    """Node 2: Genereer domain model op basis van requirements en FA data-sectie."""
    print("🏗️  Domain model genereren...")

    # Extraheer alle entiteit/data omschrijvingen uit de FA (## Data sectie + afbeeldingsbeschrijvingen)
    fa_data_notes = ""
    in_data = False
    for line in state["fa_content"].splitlines():
        low = line.lower().strip()
        if low.startswith("## data") or low.startswith("## domein") or low.startswith("## domain"):
            in_data = True
        elif low.startswith("## ") and in_data:
            in_data = False
        if in_data and line.strip():
            fa_data_notes += f"  {line}\n"

    # Voeg ook beschrijvingen van afbeeldingen toe (beginnen met **[Afbeelding:)
    for line in state["fa_content"].splitlines():
        if line.startswith("**[Afbeelding:") or line.startswith("### "):
            fa_data_notes += f"  {line}\n"

    data_hint = (
        f"\nExpliciet beschreven entiteiten en velden in de FA "
        f"(gebruik dit als primaire bron — neem ALLE genoemde velden over):\n{fa_data_notes}\n"
        if fa_data_notes.strip() else ""
    )

    extra_context_hint = (
        f"\n⚠️ VERPLICHTE PROJECTREGELS (deze OVERSCHRIJVEN alle aannames en defaults — nooit negeren):\n{state['extra_context']}\n"
        if state.get("extra_context") else ""
    )

    prompt = f"""Je bent een SDLC-analyse agent.

Genereer een domain model voor een Java/Spring Boot applicatie.
{extra_context_hint}{data_hint}
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
          "type": "String|Integer|Boolean|LocalDateTime|UUID|BigDecimal|<EnumTypeName>",
          "constraints": ["notNull", "minLength:5", "maxLength:255"],
          "testCases": ["empty", "too_short", "too_long", "missing", "invalid_value"]
        }}
      ]
    }}
  ]
}}

Regels:
- Als entiteiten en velden expliciet beschreven zijn in de FA (zie boven): neem die EXACT over — verzin geen velden, laat er ook geen weg
- Als een enum type zichtbaar is in de FA (bv. PlannerFeedbackActivityType, OrderStatus): voeg het toe als aparte entiteit met name="<EnumNaam>" en fields=[{{"name": "WAARDE", "type": "enum_value"}}], EN gebruik de exacte enum-naam als type voor het veld dat ernaar verwijst
- testCases ALLEEN uit: empty, too_short, too_long, missing, invalid_value, duplicate_per_day
- Geen extra velden buiten name, type, constraints en testCases
- Stack: Java/Spring Boot (JPA entiteiten)
- Voeg standaard technische velden toe die niet in de FA staan maar logisch noodzakelijk zijn
  (bv. createdAt, updatedAt) — maar NOOIT ten koste van FA-beschreven velden
"""
    return {"domain_model": llm_json(prompt)}


def generate_api_design(state: TAState) -> dict:
    """Node 3: Genereer REST API design op basis van domain model en requirements."""
    print("🔌 API design genereren...")

    # Extraheer expliciet vermelde endpoints uit de FA voor scope enforcement
    fa_api_notes = ""
    for line in state["fa_content"].splitlines():
        if "endpoint:" in line.lower() or "/api/" in line.lower():
            fa_api_notes += f"  {line.strip()}\n"

    scope_hint = f"""
Expliciet vermelde endpoints in de FA (gebruik dit als primaire bron):
{fa_api_notes if fa_api_notes else "  (geen expliciete endpoints vermeld — leid af uit requirements en scope)"}

Scope van de feature:
{json.dumps(state["scope"], indent=2)}
""" if fa_api_notes else f"""
Scope van de feature:
{json.dumps(state["scope"], indent=2)}
"""

    extra_context_hint = (
        f"\n⚠️ VERPLICHTE PROJECTREGELS (deze OVERSCHRIJVEN alle aannames en defaults — nooit negeren):\n{state['extra_context']}\n"
        if state.get("extra_context") else ""
    )

    prompt = f"""Je bent een SDLC-analyse agent.

Genereer het REST API design voor een Spring Boot applicatie.
{extra_context_hint}
Domain model:
{json.dumps(state["domain_model"], indent=2)}

Requirements:
{json.dumps(state["requirements"], indent=2)}
{scope_hint}
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
- Genereer ALLEEN endpoints die voortvloeien uit de FA scope en requirements
- Als de FA expliciete endpoints vermeldt, gebruik die als basis
- Logische technische endpoints die nodig zijn voor een goede werking mogen toegevoegd worden
  (bv. foutafhandeling, authenticatie-checks) maar GEEN volledige CRUD voor entiteiten
  die niet in de scope vallen
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

    # Extraheer expliciet vermelde routes en componenten uit de FA
    fa_ux_notes = ""
    in_ux = False
    for line in state["fa_content"].splitlines():
        if "ux notes" in line.lower() or "ux note" in line.lower():
            in_ux = True
        if in_ux and line.strip():
            fa_ux_notes += f"  {line.strip()}\n"

    ux_hint = f"\nExpliciet vermelde routes/componenten in de FA:\n{fa_ux_notes}" if fa_ux_notes else ""
    extra_context_hint = (
        f"\n⚠️ VERPLICHTE PROJECTREGELS (deze OVERSCHRIJVEN alle aannames en defaults — nooit negeren):\n{state['extra_context']}\n"
        if state.get("extra_context") else ""
    )

    if fa_type == "frontend-only":
        prompt = f"""Je bent een SDLC-analyse agent.

Genereer de frontend structuur voor een React/TypeScript applicatie.
Dit is een FRONTEND-ONLY feature — er zijn geen nieuwe backend endpoints, alleen bestaande API wordt geconsumeerd.
{extra_context_hint}{ux_hint}

Requirements:
{json.dumps(state["requirements"], indent=2)}

Geef ALLEEN een JSON object terug:
{{
  "routes": [
    {{
      "path": "/pad/naar/pagina",
      "components": [
        {{"name": "ComponentNaam", "responsibility": "Wat deze component doet"}}
      ]
    }}
  ],
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
- Elke route bevat de componenten die op die route worden gebruikt
- Componenten: PascalCase, inclusief loading/error/empty state componenten
- Stack: React 18, TypeScript
- tests.unit: alle UI states (loading, empty, error, success) testen
- tests.integration: met gemockte API responses
- tests.e2e: gebruikersflow in gewone taal
"""
    else:
        prompt = f"""Je bent een SDLC-analyse agent.

Genereer de frontend structuur voor een React/TypeScript applicatie.
{extra_context_hint}{ux_hint}

API endpoints:
{json.dumps(state["api_design"].get("endpoints", []), indent=2)}

Requirements:
{json.dumps(state["requirements"], indent=2)}

Scope van de feature:
{json.dumps(state["scope"], indent=2)}

Geef ALLEEN een JSON object terug:
{{
  "routes": [
    {{
      "path": "/pad/naar/pagina",
      "components": [
        {{"name": "ComponentNaam", "responsibility": "Wat deze component doet"}}
      ]
    }}
  ],
  "tests": {{
    "unit": ["KlasseNaam.methodeNaam of ComponentNaam render"],
    "integration": ["POST /api/endpoint → 201 Created"],
    "e2e": ["Beschrijving van volledige gebruikersflow"]
  }}
}}

Regels:
- Genereer ALLEEN routes en componenten die in de FA scope vallen
- Als de FA expliciete routes/componenten vermeldt, gebruik die als basis
- Logische technische componenten mogen toegevoegd worden (bv. ErrorDisplay, LoadingSpinner)
  maar GEEN pagina's of flows die buiten de scope vallen
- Stack: React 18, TypeScript
- tests.unit: klassenamen of component render tests
- tests.integration: endpoint + verwacht statuscode
- tests.e2e: gebruikersflow in gewone taal
"""
    result = llm_json(prompt)
    routes = result.get("routes", [])

    # Flatten component names from route objects for traceability use
    all_components = []
    for r in routes:
        if isinstance(r, dict):
            for c in r.get("components", []):
                name = c.get("name") if isinstance(c, dict) else c
                if name and name not in all_components:
                    all_components.append(name)

    return {
        "frontend_design": {
            "routes": routes,
            "components": all_components,
        },
        "tests_design": result.get("tests", {"unit": [], "integration": [], "e2e": []}),
    }


def generate_acceptance_criteria(state: TAState) -> dict:
    """
    Node 5b: Genereer acceptance criteria voor elke requirement.
    Elke AC beschrijft een meetbaar, testbaar scenario in Given/When/Then formaat.
    Gebruikt requirements, domeinmodel, API design en FA-inhoud als context.
    """
    print("✅ Acceptance Criteria genereren...")

    valid_req_ids = {r["id"] for r in state["requirements"] if "id" in r}

    # Extraheer bestaande ACs uit de FA als startpunt (kunnen leeg zijn)
    fa_ac_section = ""
    in_ac = False
    for line in state["fa_content"].splitlines():
        low = line.lower().strip()
        if low.startswith("## acceptance criteria"):
            in_ac = True
        elif low.startswith("## ") and in_ac:
            in_ac = False
        if in_ac:
            fa_ac_section += line + "\n"

    fa_ac_hint = (
        f"\nDe FA bevat al de volgende (mogelijk onvolledige) acceptance criteria — gebruik deze als startpunt "
        f"en vul ze aan of verbeter ze waar nodig:\n{fa_ac_section}\n"
        if fa_ac_section.strip() else
        "\nDe FA bevat geen acceptance criteria — genereer ze volledig op basis van de requirements, het domeinmodel en de API.\n"
    )

    # Compacte samenvatting van endpoints voor concrete HTTP-waarden in ACs
    endpoint_summary = "\n".join(
        f"  {ep.get('method')} {ep.get('path')} → "
        + ", ".join(f"{r.get('status')} {r.get('bodySchemaRef','')}" for r in ep.get("responses", []))
        for ep in state["api_design"].get("endpoints", [])
    )

    # Compacte samenvatting van entiteiten voor concrete veldnamen in ACs
    entity_summary = "\n".join(
        f"  {ent.get('name')}: " + ", ".join(f.get("name","") for f in ent.get("fields", []))
        for ent in state["domain_model"].get("entities", [])
    )

    prompt = f"""Je bent een ervaren SDLC-kwaliteitsborging agent.

Jouw taak: schrijf VOLLEDIGE, CONCRETE en TESTBARE acceptance criteria voor elke requirement.
Dit is een kritisch onderdeel van de Technische Analyse — slechte of vage ACs zijn onacceptabel.
{fa_ac_hint}
Requirements:
{json.dumps(state["requirements"], indent=2)}

Beschikbare API endpoints:
{endpoint_summary if endpoint_summary else "  (nog niet beschikbaar)"}

Beschikbare domeinentiteiten en velden:
{entity_summary if entity_summary else "  (nog niet beschikbaar)"}

Geef ALLEEN een JSON object terug:
{{
  "acceptanceCriteria": [
    {{
      "acId": "AC-001-1",
      "reqId": "REQ-001",
      "given": "de beginsituatie of context",
      "when": "de actie die de gebruiker of het systeem uitvoert",
      "then": "het meetbare, verwachte resultaat",
      "testType": "integration"
    }}
  ]
}}

VERPLICHTE KWALITEITSREGELS — elke AC die hier niet aan voldoet is onvoldoende:

Structuur:
- acId formaat: AC-{{reqId nummer}}-{{volgnummer}} (bv. AC-001-1, AC-001-2)
- reqId: UITSLUITEND IDs uit de bovenstaande requirements lijst
- Minimaal 2 ACs per requirement: altijd 1 happy path + minimaal 1 negatief/randgeval
- Voor requirements met validatieregels, constraints of business rules: genereer een AC per validatieregel

Concreetheid (meest kritieke regel):
- given: noem de exacte beginstaat met echte waarden (bv. "Een product met voorraad 5 stuks en drempel 10")
- when: noem de exacte actie met echte invoerwaarden (bv. "POST /api/orders met quantity=3 en productId=<uuid>")
- then: noem het exacte meetbare resultaat (bv. "HTTP 201 met body {{orderNumber: 'ORD-2024-000001', status: 'PENDING'}}")
- Gebruik NOOIT vage formuleringen zoals "de juiste foutmelding", "succesvol verwerkt" of "correct gedrag"
- Gebruik WEL: HTTP-statuscodes, veldnamen, enum-waarden, grensbedragen, aantallen uit de requirements

Dekking — voor elke requirement:
- Happy path: de normale, succesvolle werking
- Validatiefout: ontbrekend verplicht veld, te lange waarde, ongeldige enum, negatief getal, etc.
- Randgeval (indien van toepassing): grenswaarden (exact max, exact min, exact 0), duplicaten, conflicten
- Autorisatie (indien van toepassing): ROLE_USER vs ROLE_ADMIN

testType:
- unit = pure logica zonder I/O (validators, berekeningen, state machines, domeinregels)
- integration = API-endpoint aanroep met database-interactie (de meeste backend ACs)
- e2e = volledige gebruikersstroom van UI tot database (formulier invullen, knop klikken, resultaat zien)
"""
    result = llm_json(prompt)
    raw_acs = result.get("acceptanceCriteria", [])

    # Filter out any ACs referencing invented reqIds
    valid_acs = [ac for ac in raw_acs if ac.get("reqId", "") in valid_req_ids]

    return {"acceptance_criteria": valid_acs}


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
- Gebruik UITSLUITEND de reqId waarden die voorkomen in de requirements lijst hierboven
- Voeg GEEN nieuwe REQ-nummers toe die niet in de lijst staan — niet voor business rules, NFRs of aannames
- Elk reqId mag EXACT ÉÉN keer voorkomen — geen duplicaten
- backendRefs: ALLEEN klassen uit de beschikbare backend klassen
- frontendRefs: ALLEEN componenten uit de beschikbare frontend componenten
- testRefs: beschrijf concreet wat getest wordt voor deze requirement
- Een requirement die puur backend is heeft een lege frontendRefs lijst
- Een requirement die puur UI is heeft een lege backendRefs lijst
"""
    valid_req_ids = {r["id"] for r in state["requirements"] if "id" in r}
    result = llm_json(prompt)

    # Keep only entries whose reqId exists in the FA requirements list
    # Then deduplicate by reqId — keep first occurrence
    seen: set[str] = set()
    deduped = []
    for entry in result.get("traceability", []):
        req_id = entry.get("reqId", "")
        if req_id and req_id in valid_req_ids and req_id not in seen:
            seen.add(req_id)
            deduped.append(entry)

    return {"traceability": deduped}


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

    ALLOWED_TEST_CASES = {"empty", "too_short", "too_long", "missing", "invalid_value", "duplicate_per_day"}

    raw_domain = state["domain_model"]
    clean_entities = []
    for ent in raw_domain.get("entities", []):
        clean_fields = []
        for f in ent.get("fields", []):
            clean_f = {k: v for k, v in f.items() if k in ("name", "type", "constraints", "testCases")}
            if "testCases" in clean_f:
                clean_f["testCases"] = [tc for tc in clean_f["testCases"] if tc in ALLOWED_TEST_CASES]
            clean_fields.append(clean_f)
        clean_entities.append({"name": ent["name"], "fields": clean_fields})
    domain = {"entities": clean_entities}

    # Normalize priority synonyms → schema enum values
    _PRIORITY_MAP = {
        "must": "must", "must-have": "must", "high": "must",
        "should": "should", "should-have": "should", "medium": "should",
        "could": "could", "could-have": "could", "low": "could", "nice-to-have": "could",
    }
    # REQ-NNN pattern guard (exactly 3 digits)
    import re as _re
    _REQ_PATTERN = _re.compile(r"^REQ-\d{3}$")

    # Strip extra fields from requirements (schema: id, text, priority only)
    clean_requirements = []
    for r in state["requirements"]:
        req_id = r.get("id", "")
        if not isinstance(req_id, str) or not _REQ_PATTERN.match(req_id):
            continue
        priority_raw = str(r.get("priority", "must")).lower()
        priority = _PRIORITY_MAP.get(priority_raw, "must")
        clean_requirements.append({"id": req_id, "text": r.get("text", ""), "priority": priority})

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
    # Normalize routes: LLM returns route objects {path, components} but schema expects strings
    raw_routes = state["frontend_design"].get("routes", [])
    route_paths = [
        r["path"] if isinstance(r, dict) and "path" in r else r
        for r in raw_routes
    ]
    frontend = {
        "routes":     route_paths,
        "components": state["frontend_design"].get("components", []),
    }
    tests = {
        "unit":        state["tests_design"].get("unit", []),
        "integration": state["tests_design"].get("integration", []),
        "e2e":         state["tests_design"].get("e2e", []),
    }

    raw_scope = state["scope"]
    clean_scope = {
        "inScope":    raw_scope.get("inScope", []),
        "outOfScope": raw_scope.get("outOfScope", []),
    }

    raw_error_fmt = api.get("errorFormat", {})
    clean_error_fmt = {
        "type":   raw_error_fmt.get("type", "ApiError"),
        "fields": raw_error_fmt.get("fields", []),
    }
    api["errorFormat"] = clean_error_fmt

    ta = {
        "meta": {
            "featureId": state["feature_id"],
            "title": _extract_title(state["fa_content"], state["feature_id"]),
            "version": "1.0.0",
        },
        "scope": clean_scope,
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
        # Acceptance criteria: filter op geldige AC-NNN-N IDs en bekende reqIds
        "acceptanceCriteria": [
            {
                "acId":     ac["acId"],
                "reqId":    ac["reqId"],
                "given":    ac.get("given", ""),
                "when":     ac.get("when", ""),
                "then":     ac.get("then", ""),
                "testType": ac.get("testType", "integration"),
            }
            for ac in state.get("acceptance_criteria", [])
            if (
                isinstance(ac.get("acId"), str)
                and _re.match(r"^AC-[0-9]{3}-[0-9]+$", ac["acId"])
                and isinstance(ac.get("reqId"), str)
                and _REQ_PATTERN.match(ac["reqId"])
            )
        ],
        # Filter traceability: alleen geldige REQ-NNN IDs, whitelist keys, testRefs altijd als array
        "traceability": [
            {
                "reqId":        t["reqId"],
                "backendRefs":  t.get("backendRefs", []),
                "frontendRefs": t.get("frontendRefs", []),
                "testRefs": (
                    t["testRefs"] if isinstance(t.get("testRefs"), list)
                    else [t["testRefs"]] if isinstance(t.get("testRefs"), str)
                    else []
                ),
            }
            for t in state["traceability"]
            if isinstance(t.get("reqId"), str) and _REQ_PATTERN.match(t["reqId"])
        ],
    }

    return {"ta_json": ta}


def _strip_md_fence(text: str) -> str:
    """Verwijder eventuele markdown code block wrapper."""
    text = text.strip()
    if text.startswith("```markdown"):
        text = text[len("```markdown"):].lstrip()
    elif text.startswith("```"):
        text = text[3:].lstrip()
    if text.endswith("```"):
        text = text[:-3].rstrip()
    return text.strip()


def _strip_md_fence(text: str) -> str:
    """Verwijder eventuele markdown code block wrapper."""
    text = text.strip()
    if text.startswith("```markdown"):
        text = text[len("```markdown"):].lstrip()
    elif text.startswith("```"):
        text = text[3:].lstrip()
    if text.endswith("```"):
        text = text[:-3].rstrip()
    return text.strip()


def generate_ta_markdown(state: TAState) -> dict:
    """
    Node 8: Genereer de TA als Markdown document, per sectiegroep.
    Elke groep is een aparte LLM-call om afkapping bij grote features te voorkomen.
    """
    print("📄 TA Markdown genereren...")

    title   = state["ta_json"]["meta"]["title"]
    fa_type = state.get("fa_type", "full-stack")
    extra_context_hint = (
        f"\n⚠️ VERPLICHTE PROJECTREGELS (deze OVERSCHRIJVEN alle aannames en defaults — nooit negeren):\n{state['extra_context']}\n"
        if state.get("extra_context") else ""
    )

    # Gemeenschappelijke instructies voor elke sectie-call
    base_instruction = (
        f"Je bent een SDLC-analyse agent. Schrijf in het Nederlands. "
        f"Wees concreet en technisch. Geef ALLEEN Markdown terug, geen JSON. "
        f"Elke ## heading begint een nieuwe sectie."
        f"{extra_context_hint}"
    )

    # ── Bouw prompts (type-specifiek voor sectie 5) ───────────────────────────
    if fa_type == "event-driven":
        prompt_s5 = f"""{base_instruction}

Schrijf sectie 5 van de Technische Analyse voor feature: {title}

## 5. Messaging Design
Beschrijf topics, events, DLQ-strategie en retry-strategie met tabellen.
Gegevens: {json.dumps(state.get("messaging_design", {}), indent=2)}
"""
    else:
        prompt_s5 = f"""{base_instruction}

Schrijf sectie 5 van de Technische Analyse voor feature: {title}

## 5. API Design

Gebruik de volgende structuur — GEEN brede tabellen met alle endpoints op één rij.

### 5.1 Error Formaat
Toon het standaard error response formaat als JSON code block.

### 5.2 Endpoints
Maak voor elk endpoint een aparte ### subsectie met:
- Een regel: `METHOD /pad — korte omschrijving`
- Een kleine tabel met kolommen: | Veld | Waarde | voor method, path, auth, request DTO
- Een tabel voor responses met kolommen: | Status | Body | Omschrijving |
- Een bullet-lijst van validatieregels (alleen indien aanwezig)

Gegevens: {json.dumps(state["api_design"], indent=2)}
"""

    prompts = [
        f"""{base_instruction}

Titel: {title}

Schrijf secties 1, 2 en 3 van de Technische Analyse:

## 1. Scope
In scope en out of scope als bullet-lijsten.
Gegevens: {json.dumps(state["scope"], indent=2)}

## 2. Assumptions
{json.dumps(state["assumptions"], indent=2)}

## 3. Open Questions
{json.dumps(state["open_questions"], indent=2)}
""",
        f"""{base_instruction}

Schrijf sectie 4 van de Technische Analyse voor feature: {title}

## 4. Domain Model
Maak voor elke entiteit een aparte ### subsectie met naam van de entiteit als heading.
Daaronder een tabel met kolommen: | Veld | Type | Constraints | Testcases |
Sluit af met een ### Enums subsectie voor alle enum types.
Gegevens: {json.dumps(state["domain_model"], indent=2)}
""",
        prompt_s5,
        f"""{base_instruction}

Schrijf sectie 6 van de Technische Analyse voor feature: {title}

## 6. Backend Design
Beschrijf kort de gelaagde architectuur (Controller → Service → Repository).
Maak daarna per module een aparte ### subsectie (bv. ### Order Module, ### Customer Module).
Binnen elke module: een tabel met kolommen | Klasse | Verantwoordelijkheid |
Zet NOOIT alle klassen in één grote tabel — splits altijd per module.
Gegevens: {json.dumps(state["backend_design"], indent=2)}
""",
        f"""{base_instruction}

Schrijf sectie 7 van de Technische Analyse voor feature: {title}

## 7. Frontend Design
De gegevens bevatten een lijst van routes. Elke route heeft een "path" en een "components" lijst.
Maak voor elke route een aparte ### subsectie met het path als heading (bv. ### /checkout).
Schrijf binnen elke subsectie een tabel met kolommen: | Component | Verantwoordelijkheid |
Vul elke rij in met de component naam en zijn responsibility uit de gegevens.
Sla GEEN enkele route over — alle routes moeten een eigen subsectie krijgen.
Gegevens: {json.dumps(state["frontend_design"], indent=2)}
""",
        f"""{base_instruction}

Schrijf secties 8, 9 en 10 van de Technische Analyse voor feature: {title}

## 8. Security & Privacy
Authenticatie, autorisatie en privacyoverwegingen specifiek voor deze feature.

## 9. Observability
Logging, metrics en correlation ID gebruik. Geef concrete voorbeelden van wat gelogd wordt.

## 10. Performance & Scalability
Performance-eisen, database-indexen en schaalbaarheid voor deze feature.

Context:
- Requirements: {json.dumps([r["text"] for r in state["requirements"]], indent=2)}
- Endpoints: {json.dumps([e["path"] for e in state["api_design"].get("endpoints", [])], indent=2)}
""",
        f"""{base_instruction}

Schrijf sectie 11 van de Technische Analyse voor feature: {title}

## 11. Test Strategy
Schrijf subsecties voor unit tests, integration tests en e2e tests als bullet-lijsten.
Gegevens: {json.dumps(state["tests_design"], indent=2)}
""",
    ]

    # ── Secties 1-11 parallel genereren met voortgangsmelding per sectie ────────
    section_labels = [
        "Secties 1-3  (Scope, Assumptions, Open Questions)",
        "Sectie 4     (Domain Model)",
        "Sectie 5     (API / Messaging Design)",
        "Sectie 6     (Backend Design)",
        "Sectie 7     (Frontend Design)",
        "Secties 8-10 (Security, Observability, Performance)",
        "Sectie 11    (Test Strategy)",
    ]
    sections: list[str | None] = [None] * len(prompts)

    def _run_section(idx: int, prompt: str) -> tuple[int, str]:
        result = _strip_md_fence(llm_text(prompt))
        print(f"  ✅ {section_labels[idx]}")
        return idx, result

    print("  📝 Secties 1-11 parallel genereren...")
    with ThreadPoolExecutor(max_workers=len(prompts)) as executor:
        futures = [executor.submit(_run_section, i, p) for i, p in enumerate(prompts)]
        for f in futures:
            idx, result = f.result()
            sections[idx] = result

    # ── Sectie 12: Acceptance Criteria (direct opgebouwd, geen LLM) ─────────
    print("  ✅ Sectie 12    (Acceptance Criteria)")
    ac_rows = ""
    for ac in state.get("acceptance_criteria", []):
        ac_id    = ac.get("acId", "")
        req_id   = ac.get("reqId", "")
        given    = ac.get("given", "").replace("|", "\\|")
        when     = ac.get("when", "").replace("|", "\\|")
        then     = ac.get("then", "").replace("|", "\\|")
        testtype = ac.get("testType", "")
        ac_rows += f"| {ac_id} | {req_id} | {given} | {when} | {then} | {testtype} |\n"

    acceptance_criteria_md = (
        "## 12. Acceptance Criteria\n\n"
        "| AC-ID | REQ | Gegeven | Wanneer | Dan | Testtype |\n"
        "|-------|-----|---------|---------|-----|----------|\n"
        + ac_rows
    )
    sections.append(acceptance_criteria_md)

    # ── Sectie 13: Traceability Matrix (direct opgebouwd, geen LLM) ──────────
    print("  ✅ Sectie 13    (Traceability Matrix)")
    traceability_rows = ""
    for t in state["traceability"]:
        req_id   = t.get("reqId", "")
        backend  = ", ".join(t.get("backendRefs", []))
        frontend = ", ".join(t.get("frontendRefs", []))
        tests    = "; ".join(t.get("testRefs", []))
        traceability_rows += f"| {req_id} | {backend} | {frontend} | {tests} |\n"

    traceability_md = (
        "## 13. Traceability Matrix\n\n"
        "| REQ | Backend | Frontend | Tests |\n"
        "|-----|---------|----------|-------|\n"
        + traceability_rows
    )
    sections.append(traceability_md)

    markdown = f"# {title}\n\n" + "\n\n".join(sections)

    # Voeg FA-afbeeldingen in op de juiste TA-secties
    fa_path_str = state.get("fa_path_str", "")
    ta_path_str = state.get("ta_path_str", "")
    if fa_path_str and ta_path_str:
        fa_md_text = Path(fa_path_str).read_text(encoding="utf-8")
        images = _extract_fa_images(fa_md_text, Path(fa_path_str), Path(ta_path_str))
        markdown = _inject_images_into_ta(markdown, images)

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
    return "generate_acceptance_criteria"


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


def expand_fa_images(fa_text: str, fa_path: Path) -> str:
    """
    Zoek alle Markdown afbeeldingsreferenties in de FA (![...](pad)) op,
    beschrijf elke afbeelding via het LLM en vervang de referentie inline
    door een leesbare tekstbeschrijving.

    Relatieve paden worden opgelost ten opzichte van de FA-bestandslocatie.
    Afbeeldingen die niet gevonden worden of een niet-ondersteund formaat hebben,
    worden overgeslagen (de originele referentie blijft staan).
    """
    import re as _re
    pattern = _re.compile(r'!\[([^\]]*)\]\(([^)]+)\)')
    fa_dir = fa_path.parent

    def _replace(match: "_re.Match") -> str:  # type: ignore[name-defined]
        alt_text = match.group(1)
        img_src  = match.group(2).strip()

        # Sla data-URI's en externe URLs over
        if img_src.startswith("http://") or img_src.startswith("https://") or img_src.startswith("data:"):
            return match.group(0)

        img_path = (fa_dir / img_src).resolve()
        if not img_path.is_file():
            print(f"  ⚠️  Afbeelding niet gevonden in FA: {img_src}", file=sys.stderr)
            return match.group(0)

        if img_path.suffix.lower() not in IMAGE_EXTENSIONS:
            return match.group(0)

        label = alt_text if alt_text else img_path.name
        print(f"  🖼️  Afbeelding analyseren (FA): {img_path.name}")
        description = _describe_image(img_path)
        print(f"  ✅ Afbeelding verwerkt (FA): {img_path.name}")
        return (
            f"<!-- Afbeelding: {label} -->\n"
            f"**[Afbeelding: {label}]**\n\n"
            f"{description}\n"
        )

    return pattern.sub(_replace, fa_text)


_FA_IMAGE_CATEGORIES = {
    "frontend":  {"homepage", "shop", "overzicht", "product", "checkout", "order", "cart", "ui",
                  "mockup", "wireframe", "figma", "scherm", "pagina", "detail", "login", "register"},
    "backend":   {"sequence", "component", "deployment", "uml", "diagram", "flow", "architectuur",
                  "service", "module", "class"},
    "database":  {"erd", "database", "db", "schema", "datamodel", "domein", "entity", "model"},
    "api":       {"api", "contract", "endpoint", "swagger", "openapi", "rest"},
}

# TA section headings to inject images under (regex-safe prefix of the heading line)
_SECTION_HEADINGS = {
    "frontend": "## 7. Frontend Design",
    "backend":  "## 6. Backend Design",
    "database": "## 4. Domain Model",
    "api":      "## 5. API",
}


def _categorize_section(heading: str) -> str | None:
    """Geef de categorie terug op basis van de sectiekop (lowercase)."""
    h = heading.lower()
    for cat, keywords in _FA_IMAGE_CATEGORIES.items():
        if any(k in h for k in keywords):
            return cat
    return None


def _extract_fa_images(fa_text: str, fa_path: Path, ta_path: Path) -> dict[str, list[str]]:
    """
    Parseer de FA-markdown en categoriseer alle afbeeldingsreferenties op TA-sectie.

    Retourneert een dict {"frontend": [...md_refs...], "backend": [...], "database": [...], "api": [...]}.
    Paden worden omgezet naar relatieve paden ten opzichte van de TA-map.
    """
    import re as _re

    result: dict[str, list[str]] = {"frontend": [], "backend": [], "database": [], "api": []}
    current_cat: str | None = None
    pattern = _re.compile(r'!\[([^\]]*)\]\(([^)]+)\)')

    for line in fa_text.splitlines():
        if line.startswith("#"):
            heading = line.lstrip("#").strip()
            current_cat = _categorize_section(heading)

        m = pattern.search(line)
        if not m:
            continue
        alt = m.group(1).strip()
        src = m.group(2).strip()

        if src.startswith("http") or src.startswith("https") or src.startswith("data:"):
            continue

        img_abs = (fa_path.parent / src).resolve()
        if not img_abs.is_file():
            continue

        try:
            rel = os.path.relpath(img_abs, ta_path.parent)
        except ValueError:
            rel = str(img_abs)

        md_ref = f"![{alt}]({rel})"

        # Gebruik sectiekategorie; als onbekend, probeer alt-tekst
        cat = current_cat or _categorize_section(alt)
        if cat and cat in result:
            result[cat].append(md_ref)

    return result


def _inject_images_into_ta(ta_markdown: str, images: dict[str, list[str]]) -> str:
    """
    Voeg FA-afbeeldingen in op de juiste plek in de TA-markdown.

    Per categorie: zoek de bijbehorende ## sectie-heading en voeg de
    afbeeldingen in als blok direct na de heading-regel.
    """
    lines = ta_markdown.splitlines(keepends=True)
    output: list[str] = []
    inserted: set[str] = set()

    for line in lines:
        output.append(line)
        stripped = line.rstrip("\n")
        for cat, heading_prefix in _SECTION_HEADINGS.items():
            if stripped.startswith(heading_prefix) and cat not in inserted:
                refs = images.get(cat, [])
                if refs:
                    output.append("\n")
                    for ref in refs:
                        output.append(f"{ref}\n\n")
                    inserted.add(cat)
                break

    return "".join(output)


def _extract_title(fa_content: str, fallback: str) -> str:
    """
    Haal de feature naam uit de eerste # heading van de FA en bouw er een
    TA-titel van: "Technische Analyse - <naam>".

    Strips het "Feature-<id>:" prefix en vervangt "Functionele analyse" door
    "Technische Analyse".
    """
    import re as _re
    for line in fa_content.splitlines():
        if line.startswith("# "):
            raw = line[2:].strip()
            # Verwijder "Feature-<id>:" prefix (bv. "Feature-feature-011-foo: ")
            raw = _re.sub(r'^Feature-[^:]+:\s*', '', raw, flags=_re.IGNORECASE)
            # Vervang "Functionele analyse" (varianten) door "Technische Analyse"
            raw = _re.sub(r'Functionele\s+[Aa]nalyse', 'Technische Analyse', raw)
            # Zorg dat het altijd begint met "Technische Analyse"
            if not raw.lower().startswith("technische analyse"):
                raw = f"Technische Analyse - {raw}"
            return raw
    return f"Technische Analyse - {fallback}"


# ── Graph ──────────────────────────────────────────────────────────────────────

def build_graph():
    graph = StateGraph(TAState)

    graph.add_node("classify_fa",               classify_fa)
    graph.add_node("parse_fa",                  parse_fa)
    graph.add_node("generate_domain_model",     generate_domain_model)
    graph.add_node("generate_api_design",       generate_api_design)
    graph.add_node("generate_messaging_design", generate_messaging_design)
    graph.add_node("generate_backend_design",   generate_backend_design)
    graph.add_node("generate_frontend_design",       generate_frontend_design)
    graph.add_node("generate_acceptance_criteria",   generate_acceptance_criteria)
    graph.add_node("generate_traceability",          generate_traceability)
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

    # Na backend: full-stack → frontend, rest-api/event-driven → acceptance criteria
    graph.add_conditional_edges("generate_backend_design", route_after_backend_design)

    # Frontend (altijd) → acceptance criteria
    graph.add_edge("generate_frontend_design",       "generate_acceptance_criteria")

    # Acceptance criteria → traceability
    graph.add_edge("generate_acceptance_criteria",   "generate_traceability")

    graph.add_edge("generate_traceability",          "assemble_ta_json")
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
    parser.add_argument(
        "--context",
        default="",
        help="Extra context voor de agent (bv. technische beslissingen, team conventies).",
    )
    parser.add_argument(
        "--context-dir",
        default="",
        help="Map met extra context bestanden (.md, .txt). Alle bestanden in de map worden geladen.",
    )
    parser.add_argument(
        "--context-files",
        nargs="+",
        default=[],
        metavar="FILE",
        help="Specifieke bestanden als extra context (los van --context-dir). Meerdere bestanden mogelijk.",
    )
    return parser.parse_args()


def main():
    args = parse_args()
    feature_id = args.feature_id

    # AISDLC_REPO_ROOT wordt gezet door fa-to-ta.sh zodat de agent werkt
    # vanuit zowel de parent repo als een child repo via .sdlc-tools
    if os.environ.get("AISDLC_REPO_ROOT"):
        base = Path(os.environ["AISDLC_REPO_ROOT"])
    else:
        base = Path(__file__).parent.parent.parent.parent

    fa_path     = base / "docs" / "functional-analysis" / f"{feature_id}.md"
    ta_md_path  = base / "docs" / "technical-analysis"  / f"{feature_id}.md"
    ta_json_path= base / "docs" / "technical-analysis"  / f"{feature_id}.ta.json"
    schema_path = Path(__file__).parent.parent.parent / "schemas" / "ta.schema.json"

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

    # ── Extra context samenvoegen ──────────────────────────────────────────────
    context_parts = []

    # 1) Inline --context string
    if args.context:
        context_parts.append(args.context)

    # 2) Alle bestanden uit --context-dir
    if args.context_dir:
        context_dir = Path(args.context_dir)
        if not context_dir.is_dir():
            print(f"⚠️  --context-dir niet gevonden: {context_dir}", file=sys.stderr)
        else:
            dir_files = sorted(
                f for f in context_dir.iterdir()
                if f.is_file() and f.suffix in CONTEXT_EXTENSIONS
            )
            for f in dir_files:
                label = "🖼️  Afbeelding" if f.suffix.lower() in IMAGE_EXTENSIONS else "📄 Context"
                if f.suffix.lower() in IMAGE_EXTENSIONS:
                    print(f"  {label} analyseren (dir): {f.name}")
                context_parts.append(f"### {f.name}\n{read_context_file(f)}")
                if f.suffix.lower() not in IMAGE_EXTENSIONS:
                    print(f"  📄 Context geladen (dir): {f.name}")
                else:
                    print(f"  ✅ Afbeelding verwerkt (dir): {f.name}")

    # 3) Specifieke bestanden via --context-files
    for file_path_str in args.context_files:
        f = Path(file_path_str)
        if not f.is_file():
            print(f"⚠️  --context-files bestand niet gevonden: {f}", file=sys.stderr)
            continue
        if f.suffix.lower() in IMAGE_EXTENSIONS:
            print(f"  🖼️  Afbeelding analyseren (file): {f.name}")
        context_parts.append(f"### {f.name}\n{read_context_file(f)}")
        if f.suffix.lower() in IMAGE_EXTENSIONS:
            print(f"  ✅ Afbeelding verwerkt (file): {f.name}")
        else:
            print(f"  📄 Context geladen (file): {f.name}")

    extra_context = "\n\n".join(context_parts)
    if extra_context:
        print()

    app = build_graph()
    final = app.invoke({
        "feature_id":       feature_id,
        "fa_content":       expand_fa_images(fa_path.read_text(), fa_path),
        "fa_path_str":      str(fa_path),
        "ta_path_str":      str(ta_md_path),
        "fa_type":          "",
        "fa_type_manual":   args.fa_type,
        "extra_context":    extra_context,
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
        "tests_design":         {"unit": [], "integration": [], "e2e": []},
        "acceptance_criteria":  [],
        "traceability":         [],
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

    print("\n✅ Klaar!")
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
