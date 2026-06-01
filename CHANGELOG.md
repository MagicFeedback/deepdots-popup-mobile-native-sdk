# Changelog

All notable changes to the Deepdots Popup SDK are documented in this file.

## Unreleased

### Fixed

- **Survey popup unreadable in system dark mode (iOS).** When the host device
  was in dark mode the survey area rendered with a black background and
  near-invisible text, while the popup card header stayed light. Root cause:
  the survey is hosted in a `WKWebView` via Compose's UIKit interop, which
  punches a transparent hole in the Compose canvas; with a transparent WebView
  the host view controller's background (black in system dark mode) showed
  through — it was **not** a `prefers-color-scheme` / CSS issue. The survey
  WebView now paints the popup's themed background as an opaque color, so the
  survey area always matches the rest of the popup. The same opaque themed
  background is applied on Android as a precaution. Also pins the survey
  document to `color-scheme: light` and forces the iOS WebView's
  `overrideUserInterfaceStyle` to light so native form controls don't adopt
  dark styling.
  - `SurveyView` now takes a `backgroundColor` parameter
    (`SurveyView.kt`, `PopupView.kt`, `SurveyView.ios.kt`,
    `SurveyView.android.kt`, `MagicFeedbackHtml.kt`).

> Note: true dark-mode theming of the survey is **not** included — the
> MagicFeedback web form has no dark variant (its stylesheet has no
> `prefers-color-scheme` rules and uses light-assumed text colors), so a dark
> background would make the survey text unreadable. Real dark support is
> tracked separately and requires theming the survey content layer first.

## 0.2.2

### Added

- Built-in localization for the SDK's default button labels (Send / Cancel /
  Start survey / Complete survey / Back). `provideLang` now drives the
  fallback used when `PopupDefinition.actions` does not supply an explicit
  label. Supported locales: `en`, `es`, `da`, `no` (incl. `nb` / `nn`), `sv`,
  `fi`, `zh-CN` (incl. `zh-Hans`). Region tags (`es-ES`, `nb-NO`, `zh-Hans`)
  and underscore variants (`es_419`) are accepted; unsupported locales fall
  back to English. Host- or server-supplied labels still take precedence.
- Public read-only `DeepdotsPopups.environment: Environment` property so
  host apps can assert at runtime which backend their `publicKey` is
  hitting. Reflects the value passed in `InitOptions.environment` and
  defaults to `Environment.Production` before `init()` is called.

### Fixed

- Survey WebView popups now wrap the form container with the `deepdots-popup`
  CSS class. The official popup stylesheet scopes most input, select,
  textarea, radio and focus rules under `.deepdots-popup`, so without this
  wrapper Android (and iOS) WebViews fell back to the platform's default
  user-agent styles. Symptoms reported by integrators included text inputs
  showing the orange WebView focus outline and rating questions rendering as
  a native multi-`<select>` with the default blue selection highlight. With
  the wrapper in place all scoped rules now apply correctly.

### Changed

- Bumped bundled `@magicfeedback/native` from `2.1.7-alpha.9` to the latest
  stable `2.2.4`. The CDN URLs (jsDelivr and unpkg) and the three local
  fallback bundles (`shared/src/androidMain/assets/magicfeedback/`,
  `shared/src/iosMain/resources/magicfeedback/`,
  `iosApp/iosApp/magicfeedback/`) have all been refreshed.
- `example-android` now depends on `:shared` via `project()` instead of the
  published Maven artifact so unreleased SDK changes are exercisable in the
  demo without first publishing a snapshot.
- `example-android` Login screen exposes a language picker for all seven
  supported locales, and the Home top bar surfaces the active environment
  and language as a subtitle (`env=… · lang=…`).
- Translated remaining Spanish KDocs and inline source comments to English
  across the public `commonMain` surface so IDE hover documentation reads
  in English for SDK consumers. The internal example
  `ui/example-ts-renderPopup.ts` was translated as well.
- Added a clarifying KDoc to `DeepdotsPopups.markSurveyAnswered()` noting
  that the SDK invokes it automatically on survey completion; hosts do not
  need to call it manually in normal flows.

### Documentation

- Documented the i18n behavior and the new `environment` getter in the
  English, Spanish, and Danish quickstart pages, and added a "Diagnostics"
  entry to the API reference.
- Removed the internal `Storage` / `KeyValueStorage` option from the public
  documentation (quickstart, models reference, server-mode guide). The SDK
  is designed for server mode where frequency caps are enforced by the
  backend; the in-memory cooldown cache is an internal detail and is no
  longer surfaced as a configurable knob.

## 0.2.0

### Breaking changes

- `InitOptions.debug` no longer controls the backend base URL. It now only controls
  SDK log output. Integrations that relied on `debug = true` to point the SDK at
  `https://api-dev.deepdots.com` must now opt in explicitly via the new
  `InitOptions.environment` field.

### Added

- `Environment { Production, Development }` enum exposed from
  `com.deepdots.sdk.models`.
- `InitOptions.environment: Environment?` — controls the backend base URL.
  Defaults to `Environment.Production` (`https://api.deepdots.com`). Set
  `Environment.Development` to hit `https://api-dev.deepdots.com`.

### Changed

- `SdkRuntime.env` is now derived from `InitOptions.environment` instead of
  `InitOptions.debug`. Combinations like `debug = true` with
  `environment = Environment.Production` now correctly emit SDK logs while
  talking to the production backend.
- Android and iOS demos (`example-android`, `example-android-local`,
  `iosApp/DeepdotsDemo.swift`) updated to set
  `environment = Environment.Development` so they keep pointing at the dev
  backend.
- Documentation (quickstart, server-mode guide) updated for English, Spanish
  and Danish to reflect the new field and the decoupling of `debug` from the
  environment.

### Migration

If you were relying on `debug = true` to hit the dev backend, add the new
field explicitly:

```kotlin
val options = InitOptions(
    debug = true,
    environment = Environment.Development,   // previously implied by debug = true
    mode = Mode.Server,
    popupOptions = PopupOptions(publicKey = "<your-public-key>")
)
```

To talk to production while keeping SDK logs on (previously impossible):

```kotlin
val options = InitOptions(
    debug = true,                            // logs on
    environment = Environment.Production,    // (or omit — it's the default)
    mode = Mode.Server,
    popupOptions = PopupOptions(publicKey = "<your-public-key>")
)
```

### Release follow-ups

- Publish `com.deepdots.sdk:shared-android:0.2.0` to Maven Central.
- Build `DeepdotsSDK-0.2.0.xcframework.zip`, upload it to the `0.2.0` release
  of `MagicFeedback/DeepdotsSDK-SPM`, then bump `spm/Package.swift` to the new
  URL and checksum.

## 0.1.7

- Last release before the `environment` / `debug` decoupling.
- See git history for prior changes.
