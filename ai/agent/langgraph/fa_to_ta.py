"""
LangGraph FA→TA agent.

Gebruik:
  python fa_to_ta.py <feature-id> [--fa-skeleton PATH] [--ta-skeleton PATH]

Argumenten:
  feature-id        ID van de feature (bv. feature-001-support-ticket)
  --fa-skeleton     Pad naar FA skelet (standaard: templates/fa_skeleton.md)
  --ta-skeleton     Pad naar TA skelet (standaard: templates/ta_skeleton.md)

Omgevingsvariabelen:
  GEMINI_API_KEY    Verplicht
  GEMINI_MODEL      Optioneel (standaard: gemini-2.5-flash-lite)
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

class TAState(TypedDict):
    # Inputs
    feature_id: str
    fa_content: str
    fa_skeleton: str
    ta_skeleton: str
    ta_schema: dict
    current_ta: dict
    current_flow: dict
    current_ctx: str
    # Tussenresultaten
    requirements: list
    domain_model: dict
    api_design: dict
    backend_design: dict
    frontend_design: dict
    tests_design: dict
    # Outputs
    ta_json: dict
    flow_json: dict
    test_context: str
    # Beheer
    validation_errors: list
    retry_count: int
    notes: str


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
        text = text[text.find("```json") + 7:]
        text = text[:text.rfind("```")]
    elif "```" in text:
        text = text[text.find("```") + 3:]
        text = text[:text.rfind("```")]

    start = text.find("{")
    end = text.rfind("}") + 1
    if start == -1 or end == 0:
        raise ValueError(f"Geen JSON gevonden in LLM response:\n{text[:500]}")

    return json.loads(text[start:end])


# ── Nodes ──────────────────────────────────────────────────────────────────────

def extract_requirements(state: TAState) -> dict:
    """Node 1: Extraheer gestructureerde requirements uit de FA."""
    print("🔍 Requirements extraheren uit FA...")

    skeleton_hint = (
        f"\nFA skelet (gebruik dit als structuurreferentie):\n{state['fa_skeleton']}"
        if state.get("fa_skeleton")
        else ""
    )

    prompt = f"""Je bent een SDLC-analyse agent.

Lees de Functionele Analyse en extraheer ALLE requirements als gestructureerde objecten.
{skeleton_hint}

FA inhoud:
---
{state["fa_content"]}
---

Bestaande requirements (houd IDs stabiel, geen duplicaten):
{json.dumps(state["current_ta"].get("requirements", []), indent=2)}

Geef ALLEEN een JSON object terug:
{{
  "requirements": [
    {{"id": "REQ-001", "text": "...", "priority": "must|should|could"}}
  ],
  "notes": "korte samenvatting van wijzigingen"
}}

Regels:
- IDs voldoen aan patroon REQ-NNN (drie cijfers, bv. REQ-001)
- priority is exact: must, should of could
- Geen duplicaten van bestaande requirements
- Houd bestaande IDs stabiel
"""
    result = llm_json(prompt)
    return {
        "requirements": result.get("requirements", []),
        "notes": result.get("notes", ""),
    }


def generate_domain_model(state: TAState) -> dict:
    """Node 2: Genereer domain model op basis van requirements."""
    print("🏗️  Domain model genereren...")

    prompt = f"""Je bent een SDLC-analyse agent.

Genereer een domain model voor een Java/Spring Boot applicatie op basis van deze requirements.

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
          "type": "String|Long|Integer|Boolean|LocalDateTime",
          "constraints": ["notNull", "minLength:5", "maxLength:255"],
          "testCases": ["empty", "too_short", "too_long", "missing", "invalid_value"]
        }}
      ]
    }}
  ]
}}

