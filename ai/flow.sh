#!/usr/bin/env bash
set -euo pipefail

FEATURE="${1:-}"
if [[ -z "$FEATURE" ]]; then
  echo "Usage: ./ai/flow.sh <feature-id>"
  exit 1
fi

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"

echo "=============================================="
echo "AI-SDLC FLOW"
echo "Feature: ${FEATURE}"
echo "Repo:    ${ROOT_DIR}"
echo "=============================================="

run_step () {
  local title="$1"
  shift
  echo ""
  echo "==> ${title}"
  "$@"
  echo "✅ ${title} (ok)"
}

# --- Preflight checks for Gemini sync ---
echo ""
echo "==> Preflight"

# Optional printing of Gemini model
if [[ -n "${GEMINI_MODEL:-}" ]]; then
  echo "GEMINI_MODEL=${GEMINI_MODEL}"
else
  echo "GEMINI_MODEL not set (optional)."
fi

# Check key before running sync (only if sync script is present)
if [[ -f "${ROOT_DIR}/ai/sync-from-fa.sh" ]]; then
  if [[ -z "${GEMINI_API_KEY:-}" ]]; then
    echo "❌ GEMINI_API_KEY is not set."
    echo "   Fix: export GEMINI_API_KEY=\"<your key>\""
    echo "   Or put it in your shell profile and restart the terminal."
    exit 1
  fi
  echo "✅ GEMINI_API_KEY is set."
fi

echo "✅ Preflight (ok)"

# 1) Sync FA -> TA + Flow + Context (LLM agent)
run_step "1) Sync from FA (LLM)" \
  "${ROOT_DIR}/ai/sync-from-fa.sh" "${FEATURE}"

# 2) Validate TA + Flow JSON schemas
run_step "2) Validate TA + Flow JSON (AJV)" \
  npm --prefix "${ROOT_DIR}/ai/validator" run validate

# 3) Generate backend + frontend tests from artifacts
run_step "3) Generate tests" \
  "${ROOT_DIR}/ai/generate-tests.sh" "${FEATURE}"

# 4) Run backend tests
run_step "4) Run backend tests (mvn test)" \
  bash -lc "cd '${ROOT_DIR}/backend' && mvn test"

# 5) Run frontend tests
run_step "5) Run frontend tests (npm test)" \
  bash -lc "cd '${ROOT_DIR}/frontend' && npm test"

echo ""
echo "=============================================="
echo "✅ FLOW COMPLETE"
echo "Feature: ${FEATURE}"
echo "=============================================="