#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "$0")/.." && pwd)"
GRADLE_PROPERTIES="$ROOT_DIR/gradle.properties"
DEFAULT_VERSION="$(grep '^PUBLISHING_VERSION=' "$GRADLE_PROPERTIES" | cut -d'=' -f2-)"
SPM_RELEASE_REPO="${SPM_RELEASE_REPO:-MagicFeedback/DeepdotsSDK-SPM}"
VERSION="${1:-${PUBLISHING_VERSION:-$DEFAULT_VERSION}}"
SPM_BASE_URL="${2:-${SPM_RELEASE_BASE_URL:-https://github.com/${SPM_RELEASE_REPO}/releases/download/${VERSION}}}"

echo "[release] Preparing SDK release assets"
echo "[release] Version: $VERSION"
echo "[release] iOS release base URL: $SPM_BASE_URL"

"$ROOT_DIR/scripts/prepare_maven_upload_zip.sh" "$VERSION"
"$ROOT_DIR/scripts/prepare_spm_release.sh" "$VERSION" "$SPM_BASE_URL"

echo
echo "[release] Android bundle ready:"
echo "  $ROOT_DIR/shared-android-$VERSION-maven-ready.zip"
echo "[release] iOS bundle ready:"
echo "  $ROOT_DIR/dist/spm/DeepdotsSDK-$VERSION.xcframework.zip"
echo "  $ROOT_DIR/dist/spm/DeepdotsSDK-$VERSION.xcframework.zip.checksum"
echo
echo "[release] Next deploy steps"
echo "  1. Upload the Android zip to Sonatype Central Portal."
echo "  2. Upload the iOS zip to the GitHub release/tag $VERSION in $SPM_RELEASE_REPO."
echo "  3. Commit the generated spm/Package.swift to the SPM repository for tag $VERSION."
