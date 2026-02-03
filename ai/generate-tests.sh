#!/usr/bin/env bash
set -euo pipefail

FEATURE="${1:-feature-001-support-ticket}"

# echo "==> (1) Validate TA + Flow JSON"
# pushd ai/validator >/dev/null
# npm ci
# npm run validate
# popd >/dev/null

echo "==> (1) Validate TA + Flow JSON (skipped – validator not configured yet)"

echo "==> (2) Generate backend tests (TestRestTemplate)"
node ai/testgen/generate-backend-tests.mjs "$FEATURE"

echo "==> (3) Generate frontend tests (RTL)"
node ai/testgen/generate-frontend-tests.mjs "$FEATURE"

echo "==> Done. Run:"
echo "  cd backend && mvn test"
echo "  cd frontend && npm test"