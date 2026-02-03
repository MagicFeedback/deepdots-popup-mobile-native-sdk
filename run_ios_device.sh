#!/usr/bin/env zsh
set -euo pipefail

# Usage:
#   UDID='<device-udid>' ./run_ios_device.sh [Debug|Release]
#
# Requirements:
# - iPad unlocked, trusted, Developer Mode enabled (Settings > Privacy & Security)
# - Xcode has paired the device (Window > Devices and Simulators)
# - Target 'iosApp' has a Development Team set and Automatic Signing enabled
# - Xcode 15+ (uses devicectl)

SCHEME="iosApp"
PROJECT="iosApp/iosApp.xcodeproj"
CONFIG=${1:-Debug}
UDID=${UDID:-""}

function info(){ echo "[run_ios_device] $1"; }

if [[ -z "$UDID" ]]; then
  echo "Error: UDID is required. Example: UDID='00008120-000E552A26C00032' ./run_ios_device.sh" >&2
  echo "Hint: Get it from: xcodebuild -showdestinations -project $PROJECT -scheme $SCHEME" >&2
  exit 2
fi

# 1) Build for device
info "Building $SCHEME ($CONFIG) for device $UDID"
xcodebuild \
  -project "$PROJECT" \
  -scheme "$SCHEME" \
  -configuration "$CONFIG" \
  -destination "platform=iOS,id=$UDID" \
  -allowProvisioningUpdates \
  build | grep -E "(\*\* BUILD|error:|warning:|BUILD SUCCEEDED)" || true

# 2) Locate .app for iphoneos
APP_PATH=$(ls -d ~/Library/Developer/Xcode/DerivedData/iosApp-*/Build/Products/${CONFIG}-iphoneos/DeepdotsPopupSDK.app 2>/dev/null | head -n1)
if [[ -z "$APP_PATH" ]]; then
  echo "Error: .app not found in DerivedData for iphoneos. Check signing or build output." >&2
  exit 3
fi
info "Found app: $APP_PATH"

# 3) Install via devicectl (Xcode 15+)
info "Installing app to device $UDID"
xcrun devicectl device install app --device "$UDID" "$APP_PATH"

# 4) Launch by bundle id
PLIST="$APP_PATH/Info.plist"
BUNDLE_ID=$(defaults read "$PLIST" CFBundleIdentifier 2>/dev/null || true)
if [[ -z "$BUNDLE_ID" ]]; then
  echo "Error: CFBundleIdentifier not found in Info.plist" >&2
  exit 4
fi
info "Launching $BUNDLE_ID"
xcrun devicectl device process launch --device "$UDID" "$BUNDLE_ID" || true

info "Done. If you see signing errors, set a Development Team in Xcode > target iosApp > Signing & Capabilities."
