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

# Stap 1a: classificeer welke pagina's visuele content hebben (alle pagina's tegelijk)
_CLASSIFY_PROMPT = """\
Je ontvangt {n} afbeeldingen. Elke afbeelding is één pagina van een PDF.
Afbeelding 1 = Pagina 1, Afbeelding 2 = Pagina 2, …, Afbeelding {n} = Pagina {n}.

Geef voor elke pagina aan of ze ECHTE visuele content bevatten.

VISUEEL (telt mee) — de pagina bevat minstens één van:
  • Een diagram met vormen, pijlen of verbindingen (ERD, sequence, component, deployment)
  • Een UI-mockup, wireframe of Figma-schermontwerp met herkenbare interface-elementen
  • Een grafiek of technisch schermontwerp

GEEN VISUEEL (telt NIET mee) — de pagina bestaat uitsluitend uit:
  • Lopende tekst, titels of subtitels
  • Bullet lists, genummerde lijsten of acceptance-criteria opsommingen
  • Tabellen van tekstuele data (ook als ze gekleurde headers hebben)
  • Requirements, business rules, NFR's of AC's als tekstblokken
  • Paginatitels, inhoudsopgave of sectiekoppen

Twijfelregel: als de pagina ALLEEN tekst, lijsten of tekst-tabellen bevat —
ook met opmaak, kleur of kaders — dan is het GEEN visuele pagina.

Geef ALLEEN dit JSON object terug (geen uitleg, geen code block):
{{"visual_pages": [<paginanummers met visuele content>]}}
"""

# Stap 1b: analyseer één enkele visuele pagina in detail
_CROP_PROMPT = """\
Je ontvangt één afbeelding: pagina {page} van een PDF.

Zoek alle afzonderlijke VISUELE frames op deze pagina.

Een VISUEEL frame IS:
  • Een diagram met vormen, pijlen of verbindingen (ERD, sequence, component, deployment)
  • Een UI-mockup, wireframe of Figma-schermontwerp met interface-elementen (knoppen, velden, menu's)

Een VISUEEL frame is NIET:
  • Een tekstblok, lijst of tabel van tekst — ook niet met gekleurde achtergrond of kaders
  • Acceptance criteria, requirements of business rules als tekst
  • Titels, subtitels of sectiekoppen
  • Tekst-rijen in een tabel

Als de pagina ALLEEN tekst, lijsten of tekst-tabellen bevat, geef dan "designs": [] terug.

Voor elk ECHT visueel frame:
1. Lees de TITEL exact zoals zichtbaar in of direct boven het frame — verzin niets.
2. Bepaal de crop-box: de TITEL + eventuele korte beschrijvingstekst direct onder de titel
   + het visuele frame zelf, als één geheel.
   De bovenkant van de crop begint bij de titeltekst direct boven het frame.
   Sluit uit: paginaheader, sectienummer (bv. "2."), paginanummer,
   en alle witruimte en andere content ver buiten het frame+titel blok.
   Waarden zijn fracties 0.0–1.0 van de paginagrootte.
   top = bovenkant van de titel boven het frame,
   bottom = onderkant van het visuele frame (niet verder),
   left = linkerkant van het frame/titel blok (krap, zonder brede marges),
   right = rechterkant van het frame/titel blok (krap, zonder brede marges).

Geef ALLEEN dit JSON object terug (geen uitleg, geen code block):
{{
  "designs": [
    {{
      "title": "<exacte titel uit de afbeelding>",
      "type": "<erd|deployment|component|sequence|ui-mockup|other>",
      "crop": {{"top": <0.0-1.0>, "left": <0.0-1.0>, "right": <0.0-1.0>, "bottom": <0.0-1.0>}}
    }}
  ]
}}
"""


