#!/usr/bin/env zsh
# Automatiza vendorización del SDK de MagicFeedback y build+run de la app iOS de ejemplo en simulador.
# Uso:
#   scripts/build_and_run_ios.sh [version] [simulator]
# Ejemplo:
#   scripts/build_and_run_ios.sh 2.1.2-beta.2 'iPhone 15'
# Si no se indica simulator, usa 'iPhone 15'.

set -euo pipefail
ROOT_DIR=$(cd "$(dirname "$0")"/.. && pwd)
VERSION=${1:-2.1.2-beta.2}
SIM_NAME=${2:-'iPhone 15'}
GRADLEW="$ROOT_DIR/gradlew"

info() { echo "[iOS] $1"; }

# 1) Vendorizar SDK navegador en shared
if [[ ! -f "$ROOT_DIR/scripts/vendor_magicfeedback.sh" ]]; then
  echo "[ERROR] No se encuentra scripts/vendor_magicfeedback.sh" && exit 1
fi
info "Vendorizando SDK navegador (@magicfeedback/native@$VERSION)"
zsh "$ROOT_DIR/scripts/vendor_magicfeedback.sh" "$VERSION"

# 2) Construir frameworks iOS del módulo shared (KMP)
info "Construyendo frameworks iOS (shared)"
"$GRADLEW" :shared:assemble

# 3) Preparar simulador
info "Localizando simulador: $SIM_NAME"
SIM_UDID=$(xcrun simctl list devices | grep -m 1 "$SIM_NAME (" | sed -E 's/.*\(([-A-F0-9]+)\).*/\1/')
if [[ -z "$SIM_UDID" ]]; then
  echo "[ERROR] No se encontró simulador '$SIM_NAME'" && exit 1
fi

# 4) Build iOS app con xcodebuild (Debug, simulador)
IOS_PROJ="$ROOT_DIR/iosApp/iosApp.xcodeproj"
SCHEME="iOSApp"
DEST="platform=iOS Simulator,id=$SIM_UDID"
info "Compilando Xcode scheme=$SCHEME destino=$DEST"
xcodebuild -project "$IOS_PROJ" -scheme "$SCHEME" -configuration Debug -destination "$DEST" build | xcpretty || true

# 5) Launch app en simulador
APP_BUNDLE_ID="com.deepdots.demo"
info "Lanzando app $APP_BUNDLE_ID en simulador $SIM_UDID"
xcrun simctl boot "$SIM_UDID" || true
xcrun simctl launch "$SIM_UDID" "$APP_BUNDLE_ID" || echo "[iOS] Nota: si el bundle id difiere, abre Xcode y ejecuta en el simulador."

info "Listo. Abre el simulador y verifica la carga del popup. Revisa consola de Xcode para logs MagicFeedback."
