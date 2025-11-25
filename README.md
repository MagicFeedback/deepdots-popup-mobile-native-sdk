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
11. Roadmap (summary)
12. Troubleshooting

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
sdk.init(
    InitOptions(
        debug = true,
        popups = listOf(popupDef),
        autoLaunch = true,
        provideLang = { "en" },
        providePath = { "/home" }
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
sdk.init(options: InitOptions(debug: true, mode: Mode.client, popups: [popupDef], provideLang: { "en" }, providePath: { "/home" }, autoLaunch: true, storage: InMemoryStorage()))
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

## 11. Roadmap (quick summary)
See `ROADMAP.md` (Task 19) once created:
- Advanced theming.
- Server-driven popups.
- Animations / transitions.
- Additional triggers (real scroll, exit intent UX).
- Advanced persistence (DB, analytics integration).

## 12. Troubleshooting
| Issue | Common Cause | Solution |
|-------|--------------|----------|
| "unused" warnings | Examples/Tests not referencing everything | Ignore or add `@Suppress` |
| Popup not appearing (iOS) | Wrong `PlatformContext` | Ensure valid `rootViewController` |
| Cooldown not respected | Non-persistent storage | Provide custom `KeyValueStorage` |
| Missing ComposeApp module (iOS) | Framework not built | Run build and re-import |
| HTML lacks styles | Limited parser | Only `<p>`, `<b>`, `<i>` supported now |

## License / Authors
Pending definition. Internal demo usage.

---
Further improvements will be added in future tasks (Task 18 cleanup, Task 19 detailed roadmap).
