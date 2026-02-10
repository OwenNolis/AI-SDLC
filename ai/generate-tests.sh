#!/usr/bin/env bash
set -euo pipefail

FEATURE="${1:-}"
if [[ -z "$FEATURE" ]]; then
  echo "Usage: ./ai/generate-tests.sh <feature-id>"
  exit 1
fi

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"

echo "==> (1) Validate TA + Flow JSON"

npm --prefix "${ROOT_DIR}/ai/validator" run validate

echo "==> (2) Generate backend tests (TestRestTemplate)"

BACKEND_CMD=(
  node
  "${ROOT_DIR}/ai/testgen/generate-backend-tests.mjs"
  "${FEATURE}"
)

if [[ "${BACKEND_MATRIX:-0}" == "1" ]]; then
  echo "ℹ️  BACKEND_MATRIX=1 → enabling TA validation matrix tests"
  BACKEND_CMD+=("--matrix")
fi

"${BACKEND_CMD[@]}"

echo "==> (3) Generate frontend tests (RTL)"

node "${ROOT_DIR}/ai/testgen/generate-frontend-tests.mjs" "${FEATURE}"

echo "==> Done. Run:"
echo "  cd backend && mvn test"
echo "  cd frontend && npm test"