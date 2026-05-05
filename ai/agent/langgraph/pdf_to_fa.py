"""
PDF → FA Markdown converter.

Converteert een Functionele Analyse in PDF-formaat naar een gestructureerde
Markdown FA die vervolgens door fa_to_ta.py kan worden verwerkt.
Diagrammen en UI-designs worden als afbeeldingen geëxtraheerd en inline
in de Markdown opgenomen.

Gebruik:
  python pdf_to_fa.py <pdf-pad> <feature-id> [opties]

Argumenten:
  pdf-pad       Pad naar het PDF-bestand
  feature-id    ID voor het uitvoerbestand (bv. feature-011-order-management)

Opties:
  --output-dir  Map waar het .md bestand wordt opgeslagen
                (standaard: docs/functional-analysis naast repo root)
  --lang        Taal van de output: nl (standaard) of en
  --dpi         Resolutie voor pagina-rendering (standaard: 150)

Vereiste systeemtools (één van onderstaande):
  pdftoppm      Onderdeel van poppler: brew install poppler
  pymupdf       pip install pymupdf  (werkt niet op Python 3.14 door pyexpat bug)
  gs            Ghostscript: brew install ghostscript

Omgevingsvariabelen:
  GEMINI_API_KEY  Verplicht
  GEMINI_MODEL    Optioneel (standaard: gemini-2.5-flash-lite)

Output:
  <output-dir>/<feature-id>.md
  <output-dir>/<feature-id>/page-N.png   (één per pagina)
"""

import argparse
import base64
import os
import re
import shutil
import subprocess
import sys
from pathlib import Path

from dotenv import load_dotenv
from langchain_core.messages import HumanMessage
from langchain_google_genai import ChatGoogleGenerativeAI

# Optional: pymupdf (fails on Python 3.14 due to pyexpat bug, works on 3.12/3.13)
try:
    import fitz as _fitz
    _PYMUPDF_AVAILABLE = True
except Exception:
    _fitz = None  # type: ignore[assignment]
    _PYMUPDF_AVAILABLE = False

# Optional: pypdf (same pyexpat issue on 3.14, used only for page count)
try:
    from pypdf import PdfReader as _PdfReader
    _PYPDF_AVAILABLE = True
except Exception:
    _PdfReader = None  # type: ignore[assignment,misc]
    _PYPDF_AVAILABLE = False

# Laad .env vanuit de repo root
load_dotenv(Path(__file__).parent.parent.parent.parent / ".env")

# Max PDF-grootte voor de blob-fallback (20 MB)
_MAX_PDF_BYTES_FOR_BLOB = 20 * 1024 * 1024


# ── Rendering backend detectie ─────────────────────────────────────────────────

def _detect_render_backend() -> str | None:
    if _PYMUPDF_AVAILABLE:
        return "pymupdf"
    if shutil.which("pdftoppm"):
        return "pdftoppm"
    if shutil.which("gs"):
        return "ghostscript"
    return None


_RENDER_BACKEND: str | None = _detect_render_backend()


# ── PDF pagina's renderen als PNG ──────────────────────────────────────────────

def render_pdf_pages(pdf_path: Path, output_dir: Path, dpi: int = 150) -> list[Path]:
    """
    Render elke PDF-pagina als een PNG-afbeelding.
    Geeft een gesorteerde lijst van padnamen terug (page-1.png, page-2.png, …).
    """
    output_dir.mkdir(parents=True, exist_ok=True)

    if _RENDER_BACKEND == "pymupdf":
        return _render_pymupdf(pdf_path, output_dir, dpi)
    if _RENDER_BACKEND == "pdftoppm":
        return _render_pdftoppm(pdf_path, output_dir, dpi)
    if _RENDER_BACKEND == "ghostscript":
        return _render_ghostscript(pdf_path, output_dir, dpi)
    return []


def _render_pymupdf(pdf_path: Path, output_dir: Path, dpi: int) -> list[Path]:
    doc = _fitz.open(str(pdf_path))
    scale = dpi / 72
    paths: list[Path] = []
    for i, page in enumerate(doc, 1):
        mat = _fitz.Matrix(scale, scale)
        pix = page.get_pixmap(matrix=mat)
        out = output_dir / f"page-{i}.png"
        pix.save(str(out))
        paths.append(out)
    doc.close()
    return paths


def _render_pdftoppm(pdf_path: Path, output_dir: Path, dpi: int) -> list[Path]:
    prefix = str(output_dir / "page")
    subprocess.run(
        ["pdftoppm", "-r", str(dpi), "-png", str(pdf_path), prefix],
        check=True,
        capture_output=True,
    )
    # pdftoppm schrijft page-1.png, page-2.png, … of page-01.png afhankelijk van versie
    raw = sorted(output_dir.glob("page-*.png"), key=lambda p: p.name)
    # Normaliseer naar page-1.png, page-2.png, …
    normalized: list[Path] = []
    for i, src in enumerate(raw, 1):
        dst = output_dir / f"page-{i}.png"
        if src != dst:
            src.rename(dst)
        normalized.append(dst)
    return normalized


