#!/usr/bin/env bash
set -euo pipefail

FEATURE="${1:-feature-001-support-ticket}"

echo "==> Validating schemas"
pushd ai/validator >/dev/null
npm ci
npm run validate
popd >/dev/null

echo "==> Generating backend tests"
node ai/testgen/generate-backend-tests.mjs "$FEATURE"

echo "==> Generating frontend tests"
node ai/testgen/generate-frontend-tests.mjs "$FEATURE"

echo "==> Done. Now run:"
echo "  (backend)  cd backend && mvn test"
echo "  (frontend) cd frontend && npm test"