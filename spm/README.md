# DeepdotsSDK via Swift Package Manager (Binary)

This folder is generated for SPM binary distribution. Use the helper script to build the XCFramework and generate a Package.swift pointing at a hosted zip and checksum.

Usage:

```bash
# From repo root
./scripts/prepare_spm_release.sh 0.1.2 https://github.com/MagicFeedback/DeepdotsSDK-SPM/releases/download/0.1.2

# Then commit spm/Package.swift and publish the zip at the URL above.
```

Consumer (iOS App):
- In Xcode, File > Add Packages... and use the URL to your Package.swift (repo hosting it or a tag).
- Add the product "ComposeApp" to your target.
- Swift code already imports ComposeApp.

Notes:
- The binary target is named ComposeApp to match the KMP framework module imported by Swift.
- Supported: iOS 13+, arm64 devices and arm64 simulators.
- If you change the module name, also change baseName in shared/build.gradle.kts and the Package.swift binary target name.