def _render_ghostscript(pdf_path: Path, output_dir: Path, dpi: int) -> list[Path]:
    output_pattern = str(output_dir / "page-%d.png")
    subprocess.run(
        [
            "gs", "-dBATCH", "-dNOPAUSE", "-dQUIET",
            "-sDEVICE=png16m", f"-r{dpi}",
            f"-sOutputFile={output_pattern}",
            str(pdf_path),
        ],
        check=True,
        capture_output=True,
    )
    return sorted(output_dir.glob("page-*.png"), key=lambda p: p.name)


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


# ── Prompts ────────────────────────────────────────────────────────────────────

def _build_prompt_pages(feature_id: str, lang: str, num_pages: int, img_dir_name: str) -> str:
    """Prompt voor conversie op basis van individuele pagina-afbeeldingen."""
    lang_instruction = (
        "Schrijf de output in het Nederlands."
        if lang == "nl"
        else "Write the output in English."
    )

    return f"""Je bent een SDLC-documentatie assistent met expertise in technische documentatie.
Jouw taak: converteer ALLE inhoud van deze PDF naar een volledige Markdown FA.
{lang_instruction}

Je ontvangt {num_pages} afbeeldingen. Elke afbeelding is één pagina van de PDF:
Afbeelding 1 = Pagina 1, Afbeelding 2 = Pagina 2, …, Afbeelding {num_pages} = Pagina {num_pages}.

Werk in drie fasen:

── FASE 1: INVENTARISEER ELKE PAGINA ──────────────────────────────────────────
Ga door elke afbeelding. Stel voor jezelf vast:
- Wat is de exacte sectietitel of heading op deze pagina?
- Bevat de pagina tekst, een diagram, een tabel, een UI-mockup of een combinatie?

── FASE 2: EXTRAHEER VISUELE INHOUD ───────────────────────────────────────────
Voor ELKE pagina met een diagram, afbeelding of UI-ontwerp — beschrijf de inhoud
VOLLEDIG en EXACT. Per diagramtype:

- **Database ERD / datamodel**: per tabel/entiteit: alle kolommen, datatypen,
  nullable/not-null, primary keys, foreign keys en relaties (1:1, 1:N, N:M)
- **Deployment diagram**: elke service/node met naam, technologie, poort, verbindingen
- **Component diagram**: alle componenten, interfaces en afhankelijkheden
- **Sequence diagram**: alle actoren en stappen in volgorde met retourwaarden
- **Figma / UI-mockup / wireframe**: elk scherm apart; elk formulierveld, label,
  placeholder, knop, dropdown, validatie, navigatieflow en foutstate
- **Overige afbeeldingen**: beschrijf inhoud volledig

── FASE 3: SCHRIJF DE VOLLEDIGE MARKDOWN FA ────────────────────────────────────
VERPLICHTE REGELS:

Afbeeldingen insluiten:
- Voor elke pagina die een diagram of UI-design bevat: voeg DIRECT NA de sectietitel
  de volgende Markdown-afbeeldingsreferentie in:
  ![<exacte diagramtitel uit de PDF>]({img_dir_name}/page-N.png)
  waarbij N het paginanummer is (bv. page-3.png voor pagina 3)
- Beschrijf daarna alsnog de volledige inhoud van het diagram in tekst
- Tekstpagina's krijgen GEEN afbeeldingsreferentie

Structuur:
- Begin met: # Feature-{feature_id}: {{exacte titel uit de PDF}}
- Gebruik de EXACTE sectietitels uit de PDF als ## headings
- Voeg voor elk diagram/UI-design een eigen ## sectie toe met de exacte PDF-titel
  (bv. ## Database ERD, ## Deployment Diagram, ## Component Diagram,
   ## Sequence Diagram, ## UI Designs, ## Recap)
- Standaardsecties (als aanwezig in PDF): ## Doel, ## Scope, ## Requirements,
  ## Business rules, ## Non-functional, ## Data, ## API notes,
  ## Acceptance Criteria, ## UX notes

Inhoud:
- Verzin NIETS — als iets niet in de PDF staat, laat het dan weg
- Behoud alle technische details exact: veldnamen, types, constraints,
  HTTP-methodes, paden, statuscodes, enum-waarden
- Gebruik REQ-/BR-/NFR-/AC-nummering exact zoals in de PDF;
  genereer doorlopende nummering als die ontbreekt
- Laat geen enkel requirement, business rule, acceptance criterion of
  diagramsectie weg

Opmaak:
- Geef de output als RAW Markdown — geen code block omheen
- Gebruik bullet lists voor requirements, business rules en NFRs
- Gebruik tabellen of geneste bullet lists voor diagraminhoud

Het feature-id voor dit document is: {feature_id}
"""


