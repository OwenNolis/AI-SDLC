#!/usr/bin/env bash
set -euo pipefail

FEATURE="${1:-}"
if [[ -z "$FEATURE" ]]; then
  echo "Usage: ./ai/sync-from-fa.sh <feature-id>"
  exit 1
fi

# Self-healing dependency install
if [[ ! -d "ai/agent/node_modules" ]]; then
  echo "Installing ai/agent dependencies..."
  (cd ai/agent && npm ci)
fi

node ai/agent/sync-from-fa.mjs "$FEATURE"