Regels:
- testCases ALLEEN uit: empty, too_short, too_long, missing, invalid_value, duplicate_per_day
- Stack: Java/Spring Boot
- Geen extra velden buiten name, type, constraints en testCases
"""
    return {"domain_model": llm_json(prompt)}


def generate_api_design(state: TAState) -> dict:
    """Node 3: Genereer API design op basis van domain model."""
    print("🔌 API design genereren...")

    prompt = f"""Je bent een SDLC-analyse agent.

Genereer het REST API design op basis van dit domain model en deze requirements.

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
      "path": "/api/...",
      "summary": "...",
      "request": {{
        "bodySchemaRef": "CreateXxxRequest",
        "validationRules": ["veld: validatieregel"]
      }},
      "responses": [
        {{"status": 201, "bodySchemaRef": "XxxResponse", "notes": "succesvol aangemaakt"}},
        {{"status": 400, "bodySchemaRef": "ApiError", "notes": "validatiefout"}},
        {{"status": 500, "bodySchemaRef": "ApiError", "notes": "server error"}}
      ]
    }}
  ]
}}

Regels:
- method EXACT: GET, POST, PUT, PATCH of DELETE
- Stack: Spring Boot REST
- Geen extra velden
"""
    return {"api_design": llm_json(prompt)}


def generate_backend_frontend(state: TAState) -> dict:
    """Node 4: Genereer backend modules en frontend componenten."""
    print("⚙️  Backend en frontend design genereren...")

    skeleton_hint = (
        f"\nTA skelet (gebruik dit als structuurreferentie voor secties 6 en 7):\n{state['ta_skeleton']}"
        if state.get("ta_skeleton")
        else ""
    )

    prompt = f"""Je bent een SDLC-analyse agent.

Genereer de backend modulestructuur en frontend componentenlijst.
{skeleton_hint}

API design:
{json.dumps(state["api_design"], indent=2)}

Domain model:
{json.dumps(state["domain_model"], indent=2)}

Geef ALLEEN een JSON object terug:
{{
  "backend": {{
    "modules": [
      {{
        "name": "moduleName",
        "classes": [
          {{"name": "ClassName", "responsibility": "..."}}
        ]
      }}
    ]
  }},
  "frontend": {{
    "routes": ["/pad"],
    "components": ["ComponentName"]
  }},
  "tests": {{
    "unit": ["ClassName.methodName"],
    "integration": ["POST /api/... → 201"],
    "e2e": ["Gebruikersflow beschrijving"]
  }}
}}
"""
    result = llm_json(prompt)
    return {
        "backend_design": result.get("backend", {}),
        "frontend_design": result.get("frontend", {}),
        "tests_design": result.get("tests", {"unit": [], "integration": [], "e2e": []}),
    }


def assemble_ta_json(state: TAState) -> dict:
    """Node 5: Assembleer de volledige TA JSON uit alle losse delen."""
    print("📦 TA JSON assembleren...")

    existing_meta = state["current_ta"].get("meta", {})

    all_classes = [
        cls["name"]
        for module in state["backend_design"].get("modules", [])
        for cls in module.get("classes", [])
    ]
    all_components = state["frontend_design"].get("components", [])

    traceability = [
        {
            "reqId": req["id"],
            "backendRefs": all_classes[:2] if all_classes else [],
            "frontendRefs": all_components[:1] if all_components else [],
            "testRefs": [f"test_{req['id'].lower().replace('-', '_')}"],
        }
        for req in state["requirements"]
    ]

    ta = {
        "meta": {
            "featureId": state["feature_id"],
            "title": existing_meta.get("title", state["feature_id"]),
            "version": existing_meta.get("version", "1.0.0"),
            "createdAt": existing_meta.get("createdAt", ""),
        },
        "scope": state["current_ta"].get("scope", {"inScope": [], "outOfScope": []}),
        "assumptions": state["current_ta"].get("assumptions", []),
        "openQuestions": state["current_ta"].get("openQuestions", []),
        "requirements": state["requirements"],
        "domain": state["domain_model"],
        "api": state["api_design"],
        "backend": state["backend_design"],
        "frontend": state["frontend_design"],
        "tests": state["tests_design"],
        "traceability": traceability,
    }

    return {"ta_json": ta}


def generate_flow_and_ctx(state: TAState) -> dict:
    """Node 6: Genereer flow scenarios en test context."""
    print("🔄 Flow scenarios en test context genereren...")

    prompt = f"""Je bent een SDLC-analyse agent.

