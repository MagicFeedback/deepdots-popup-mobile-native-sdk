#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "$0")/.." && pwd)"
GRADLE_PROPERTIES="$ROOT_DIR/gradle.properties"
DEFAULT_VERSION="$(grep '^PUBLISHING_VERSION=' "$GRADLE_PROPERTIES" | cut -d'=' -f2-)"
VERSION="${1:-${PUBLISHING_VERSION:-$DEFAULT_VERSION}}"
DEST_REPO="${2:-${SPM_REPO_PATH:-/Users/sarias/develop/DeepdotsSDK-SPM}}"

ZIP_NAME="DeepdotsSDK-${VERSION}.xcframework.zip"
ZIP_PATH="$ROOT_DIR/dist/spm/$ZIP_NAME"
CHECKSUM_PATH="$ROOT_DIR/dist/spm/$ZIP_NAME.checksum"
PACKAGE_PATH="$ROOT_DIR/spm/Package.swift"
DEST_RELEASE_DIR="$DEST_REPO/releases/download/$VERSION"

require_file() {
  if [[ ! -f "$1" ]]; then
    echo "[sync] ERROR: missing file: $1" >&2
    exit 1
  fi
}

if [[ ! -d "$DEST_REPO" ]]; then
  echo "[sync] ERROR: destination repo not found: $DEST_REPO" >&2
  exit 2
fi

require_file "$ZIP_PATH"
require_file "$CHECKSUM_PATH"
require_file "$PACKAGE_PATH"

echo "[sync] Syncing SPM release assets"
echo "[sync] Version: $VERSION"
echo "[sync] Destination repo: $DEST_REPO"

mkdir -p "$DEST_RELEASE_DIR"
cp "$ZIP_PATH" "$CHECKSUM_PATH" "$DEST_RELEASE_DIR/"
cp "$PACKAGE_PATH" "$DEST_REPO/Package.swift"

echo "[sync] Copied:"
echo "  $DEST_RELEASE_DIR/$ZIP_NAME"
echo "  $DEST_RELEASE_DIR/$ZIP_NAME.checksum"
echo "  $DEST_REPO/Package.swift"
