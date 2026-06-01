#!/usr/bin/env zsh
set -euo pipefail

# Configurables
SCHEME="iosApp"
WORKSPACE="iosApp/iosApp.xcworkspace"  # opcional; si no existe o falta Pods, usamos el xcodeproj
PROJECT="iosApp/iosApp.xcodeproj"
DEST_NAME=${DEST_NAME:-""}
DEST_ID=${DEST_ID:-""}
CONFIG=${CONFIG:-"Debug"}
LOCAL_XCFRAMEWORK_DIR="dist/spm-local"
LOCAL_XCFRAMEWORK_PATH="$LOCAL_XCFRAMEWORK_DIR/DeepdotsSDK.xcframework"
LOCAL_SIM_FRAMEWORK="shared/build/bin/iosSimulatorArm64/debugFramework/DeepdotsSDK.framework"

function info() { echo "[run_ios_example] $1"; }

# Resolve a Simulator ID to avoid destination timeouts
function resolve_simulator_id() {
  local chosen_id=""
  if [[ -n "${DEST_ID}" ]]; then
    echo "$DEST_ID"; return 0
  fi

  # Prefer xcodebuild -showdestinations (knows what the scheme supports)
  local dests
  if dests=$(xcodebuild -showdestinations -project "$PROJECT" -scheme "$SCHEME" 2>/dev/null); then
    if [[ -n "$DEST_NAME" ]]; then
      chosen_id=$(echo "$dests" | awk -v TGT="$DEST_NAME" '/platform:iOS Simulator/ && $0 ~ "name:"TGT"}" { for(i=1;i<=NF;i++){ if($i ~ /^id:/){ gsub(/id:/, "", $i); gsub(/,/, "", $i); print $i; exit } } }' | head -n1)
    fi
    if [[ -z "$chosen_id" ]]; then
      # pick first iPhone simulator
      chosen_id=$(echo "$dests" | awk '/platform:iOS Simulator/ && /name:iPhone/ { for(i=1;i<=NF;i++){ if($i ~ /^id:/){ gsub(/id:/, "", $i); gsub(/,/, "", $i); print $i; exit } } }' | head -n1)
    fi
    if [[ -z "$chosen_id" ]]; then
      # pick any iOS Simulator
      chosen_id=$(echo "$dests" | awk '/platform:iOS Simulator/ { for(i=1;i<=NF;i++){ if($i ~ /^id:/){ gsub(/id:/, "", $i); gsub(/,/, "", $i); print $i; exit } } }' | head -n1)
    fi
  fi

  # Fallback: parse simctl devices available
  if [[ -z "$chosen_id" ]]; then
    local list
    list=$(xcrun simctl list devices available)
    if [[ -n "$DEST_NAME" ]]; then
      chosen_id=$(echo "$list" | awk -v TGT="$DEST_NAME" '$0 ~ TGT" \(" { match($0, /\(([A-F0-9-]+)\)/, m); if(m[1] != "") { print m[1]; exit } }')
    fi
    if [[ -z "$chosen_id" ]]; then
      chosen_id=$(echo "$list" | awk '/iPhone/ { match($0, /\(([A-F0-9-]+)\)/, m); if(m[1] != "") { print m[1]; exit } }')
    fi
  fi

  echo "$chosen_id"
}

# 0) Preparar simulador
info "Abriendo Simulator y arrancando el dispositivo: $DEST_NAME"
open -a Simulator || true
sleep 1
SIM_ID=$(resolve_simulator_id)
if [[ -z "$SIM_ID" ]]; then
  echo "[run_ios_example] Error: no se pudo resolver un ID de destino de Simulator" >&2
  exit 1
fi
info "Usando el ID del simulador: $SIM_ID"
# Arrancar y esperar hasta que esté listo
xcrun simctl bootstatus "$SIM_ID" -b || xcrun simctl boot "$SIM_ID" || true
xcrun simctl bootstatus "$SIM_ID" -b || true