def _build_prompt_blob(feature_id: str, lang: str) -> str:
    """Fallback prompt wanneer pagina-rendering niet beschikbaar is."""
    lang_instruction = (
        "Schrijf de output in het Nederlands."
        if lang == "nl"
        else "Write the output in English."
    )

    return f"""Je bent een SDLC-documentatie assistent met expertise in technische documentatie.
Jouw taak: converteer ALLE inhoud van deze PDF naar een volledige Markdown FA.
{lang_instruction}

Werk in drie fasen:

── FASE 1: INVENTARISEER ELKE PAGINA ──────────────────────────────────────────
Ga door elke pagina van de PDF. Stel voor jezelf vast:
- Wat is de exacte sectietitel of heading op deze pagina?
- Bevat de pagina tekst, een diagram, een tabel, een UI-mockup of een combinatie?

── FASE 2: EXTRAHEER VISUELE INHOUD ───────────────────────────────────────────
Voor ELKE pagina met een diagram, afbeelding of UI-ontwerp:

- **Database ERD / datamodel**: per tabel/entiteit alle kolommen, datatypen,
  keys en relaties
- **Deployment diagram**: elke service/node met naam, technologie, verbindingen
- **Component diagram**: alle componenten en afhankelijkheden
- **Sequence diagram**: alle actoren en stappen in volgorde
- **Figma / UI-mockup / wireframe**: elk scherm, veld, knop, validatie en flow
- **Overige afbeeldingen**: beschrijf inhoud volledig

── FASE 3: SCHRIJF DE VOLLEDIGE MARKDOWN FA ────────────────────────────────────
VERPLICHTE REGELS:
- Begin met: # Feature-{feature_id}: {{exacte titel uit de PDF}}
- Gebruik de EXACTE sectietitels uit de PDF als ## headings
- Voeg voor elk diagram een eigen ## sectie toe met de exacte PDF-titel
- Beschrijf elk diagram volledig in tekst in die sectie
- Standaardsecties: ## Doel, ## Scope, ## Requirements, ## Business rules,
  ## Non-functional, ## Data, ## API notes, ## Acceptance Criteria, ## UX notes
- Verzin NIETS — als iets niet in de PDF staat, laat het dan weg
- Behoud alle technische details exact
- Geef de output als RAW Markdown — geen code block omheen

Het feature-id voor dit document is: {feature_id}
"""


# ── Conversie ──────────────────────────────────────────────────────────────────

def convert_with_page_images(
    page_images: list[Path],
    feature_id: str,
    lang: str,
    img_dir_name: str,
) -> str:
    """Stuur alle pagina-afbeeldingen naar Gemini en genereer de FA Markdown."""
    print(f"  📤 {len(page_images)} pagina-afbeeldingen versturen naar Gemini...")
    content: list[dict] = []

    for img_path in page_images:
        img_b64 = base64.standard_b64encode(img_path.read_bytes()).decode()
        content.append({
            "type": "image_url",
            "image_url": {"url": f"data:image/png;base64,{img_b64}"},
        })

    content.append({
        "type": "text",
        "text": _build_prompt_pages(feature_id, lang, len(page_images), img_dir_name),
    })

    response = get_llm().invoke([HumanMessage(content=content)])
    return response.content.strip()


def convert_with_pdf_blob(pdf_bytes: bytes, feature_id: str, lang: str) -> str:
    """Stuur de PDF als blob naar Gemini (fallback zonder rendering)."""
    print("  📤 PDF versturen naar Gemini (blob fallback — geen afbeeldingen)...")
    pdf_b64 = base64.standard_b64encode(pdf_bytes).decode()

    message = HumanMessage(content=[
        {
            "type": "image_url",
            "image_url": {"url": f"data:application/pdf;base64,{pdf_b64}"},
        },
        {
            "type": "text",
            "text": _build_prompt_blob(feature_id, lang),
        },
    ])
    response = get_llm().invoke([message])
    return response.content.strip()


# ── Hulpfuncties ───────────────────────────────────────────────────────────────

def _strip_code_fence(text: str) -> str:
    text = text.strip()
    if text.startswith("```markdown"):
        text = text[len("```markdown"):].lstrip()
    elif text.startswith("```"):
        text = text[3:].lstrip()
    if text.endswith("```"):
        text = text[:-3].rstrip()
    return text.strip()


def _validate_fa_output(text: str) -> list[str]:
    warnings = []
    for section in ("## Doel", "## Scope", "## Requirements"):
        if section not in text:
            warnings.append(f"Sectie '{section}' ontbreekt in de gegenereerde FA")
    if not re.search(r"- REQ-\d{3}:", text):
        warnings.append("Geen requirements gevonden (REQ-NNN formaat)")
    return warnings


