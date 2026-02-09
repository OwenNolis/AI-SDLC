#!/usr/bin/env bash
set -euo pipefail

FEATURE="${1:-}"
if [[ -z "$FEATURE" ]]; then
  echo "Usage: ./ai/sync-from-fa.sh <feature-id>"
  exit 1
fi

node ai/agent/sync-from-fa.mjs "$FEATURE"