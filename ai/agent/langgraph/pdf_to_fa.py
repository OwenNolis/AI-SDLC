"""
PDF → FA Markdown converter.

Converteert een Functionele Analyse in PDF-formaat naar een gestructureerde
Markdown FA die vervolgens door fa_to_ta.py kan worden verwerkt.

Gebruik:
  python pdf_to_fa.py <pdf-pad> <feature-id> [opties]

Argumenten:
  pdf-pad       Pad naar het PDF-bestand
  feature-id    ID voor het uitvoerbestand (bv. feature-011-order-management)

Opties:
  --output-dir  Map waar het .md bestand wordt opgeslagen
                (standaard: docs/functional-analysis naast repo root)
  --lang        Taal van de output: nl (standaard) of en

Omgevingsvariabelen:
  GEMINI_API_KEY  Verplicht
  GEMINI_MODEL    Optioneel (standaard: gemini-2.5-flash-lite)

Output:
  <output-dir>/<feature-id>.md
"""

import argparse
import base64
import os
import re
import sys
from pathlib import Path

from dotenv import load_dotenv
from langchain_core.messages import HumanMessage
from langchain_google_genai import ChatGoogleGenerativeAI

try:
    from pypdf import PdfReader as _PdfReader
    _PYPDF_AVAILABLE = True
except Exception:
    _PdfReader = None  # type: ignore[assignment,misc]
    _PYPDF_AVAILABLE = False

# Laad .env vanuit de repo root
load_dotenv(Path(__file__).parent.parent.parent.parent / ".env")

# Max PDF-grootte die we als base64 naar Gemini sturen (20 MB ongecodeerd)
_MAX_PDF_BYTES_FOR_MULTIMODAL = 20 * 1024 * 1024


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


def _build_prompt(feature_id: str, lang: str, extra_text: str = "") -> str:
    """Bouw de conversie-prompt op."""
    lang_instruction = (
        "Schrijf de output in het Nederlands."
        if lang == "nl"
        else "Write the output in English."
    )

    extra_hint = (
        f"\n\nDe volgende tekst is rechtstreeks uit de PDF geëxtraheerd als aanvulling "
        f"(gebruik dit als fallback als de PDF-inhoud onvolledig is):\n\n{extra_text}\n"
        if extra_text
        else ""
    )

    return f"""Je bent een SDLC-documentatie assistent met expertise in technische documentatie.
Jouw taak: converteer ALLE inhoud van deze PDF Functionele Analyse naar een volledige Markdown FA.
{lang_instruction}
{extra_hint}

Werk in drie fasen:

── FASE 1: INVENTARISEER ELKE PAGINA ──────────────────────────────────────
Ga door elke pagina van de PDF. Stel voor jezelf vast:
- Wat is de exacte sectietitel of heading op deze pagina?
- Bevat de pagina tekst, een diagram, een tabel, een UI-mockup of een combinatie?

── FASE 2: EXTRAHEER VISUELE INHOUD ───────────────────────────────────────
Voor ELKE pagina met een diagram, afbeelding of visuele inhoud — beschrijf de inhoud
VOLLEDIG en EXACT. Sla niets over. Gebruik per diagramtype de volgende aanpak:

- **Database ERD / datamodel**: per tabel een ### heading; vermeld ELKE kolom met naam,
  datatype, nullable/not-null, primary key, foreign keys en relaties (1:1, 1:N, N:M)
- **Deployment diagram**: elke service/node met naam, technologie, poorten en verbindingen
- **Component diagram**: alle componenten, interfaces en afhankelijkheden
- **Sequence diagram**: alle actoren en stappen in volgorde, inclusief retourwaarden
- **Figma / UI-mockup / wireframe**: elk scherm apart; vermeld elk formulierveld,
  label, placeholder, knop, dropdown, validatie, navigatieflow en foutstate
- **Overige afbeeldingen**: beschrijf inhoud volledig

── FASE 3: SCHRIJF DE VOLLEDIGE MARKDOWN FA ──────────────────────────────
Schrijf de FA op basis van fasen 1 en 2. Houd je aan deze VERPLICHTE REGELS:

Structuur:
- Begin met: # Feature-{feature_id}: {{exacte titel uit de PDF}}
- Gebruik de EXACTE sectietitels uit de PDF als ## headings
- Voeg voor elk diagram een eigen ## sectie toe met de exacte PDF-titel
  (bv. ## Database ERD, ## Deployment Diagram, ## Component Diagram,
   ## Sequence Diagram, ## UI Designs, ## Recap)
- Plaats de volledige diagrambeschrijving uit fase 2 in die sectie
- Standaardsecties die aanwezig zijn in de PDF altijd meenemen:
  ## Doel, ## Scope, ## Requirements, ## Business rules, ## Non-functional,
  ## Data, ## API notes, ## Acceptance Criteria, ## UX notes

Inhoud:
- Verzin NIETS — als iets niet in de PDF staat, laat het dan weg
- Behoud alle technische details exact: veldnamen, types, constraints,
  HTTP-methodes, paden, statuscodes, enum-waarden, queries, formules
- Gebruik REQ-/BR-/NFR-/AC-nummering exact zoals in de PDF;
  genereer doorlopende nummering als die ontbreekt
- Laat geen enkel requirement, business rule, acceptance criterion of
  diagramsectie weg

Opmaak:
- Geef de output als RAW Markdown — geen code block omheen
- Gebruik bullet lists voor requirements, business rules en NFRs
- Gebruik tabellen of geneste bullet lists voor diagraminhoud waar dat overzichtelijk is

Het feature-id voor dit document is: {feature_id}
"""


