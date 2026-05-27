#!/usr/bin/env bash
#
# deploy_sdk_release.sh — full release pipeline.
#
# Runs pre-flight safety checks, prepares Android + iOS bundles, then triggers
# the SPM repo's update_version.sh to publish the iOS GitHub Release with
# notes pulled from CHANGELOG.md.
#
# Usage:
#   scripts/deploy_sdk_release.sh [version] [spm_repo_path]
#
# Flags (via env):
#   SKIP_PREFLIGHT=1   bypass all guards (use with care, e.g. dry runs)
#   ALLOW_DIRTY=1      allow uncommitted changes
#
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "$0")/.." && pwd)"
GRADLE_PROPERTIES="$ROOT_DIR/gradle.properties"
DEFAULT_VERSION="$(grep '^PUBLISHING_VERSION=' "$GRADLE_PROPERTIES" | cut -d'=' -f2-)"
VERSION="${1:-${PUBLISHING_VERSION:-$DEFAULT_VERSION}}"
SPM_REPO="${2:-${SPM_REPO_PATH:-/Users/sarias/develop/DeepdotsSDK-SPM}}"
SPM_UPDATE_SCRIPT="$SPM_REPO/update_version.sh"
CHANGELOG="$ROOT_DIR/CHANGELOG.md"
NOTES_FILE=""

cleanup() {
  if [[ -n "$NOTES_FILE" && -f "$NOTES_FILE" ]]; then
    rm -f "$NOTES_FILE"
  fi
}
trap cleanup EXIT

fail() {
  echo "[deploy] ERROR: $*" >&2
  exit 1
}

# ---------- Pre-flight guards ----------
preflight() {
  echo "[deploy] Pre-flight checks for $VERSION"

  if [[ ! -d "$SPM_REPO" ]]; then
    fail "SPM repo not found: $SPM_REPO"
  fi
  if [[ ! -f "$SPM_UPDATE_SCRIPT" ]]; then
    fail "update_version.sh not found at: $SPM_UPDATE_SCRIPT"
  fi

  # 1. Working tree clean (unless explicitly overridden).
  if [[ "${ALLOW_DIRTY:-0}" != "1" ]]; then
    if ! git -C "$ROOT_DIR" diff --quiet || ! git -C "$ROOT_DIR" diff --cached --quiet; then
      echo "[deploy] Uncommitted changes in $(basename "$ROOT_DIR"):" >&2
      git -C "$ROOT_DIR" status --short >&2
      fail "commit or stash changes first, or rerun with ALLOW_DIRTY=1"
    fi
  fi

  # 2. PUBLISHING_VERSION in gradle.properties must equal the version we are
  #    about to ship. Catches "forgot to bump_version" mistakes early.
  if [[ "$DEFAULT_VERSION" != "$VERSION" ]]; then
    fail "gradle.properties PUBLISHING_VERSION ($DEFAULT_VERSION) != target version ($VERSION). Run scripts/bump_version.sh $VERSION first."
  fi

  # 3. Tag must not already exist (local or remote).
  if git -C "$ROOT_DIR" rev-parse "refs/tags/$VERSION" >/dev/null 2>&1; then
    fail "tag $VERSION already exists locally — bump or delete it"
  fi
  if git -C "$ROOT_DIR" ls-remote --exit-code --tags origin "refs/tags/$VERSION" >/dev/null 2>&1; then
    fail "tag $VERSION already exists on origin — bump or delete it remotely first"
  fi

  # 4. CHANGELOG must have an entry for this version.
  if [[ ! -f "$CHANGELOG" ]]; then
    fail "CHANGELOG.md not found at $CHANGELOG"
  fi
  if ! grep -qE "^## ${VERSION}([[:space:]]|$)" "$CHANGELOG"; then
    fail "CHANGELOG.md has no '## $VERSION' section. Add release notes before publishing."
  fi

  # 5. gh CLI authenticated (update_version.sh needs it to publish the release).
  if ! command -v gh >/dev/null 2>&1; then
    fail "'gh' CLI is not installed"
  fi
  if ! gh auth status >/dev/null 2>&1; then
    fail "'gh' CLI not authenticated. Run: gh auth login"
  fi

  echo "[deploy] Pre-flight OK"
}

if [[ "${SKIP_PREFLIGHT:-0}" == "1" ]]; then
  echo "[deploy] SKIP_PREFLIGHT=1 — skipping safety checks (DANGER)"
else
  preflight
fi

# ---------- Release notes from CHANGELOG ----------
NOTES_FILE="$(mktemp -t "deepdots-release-notes-${VERSION}.XXXXXX")"
"$ROOT_DIR/scripts/extract_changelog_section.sh" "$VERSION" "$CHANGELOG" > "$NOTES_FILE"
echo "[deploy] Release notes prepared ($(wc -l < "$NOTES_FILE" | tr -d ' ') lines): $NOTES_FILE"

# ---------- Build assets ----------
echo "[deploy] Preparing SDK release assets (Android + iOS)"
"$ROOT_DIR/scripts/prepare_sdk_release.sh" "$VERSION"

# ---------- Publish iOS via SPM repo's update_version.sh ----------
echo "[deploy] Publishing iOS SPM release via update_version.sh"
# update_version.sh accepts an optional 2nd arg for a release notes file.
(cd "$SPM_REPO" && /bin/bash "$SPM_UPDATE_SCRIPT" "$VERSION" "$NOTES_FILE")

echo
echo "[deploy] Done."
echo "[deploy] Android bundle ready for manual upload to Sonatype Central Portal:"
echo "  $ROOT_DIR/shared-android-$VERSION-maven-ready.zip"
echo "[deploy] iOS SPM release: https://github.com/MagicFeedback/DeepdotsSDK-SPM/releases/tag/$VERSION"
