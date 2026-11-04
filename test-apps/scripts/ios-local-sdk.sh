#!/bin/bash
# iOS test apps on the SDK of THIS commit (versions `local`), for the release PR: builds the
# iOS-simulator framework from shared/, wraps it as dist/spm-local/DeepdotsSDK.xcframework (the
# binary target of spm-local/Package.swift) and points the iOS test app projects at that local
# package instead of the published DeepdotsSDK-SPM. Changes the project.yml files in place: CI
# only, or revert them afterwards.
#   test-apps/scripts/ios-local-sdk.sh
set -euo pipefail
ROOT=$(cd "$(dirname "$0")/../.." && pwd)
cd "$ROOT"

./gradlew :shared:linkDebugFrameworkIosSimulatorArm64
FRAMEWORK=shared/build/bin/iosSimulatorArm64/debugFramework/DeepdotsSDK.framework
rm -rf dist/spm-local/DeepdotsSDK.xcframework
mkdir -p dist/spm-local
xcodebuild -create-xcframework -framework "$FRAMEWORK" -output dist/spm-local/DeepdotsSDK.xcframework

for yml in test-apps/ios/project.yml test-apps/basic-ios/project.yml; do
  [ -f "$yml" ] || continue
  python3 - "$yml" <<'EOF'
import sys
p = sys.argv[1]
s = open(p).read()
s = s.replace("    url: https://github.com/MagicFeedback/DeepdotsSDK-SPM\n    exactVersion: ${SDK_VERSION}",
              "    path: ../../spm-local")
s = s.replace("product: ComposeApp", "product: DeepdotsSDK")
assert "path: ../../spm-local" in s, f"{p}: SDK package not found"
open(p, "w").write(s)
EOF
  echo "[lab] $yml -> spm-local (SDK built from $(git rev-parse --short HEAD))"
done
