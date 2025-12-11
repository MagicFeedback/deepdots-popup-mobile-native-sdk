# Deepdots Popup SDK (Kotlin Multiplatform)

Multiplatform SDK (Android + iOS) to show popups and launch surveys using triggers, conditions, segmentation and simple HTML content.

## Table of Contents
1. Introduction
2. Features
3. Installation / Integration
   - Android (Gradle)
   - iOS (KMP Framework)
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

### Android
1. Include the `:shared` module (already present if you cloned the repository).
2. In your app module add:
```kotlin
dependencies {
    implementation(project(":shared"))
}
```
3. Ensure minSdk >= 24 and Compose enabled.
4. Use `PlatformContext(activity)` when calling `show()`.

### iOS
1. Build the KMP framework:
```bash
./gradlew :shared:assemble
```
This produces the framework (baseName `ComposeApp`) for iOS targets (Arm64 + Simulator Arm64).
2. In Xcode add the produced framework (or later an XCFramework when automated in Task 16).
3. Import the module in Swift, e.g. `import ComposeApp`.
4. Call SDK methods passing `PlatformContext(viewController)` when you show a popup.

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
let popupDef = PopupDefinition(
    id: "popup-ios",
    title: "Hello iOS",
    message: "<p><b>iOS Demo</b></p>",
    trigger: TriggerTimeOnPage(value: 4, condition: [Condition(answered: false, cooldownDays: 1)]),
    actions: Actions(accept: ActionAccept(label: "Yes", surveyId: "survey-ios"), decline: ActionDecline(label: "No", cooldownDays: 1)),
    surveyId: "survey-ios",
    productId: "product-ios",
    style: Style(theme: Theme.light, position: Position.center, imageUrl: nil),
    segments: nil
)
let sdk = Deepdots.create()
// Set initial path (update on navigation changes)
sdk.setPath("/home")
sdk.init(options: InitOptions(debug: true, mode: Mode.client, popupOptions: PopupOptions(popups: [popupDef]), provideLang: { "en" }, autoLaunch: true, storage: InMemoryStorage()))
// Manual show
if let root = UIApplication.shared.connectedScenes.compactMap({ ($0 as? UIWindowScene)?.keyWindow }).first?.rootViewController {
    sdk.show(options: ShowOptions(surveyId: "survey-ios", productId: "product-ios", data: ["source": "manual_button"]), context: PlatformContext(viewController: root))
}
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

## 10. Building Artifacts
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

Preliminary XCFramework packaging:
```bash
# (Will be automated in Task 16)
./gradlew :shared:assemble
# Combine arm64 + simulator later with a script
```

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