def convert_with_multimodal(pdf_bytes: bytes, feature_id: str, lang: str) -> str:
    """Stuur de PDF als base64 naar Gemini en laat het de FA genereren."""
    print("  📤 PDF versturen naar Gemini (multimodal)...")
    pdf_b64 = base64.standard_b64encode(pdf_bytes).decode()
    prompt = _build_prompt(feature_id, lang)

    message = HumanMessage(content=[
        {
            "type": "image_url",
            "image_url": {"url": f"data:application/pdf;base64,{pdf_b64}"},
        },
        {
            "type": "text",
            "text": prompt,
        },
    ])
    response = get_llm().invoke([message])
    return response.content.strip()


def extract_text_pypdf(pdf_path: Path) -> str:
    """Extraheer ruwe tekst uit de PDF met pypdf als fallback."""
    if not _PYPDF_AVAILABLE:
        return ""
    reader = _PdfReader(pdf_path)
    pages = []
    for i, page in enumerate(reader.pages, 1):
        text = page.extract_text() or ""
        if text.strip():
            pages.append(f"--- Pagina {i} ---\n{text}")
    return "\n\n".join(pages)


def convert_with_text_fallback(pdf_path: Path, feature_id: str, lang: str) -> str:
    """Extraheer tekst met pypdf en laat Gemini de FA structureren."""
    if _PYPDF_AVAILABLE:
        print("  📄 Tekst extraheren met pypdf (fallback voor grote PDF)...")
        raw_text = extract_text_pypdf(pdf_path)
        if not raw_text.strip():
            print("  ⚠️  Geen tekst gevonden in PDF (scan-only?). Resultaat kan leeg zijn.", file=sys.stderr)
    else:
        print("  ⚠️  pypdf niet beschikbaar — PDF wordt zonder tekst-extractie verwerkt.", file=sys.stderr)
        raw_text = ""

    prompt = _build_prompt(feature_id, lang, extra_text=raw_text)
    message = HumanMessage(content=prompt)
    response = get_llm().invoke([message])
    return response.content.strip()


def _strip_code_fence(text: str) -> str:
    """Verwijder eventuele markdown code block wrapper."""
    text = text.strip()
    if text.startswith("```markdown"):
        text = text[len("```markdown"):].lstrip()
    elif text.startswith("```"):
        text = text[3:].lstrip()
    if text.endswith("```"):
        text = text[:-3].rstrip()
    return text.strip()