def _build_prompt_pages(
    feature_id: str,
    lang: str,
    num_pages: int,
    img_dir_name: str,
    entries: "list[_VisualEntry]",
) -> str:
    """Genereer de FA op basis van pagina-afbeeldingen en de exacte bestandsnamen per design."""
    lang_instruction = (
        "Schrijf de output in het Nederlands."
        if lang == "nl"
        else "Write the output in English."
    )

    # Exacte titel → bestandsnaam mapping voor Pass 2
    design_lines = "\n".join(
        f'  "{e.title}" → {img_dir_name}/{e.out_name}  (pagina {e.page})'
        for e in entries
    ) or "  (geen visuele designs gedetecteerd)"

    return f"""Je bent een SDLC-documentatie assistent.
Jouw taak: converteer ALLE inhoud van deze PDF naar een gestructureerde Markdown FA.
{lang_instruction}

Je ontvangt {num_pages} afbeeldingen (één per pagina).
De onderstaande visuele designs zijn uit de PDF geëxtraheerd en opgeslagen als afbeeldingen.
Gebruik UITSLUITEND de opgegeven bestandsnamen — verzin geen andere paden:

{design_lines}

── REGELS VOOR VISUELE DESIGNS ────────────────────────────────────────────────
Voor elk design uit de lijst hierboven:
1. Maak een ## sectie met exact de opgegeven titel
2. Voeg op de volgende regel de afbeeldingsreferentie in met het EXACTE opgegeven pad:
   ![titel](pad/zoals/hierboven/opgegeven.png)
3. GEEN beschrijvingstekst, GEEN samenvatting, GEEN bullets — alleen ## titel + afbeelding

── REGELS VOOR TEKSTPAGINA'S ──────────────────────────────────────────────────
Extraheer de volledige tekstinhoud. Behoud alle technische details:
veldnamen, types, constraints, HTTP-methodes, paden, statuscodes, enum-waarden.
Gebruik de REQ-/BR-/NFR-/AC-nummering exact zoals in de PDF.

── STRUCTUUR ───────────────────────────────────────────────────────────────────
- Begin met: # Feature-{feature_id}: {{exacte titel uit de PDF}}
- Gebruik de EXACTE sectietitels uit de PDF als ## headings
- Plaatst visuele design-secties op de positie waar ze in de PDF staan
- Standaardsecties (als aanwezig): ## Doel, ## Scope, ## Requirements,
  ## Business rules, ## Non-functional, ## Data, ## API notes,
  ## Acceptance Criteria, ## UX notes
- Verzin NIETS — laat weg wat niet in de PDF staat
- Geef de output als RAW Markdown — geen code block omheen

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

def _build_image_content(page_images: list[Path]) -> list[dict]:
    """Bouw de lijst van base64-afbeelding content blocks op."""
    content: list[dict] = []
    for img_path in page_images:
        b64 = base64.standard_b64encode(img_path.read_bytes()).decode()
        content.append({
            "type": "image_url",
            "image_url": {"url": f"data:image/png;base64,{b64}"},
        })
    return content


class _VisualEntry:
    """Één visueel design: paginanummer, titel, crop-box en uitvoerbestandsnaam."""
    __slots__ = ("page", "title", "crop", "out_name")

    def __init__(self, page: int, title: str, crop: dict, out_name: str):
        self.page = page
        self.title = title
        self.crop = crop
        self.out_name = out_name  # bv. "page-3.png" of "page-3-1.png"


def _classify_visual_pages(page_images: list[Path]) -> list[int]:
    """Stap 1a: stuur alle pagina's tegelijk en vraag welke visuele content bevatten."""
    import json as _json
    n = len(page_images)
    print(f"  🔍 Stap 1a — visuele pagina's classificeren ({n} pagina's)...")
    content = _build_image_content(page_images)
    content.append({"type": "text", "text": _CLASSIFY_PROMPT.format(n=n)})
    response = get_llm().invoke([HumanMessage(content=content)])
    raw = response.content.strip()
    if "```" in raw:
        raw = raw[raw.find("{"):raw.rfind("}") + 1]
    try:
        data = _json.loads(raw)
        visual = sorted(int(p) for p in data.get("visual_pages", []))
        print(f"  ✅ Visuele pagina('s): {visual}")
        return visual
    except Exception as e:
        print(f"  ⚠️  Classificatie mislukt ({e}) — alle pagina's als visueel behandeld")
        return list(range(1, n + 1))


