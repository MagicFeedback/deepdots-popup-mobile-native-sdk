# Deepdots Popup SDK (Kotlin Multiplatform)

[![Maven Central](https://img.shields.io/maven-central/v/com.deepdots.sdk/shared-android)](https://central.sonatype.com/artifact/com.deepdots.sdk/shared-android)

Multiplatform SDK (Android + iOS) to show popups and launch surveys using triggers, conditions, segmentation and simple HTML content.

## Table of Contents
1. Introduction
2. Features
3. Installation / Integration (Server mode)
   - Android (Gradle - Maven Central)
   - iOS (Swift Package Manager - Binary) [Official]
4. Quick Start (Server mode)
   - Initialization
   - Manual popup display
   - Listen for events
5. Triggers & Conditions (remote)
6. Segmentation (lang / path)
7. Cooldown Persistence
8. Public API (entry points)
9. Full Examples (Android / iOS)
10. Building Artifacts (AAR / iOS Frameworks)
11. Runtime Style Overrides
12. Error Handling (Validation & Submit)
13. Troubleshooting
14. MagicFeedback Integration (@magicfeedback/native)
15. Publishing (Maintainers)

---
## 1. Introduction
Deepdots Popup SDK helps you:
- Define popups (id, title, basic HTML message, actions, style).
- Launch them manually or automatically via triggers (time on page, scroll, exit, host events, host clicks).
- Apply segmentation (language, path/screen) and cooldown rules by popup state (`SHOWED`, `PARTIAL`, `COMPLETED`).
- Listen to events for analytics (popup shown, clicked, survey completed).

## 2. Features
- Kotlin Multiplatform (`:shared` module).
- Compose Multiplatform UI rendering.
- Coroutines for triggers and popup queue.
- Configurable persistence (in-memory or your own) for cooldowns.
- Basic HTML support (`<p>`, `<b>`, `<i>`).
- Inline survey renderer with platform bridges and runtime customization.

## 3. Installation / Integration (Server mode)

### Android (Gradle - Maven Central)
- Add Maven Central (already in demos) and depend on the published artifact:
```kotlin
dependencies {
    implementation("com.deepdots.sdk:shared-android:0.3.0")
}
```
- Server mode uses your `publicKey` and remote popups. In the demo (`example-android/MainActivity.kt`), update `publicKey` and `metadata` (e.g., userId). Paths are set via `setPath("/home")`, `setPath("/detail/1")`, etc.
- Build/run demo:
```bash
./gradlew :example-android:assembleDebug
./gradlew :example-android:installDebug
```

### iOS (Swift Package Manager - Binary) [Official]
- Add package: `https://github.com/MagicFeedback/DeepdotsSDK-SPM`, version `0.6.0` (requires the release with `DeepdotsSDK-0.6.0.xcframework.zip` uploaded).
- **Required `Info.plist` key.** Add the following to your app's `Info.plist`. The popup is rendered with Compose Multiplatform, which requires this key on iOS — without it the app can crash on ProMotion (120 Hz) devices when the popup is shown:
  ```xml
  <key>CADisableMinimumFrameDurationOnPhone</key>
  <true/>
  ```
- In the demo (`iosApp/DeepdotsDemo.swift`), set your `publicKey` and optional metadata (userId). Paths are updated when navigating (`/home`, `/detail/1`, `/detail/2`, `/detail/3`, `/detail/4`).
- The checked-in iOS demo project resolves a local package from `spm-local/`. Run `./run_ios_example.sh` to regenerate `dist/spm-local/DeepdotsSDK.xcframework` before opening Xcode if that local binary is missing.
- Resolve/build demo:
```bash
cd iosApp
xcodebuild -resolvePackageDependencies -project iosApp.xcodeproj -scheme iosApp -destination 'generic/platform=iOS Simulator'
xcodebuild -scheme iosApp -project iosApp.xcodeproj -destination 'platform=iOS Simulator,name=iPhone 16e' build
```

## 4. Quick Start (Server mode)

### Initialization
- Android demo snippet (`MainActivity.kt`):
```kotlin
val options = InitOptions(
    debug = true,
    mode = Mode.server,
    popupOptions = PopupOptions(publicKey = "<your-key>", popups = null, companyId = null),
    provideLang = { "en" },
    autoLaunch = true,
    metadata = mapOf("userId" to "demo-user")
)
val sdk = DeepdotsPopups().apply { initialize(options); setPath("/home") }
```
- iOS demo snippet (`DeepdotsDemo.swift`):
```swift
let options = InitOptions(
    debug: true,
    mode: .server,
    popupOptions: PopupOptions(id: nil, publicKey: "<your-key>", popups: nil, companyId: nil),
    provideLang: { Locale.current.language.languageCode?.identifier ?? "en" },
    autoLaunch: true,
    storage: nil,
    metadata: ["userId": uid]
)
let instance = DeepdotsSDK.DeepdotsPopups()
instance.initialize(options: options)
instance.setPath(path: "/home")
```

### Manual popup display
```kotlin
sdk.show(ShowOptions(surveyId = "survey-123", productId = "product-xyz"), PlatformContext(activity))
```
```swift
// iOS: similar call via DeepdotsSDK.DeepdotsPopups().show(...) if needed
```

### Listen for events
- Android/iOS demos wire:
  - `popupShown`, `popupClicked`, `surveyCompleted` to log outputs.

## 5. Triggers & Conditions (remote)
- Server mode fetches popup definitions from backend and normalizes both current and legacy payloads.
- Trigger support:
  - `time_on_page`: auto after N seconds.
  - `scroll`: host app reports progress through `onScroll(percentage)`.
  - `exit`: queued on the source path and shown on the destination path after the configured delay.
  - `event`: host app calls `triggerEvent(name)`.
  - `click`: host app calls `triggerClick(targetId)`.
- A popup can define multiple triggers; the first eligible trigger may queue/show it.
- Cooldown rules are evaluated per popup using:
  - `SHOWED`: last time that popup was shown.
  - `PARTIAL`: last partial survey progress for the survey.
  - `COMPLETED`: last completed survey progress for the survey.
- Legacy `conditions` / `trigger.condition` payloads are still accepted for backward compatibility.

## 6. Segmentation (lang / path)
- Provide `lang` and `path` so server-side segments can match.
  - Android: `provideLang` lambda + `setPath(path)` on navigation.
  - iOS: same via `provideLang` and `setPath(path:)` in `DeepdotsDemo.swift`.
- Ensure paths you navigate (`/home`, `/detail/1`, `/detail/2`, etc.) match the segments defined in your backend.

## 7. Cooldown Persistence
- Uses in-memory by default. You can plug a custom `KeyValueStorage` (Android/iOS) if you need persistence across sessions. Demos use defaults.

## 8. Public API (entry points)
- `DeepdotsPopups` with `initialize(options)`, `autoLaunch()`, `setPath`, `onScroll(percentage)`, `onExit()`, `triggerEvent(name)`, `triggerClick(targetId)`, `show(...)`, `showByPopupId(...)`, `on(...)`, `off(...)`, `attachContext(...)`.
- Types: `InitOptions`, `PopupOptions`, `ShowOptions`, `PopupDefinition`, `Trigger`, `CooldownCondition`, `LegacyCondition`, `Segments`, `Events`, `FeedbackSession`.
- Analytics session: `getFeedbackSessionId()` and `InitOptions.onFeedbackSession` give the id of the feedback the current analytics session becomes (see below).

### Linking your backend to a session

Every analytics session (with `InitOptions.analytics` set) becomes one feedback in Deepdots, and
the API stores the session's id on it as `sdkSessionId`. Send that id to your backend and it can
find the feedback (`GET /feedbacks?filter={"where":{"sdkSessionId":"<id>"}}`) and add data to it
later, such as the push deliveries the app never sees. The lookup can return more than one
feedback: a session can be completed twice (the API closes it for inactivity and the app posts
to it again later), and both feedbacks carry the same id. Sort by `createdAt` and take the latest.

```kotlin
val options = InitOptions(
    popupOptions = PopupOptions(publicKey = "<your-key>"),
    analytics = AnalyticsKeys(publicKey = "<your-key>", integration = "<integration-id>"),
    onFeedbackSession = { session ->
        // Open: first batch accepted. Closed: the feedback exists within seconds.
        // Called on a background thread.
        myBackend.reportSession(session.sessionId, session.status)
    },
)
```

- `Open` comes when the API accepts the session's first batch, `Closed` when it accepts the
  closing one (`completed: true`). A session whose only batch is the closing one reports just
  `Closed`.
- On mobile the session ends with `onBackground()` (also `endSession()`, `setUserId(...)` and
  `setTrackingEnabled(false)`). `Closed` does not come when the app is killed or suspended
  before the closing request returns, nor when the closing request fails (network error, 5xx):
  its events are re-sent with the next session and the API closes the old record itself, later.
  In both cases the id already came with `Open`.
- Key what you store by `sessionId`, not by arrival order: the closing request and the next
  session's first one can be in flight at the same time, so the new session's `Open` can arrive
  before the previous one's `Closed`.
- The callback runs on a background thread; a callback that throws is caught and logged and
  never affects delivery.
- `getFeedbackSessionId()` returns the open session's id, or `null` before its first batch is
  accepted, after it closes, and always without `InitOptions.analytics`.
- This is not `getSessionId()`, the SDK's own session id. That one travels in the metadata as
  `deepdots_session_id`, and the feedback can't be looked up by it.
- iOS (Swift): Kotlin default arguments are not exported, so pass `onFeedbackSession: nil` (or a
  closure taking a `FeedbackSession`) when you build `InitOptions`.

## 9. Full Examples (Android / iOS)
- Android: `example-android/MainActivity.kt` shows Server mode init, path updates, event logging, and a fourth demo screen that fires `custom-event`.
- iOS: `iosApp/DeepdotsDemo.swift` shows Server mode init, navigation-driven `setPath`, scroll reporting, exit, and a fourth demo screen that fires `custom-event`.

## 10. Building Artifacts (for contributors)
### Android (AAR)
```bash
./gradlew :shared:assembleRelease
```
Output in `shared/build/outputs/aar/`.

### iOS (Frameworks)
```bash
./gradlew :shared:assemble
```
Frameworks for each iOS target are placed in `shared/build/bin/`.

## 11. Runtime Style Overrides
Events `loaded`/`popup_clicked` may include style overrides in the payload:
- Colors: `buttonPrimaryColor`, `boxBackgroundColor`.
- Start message: `startMessage` (shows a Start button initially if present).
- Image/Logo: `image` or `logo` URL; `imageSize`/`logoSize` (small|medium|large); `imagePosition`/`logoPosition` (left|right|center).
- Popup sizing: `popupMaxWidth` (dp), `popupMaxHeightFraction` (0.5–0.98).

## 12. Error Handling (Validation & Submit)
- Validation errors:
  - Events: `validation_error_required` or any `validation_error*` with optional `payload.message`.
  - UI: shows an inline banner under the survey; keeps Back/Send visible.
- Submit errors:
  - Event: `submit_error` with optional `payload.message` (e.g., "No response").
  - UI: also inline banner under the survey for correction/retry; Back/Send remain visible.
- Platform specifics:
  - Android: we synthesize these from WebView console logs if the bridge doesn’t emit them.
  - iOS: a WKUserScript forwards console `log/error` to the bridge as structured events.

## 13. Troubleshooting
| Issue | Common Cause | Solution |
|-------|--------------|----------|
| Spinner not showing | Initial state not Loading | Initial state is Loading; verify `loaded`/`popup_clicked` arrive to hide it |
| No validation banner | Bridge not emitting or logs not forwarded | Android: check WebView logs; iOS: ensure WKUserScript is injected before load |
| Footer hidden on Android | Survey area too tall | WebView uses fixed height; popup keeps footer visible; adjust height if needed |
| iOS no image/logo | Asset not in bundle | Place `magicfeedback-sdk.browser.js` and ensure copy resources |
| Progress state wrong | Events missing progress/total | We update global progress/total from payload when present and derive state |

## 14. MagicFeedback Integration (@magicfeedback/native)
The SDK builds HTML to load the MagicFeedback bundle from a local asset (if available) and then falls back to CDN sources.

### Shared Builder
```kotlin
val html = Deepdots.getSurveyHtml(surveyId = "survey-123", productId = "product-xyz")
```
Emitted lifecycle events include:
- `popup_clicked`, `survey_completed`
- `error:init`, `error:timeout`, `error:module`, `error:module-load`
- Validation/submit errors synthesized via platform logging when not bridged directly.

### Packaging Local Asset
Android:
- Copy to `shared/src/androidMain/assets/magicfeedback/magicfeedback-sdk.browser.js`.

iOS:
- Add to Xcode target under `magicfeedback/`.

### Asset Update Script
See `scripts/update_magicfeedback_asset.sh`.

## 15. Publishing (Maintainers)

### Automated release (GitHub Actions)
Releases are published by `.github/workflows/release.yml` when a `vX.Y.Z` tag is pushed:
1. On `dev`: `./scripts/bump_version.sh X.Y.Z`, add a `## X.Y.Z` section to `CHANGELOG.md`, commit `chore(release): X.Y.Z` and merge it.
2. Tag the merge commit and push the tag:
```bash
git tag -a vX.Y.Z <merge-commit> -m vX.Y.Z && git push origin vX.Y.Z
```
The workflow checks that the tag matches `PUBLISHING_VERSION`, that the commit is on `dev` and that the CHANGELOG has the section; runs the JVM and iOS simulator tests; and then publishes, in order:
- Android to Maven Central (`com.deepdots.sdk:shared-android:X.Y.Z`), signed. Skipped with a warning when the Maven Central or signing secrets are missing.
- iOS: the XCFramework zip on the `X.Y.Z` release of `MagicFeedback/DeepdotsSDK-SPM`, plus its `Package.swift`, and checks that SPM resolves it.
- The `vX.Y.Z` GitHub Release of this repo, with the AAR and the CHANGELOG notes.
- Merges the tagged commit into `main` and redeploys the docs. On a conflict it opens a PR instead.

Nothing already published is overwritten: an existing version is skipped. Required secrets and optional variables are listed at the top of the workflow file.

The scripts below are the manual fallback.

### Unified release prep
- Choose the version either by updating `PUBLISHING_VERSION` in `gradle.properties` or by passing it as the first argument.
- Prepare both release bundles in one shot:
```bash
./scripts/prepare_sdk_release.sh 0.1.8
```
- Optional: if the SPM release lives in another repo/base URL, pass it explicitly:
```bash
./scripts/prepare_sdk_release.sh 0.1.8 https://github.com/MagicFeedback/DeepdotsSDK-SPM/releases/download/0.1.8
```
- Generated outputs:
  - Android: `shared-android-<version>-maven-ready.zip`
  - iOS: `dist/spm/DeepdotsSDK-<version>.xcframework.zip`
  - iOS checksum: `dist/spm/DeepdotsSDK-<version>.xcframework.zip.checksum`
  - Updated binary package manifest: `spm/Package.swift`

### Android (Maven Central via zip upload)
- The Android release bundle is prepared from a local staging Maven repo inside `build/release-staging-repo`, so no manual `publishToMavenLocal` step is required.
- If you only want the Android bundle:
```bash
./scripts/prepare_maven_upload_zip.sh 0.1.8
```
- Upload `shared-android-<version>-maven-ready.zip` to Sonatype Central Portal. The script regenerates `.asc`, `.md5`, and `.sha1` files for every published artifact.
- For direct Gradle publishing instead of zip upload, configure `ossrhUsername` / `ossrhPassword` or `OSSRH_USERNAME` / `OSSRH_PASSWORD`, then run:
```bash
./gradlew :shared:publishToMavenCentral -PPUBLISHING_VERSION=0.1.8
```

### iOS (SPM Binary via GitHub Releases)
- If you only want the iOS bundle:
```bash
./scripts/prepare_spm_release.sh 0.1.8 https://github.com/MagicFeedback/DeepdotsSDK-SPM/releases/download/0.1.8
```
- The script builds the release frameworks, creates the XCFramework zip, computes the checksum, and rewrites `spm/Package.swift`.
- Upload `dist/spm/DeepdotsSDK-<version>.xcframework.zip` to the GitHub release/tag `<version>` in `MagicFeedback/DeepdotsSDK-SPM`.
- Commit the generated `spm/Package.swift` into the SPM repo and publish/tag that change.

### Signing configuration
- Gradle publishing accepts either `signingInMemoryKey` / `SIGNING_KEY` plus `signing.password` / `SIGNING_PASSWORD`, or `signing.secretKeyRingFile` plus `signing.password`.
- The Maven zip helper also requires local `gpg` and `zip`.
