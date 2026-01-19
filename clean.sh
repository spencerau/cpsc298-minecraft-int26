#!/usr/bin/env bash
set -euo pipefail

# Resolve script directory (project root)
script_dir="$(cd "$(dirname "$0")" && pwd)"

# Prefer virtualenv python if present
PYTHON_CMD="$script_dir/.venv/bin/python"
if [ ! -x "$PYTHON_CMD" ]; then
	PYTHON_CMD="$(command -v python3 || command -v python || true)"
fi

if [ -z "$PYTHON_CMD" ]; then
	echo "Error: no Python interpreter found. Install Python or create .venv." >&2
	exit 1
fi

echo "Running clean_mods.py with: $PYTHON_CMD"
"$PYTHON_CMD" "$script_dir/tools/modgen/clean_mods.py"
