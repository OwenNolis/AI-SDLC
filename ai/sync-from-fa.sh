#!/usr/bin/env bash
set -euo pipefail

FEATURE="${1:-}"
if [[ -z "$FEATURE" ]]; then
  echo "Usage: ./ai/sync-from-fa.sh <feature-id>"
  exit 1
fi

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

# Self-healing dependency install
if [[ ! -d "${SCRIPT_DIR}/agent/node_modules" ]]; then
  echo "Installing ai/agent dependencies..."
  (cd "${SCRIPT_DIR}/agent" && npm ci)
fi

node "${SCRIPT_DIR}/agent/sync-from-fa.mjs" "$FEATURE"