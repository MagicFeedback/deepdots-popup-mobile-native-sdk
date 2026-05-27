#!/usr/bin/env bash
#
# extract_changelog_section.sh — print the CHANGELOG.md section for a given
# version. The CHANGELOG is expected to use a top-level structure of:
#
#   ## <version>
#   ...content...
#   ## <previous version>
#
# Usage:
#   scripts/extract_changelog_section.sh <version> [path/to/CHANGELOG.md]
#
# Exits 0 with the section body on stdout, or 1 if the section is missing.
#
set -euo pipefail

if [[ $# -lt 1 ]]; then
  echo "Usage: $0 <version> [changelog_path]" >&2
  exit 2
fi

VERSION="$1"
CHANGELOG="${2:-$(cd "$(dirname "$0")/.." && pwd)/CHANGELOG.md}"

if [[ ! -f "$CHANGELOG" ]]; then
  echo "[changelog] ERROR: file not found: $CHANGELOG" >&2
  exit 1
fi

SECTION=$(
  awk -v ver="$VERSION" '
    BEGIN { capture = 0 }
    {
      if ($0 ~ "^## " ver "([[:space:]]|$)") { capture = 1; next }
      if (capture && $0 ~ /^## /) { exit }
      if (capture) { print }
    }
  ' "$CHANGELOG"
)

# Trim leading/trailing blank lines for cleaner release notes output.
SECTION=$(printf '%s\n' "$SECTION" | awk 'NF || found { found=1; print }' | awk '
  { lines[NR] = $0 }
  END {
    last = NR
    while (last > 0 && lines[last] ~ /^[[:space:]]*$/) last--
    for (i = 1; i <= last; i++) print lines[i]
  }
')

if [[ -z "$SECTION" ]]; then
  echo "[changelog] ERROR: no '## $VERSION' section found in $CHANGELOG" >&2
  exit 1
fi

printf '%s\n' "$SECTION"