def _count_pdf_pages(pdf_path: Path) -> int | None:
    if _PYPDF_AVAILABLE:
        try:
            return len(_PdfReader(pdf_path).pages)
        except Exception:
            pass
    # Fallback: use pdftoppm to count (render to /dev/null and count output)
    if shutil.which("pdfinfo"):
        try:
            result = subprocess.run(
                ["pdfinfo", str(pdf_path)], capture_output=True, text=True
            )
            for line in result.stdout.splitlines():
                if line.lower().startswith("pages:"):
                    return int(line.split(":")[-1].strip())
        except Exception:
            pass
    return None


# ── Main ───────────────────────────────────────────────────────────────────────

def main():
    parser = argparse.ArgumentParser(
        description="Converteer een PDF Functionele Analyse naar Markdown met inline afbeeldingen"
    )
    parser.add_argument("pdf_path", help="Pad naar het PDF-bestand")
    parser.add_argument(
        "feature_id",
        help="Feature ID (bv. feature-011-order-management)",
    )
    parser.add_argument(
        "--output-dir",
        default="",
        help="Uitvoermap voor het .md bestand (standaard: docs/functional-analysis)",
    )
    parser.add_argument(
        "--lang",
        choices=["nl", "en"],
        default="nl",
        help="Taal van de gegenereerde FA (standaard: nl)",
    )
    parser.add_argument(
        "--dpi",
        type=int,
        default=150,
        help="Resolutie voor pagina-rendering in DPI (standaard: 150)",
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
    img_dir = output_dir / args.feature_id          # bv. docs/functional-analysis/feature-011/
    img_dir_name = args.feature_id                  # relatief pad in de Markdown

    print("==============================================")
    print("AI-SDLC — PDF → FA Markdown converter")
    print(f"PDF    : {pdf_path.name}")
    print(f"Output : {output_path}")
    print(f"Images : {img_dir}/")
    print(f"Render : {_RENDER_BACKEND or 'geen (blob fallback)'}")
    print("==============================================\n")

    pdf_bytes = pdf_path.read_bytes()
    pdf_size = len(pdf_bytes)
    num_pages = _count_pdf_pages(pdf_path)
    page_label = f", {num_pages} pagina('s)" if num_pages else ""
    print(f"📂 PDF geladen: {pdf_size / 1024:.1f} KB{page_label}")

    # ── Stap 1: pagina's renderen ──────────────────────────────────────────────
    page_images: list[Path] = []
    if _RENDER_BACKEND:
        print(f"\n🖼️  Pagina's renderen als PNG ({args.dpi} DPI)...")
        try:
            page_images = render_pdf_pages(pdf_path, img_dir, dpi=args.dpi)
            print(f"  ✅ {len(page_images)} pagina-afbeeldingen opgeslagen in {img_dir.name}/")
        except Exception as e:
            print(f"  ⚠️  Rendering mislukt ({e}) — blob fallback wordt gebruikt", file=sys.stderr)
            page_images = []
    else:
        print(
            "\n⚠️  Geen rendering tool beschikbaar (pymupdf/pdftoppm/gs).\n"
            "   Installeer poppler voor afbeeldingen: brew install poppler\n"
            "   Blob-modus: diagrammen worden beschreven maar niet als afbeelding opgeslagen.",
            file=sys.stderr,
        )

    # ── Stap 2: FA genereren ───────────────────────────────────────────────────
    print("\n🤖 FA Markdown genereren...")
    if page_images:
        fa_markdown = convert_with_page_images(page_images, args.feature_id, args.lang, img_dir_name)
    elif pdf_size <= _MAX_PDF_BYTES_FOR_BLOB:
        fa_markdown = convert_with_pdf_blob(pdf_bytes, args.feature_id, args.lang)
    else:
        print(f"❌ PDF te groot ({pdf_size // (1024*1024)} MB) en geen rendering beschikbaar.", file=sys.stderr)
        sys.exit(1)

    fa_markdown = _strip_code_fence(fa_markdown)

    # ── Stap 3: valideren en schrijven ────────────────────────────────────────
    warnings = _validate_fa_output(fa_markdown)
    if warnings:
        print("\n⚠️  Waarschuwingen:")
        for w in warnings:
            print(f"   - {w}")

    output_path.write_text(fa_markdown, encoding="utf-8")

    print(f"\n✅ FA Markdown opgeslagen : {output_path}")
    if page_images:
        print(f"✅ Pagina-afbeeldingen    : {img_dir}/ ({len(page_images)} bestanden)")
    print("\nVolgende stap:")
    print(f"  python fa_to_ta.py {args.feature_id}")


if __name__ == "__main__":
    main()
