#!/usr/bin/env zsh
# Compila el SDK para el simulador, la app de referencia iOS y la lanza en un simulador.
#   reference-ios/run.sh                      # iPhone 17, integración de pruebas de api-dev
#   SIM="iPhone 17 Pro" reference-ios/run.sh
#   DEEPDOTS_PUBLIC_KEY=<clave> DEEPDOTS_ENV=prod reference-ios/run.sh
set -euo pipefail
cd "$(dirname "$0")/.."

SIM=${SIM:-"iPhone 17"}
DERIVED=${DERIVED:-"build/reference-ios"}
export JAVA_HOME=${JAVA_HOME:-"/Applications/Android Studio.app/Contents/jbr/Contents/Home"}

./gradlew :shared:linkDebugFrameworkIosSimulatorArm64 -q
rm -rf dist/spm-local/DeepdotsSDK.xcframework
mkdir -p dist/spm-local
xcodebuild -create-xcframework \
  -framework shared/build/bin/iosSimulatorArm64/debugFramework/DeepdotsSDK.framework \
  -output dist/spm-local/DeepdotsSDK.xcframework >/dev/null

overrides=()
[[ -n "${DEEPDOTS_PUBLIC_KEY:-}" ]] && overrides+=("DEEPDOTS_PUBLIC_KEY=$DEEPDOTS_PUBLIC_KEY")
[[ -n "${DEEPDOTS_ENV:-}" ]] && overrides+=("DEEPDOTS_ENV=$DEEPDOTS_ENV")

xcodebuild -project reference-ios/ReferenceIos.xcodeproj -scheme ReferenceIos -configuration Debug \
  -destination "platform=iOS Simulator,name=$SIM" -derivedDataPath "$DERIVED" "${overrides[@]}" build \
  | grep -E "error:|BUILD (SUCCEEDED|FAILED)"

xcrun simctl boot "$SIM" 2>/dev/null || true
open -a Simulator
xcrun simctl install "$SIM" "$DERIVED/Build/Products/Debug-iphonesimulator/DeepdotsReference.app"
# --console-pty: los logs de la app ([DeepdotsRef]) y del SDK ([DeepdotsPopups]) salen aquí.
xcrun simctl launch --console-pty "$SIM" com.deepdots.reference
