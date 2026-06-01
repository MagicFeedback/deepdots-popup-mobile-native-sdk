#!/usr/bin/env bash
# Prepara un release SPM binario del SDK iOS (XCFramework zip + checksum + Package.swift)
# Uso:
#   scripts/prepare_spm_release.sh [version] [artifact_base_url]
# Ejemplo:
#   scripts/prepare_spm_release.sh 0.1.4 https://github.com/MagicFeedback/DeepdotsSDK-SPM/releases/download/0.1.4
# Si no se pasa version, usa PUBLISHING_VERSION desde gradle.properties.
# Si no se pasa artifact_base_url, usa SPM_RELEASE_BASE_URL o construye
# https://github.com/<SPM_RELEASE_REPO>/releases/download/<version>.
# Salida:
#   - dist/spm/DeepdotsSDK.xcframework
#   - dist/spm/DeepdotsSDK-<version>.xcframework.zip
#   - dist/spm/DeepdotsSDK-<version>.xcframework.zip.checksum
#   - spm/Package.swift (generado con URL + checksum)

set -euo pipefail
ROOT_DIR=$(cd "$(dirname "$0")"/.. && pwd)
GRADLE_PROPERTIES="$ROOT_DIR/gradle.properties"
DEFAULT_VERSION=$(grep '^PUBLISHING_VERSION=' "$GRADLE_PROPERTIES" | cut -d'=' -f2-)
DEFAULT_RELEASE_REPO="${SPM_RELEASE_REPO:-MagicFeedback/DeepdotsSDK-SPM}"
VERSION="${1:-${PUBLISHING_VERSION:-$DEFAULT_VERSION}}"
BASE_URL="${2:-${SPM_RELEASE_BASE_URL:-https://github.com/${DEFAULT_RELEASE_REPO}/releases/download/${VERSION}}}"
ARTIFACT_NAME="DeepdotsSDK-${VERSION}.xcframework.zip"
DIST_DIR="$ROOT_DIR/dist/spm"
SPM_DIR="$ROOT_DIR/spm"

info() { echo "[SPM] $1"; }

# 1) Build frameworks iOS (arm64 device + arm64 simulator)
info "Compilando frameworks iOS (KMP) para version ${VERSION}"
"$ROOT_DIR/gradlew" \
  :shared:linkReleaseFrameworkIosArm64 \
  :shared:linkReleaseFrameworkIosSimulatorArm64 \
  "-PPUBLISHING_VERSION=${VERSION}"

# 2) Localizar frameworks
FW_DEV="$ROOT_DIR/shared/build/bin/iosArm64/releaseFramework/DeepdotsSDK.framework"
FW_SIM="$ROOT_DIR/shared/build/bin/iosSimulatorArm64/releaseFramework/DeepdotsSDK.framework"
if [[ ! -d "$FW_DEV" || ! -d "$FW_SIM" ]]; then
  echo "[SPM] ERROR: No se encontraron los frameworks esperados:" >&2
  echo "  $FW_DEV" >&2
  echo "  $FW_SIM" >&2
  exit 2
fi

# 3) Crear XCFramework combinado
mkdir -p "$DIST_DIR"
XCFRAMEWORK_OUT="$DIST_DIR/DeepdotsSDK.xcframework"
info "Creando XCFramework combinado"
rm -rf "$XCFRAMEWORK_OUT"
xcodebuild -create-xcframework \
  -framework "$FW_DEV" \
  -framework "$FW_SIM" \
  -output "$XCFRAMEWORK_OUT"

# 4) Empaquetar zip
info "Empaquetando $ARTIFACT_NAME"
rm -f "$DIST_DIR/$ARTIFACT_NAME"
(
  cd "$DIST_DIR"
  zip -r "$ARTIFACT_NAME" "$(basename "$XCFRAMEWORK_OUT")" >/dev/null
)

# 5) Calcular checksum
info "Calculando checksum"
CHECKSUM=$(swift package compute-checksum "$DIST_DIR/$ARTIFACT_NAME")
echo "$CHECKSUM" > "$DIST_DIR/$ARTIFACT_NAME.checksum"

# 6) Generar Package.swift con URL + checksum
mkdir -p "$SPM_DIR"
PKG_SWIFT="$SPM_DIR/Package.swift"
ARTIFACT_URL="$BASE_URL/$ARTIFACT_NAME"
cat > "$PKG_SWIFT" <<EOF
// swift-tools-version: 5.7
import PackageDescription

let package = Package(
    name: "DeepdotsSDK",
    platforms: [
        .iOS(.v13)
    ],
    products: [
        .library(name: "DeepdotsSDK", targets: ["DeepdotsSDK"]) // módulo consumido desde Swift
    ],
    targets: [
        .binaryTarget(
            name: "DeepdotsSDK",
            url: "$ARTIFACT_URL",
            checksum: "$CHECKSUM"
        )
    ]
)
EOF

info "Listo. Publica el zip en: $ARTIFACT_URL"
info "Checksum: $CHECKSUM"
info "Archivo generado: $PKG_SWIFT"
