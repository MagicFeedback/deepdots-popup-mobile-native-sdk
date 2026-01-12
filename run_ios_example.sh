#!/usr/bin/env zsh
set -euo pipefail

# Configurables
SCHEME="iosApp"
WORKSPACE="iosApp/iosApp.xcworkspace"  # opcional; si no existe o falta Pods, usamos el xcodeproj
PROJECT="iosApp/iosApp.xcodeproj"
DEST_NAME=${DEST_NAME:-"iPhone 17"}
CONFIG=${CONFIG:-"Debug"}

function info() { echo "[run_ios_example] $1"; }

# 0) Preparar simulador
info "Opening Simulator and booting device: $DEST_NAME"
open -a Simulator || true
sleep 2
xcrun simctl bootstatus booted || xcrun simctl boot "$DEST_NAME" || true

# 1) Construir la app para el simulador usando SPM
BUILD_LOG=$(mktemp)
PODS_DEBUG_XCCONFIG="iosApp/Pods/Target Support Files/Pods-iosApp/Pods-iosApp.debug.xcconfig"
USE_WORKSPACE=0
if [[ -d "$WORKSPACE" && -f "$PODS_DEBUG_XCCONFIG" ]]; then
  USE_WORKSPACE=1
fi

if [[ "$USE_WORKSPACE" -eq 1 ]]; then
  info "Building with workspace (SPM/Pods): $WORKSPACE"
  xcodebuild \
    -workspace "$WORKSPACE" \
    -scheme "$SCHEME" \
    -configuration "$CONFIG" \
    -destination "platform=iOS Simulator,name=$DEST_NAME" \
    build | tee "$BUILD_LOG" | grep -E "(BUILD|error:|warning:)" || true
else
  info "Building with project (SPM, no Pods): $PROJECT"
  xcodebuild \
    -project "$PROJECT" \
    -scheme "$SCHEME" \
    -configuration "$CONFIG" \
    -destination "platform=iOS Simulator,name=$DEST_NAME" \
    build | tee "$BUILD_LOG" | grep -E "(BUILD|error:|warning:)" || true
fi

if grep -q "error:" "$BUILD_LOG"; then
  echo "Build reported errors. Aborting." >&2
  exit 2
fi

# 2) Localizar el .app en DerivedData e instalarlo en el simulador
info "Locating built .app in DerivedData"
APP_PATH=$(ls -d ~/Library/Developer/Xcode/DerivedData/iosApp-*/Build/Products/${CONFIG}-iphonesimulator/DeepdotsPopupSDK.app 2>/dev/null | head -n1)
if [[ -z "$APP_PATH" ]]; then
  echo "Error: .app not found in DerivedData" >&2
  echo "Sugerencia: abre Xcode y compila el esquema 'iosApp' para el simulador, asegurando que el paquete SPM 'ComposeApp' esté vinculado y eliminando referencias a Pods si ya no se usan." >&2
  exit 1
fi
info "Installing app: $APP_PATH"
xcrun simctl install booted "$APP_PATH"

# 3) Lanzar la app por bundle identifier detectado desde Info.plist
PLIST="$APP_PATH/Info.plist"
if [[ ! -f "$PLIST" ]]; then
  echo "Error: Info.plist not found in app bundle" >&2
  exit 3
fi
BUNDLE_ID=$(defaults read "$PLIST" CFBundleIdentifier 2>/dev/null || true)
if [[ -z "$BUNDLE_ID" ]]; then
  echo "Error: Missing CFBundleIdentifier in Info.plist" >&2
  exit 13
fi
info "Launching app: $BUNDLE_ID"
xcrun simctl launch booted "$BUNDLE_ID" || true

# 4) Mostrar logs recientes relevantes
info "Recent iOS logs containing [iOS] events (last 2m):"
/usr/bin/log show --predicate 'eventMessage CONTAINS "[iOS]"' --style syslog --last 2m | tail -n 100 || true

info "Done. To change device, run: DEST_NAME='iPhone 16e' ./run_ios_example.sh"
