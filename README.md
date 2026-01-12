# Deepdots Popup SDK (Kotlin Multiplatform)

[![Maven Central](https://img.shields.io/maven-central/v/com.deepdots.sdk/shared-android)](https://central.sonatype.com/artifact/com.deepdots.sdk/shared-android)

Multiplatform SDK (Android + iOS) to show popups and launch surveys using triggers, conditions, segmentation and simple HTML content.

## Table of Contents
1. Introduction
2. Features
3. Installation / Integration
   - Android (Gradle - Maven Central)
   - iOS (Swift Package Manager - Binary) [Official]
   - iOS (XCFramework manual)
4. Quick Start
   - Initialization
   - Manual popup display
   - Listen for events
5. Triggers & Conditions
6. Segmentation (lang / path)
7. Cooldown Persistence
8. Public API
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
- Launch them manually or automatically via triggers (time on page, scroll*, exit intent*).
- Apply conditions (cooldown, answered) and segmentation (language, path/screen).
- Listen to events for analytics (popup shown, clicked, survey completed).

> *Scroll and exit intent are defined as structures but logic will arrive in future tasks.

## 2. Features
- Kotlin Multiplatform (`:shared` module).
- Compose Multiplatform UI rendering.
- Coroutines for triggers and popup queue.
- Configurable persistence (in-memory or your own) for cooldowns.
- Basic HTML support (`<p>`, `<b>`, `<i>`).
- Inline survey renderer with platform bridges and runtime customization.

## 3. Installation / Integration

### Android (Maven Central)
- Coordinates (latest):
  - Group: `com.deepdots.sdk`
  - Artifact: `shared-android`
  - Version: `0.1.2`

1) Ensure you have Maven Central enabled
- settings.gradle(.kts)
```kotlin
pluginManagement {
    repositories { gradlePluginPortal(); google(); mavenCentral() }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories { google(); mavenCentral() }
}
```

2) Add the dependency in your app module
```kotlin
dependencies {
    implementation("com.deepdots.sdk:shared-android:0.1.2")
}
```

3) Requirements
- minSdk 24+
- Kotlin + Compose UI enabled in your app

> ProGuard/R8: the SDK ships with consumer rules; no additional rules required in most cases.

### iOS (Swift Package Manager - Binary) [Official]
Usa el paquete SPM binario (XCFramework) publicado en GitHub Releases.

Consumers (integración en tu app iOS):
- Xcode > File > Add Packages… y usa la URL del repo SPM: `https://github.com/MagicFeedback/DeepdotsSDK-SPM`
- Selecciona la versión (ej: 0.1.2).
- Añade el producto "ComposeApp" a tu target.
- Importa en Swift: `import ComposeApp`.

Maintainers (para preparar el release):
```bash
# Desde la raíz del repo del SDK
./scripts/prepare_spm_release.sh 0.1.2 https://github.com/MagicFeedback/DeepdotsSDK-SPM/releases/download/0.1.2
# Publica el zip en esa URL (GitHub Release) y commitea spm/Package.swift en el repo SPM
```

Notas:
- Requiere iOS 13+.
- Binario XCFramework estático (arm64 device + arm64 simulator).

### iOS (XCFramework manual)
- Alternativa sin SPM: compila el XCFramework e intégralo manualmente.
```bash
./gradlew :shared:assemble
# Combina frameworks con xcodebuild -create-xcframework o usa dist/spm/ComposeApp.xcframework del script SPM
```

## 4. Quick Start

### Initialization (Kotlin)
```kotlin
val popupDef = PopupDefinition(
    id = "popup-1",
    title = "Hello",
    message = "<p><b>Can you help us?</b></p>",
    trigger = Trigger.TimeOnPage(value = 5, condition = listOf(Condition(answered = false, cooldownDays = 2))),
    actions = Actions(
        accept = Action.Accept("Go", surveyId = "survey-123"),
        decline = Action.Decline("No", cooldownDays = 1)
    ),
    surveyId = "survey-123",
    productId = "product-xyz",
    style = Style(theme = Theme.Light, position = Position.BottomRight),
    segments = null
)
val sdk = Deepdots.create()
// Set initial path (update on navigation changes)
sdk.setPath("/home")
sdk.init(
    InitOptions(
        debug = true,
        popupOptions = PopupOptions(popups = listOf(popupDef)),
        autoLaunch = true,
        provideLang = { "en" }
    )
)
```