def _identify_designs_on_page(page_img: Path, page_num: int) -> list[tuple[str, dict]]:
    """Stap 1b: stuur één pagina en vraag naar alle afzonderlijke designs met crop-boxes."""
    import json as _json
    b64 = base64.standard_b64encode(page_img.read_bytes()).decode()
    content = [
        {"type": "image_url", "image_url": {"url": f"data:image/png;base64,{b64}"}},
        {"type": "text", "text": _CROP_PROMPT.format(page=page_num)},
    ]
    response = get_llm().invoke([HumanMessage(content=content)])
    raw = response.content.strip()
    if "```" in raw:
        raw = raw[raw.find("{"):raw.rfind("}") + 1]
    try:
        data = _json.loads(raw)
        return [
            (d["title"], d.get("crop", {}))
            for d in data.get("designs", [])
            if d.get("title")
        ]
    except Exception as e:
        print(f"  ⚠️  Crop-analyse mislukt voor pagina {page_num} ({e})")
        return [(f"Pagina {page_num}", {})]


def _identify_visual_pages(page_images: list[Path]) -> list[_VisualEntry]:
    """
    Twee-staps identificatie:
    1a — classificeer welke pagina's visueel zijn (alle pagina's tegelijk, snel)
    1b — analyseer elke visuele pagina apart voor precieze crop-boxes per design
    """
    visual_page_nums = _classify_visual_pages(page_images)

    entries: list[_VisualEntry] = []
    for page_num in visual_page_nums:
        page_img = page_images[page_num - 1]
        print(f"  ✂️  Stap 1b — designs analyseren op pagina {page_num}...")
        designs = _identify_designs_on_page(page_img, page_num)
        count = len(designs)
        for i, (title, crop) in enumerate(designs, 1):
            out_name = f"page-{page_num}.png" if count == 1 else f"page-{page_num}-{i}.png"
            entries.append(_VisualEntry(page_num, title, crop, out_name))
            label = out_name
            print(f"    → {label}: {title}")

    return entries


def _crop_and_save(src: Path, out: Path, crop: dict) -> None:
    """Crop de bronafbeelding naar crop-box en sla op als out."""
    try:
        from PIL import Image as _Image
        img = _Image.open(src)
        w, h = img.size
        if crop:
            left   = int(max(0.0, crop.get("left",   0.0)) * w)
            top    = int(max(0.0, crop.get("top",    0.0)) * h)
            right  = int(min(1.0, crop.get("right",  1.0)) * w)
            bottom = int(min(1.0, crop.get("bottom", 1.0)) * h)
            if right > left and bottom > top:
                img = img.crop((left, top, right, bottom))
        img.save(out)
    except Exception as e:
        print(f"  ⚠️  Bijsnijden mislukt voor {out.name}: {e}")


def convert_with_page_images(
    page_images: list[Path],
    feature_id: str,
    lang: str,
    img_dir_name: str,
) -> str:
    """
    Twee-pass conversie:
    Pass 1 — identificeer visuele pagina's, titels en crop-boxes; pas bijsnijden toe
    Pass 2 — genereer de FA met afbeeldingsreferenties voor visuele pagina's
    """
    # Pass 1: identificeer designs en bijsnijden
    entries = _identify_visual_pages(page_images)

    print(f"  ✂️  Afbeeldingen bijsnijden en opslaan ({len(entries)} design(s))...")
    img_dir = page_images[0].parent
    for entry in entries:
        src = page_images[entry.page - 1]
        out = img_dir / entry.out_name
        _crop_and_save(src, out, entry.crop)
    print("  ✅ Bijsnijden klaar")

    # Pass 2: genereer FA met exacte bestandsnamen per design
    print(f"  📤 Pass 2 — FA genereren ({len(page_images)} pagina's)...")
    content = _build_image_content(page_images)
    content.append({
        "type": "text",
        "text": _build_prompt_pages(feature_id, lang, len(page_images), img_dir_name, entries),
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
