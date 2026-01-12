#!/usr/bin/env bash
set -euo pipefail

# Deploy Android (AAR) to Maven Central (OSSRH)
# - Uses environment variables for credentials and signing (no secrets in git)
# - Optionally set VERSION to override PUBLISHING_VERSION
# - Supports DRY_RUN=1 to print the Gradle command without executing it
#
# Required env vars:
#   OSSRH_USERNAME, OSSRH_PASSWORD
#   SIGNING_PASSWORD (PGP key passphrase)
#   And one of:
#     SIGNING_KEY_FILE (path to ASCII-armored private key)
#     SIGNING_KEY_BASE64 (base64 of ASCII-armored private key)
# Optional env vars:
#   VERSION (overrides -PPUBLISHING_VERSION)
#   SIGNING_KEY_ID (helps metadata; not strictly required for signing)
#
# Example (Linux):
#   export OSSRH_USERNAME=xxxx
#   export OSSRH_PASSWORD=yyyy
#   export SIGNING_PASSWORD=zzzz
#   export SIGNING_KEY_BASE64=$(base64 -w0 private-key.asc)
#   VERSION=0.1.3 ./scripts/deploy_android_maven.sh
#
# Example (macOS):
#   export OSSRH_USERNAME=xxxx
#   export OSSRH_PASSWORD=yyyy
#   export SIGNING_PASSWORD=zzzz
#   export SIGNING_KEY_BASE64=$(base64 -i private-key.asc | tr -d '\n')
#   VERSION=0.1.3 ./scripts/deploy_android_maven.sh
#
# After a successful upload, go to https://central.sonatype.com/publishing to Close & Release the staging repo.

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT_DIR"

if [[ ! -x ./gradlew ]]; then
  echo "❌ gradlew not found at $ROOT_DIR. Run from project root." >&2
  exit 1
fi

# Read inputs
VERSION="${VERSION:-}"
OSSRH_USERNAME="${OSSRH_USERNAME:-${ossrhUsername:-}}"
OSSRH_PASSWORD="${OSSRH_PASSWORD:-${ossrhPassword:-}}"
SIGNING_KEY_FILE="${SIGNING_KEY_FILE:-}"
SIGNING_KEY_BASE64="${SIGNING_KEY_BASE64:-}"
SIGNING_PASSWORD="${SIGNING_PASSWORD:-${signingPassword:-${SIGNING_PASSPHRASE:-}}}"
SIGNING_KEY_ID="${SIGNING_KEY_ID:-${signingKeyId:-}}"

# If no key file but base64 provided, materialize to a temp file (portable decode)
CLEANUP_KEY_FILE=0
if [[ -z "$SIGNING_KEY_FILE" && -n "$SIGNING_KEY_BASE64" ]]; then
  tmpFile="${TMPDIR:-/tmp}/pgpkey.$$.asc"
  if echo "test" | base64 -d >/dev/null 2>&1; then
    echo "$SIGNING_KEY_BASE64" | base64 -d > "$tmpFile"
  elif echo "test" | base64 --decode >/dev/null 2>&1; then
    echo "$SIGNING_KEY_BASE64" | base64 --decode > "$tmpFile"
  else
    # macOS BSD base64 uses -D for decode
    echo "$SIGNING_KEY_BASE64" | base64 -D > "$tmpFile"
  fi
  SIGNING_KEY_FILE="$tmpFile"
  CLEANUP_KEY_FILE=1
fi

# Fallback (discouraged): use repo private key file if present
if [[ -z "$SIGNING_KEY_FILE" && -f "$ROOT_DIR/private-key.asc" ]]; then
  echo "⚠️  Using $ROOT_DIR/private-key.asc. Prefer SIGNING_KEY_BASE64 in CI to avoid committing secrets."
  SIGNING_KEY_FILE="$ROOT_DIR/private-key.asc"
fi

# Validate required inputs
missing=()
[[ -z "$OSSRH_USERNAME" ]] && missing+=("OSSRH_USERNAME")
[[ -z "$OSSRH_PASSWORD" ]] && missing+=("OSSRH_PASSWORD")
[[ -z "$SIGNING_PASSWORD" ]] && missing+=("SIGNING_PASSWORD")
[[ -z "$SIGNING_KEY_FILE" ]] && missing+=("SIGNING_KEY_FILE or SIGNING_KEY_BASE64")
if (( ${#missing[@]} )); then
  echo "❌ Missing required environment variables: ${missing[*]}" >&2
  echo "   See header of this script for usage." >&2
  exit 2
fi

if [[ ! -f "$SIGNING_KEY_FILE" ]]; then
  echo "❌ Signing key file not found: $SIGNING_KEY_FILE" >&2
  exit 3
fi

# Compose Gradle args
GRADLE_ARGS=(
  "-PossrhUsername=$OSSRH_USERNAME"
  "-PossrhPassword=$OSSRH_PASSWORD"
  "-Psigning.secretKeyRingFile=$SIGNING_KEY_FILE"
  "-Psigning.password=$SIGNING_PASSWORD"
)
[[ -n "$SIGNING_KEY_ID" ]] && GRADLE_ARGS+=("-Psigning.keyId=$SIGNING_KEY_ID")
[[ -n "$VERSION" ]] && GRADLE_ARGS+=("-PPUBLISHING_VERSION=$VERSION")

TASK=":shared:publishAndroidReleasePublicationToOSSRHRepository"

echo "📦 Publishing Android AAR to OSSRH..."
echo "   Gradle task: $TASK"
if [[ -n "$VERSION" ]]; then echo "   Version override: $VERSION"; fi

set +e
if [[ "${DRY_RUN:-0}" == "1" ]]; then
  echo "DRY_RUN=1 -> would run: ./gradlew $TASK ${GRADLE_ARGS[*]}"
  RC=0
else
  ./gradlew "$TASK" "${GRADLE_ARGS[@]}"
  RC=$?
fi
set -e

# Cleanup temp key file if created
if (( CLEANUP_KEY_FILE == 1 )); then
  rm -f "$SIGNING_KEY_FILE" || true
fi

if (( RC != 0 )); then
  echo "❌ Publish failed (exit $RC). Check Gradle output above." >&2
  exit $RC
fi

echo "✅ Upload completed."
echo "➡️  Next: Close and Release the staging repository in https://central.sonatype.com/publishing"
echo "    (The artifact will become searchable on Maven Central after sync, typically 10–30 minutes.)"

