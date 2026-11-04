# Deepdots | Popup SDK Release Validation Report

**SDK:** Deepdots native popup SDK **0.7.9** (Android `com.deepdots.sdk:shared-android:0.7.9` · iOS `DeepdotsSDK-SPM` 0.7.9)  
**Validation date:** 2026-11-04  
**Platforms:** iOS / Android  
**Overall status:** **FAILED**  
**Report type:** Automated release validation, generated from the CI test results (no manual edits)

---

## 1. Executive Summary

SDK 0.7.9 was validated as published (Maven Central and Swift Package Manager) inside dedicated native test applications that integrate the SDK the same way a customer app does.

Results ([CI run](https://github.com/MagicFeedback/deepdots-popup-mobile-native-sdk/actions/runs/41803323992/attempts/1)):

- **352** automated test executions on **16** configurations (7 iOS, 9 Android)
- **328** passed (3 on the automatic retry)
- **3** failed
- **21** not applicable on that platform
- Manual pass on real devices: **pending** (section 7)

**iOS:** FAILED · **Android:** FAILED

**Validation status:** **FAILED**

### Failures

| Platform | Configuration | Test | Detail |
|---|---|---|---|
| iOS | iOS 16.4 iPhone SE (3rd generation) Debug | path-targeting | xcuitest.installer.LocalXCTestInstaller$IOSDriverTimeoutException: iOS driver not ready in time, consider increasing timeout by configuring MAESTRO_DRIVER_STARTUP_TIMEOUT env variable |
| Android | A26 phone debug | slow-survey | Assertion is false: "Can we help?" is visible |
| Android | A26 phone release | slow-survey | Assertion is false: "Can we help?" is visible |

## 2. Test Results

| Test category | iOS | Android |
|---|---|---|
| SDK initialization and version check | PASS | PASS |
| Path-based triggers and excluded screens | **FAIL** | PASS |
| Time-based triggers | PASS | PASS |
| Event and click triggers | PASS | PASS |
| Exit triggers | PASS | PASS |
| Re-display rules (cooldown) | PASS | PASS |
| Audience segmentation by language | PASS | PASS |
| Popup opening and closing | PASS | PASS |
| Survey rendering, interactions and submission | PASS | PASS |
| Navigation and app lifecycle (background / foreground, rotation) | PASS | PASS |
| Network failures and recovery | PASS | **FAIL** |
| Detection check: a missing navigation event is caught | PASS | PASS |
| Visual regression | NOT YET AUTOMATED | NOT YET AUTOMATED |

`—` = not part of that platform's suite.

<details><summary>Every test on every configuration</summary>

| Test | A26 phone debug | A26 phone release | A30 phone debug | A30 phone release | A34 phone debug | A34 phone release | A34 tablet debug | A36 phone debug | A36 phone release | iOS 16.4 iPhone SE (3rd generation) Debug | iOS 17.5 iPhone 15 Debug | iOS 18 iPad (A16) Debug | iOS 18 iPhone 16 Pro Debug no-frame-key | iOS 18 iPhone 16 Pro Max Debug | iOS 18 iPhone 16 Release | iOS 26 iPhone 17 Debug |
|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|
| 00-version | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass |
| audience-lang | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass |
| checkout-time | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass |
| close-with-keyboard | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass |
| exit-checkout | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass |
| help-click | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass |
| lang-da-shows | pass | pass | pass | pass | retry | pass | pass | pass | pass | retry | pass | pass | pass | pass | retry | pass |
| lang-en-hidden | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass |
| lifecycle | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass |
| missing-navigation | expected | expected | expected | expected | expected | expected | expected | expected | expected | expected | expected | expected | expected | expected | expected | expected |
| not-on-cart | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass |
| offline | pass | pass | pass | pass | pass | pass | pass | pass | pass | skip | skip | skip | skip | skip | skip | skip |
| offline-open-popup | pass | pass | pass | pass | pass | pass | pass | pass | pass | skip | skip | skip | skip | skip | skip | skip |
| path-targeting | pass | pass | pass | pass | pass | pass | pass | pass | pass | error | pass | pass | pass | pass | pass | pass |
| popups-error | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass |
| rate-product | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass |
| rate-product-close | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass |
| rating-labels | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass |
| redisplay | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass |
| release-fixtures | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass |
| slow-survey | fail | fail | pass | pass | pass | pass | pass | pass | pass | skip | skip | skip | skip | skip | skip | skip |
| survey-missing | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass |

</details>

## 3. Tested Environments

| Platform | OS version | Device | Build | SDK resolved | Engine | Result |
|---|---|---|---|---|---|---|
| Android | API 26 | pixel_6 (emulator) | debug | 0.7.9 | WebView 69.0.3497.100 | **FAIL** |
| Android | API 26 | pixel_6 (emulator) | release (R8) | 0.7.9 | WebView 69.0.3497.100 | **FAIL** |
| Android | API 30 | pixel_6 (emulator) | debug | 0.7.9 | WebView 83.0.4103.106 | PASS |
| Android | API 30 | pixel_6 (emulator) | release (R8) | 0.7.9 | WebView 83.0.4103.106 | PASS |
| Android | API 34 | pixel_6 (emulator) | debug | 0.7.9 | WebView 113.0.5672.136 | PASS |
| Android | API 34 | pixel_6 (emulator) | release (R8) | 0.7.9 | WebView 113.0.5672.136 | PASS |
| Android | API 34 | pixel_tablet (emulator) | debug | 0.7.9 | WebView 113.0.5672.136 | PASS |
| Android | API 36 | pixel_6 (emulator) | debug | 0.7.9 | WebView 133.0.6943.137 | PASS |
| Android | API 36 | pixel_6 (emulator) | release (R8) | 0.7.9 | WebView 133.0.6943.137 | PASS |
| iOS | iOS-16-4 | iPhone SE (3rd generation) (simulator) | Debug · Xcode_16.4 | 0.7.9 | — | **FAIL** |
| iOS | iOS-17-5 | iPhone 15 (simulator) | Debug · Xcode_16.4 | 0.7.9 | — | PASS |
| iOS | iOS-18-6 | iPad (A16) (simulator) | Debug · Xcode_16.4 | 0.7.9 | — | PASS |
| iOS | iOS-18-6 | iPhone 16 Pro (simulator) | Debug · Xcode_16.4 | 0.7.9 | — | PASS |
| iOS | iOS-18-6 | iPhone 16 Pro Max (simulator) | Debug · Xcode_16.4 | 0.7.9 | — | PASS |
| iOS | iOS-18-6 | iPhone 16 (simulator) | Release · Xcode_16.4 | 0.7.9 | — | PASS |
| iOS | iOS-26-2 | iPhone 17 (simulator) | Debug · Xcode_26.2 | 0.7.9 | — | PASS |

All automated tests ran against the Deepdots development environment with test companies only. No customer data was used, and each configuration fails if any request reaches the production API.

## 4. Critical Safety Validation

| Scenario | iOS | Android |
|---|---|---|
| Survey that cannot load: the popup can be closed and the app stays usable | PASS | PASS |
| Popup configuration that cannot load: no popup, the app stays usable | PASS | PASS |
| No network: no popup, the app stays usable | n/a | PASS |
| Network lost right after a popup is triggered | n/a | PASS |
| Slow network while the survey loads | n/a | **FAIL** |
| Crash, freeze (ANR) or threading errors in the device log, and no request to production (every configuration) | None detected | None detected |

## 5. Detection Controls and Backward Compatibility

The test system has to prove it catches real problems. These controls run in every validation:

| Control | Result |
|---|---|
| Negative control: the bug customers saw on the control version is detected (control 0.7.0) | PASS |
| R8 control: the control version fails a minified release build, the tested one builds (control 0.7.0) | PASS |
| Expected failure detected: missing-navigation | iOS PASS · Android PASS |

## 6. Known Issues & Limitations

**Known blocking issues:** see Failures in section 1.

**Known non-blocking issues:**

- None.

**Test limitations:**

- Emulators and simulators cover the OS versions, not every manufacturer; real devices are checked by hand (section 7).
- Visual regression (screenshot comparison) is not yet automated for the native SDK.
- Tests marked as known issues are expected to fail until their fix ships; if one passes, the run fails so the mark is removed.

## 7. Real Devices (manual)

**Status: PENDING.** Filled in by hand after this report is generated: release build of the test app with this exact version on one Android phone, one iPhone and one iPad.

| Check | Android phone | iPhone | iPad |
|---|---|---|---|
| Popup appears complete, once | pending | pending | pending |
| Survey answered and sent to the end | pending | pending | pending |
| Not shown again within its cooldown | pending | pending | pending |
| Taps outside the popup do not reach the app; X closes it | pending | pending | pending |
| Background / foreground with the popup open | pending | pending | pending |
| Rotation with the popup open | pending | pending | pending |
| Keyboard does not cover Send | pending | pending | pending |
| No network: notice, can be closed, app usable | pending | pending | pending |

## 8. Release Information

- **SDK version:** 0.7.9
- **Release:** [GitHub release v0.7.9](https://github.com/MagicFeedback/deepdots-popup-mobile-native-sdk/releases/tag/v0.7.9)
- **Changelog:** [CHANGELOG](https://github.com/MagicFeedback/deepdots-popup-mobile-native-sdk/blob/main/CHANGELOG.md)
- **Test execution:** [CI run](https://github.com/MagicFeedback/deepdots-popup-mobile-native-sdk/actions/runs/41803323992/attempts/1) (logs, screenshots and per-configuration artifacts)
- **Validation date:** 2026-11-04
- **Results file SHA-256:** `689bc810009bb046aeda7fc7b7d58e6e5292e01a9328015c209b69445aef8056`

### Release notes 0.7.9

#### Fixed

- **iOS: a survey taller than its box can be scrolled again.** The survey's WebView sat inside
  the popup's scrollable middle with the default (cooperative) touch handling: the middle took
  every vertical drag, could not move because its content already fitted, and the WebView never
  scrolled. On a small iPhone (SE) the 0–10 scale, shown as a vertical list there, could not be
  scrolled past 3, so 4–10 could not be chosen. The survey now gets its touches directly and
  scrolls its own content; its content stays available to VoiceOver. The console bridge of the
  iOS WebView also drops optional chaining (iOS 13.0–13.3).

- **iOS: the keyboard no longer covers the popup's Back and Send.** The SDK relied on the Compose
  view controller to move the focused field above the keyboard, but the survey's text field lives
  in a WKWebView, which Compose does not see as focused: nothing moved, and on a free-text page
  (focused on its own after Next) the keyboard covered the footer until it was dismissed. Seen on
  every iPhone in the device matrix (iPhone SE to iPhone 17). The popup now keeps itself above the
  keyboard with the IME inset, as on Android, and the view controller no longer shifts the view on
  focus, so it is not moved twice.

- **The close button (X) stays visible when the survey does not fit.** With a tall survey page,
  the whole card scrolled, and after a "required" message it scrolled to the bottom on its own:
  the title and the X went out of view, so the popup could not be closed without scrolling back
  up. The title row and the buttons now stay fixed and only the content in between scrolls.
  Found by the end-to-end tests on iOS.
- **The popup's Send, Back, close and Start work on older WebViews.** The native buttons ran
  `window.DeepdotsActions?.send()` in the survey; a WebView older than Chrome 80 (Android 8–10
  phones whose WebView was never updated) and iOS 13.0–13.3 reject optional chaining with a
  `SyntaxError`, so the button did nothing and pages that need Send never moved on (the rating
  page worked because it advances by itself). The calls are now plain ES5, and a test fails if
  the survey's scripts use `?.` or `??` again. Found by the local device matrix on Android 8.
- **Android 8–10: the keyboard no longer covers the popup's Back and Send.** The popup is mounted
  on the window's decor view, outside the content the Activity resizes, and before Android 11 the
  keyboard was not reported to it as an inset, so `imePadding()` was 0 and the footer stayed
  under the keyboard. On those versions the popup now measures the keyboard from the window's
  visible frame and moves above it; Android 11+ keeps using the IME inset.

- **iOS: a popup at the top no longer goes under the status bar.** With a top position and a tall
  survey, the card was laid out from the very top of the screen: the title sat on the clock and
  the close button (X) under the battery icon, where it could not be tapped. The card now stays
  inside the safe area (status bar, Dynamic Island / notch, home indicator); the dimmed backdrop
  still covers the whole screen. Found by the new end-to-end tests.