### Manual popup display
```kotlin
sdk.show(
    ShowOptions(
        surveyId = "survey-123",
        productId = "product-xyz",
        data = mapOf("source" to "manual_button")
    ),
    PlatformContext(activity)
)
```

### Listen for events
```kotlin
sdk.on(Event.PopupShown) { println("Popup shown: ${it.popupId}") }
sdk.on(Event.PopupClicked) { println("Popup clicked: ${it.popupId} action=${it.extra["action"]}") }
sdk.on(Event.SurveyCompleted) { println("Survey completed: ${it.surveyId}") }
```

Behavioral notes:
- The popup initially shows a Loading spinner and switches to Start/InProgress when the survey emits `loaded`/`popup_clicked`.
- When submitting, the UI briefly shows Loading on `before_submit`.

### Swift (iOS)
```swift
// Import the module provided by the KMP iOS framework (ComposeApp)
import ComposeApp
```

## 5. Triggers & Conditions
- `Trigger.TimeOnPage(value: seconds)` launches after delay.
- `Trigger.Scroll` (placeholder).
- `Trigger.Exit` (placeholder).
- `Condition(answered = false)` only shows if survey not marked answered via `markSurveyAnswered`.
- `Condition(cooldownDays = n)` blocks display if shown within last `n` days.

## 6. Segmentation
Each `PopupDefinition` may include:
- `segments.lang`: allowed languages.
- `segments.path`: allowed app paths/screens.
Compared against `provideLang()` and `providePath()` lambdas in `InitOptions`.

## 7. Cooldown Persistence
- Uses `KeyValueStorage` (default `InMemoryStorage`).
- Provide a custom implementation for persistent storage.
- Keys: `popup_last_shown_<popupId>`.

## 8. Public API (Entry Point)
`Deepdots.kt` re-exports:
- `Deepdots.create()` / `Deepdots.createInitialized(options)`
- Typealiases: `Trigger`, `Condition`, `PopupDefinition`, `Actions`, `Action`, `Style`, `Theme`, `Position`, `Segments`, `ShowOptions`, `InitOptions`.
- Helpers: `Deepdots.now()`, `Deepdots.parseHtml(html)`, `Deepdots.dismiss(context)`.

## 9. Full Examples
### Android
See `example-android/MainActivity.kt`.
- Initializes SDK.
- AutoLaunch + manual button.
- Event logs.

### iOS
See `iosApp/DeepdotsDemo.swift`.
- Initializes SDK.
- AutoLaunch + manual button.
- Logs events in Xcode console.

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

### Android → Maven Central (OSSRH)
Use the helper script `scripts/deploy_android_maven.sh` (no secrets in git).

1) Prepare environment variables
- Linux:
```bash
export OSSRH_USERNAME=your_sonatype_username
export OSSRH_PASSWORD=your_sonatype_token
export SIGNING_PASSWORD=your_pgp_passphrase
export SIGNING_KEY_BASE64=$(base64 -w0 private-key.asc)
```
- macOS:
```bash
export OSSRH_USERNAME=your_sonatype_username
export OSSRH_PASSWORD=your_sonatype_token
export SIGNING_PASSWORD=your_pgp_passphrase
export SIGNING_KEY_BASE64=$(base64 -i private-key.asc | tr -d '\n')
```

2) Run deploy (optionally override version)
```bash
VERSION=0.1.3 ./scripts/deploy_android_maven.sh
# or
./scripts/deploy_android_maven.sh
```

3) Finalize release
- Go to https://central.sonatype.com/publishing and Close/Release the staging repository.
- Sync to Maven Central search usually takes 10–30 minutes.

Security notes:
- Do NOT commit credentials or keys to git. Prefer exporting env vars locally or using CI secret storage.
- `.gitignore` excludes `private-key.asc` already.
