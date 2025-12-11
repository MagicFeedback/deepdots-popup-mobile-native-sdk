#!/usr/bin/env zsh
set -euo pipefail

# Configurables
SCHEME="iosApp"
PROJECT="iosApp/iosApp.xcodeproj"
DEST_NAME=${DEST_NAME:-"iPhone 17"}
CONFIG=${CONFIG:-"Debug"}

function info() { echo "[run_ios_example] $1"; }

# 1) Abrir simulador y bootear el dispositivo de destino
info "Opening Simulator and booting device: $DEST_NAME"
open -a Simulator || true
sleep 2
xcrun simctl bootstatus booted || xcrun simctl boot "$DEST_NAME" || true

# 2) Construir la app para el simulador
info "Building Xcode project for simulator $DEST_NAME (scheme=$SCHEME, config=$CONFIG)"
BUILD_LOG=$(mktemp)
xcodebuild \
  -project "$PROJECT" \
  -scheme "$SCHEME" \
  -configuration "$CONFIG" \
  -destination "platform=iOS Simulator,name=$DEST_NAME" \
  build | tee "$BUILD_LOG" | grep -E "(BUILD|error:|warning:)" || true
if grep -q "error:" "$BUILD_LOG"; then
  echo "Build reported errors. Aborting." >&2
  exit 2
fi

# 3) Localizar el .app en DerivedData e instalarlo en el simulador
info "Locating built .app in DerivedData"
APP_PATH=$(ls -d ~/Library/Developer/Xcode/DerivedData/iosApp-*/Build/Products/${CONFIG}-iphonesimulator/DeepdotsPopupSDK.app 2>/dev/null | head -n1)
if [[ -z "$APP_PATH" ]]; then
  echo "Error: .app not found in DerivedData" >&2
  exit 1
fi
info "Installing app: $APP_PATH"
xcrun simctl install booted "$APP_PATH"

# 4) Lanzar la app por bundle identifier detectado desde Info.plist
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

# 5) Mostrar logs recientes relevantes
info "Recent iOS logs containing [iOS] events (last 2m):"
/usr/bin/log show --predicate 'eventMessage CONTAINS "[iOS]"' --style syslog --last 2m | tail -n 100 || true

info "Done. To change device, run: DEST_NAME='iPhone 16e' ./run_ios_example.sh"