# 1) Construir la app para el simulador usando SPM
# Antes de resolver paquetes, regeneramos el XCFramework local del simulador.
info "Regenerando DeepdotsSDK.xcframework local para Simulator"
./gradlew :shared:linkDebugFrameworkIosSimulatorArm64 >/dev/null
rm -rf "$LOCAL_XCFRAMEWORK_PATH"
mkdir -p "$LOCAL_XCFRAMEWORK_DIR"
xcodebuild -create-xcframework \
  -framework "$LOCAL_SIM_FRAMEWORK" \
  -output "$LOCAL_XCFRAMEWORK_PATH" >/dev/null

BUILD_LOG=$(mktemp)
PODS_DEBUG_XCCONFIG="iosApp/Pods/Target Support Files/Pods-iosApp/Pods-iosApp.debug.xcconfig"
USE_WORKSPACE=0
if [[ -d "$WORKSPACE" && -f "$PODS_DEBUG_XCCONFIG" ]]; then
  USE_WORKSPACE=1
fi

if [[ "$USE_WORKSPACE" -eq 1 ]]; then
  info "Construyendo con el workspace (SPM/Pods): $WORKSPACE"
  xcodebuild \
    -workspace "$WORKSPACE" \
    -scheme "$SCHEME" \
    -configuration "$CONFIG" \
    -destination "platform=iOS Simulator,id=$SIM_ID" \
    build | tee "$BUILD_LOG" | grep -E "(\*\* BUILD|error:|warning:|BUILD SUCCEEDED)" || true
else
  info "Construyendo con el proyecto (SPM, sin Pods): $PROJECT"
  xcodebuild \
    -project "$PROJECT" \
    -scheme "$SCHEME" \
    -configuration "$CONFIG" \
    -destination "platform=iOS Simulator,id=$SIM_ID" \
    build | tee "$BUILD_LOG" | grep -E "(\*\* BUILD|error:|warning:|BUILD SUCCEEDED)" || true
fi

if grep -q "error:" "$BUILD_LOG"; then
  echo "La construcción informó errores. Abortando." >&2
  exit 2
fi

# 2) Localizar el .app en DerivedData e instalarlo en el simulador
info "Localizando el .app construido en DerivedData"
APP_PATH=$(ls -d ~/Library/Developer/Xcode/DerivedData/iosApp-*/Build/Products/${CONFIG}-iphonesimulator/DeepdotsPopupSDK.app 2>/dev/null | head -n1)
if [[ -z "$APP_PATH" ]]; then
  echo "Error: .app no encontrado en DerivedData" >&2
  echo "Sugerencia: abre Xcode y compila el esquema 'iosApp' para el simulador, asegurando que el paquete SPM 'DeepdotsSDK' esté vinculado y eliminando referencias a Pods si ya no se usan." >&2
  exit 1
fi
info "Instalando app: $APP_PATH"
xcrun simctl install "$SIM_ID" "$APP_PATH" || xcrun simctl install booted "$APP_PATH"

# 3) Lanzar la app por bundle identifier detectado desde Info.plist
PLIST="$APP_PATH/Info.plist"
if [[ ! -f "$PLIST" ]]; then
  echo "Error: Info.plist no encontrado en el paquete de la app" >&2
  exit 3
fi
BUNDLE_ID=$(defaults read "$PLIST" CFBundleIdentifier 2>/dev/null || true)
if [[ -z "$BUNDLE_ID" ]]; then
  echo "Error: Falta CFBundleIdentifier en Info.plist" >&2
  exit 13
fi
info "Lanzando app: $BUNDLE_ID"
xcrun simctl launch "$SIM_ID" "$BUNDLE_ID" || xcrun simctl launch booted "$BUNDLE_ID" || true

# 4) Mostrar logs recientes relevantes
info "Logs recientes de iOS que contienen eventos [iOS] (últimos 2m):"
/usr/bin/log show --predicate 'eventMessage CONTAINS "[iOS]"' --style syslog --last 2m | tail -n 100 || true

info "Listo. Para forzar un dispositivo específico por nombre: DEST_NAME='iPhone 17' ./run_ios_example.sh o por id: DEST_ID='<UUID>' ./run_ios_example.sh"