Werk de flow test scenarios en test context bij op basis van deze Technische Analyse.

Requirements:
{json.dumps(state["requirements"], indent=2)}

API endpoints:
{json.dumps(state["api_design"].get("endpoints", []), indent=2)}

Bestaande flow JSON (bewaar structuur en bestaande IDs):
{json.dumps(state["current_flow"], indent=2)}

Geef ALLEEN een JSON object terug:
{{
  "flowJson": {{
    "meta": {{"featureId": "{state["feature_id"]}", "version": "1.0.0"}},
    "flows": [
      {{
        "id": "FLOW-001",
        "name": "...",
        "preconditions": ["..."],
        "steps": [
          {{"actor": "user|system", "action": "...", "expected": "..."}}
        ],
        "variants": []
      }}
    ]
  }},
  "testContextMd": "# Test Context\\n\\n## Aandachtspunten\\n- ..."
}}

Regels:
- Bewaar bestaande flow IDs en meta
- Geen duplicaten (zelfde id)
"""
    result = llm_json(prompt)
    flow = result.get("flowJson", state["current_flow"])

    # Dedupliceer flows op id
    if isinstance(flow.get("flows"), list):
        seen: set = set()
        flow["flows"] = [
            f for f in flow["flows"]
            if f.get("id") not in seen and not seen.add(f.get("id"))
        ]

    return {
        "flow_json": flow,
        "test_context": result.get("testContextMd", ""),
    }


def validate_schema(state: TAState) -> dict:
    """Node 7: Valideer de TA JSON tegen ta.schema.json."""
    print("✅ Valideren tegen schema...")

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
    """Node 8: Herstel validatiefouten via gerichte Gemini-call."""
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


# ── Conditionele edge ──────────────────────────────────────────────────────────

def after_validation(state: TAState) -> str:
    if state["validation_errors"] and state.get("retry_count", 0) < 3:
        return "self_correct"
    return END


# ── Graph ──────────────────────────────────────────────────────────────────────

def build_graph():
    graph = StateGraph(TAState)

    graph.add_node("extract_requirements",      extract_requirements)
    graph.add_node("generate_domain_model",     generate_domain_model)
    graph.add_node("generate_api_design",       generate_api_design)
    graph.add_node("generate_backend_frontend", generate_backend_frontend)
    graph.add_node("assemble_ta_json",          assemble_ta_json)
    graph.add_node("generate_flow_and_ctx",     generate_flow_and_ctx)
    graph.add_node("validate_schema",           validate_schema)
    graph.add_node("self_correct",              self_correct)

    graph.add_edge(START,                        "extract_requirements")
    graph.add_edge("extract_requirements",       "generate_domain_model")
    graph.add_edge("generate_domain_model",      "generate_api_design")
    graph.add_edge("generate_api_design",        "generate_backend_frontend")
    graph.add_edge("generate_backend_frontend",  "assemble_ta_json")
    graph.add_edge("assemble_ta_json",           "generate_flow_and_ctx")
    graph.add_edge("generate_flow_and_ctx",      "validate_schema")
    graph.add_conditional_edges("validate_schema", after_validation)
    graph.add_edge("self_correct",               "assemble_ta_json")

    return graph.compile()


# ── Main ───────────────────────────────────────────────────────────────────────

def parse_args():
    parser = argparse.ArgumentParser(description="LangGraph FA→TA agent")
    parser.add_argument("feature_id", help="Feature ID (bv. feature-001-support-ticket)")
    parser.add_argument(
        "--fa-skeleton",
        default=str(Path(__file__).parent / "templates" / "fa_skeleton.md"),
        help="Pad naar FA skelet (standaard: templates/fa_skeleton.md)",
    )
    parser.add_argument(
        "--ta-skeleton",
        default=str(Path(__file__).parent / "templates" / "ta_skeleton.md"),
        help="Pad naar TA skelet (standaard: templates/ta_skeleton.md)",
    )
    return parser.parse_args()


def main():
    args = parse_args()
    feature_id = args.feature_id

    base = Path(__file__).parent.parent.parent.parent
    fa_path     = base / "docs" / "functional-analysis" / f"{feature_id}.md"
    ta_path     = base / "docs" / "technical-analysis"  / f"{feature_id}.ta.json"
    flow_path   = base / "docs" / "test-scenarios"      / f"{feature_id}.flow.json"
    ctx_path    = base / "docs" / "test-context"        / f"{feature_id}.md"
    schema_path = base / "ai"   / "schemas"             / "ta.schema.json"

    print("==============================================")
    print("AI-SDLC FLOW — LangGraph FA→TA")
    print(f"Feature: {feature_id}")
    print("==============================================")

    if not fa_path.exists():
        print(f"❌ FA niet gevonden: {fa_path}", file=sys.stderr)
        sys.exit(1)
    if not schema_path.exists():
        print(f"❌ Schema niet gevonden: {schema_path}", file=sys.stderr)
        sys.exit(1)

    # Laad skeletten
    fa_skeleton_path = Path(args.fa_skeleton)
    ta_skeleton_path = Path(args.ta_skeleton)
    fa_skeleton = fa_skeleton_path.read_text() if fa_skeleton_path.exists() else ""
    ta_skeleton = ta_skeleton_path.read_text() if ta_skeleton_path.exists() else ""

    if fa_skeleton:
        print(f"📋 FA skelet: {fa_skeleton_path.name}")
    if ta_skeleton:
        print(f"📋 TA skelet: {ta_skeleton_path.name}")

    current_ta   = json.loads(ta_path.read_text())   if ta_path.exists()   else {}
    current_flow = json.loads(flow_path.read_text()) if flow_path.exists() else {}
    current_ctx  = ctx_path.read_text()              if ctx_path.exists()  else ""

    app = build_graph()
    final = app.invoke({
        "feature_id":      feature_id,
        "fa_content":      fa_path.read_text(),
        "fa_skeleton":     fa_skeleton,
        "ta_skeleton":     ta_skeleton,
        "ta_schema":       json.loads(schema_path.read_text()),
        "current_ta":      current_ta,
        "current_flow":    current_flow,
        "current_ctx":     current_ctx,
        "requirements":    [],
        "domain_model":    {},
        "api_design":      {},
        "backend_design":  {},
        "frontend_design": {},
        "tests_design":    {"unit": [], "integration": [], "e2e": []},
        "ta_json":         {},
        "flow_json":       {},
        "test_context":    "",
        "validation_errors": [],
        "retry_count":     0,
        "notes":           "",
    })

    # Schrijf outputs weg
    for p in [ta_path, flow_path, ctx_path]:
        p.parent.mkdir(parents=True, exist_ok=True)

    ta_path.write_text(json.dumps(final["ta_json"], indent=2) + "\n")
    flow_path.write_text(json.dumps(final["flow_json"], indent=2) + "\n")
    ctx_path.write_text(final["test_context"])

    if final["validation_errors"]:
        print(f"\n⚠️  Klaar met {len(final['validation_errors'])} onopgeloste validatiefout(en):")
        for err in final["validation_errors"]:
            print(f"   - {err}")
        sys.exit(1)

    print(f"\n✅ FA→TA sync voltooid voor {feature_id}")
    if final["notes"]:
        print(f"📝 {final['notes']}")


if __name__ == "__main__":
    main()