def _validate_fa_output(text: str) -> list[str]:
    """Basisvalidatie: controleer of de vereiste secties aanwezig zijn."""
    warnings = []
    required_sections = ["## Doel", "## Scope", "## Requirements"]
    for section in required_sections:
        if section not in text:
            warnings.append(f"Sectie '{section}' ontbreekt in de gegenereerde FA")
    if not re.search(r"- REQ-\d{3}:", text):
        warnings.append("Geen requirements gevonden (REQ-NNN formaat)")
    return warnings


def main():
    parser = argparse.ArgumentParser(
        description="Converteer een PDF Functionele Analyse naar Markdown"
    )
    parser.add_argument("pdf_path", help="Pad naar het PDF-bestand")
    parser.add_argument(
        "feature_id",
        help="Feature ID voor het uitvoerbestand (bv. feature-011-order-management)",
    )
    parser.add_argument(
        "--output-dir",
        default="",
        help="Uitvoermap (standaard: docs/functional-analysis naast repo root)",
    )
    parser.add_argument(
        "--lang",
        choices=["nl", "en"],
        default="nl",
        help="Taal van de gegenereerde FA (standaard: nl)",
    )
    args = parser.parse_args()

    pdf_path = Path(args.pdf_path).resolve()
    if not pdf_path.exists():
        print(f"❌ PDF niet gevonden: {pdf_path}", file=sys.stderr)
        sys.exit(1)
    if pdf_path.suffix.lower() != ".pdf":
        print(f"❌ Bestand is geen PDF: {pdf_path}", file=sys.stderr)
        sys.exit(1)

    # Bepaal uitvoermap
    if args.output_dir:
        output_dir = Path(args.output_dir)
    else:
        if os.environ.get("AISDLC_REPO_ROOT"):
            base = Path(os.environ["AISDLC_REPO_ROOT"])
        else:
            base = Path(__file__).parent.parent.parent.parent
        output_dir = base / "docs" / "functional-analysis"

    output_dir.mkdir(parents=True, exist_ok=True)
    output_path = output_dir / f"{args.feature_id}.md"

    print("==============================================")
    print("AI-SDLC — PDF → FA Markdown converter")
    print(f"PDF    : {pdf_path.name}")
    print(f"Output : {output_path}")
    print("==============================================\n")

    # Converteer
    pdf_bytes = pdf_path.read_bytes()
    pdf_size = len(pdf_bytes)
    if _PYPDF_AVAILABLE:
        num_pages = len(_PdfReader(pdf_path).pages)
        print(f"📂 PDF geladen: {pdf_size / 1024:.1f} KB, {num_pages} pagina('s)")
    else:
        print(f"📂 PDF geladen: {pdf_size / 1024:.1f} KB")

    if pdf_size <= _MAX_PDF_BYTES_FOR_MULTIMODAL:
        fa_markdown = convert_with_multimodal(pdf_bytes, args.feature_id, args.lang)
    else:
        print(f"  ℹ️  PDF groter dan {_MAX_PDF_BYTES_FOR_MULTIMODAL // (1024*1024)} MB — tekst-extractie modus")
        fa_markdown = convert_with_text_fallback(pdf_path, args.feature_id, args.lang)

    fa_markdown = _strip_code_fence(fa_markdown)

    # Valideer output
    warnings = _validate_fa_output(fa_markdown)
    if warnings:
        print("\n⚠️  Waarschuwingen:")
        for w in warnings:
            print(f"   - {w}")

    # Schrijf output
    output_path.write_text(fa_markdown, encoding="utf-8")
    print(f"\n✅ FA Markdown opgeslagen: {output_path}")
    print("\nVolgende stap:")
    print(f"  python fa_to_ta.py {args.feature_id}")


if __name__ == "__main__":
    main()
