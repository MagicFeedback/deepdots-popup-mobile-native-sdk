#!/usr/bin/env bash
set -euo pipefail

# Build Android AAR and iOS frameworks, collect into /dist
ROOT_DIR="$(cd "$(dirname "$0")" && pwd)"
DIST_DIR="$ROOT_DIR/dist"

check_java() {
  if ! command -v java >/dev/null 2>&1; then
    echo "[ERROR] Java runtime not found. Install a JDK (17 recommended)." >&2
    echo "        macOS (Homebrew): brew install --cask temurin17" >&2
    exit 1
  fi
  JAVA_VER=$(java -version 2>&1 | head -n1)
  echo "[INFO] Using $JAVA_VER"
}

check_java

echo "==> Cleaning dist directory"
rm -rf "$DIST_DIR"
mkdir -p "$DIST_DIR/android" "$DIST_DIR/ios"

echo "==> Building SDK artifacts"
./gradlew :shared:buildSdkDist --no-daemon --stacktrace || {
  echo "[ERROR] Gradle build failed. Check Java version and Gradle output above." >&2
  exit 1
}

echo "==> Listing dist contents"
find "$DIST_DIR" -maxdepth 4 -type f -print || true

echo "Done. Artifacts are in dist/."
