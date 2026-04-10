#!/usr/bin/env bash
set -euo pipefail

# ── Gebruik ───────────────────────────────────────────────────────────────────
# Parent repo:  ./ai/fa-to-ta.sh <feature-id> [--fa-type TYPE] [--ta-skeleton PATH]
# Child repo:   ./.sdlc-tools/ai/fa-to-ta.sh <feature-id> [--fa-type TYPE] [--ta-skeleton PATH]
#
# Argumenten:
#   feature-id      Verplicht. Bv. feature-001-support-ticket
#   --fa-type       Optioneel. rest-api | full-stack | frontend-only | event-driven
#   --ta-skeleton   Optioneel. Pad naar eigen TA-skelet (overschrijft type-specifiek skelet)
# ─────────────────────────────────────────────────────────────────────────────

FEATURE="${1:-}"
if [[ -z "$FEATURE" ]]; then
  echo "Gebruik: ./ai/fa-to-ta.sh <feature-id> [--fa-type TYPE] [--ta-skeleton PATH]"
  echo ""
  echo "Types:   rest-api | full-stack | frontend-only | event-driven"
  echo ""
  echo "Voorbeelden:"
  echo "  ./ai/fa-to-ta.sh feature-001-support-ticket"
  echo "  ./ai/fa-to-ta.sh feature-002-ticket-search-filter --fa-type frontend-only"
  exit 1
fi

# Paden bepalen
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
AGENT_DIR="${SCRIPT_DIR}/agent/langgraph"
ROOT_DIR="$(cd "${SCRIPT_DIR}/.." && pwd)"
VENV_DIR="${AGENT_DIR}/.venv"

# .env laden vanuit repo root (indien aanwezig)
ENV_FILE="${ROOT_DIR}/.env"
if [[ -f "$ENV_FILE" ]]; then
  set -a
  # shellcheck disable=SC1090
  source "$ENV_FILE"
  set +a
fi

# GEMINI_API_KEY controleren
if [[ -z "${GEMINI_API_KEY:-}" ]]; then
  echo "❌ GEMINI_API_KEY is niet ingesteld."
  echo "   Voeg GEMINI_API_KEY=... toe aan ${ROOT_DIR}/.env of exporteer de variabele."
  exit 1
fi

# Python detecteren (python3 heeft voorkeur, python als fallback)
if command -v python3 &>/dev/null; then
  PYTHON="python3"
elif command -v python &>/dev/null; then
  PYTHON="python"
else
  echo "❌ Python niet gevonden. Installeer Python 3.9 of hoger."
  exit 1
fi

echo "=============================================="
echo "AI-SDLC — FA → TA agent"
echo "Feature : ${FEATURE}"
echo "Python  : $($PYTHON --version)"
echo "Repo    : ${ROOT_DIR}"
echo "=============================================="

# Virtual environment aanmaken als het nog niet bestaat
if [[ ! -d "$VENV_DIR" ]]; then
  echo ""
  echo "📦 Virtual environment aanmaken..."
  "$PYTHON" -m venv "$VENV_DIR"
fi

# Dependencies installeren als requirements.txt nieuwer is dan de marker
REQ_FILE="${AGENT_DIR}/requirements.txt"
VENV_MARKER="${VENV_DIR}/.installed"
if [[ ! -f "$VENV_MARKER" ]] || [[ "$REQ_FILE" -nt "$VENV_MARKER" ]]; then
  echo "📦 Dependencies installeren..."
  "${VENV_DIR}/bin/pip" install --quiet -r "$REQ_FILE"
  touch "$VENV_MARKER"
fi

# Agent aanroepen — feature-id als eerste argument, daarna alle overige argumenten
shift
"${VENV_DIR}/bin/python" "${AGENT_DIR}/fa_to_ta.py" "$FEATURE" "$@"
