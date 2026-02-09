#!/usr/bin/env bash
set -euo pipefail

FEATURE="${1:-}"

if [[ -z "$FEATURE" ]]; then
  echo "Usage: ./ai/flow.sh <feature-id>"
  exit 1
fi

echo "==> (0) Sync FA → TA → Flow → Test Context"
./ai/sync-from-fa.sh "$FEATURE"

echo "==> (1) Validate + Generate Tests"
./ai/generate-tests.sh "$FEATURE"

echo "==> (2) Run backend tests"
( cd backend && mvn test )

echo "==> (3) Run frontend tests"
( cd frontend && npm test )

echo "==> Done."