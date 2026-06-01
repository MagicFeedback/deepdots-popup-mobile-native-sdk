# Proposal: rename the iOS framework `ComposeApp` → `DeepdotsSDK`

Status: **Proposed** (not yet implemented — breaking change, schedule for a minor release)
Raised by: integrator feedback (Slack) — "The import of `ComposeApp` is not telling a lot on what that is."

## Problem

The Kotlin/Native framework `baseName` is `ComposeApp` (`shared/build.gradle.kts`).
That single value leaks into the public iOS API in two ways:

1. **Module name** → integrators write `import ComposeApp` and `ComposeApp.DeepdotsPopups()`.
   The name is the KMP project template default and says nothing about the product.
2. **Objective-C symbol prefix** → every exported type is prefixed: `ComposeAppDeepdotsPopups`,
   `ComposeAppInitOptions`, `ComposeAppStyle`, … (visible in the generated `ComposeApp.h`).

It is also inconsistent with the rest of the distribution, which already uses **DeepdotsSDK**:
- SPM package name: `DeepdotsSDK` (`spm/Package.swift`)
- Release artifact: `DeepdotsSDK-0.2.2.xcframework.zip`
- SPM repo: `MagicFeedback/DeepdotsSDK-SPM`
- App bundle: `DeepdotsPopupSDK`

## Proposed name

**`DeepdotsSDK`** — matches the SPM package, the release zip and the repo, so the whole
chain becomes consistent: `import DeepdotsSDK` / product `DeepdotsSDK` / prefix `DeepdotsSDK*`.

(Alternative considered: `DeepdotsPopupSDK` to match the demo app bundle id. Rejected as
longer and the popup scope may broaden; `DeepdotsSDK` is the safer umbrella name.)

## Blast radius (everything that must change)

Source of truth:
- `shared/build.gradle.kts` — `binaries.framework { baseName = "ComposeApp" }` → `"DeepdotsSDK"`

Packaging / SPM:
- `spm/Package.swift` — product `.library(name:)` and `.binaryTarget(name:)`
- `spm-local/Package.swift` — product/target name
- Release scripts that build/zip/sync the framework path:
  `build_sdk_dist.sh`, `scripts/prepare_spm_release.sh`, `scripts/sync_spm_release.sh`,
  `run_ios_example.sh`, `run_ios_device.sh` (all reference `ComposeApp.framework` /
  `ComposeApp.xcframework` paths)

iOS demo project:
- `iosApp/iosApp.xcodeproj/project.pbxproj` — SPM product reference (`productName = ComposeApp`,
  build-file ref)
- `iosApp/iosApp/DeepdotsDemo.swift`, `iOSApp.swift`, `MagicFeedbackWebView.swift` —
  `import ComposeApp` → `import DeepdotsSDK` and `ComposeApp.DeepdotsPopups()` → `DeepdotsSDK.DeepdotsPopups()`

Docs:
- `README.md`, `spm/README.md` — install/usage snippets
- `.gitignore` — `dist/.../ComposeApp.framework/...` paths

Generated/derived (regenerate automatically, no manual edit):
- `dist/**` xcframeworks, headers (`*.h`), `module.modulemap`, `Info.plist`

## Impact on integrators (why it's breaking)

- **Swift**: only the import line changes — `import ComposeApp` → `import DeepdotsSDK`.
  Swift type names are unaffected (they already drop the prefix: `DeepdotsPopups`, `InitOptions`).
- **Objective-C** (rare): exported class prefix changes `ComposeApp*` → `DeepdotsSDK*`.
- The SPM **product name** changes, so integrators must re-add the product to their target.

## Recommended rollout

1. Do it in a dedicated **minor** release (e.g. `0.3.0`), never inside a patch.
2. Bump version via `scripts/bump_version.sh`, rebuild dist, publish a new
   `DeepdotsSDK-0.3.0.xcframework.zip` and update `spm/Package.swift` url + checksum.
3. Add a **Migration** section to the changelog: "Replace `import ComposeApp` with
   `import DeepdotsSDK`; re-select the `DeepdotsSDK` package product in your target."
4. Optionally keep the `0.2.x` line available for a deprecation window.

## Estimated effort

~1–2 hours: the change itself is mechanical (one `baseName` + find/replace across the
files above), most of the time is rebuilding/publishing the binary artifact and smoke-testing
the demo against the renamed product.
