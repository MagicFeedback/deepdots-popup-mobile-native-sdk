#!/bin/bash
# DD-273: builds the iOS key popup checks (XCUITest target NordlyDeviceChecks in test-apps/ios)
# on the SDK of THIS commit, for a simulator or for real devices (Firebase Test Lab).
#
#   testlab/ios-build.sh simulator            # build-for-testing for the iOS simulator
#   testlab/ios-build.sh device [out-dir]     # iphoneos build + out-dir/ios-tests.zip for Test Lab
#
# Then, on a simulator:
#   xcodebuild test-without-building -xctestrun test-apps/ios/build-testlab/Build/Products/*.xctestrun \
#     -destination 'platform=iOS Simulator,id=<udid>'
#
# Like test-apps/scripts/ios-local-sdk.sh, the app uses the local package spm-local
# (dist/spm-local/DeepdotsSDK.xcframework) instead of the published DeepdotsSDK-SPM, but the
# project spec is patched into a temporary copy: project.yml is not touched. Both targets are
# built as universal (TARGETED_DEVICE_FAMILY=1,2) so the iPad runs them natively.
#
# Signing for devices: Test Lab re-signs the app and the runner with its own profile, but the
# zip must be signed. SIGN_IDENTITY (default "-", ad-hoc) is the codesign identity; set it to an
# "Apple Development: …" identity in the keychain if Test Lab rejects the ad-hoc signature.
set -euo pipefail
MODE=${1:?usage: testlab/ios-build.sh simulator|device [out-dir]}
OUT=${2:-out/testlab}
ROOT=$(cd "$(dirname "$0")/.." && pwd)
cd "$ROOT"

case "$MODE" in
  simulator) TARGET=IosSimulatorArm64; KT=iosSimulatorArm64; SDK=iphonesimulator; DEST='generic/platform=iOS Simulator' ;;
  device)    TARGET=IosArm64;          KT=iosArm64;          SDK=iphoneos;        DEST='generic/platform=iOS' ;;
  *) echo "unknown mode $MODE (simulator|device)"; exit 2 ;;
esac

./gradlew -q ":shared:linkDebugFramework$TARGET"
rm -rf dist/spm-local/DeepdotsSDK.xcframework
mkdir -p dist/spm-local
xcodebuild -create-xcframework -framework "shared/build/bin/$KT/debugFramework/DeepdotsSDK.framework" \
  -output dist/spm-local/DeepdotsSDK.xcframework >/dev/null

cd test-apps/ios
SPEC=project.testlab.yml
trap 'rm -f "$ROOT/test-apps/ios/$SPEC"' EXIT
python3 - project.yml "$SPEC" <<'EOF'
import sys
s = open(sys.argv[1]).read()
s = s.replace("    url: https://github.com/MagicFeedback/DeepdotsSDK-SPM\n    exactVersion: ${SDK_VERSION}",
              "    path: ../../spm-local")
s = s.replace("product: ComposeApp", "product: DeepdotsSDK")
s = s.replace('TARGETED_DEVICE_FAMILY: "1"', 'TARGETED_DEVICE_FAMILY: "1,2"')
assert "path: ../../spm-local" in s and "NordlyDeviceChecks" in s, "unexpected project.yml"
open(sys.argv[2], "w").write(s)
EOF
SDK_VERSION="local-$(git rev-parse --short HEAD)" xcodegen generate --spec "$SPEC" --quiet

rm -rf build-testlab
xcodebuild build-for-testing -project NordlyTestApp.xcodeproj -scheme NordlyDeviceChecks \
  -sdk "$SDK" -destination "$DEST" -derivedDataPath build-testlab -quiet \
  CODE_SIGNING_ALLOWED=NO

PRODUCTS=build-testlab/Build/Products
if [ "$MODE" = device ]; then
  IDENTITY=${SIGN_IDENTITY:--}
  # Inside out: frameworks and plug-ins first, then each app.
  for app in "$PRODUCTS"/Debug-iphoneos/*.app; do
    find "$app" -depth \( -name '*.framework' -o -name '*.xctest' -o -name '*.dylib' \) -print0 |
      xargs -0 -I{} codesign --force --sign "$IDENTITY" --timestamp=none {}
    codesign --force --sign "$IDENTITY" --timestamp=none "$app"
    codesign --verify --deep --verbose=1 "$app"
  done
  case "$OUT" in /*) ;; *) OUT="$ROOT/$OUT" ;; esac
  mkdir -p "$OUT"
  ZIP="$OUT/ios-tests.zip"
  rm -f "$ZIP"
  # Test Lab: Debug-iphoneos and exactly one .xctestrun at the root of the zip.
  (cd "$PRODUCTS" && zip -qr "$ZIP" Debug-iphoneos ./*.xctestrun)
  echo "[testlab] $ZIP ($(du -h "$ZIP" | cut -f1)), SDK built from $(git rev-parse --short HEAD)"
else
  echo "[testlab] $(ls "$PRODUCTS"/*.xctestrun)"
fi
