# Deepdots | Popup SDK Release Validation Report

**SDK:** Deepdots native popup SDK **0.7.12** (Android `com.deepdots.sdk:shared-android:0.7.12` · iOS `DeepdotsSDK-SPM` 0.7.12)  
**Validation date:** 2026-11-17  
**Platforms:** iOS / Android  
**Overall status:** **FAILED**  
**Report type:** Automated release validation, generated from the CI test results (no manual edits)

---

## 1. Executive Summary

SDK 0.7.12 was validated as published (Maven Central and Swift Package Manager) inside dedicated native test applications that integrate the SDK the same way a customer app does.

Results ([CI run](https://github.com/MagicFeedback/deepdots-popup-mobile-native-sdk/actions/runs/44176931068/attempts/1)):

- **398** automated test executions on **16** configurations (7 iOS, 9 Android)
- **372** passed (5 on the automatic retry)
- **3** failed
- **23** not applicable on that platform
- Manual pass on real devices: **pending** (section 7)

**iOS:** FAILED · **Android:** FAILED

**Validation status:** **FAILED**

### Failures

| Platform | Configuration | Test | Detail |
|---|---|---|---|
| iOS | iOS 16.4 iPhone SE (3rd generation) Debug | popups-error | xcuitest.installer.LocalXCTestInstaller$IOSDriverTimeoutException: iOS driver not ready in time, consider increasing timeout by configuring MAESTRO_DRIVER_STARTUP_TIMEOUT env variable |
| iOS | iOS 16.4 iPhone SE (3rd generation) Debug | survey-missing | xcuitest.installer.LocalXCTestInstaller$IOSDriverTimeoutException: iOS driver not ready in time, consider increasing timeout by configuring MAESTRO_DRIVER_STARTUP_TIMEOUT env variable |
| Android | A34 tablet debug | redisplay | You have 0 devices connected, which is not enough to run 1 shards. Missing 1 device(s). |

## 2. Test Results

| Test category | iOS | Android |
|---|---|---|
| SDK initialization and version check | PASS | PASS |
| Path-based triggers and excluded screens | PASS | PASS |
| Time-based triggers | PASS | PASS |
| Event and click triggers | PASS | PASS |
| Exit triggers | PASS | PASS |
| Re-display rules (cooldown) | PASS | **FAIL** |
| Audience segmentation by language | PASS | PASS |
| Popup opening and closing | PASS | PASS |
| Survey rendering, interactions and submission | PASS | PASS |
| Navigation and app lifecycle (background / foreground, rotation) | PASS | PASS |
| Network failures and recovery | **FAIL** | PASS |
| Detection check: a missing navigation event is caught | PASS | PASS |
| Survey SDK versions assigned in the Admin (DD-70) | PASS | PASS |
| Visual regression | AUTOMATED: screenshots of the popup states (iOS + Android, standard and small screen) compared with approved baselines by the job "Visual regression · <platform> · SDK local" (test-apps/visual/README.md). | AUTOMATED: screenshots of the popup states (iOS + Android, standard and small screen) compared with approved baselines by the job "Visual regression · <platform> · SDK local" (test-apps/visual/README.md). |

`—` = not part of that platform's suite.

<details><summary>Every test on every configuration</summary>

| Test | A26 phone debug | A26 phone release | A30 phone debug | A30 phone release | A34 phone debug | A34 phone release | A34 tablet debug | A36 phone debug | A36 phone release | iOS 16.4 iPhone SE (3rd generation) Debug | iOS 17.5 iPhone 15 Debug | iOS 18 iPad (A16) Debug | iOS 18 iPhone 16 Pro Debug no-frame-key | iOS 18 iPhone 16 Pro Max Debug | iOS 18 iPhone 16 Release | iOS 26 iPhone 17 Debug |
|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|
| 00-version | pass | pass | pass | pass | pass | pass | pass | pass | pass | retry | pass | pass | pass | pass | pass | pass |
| audience-lang | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass |
| checkout-time | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass |
| close-with-keyboard | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass | retry | pass | pass |
| exit-checkout | pass | pass | pass | pass | pass | pass | pass | pass | pass | retry | pass | pass | pass | pass | pass | pass |
| help-click | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass |
| lang-da-shows | pass | pass | pass | pass | pass | pass | pass | pass | pass | retry | pass | pass | pass | pass | pass | pass |
| lang-en-hidden | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass |
| lifecycle | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass |
| missing-navigation | expected | expected | expected | expected | expected | expected | expected | expected | expected | expected | expected | expected | expected | expected | expected | expected |
| not-on-cart | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass |
| offline | pass | pass | pass | pass | pass | pass | pass | pass | pass | skip | skip | skip | skip | skip | skip | skip |
| offline-open-popup | pass | pass | pass | pass | pass | pass | pass | pass | pass | skip | skip | skip | skip | skip | skip | skip |
| path-targeting | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass |
| popups-error | pass | pass | pass | pass | pass | pass | pass | pass | pass | error | pass | pass | pass | pass | pass | pass |
| rate-delay | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass |
| rate-delay-cancel | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass |
| rate-product | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass |
| rate-product-close | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass |
| rating-labels | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass | retry |
| redisplay | pass | pass | pass | pass | pass | pass | error | pass | pass | pass | pass | pass | pass | pass | pass | pass |
| release-fixtures | pass | pass | pass | pass | pass | pass | – | pass | pass | pass | pass | pass | pass | pass | pass | pass |
| slow-survey | skip | skip | pass | pass | pass | pass | pass | pass | pass | skip | skip | skip | skip | skip | skip | skip |
| survey-missing | pass | pass | pass | pass | pass | pass | pass | pass | pass | error | pass | pass | pass | pass | pass | pass |
| survey-sdk-2.2.38 | pass | pass | pass | pass | pass | pass | – | pass | pass | pass | pass | pass | pass | pass | pass | pass |

</details>

## 3. Tested Environments

| Platform | OS version | Device | Build | SDK resolved | Engine | Result |
|---|---|---|---|---|---|---|
| Android | API 26 | pixel_6 (emulator) | debug | 0.7.12 | WebView 69.0.3497.100 | PASS |
| Android | API 26 | pixel_6 (emulator) | release (R8) | 0.7.12 | WebView 69.0.3497.100 | PASS |
| Android | API 30 | pixel_6 (emulator) | debug | 0.7.12 | WebView 83.0.4103.106 | PASS |
| Android | API 30 | pixel_6 (emulator) | release (R8) | 0.7.12 | WebView 83.0.4103.106 | PASS |
| Android | API 34 | pixel_6 (emulator) | debug | 0.7.12 | WebView 113.0.5672.136 | PASS |
| Android | API 34 | pixel_6 (emulator) | release (R8) | 0.7.12 | WebView 113.0.5672.136 | PASS |
| Android | API 34 | pixel_tablet (emulator) | debug | 0.7.12 | WebView 113.0.5672.136 | **FAIL** |
| Android | API 36 | pixel_6 (emulator) | debug | 0.7.12 | WebView 133.0.6943.137 | PASS |
| Android | API 36 | pixel_6 (emulator) | release (R8) | 0.7.12 | WebView 133.0.6943.137 | PASS |
| iOS | iOS-16-4 | iPhone SE (3rd generation) (simulator) | Debug · Xcode_16.4 | 0.7.12 | — | **FAIL** |
| iOS | iOS-17-5 | iPhone 15 (simulator) | Debug · Xcode_16.4 | 0.7.12 | — | PASS |
| iOS | iOS-18-6 | iPad (A16) (simulator) | Debug · Xcode_16.4 | 0.7.12 | — | PASS |
| iOS | iOS-18-6 | iPhone 16 Pro (simulator) | Debug · Xcode_16.4 | 0.7.12 | — | PASS |
| iOS | iOS-18-6 | iPhone 16 Pro Max (simulator) | Debug · Xcode_16.4 | 0.7.12 | — | PASS |
| iOS | iOS-18-6 | iPhone 16 (simulator) | Release · Xcode_16.4 | 0.7.12 | — | PASS |
| iOS | iOS-26-2 | iPhone 17 (simulator) | Debug · Xcode_26.2 | 0.7.12 | — | PASS |

All automated tests ran against the Deepdots development environment with test companies only. No customer data was used, and each configuration fails if any request reaches the production API.

## 4. Critical Safety Validation

| Scenario | iOS | Android |
|---|---|---|
| Survey that cannot load: the popup can be closed and the app stays usable | **FAIL** | PASS |
| Popup configuration that cannot load: no popup, the app stays usable | **FAIL** | PASS |
| No network: no popup, the app stays usable | n/a | PASS |
| Network lost right after a popup is triggered | n/a | PASS |
| Slow network while the survey loads | n/a | PASS |
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
- Visual regression compares screenshots on one standard and one small device per platform (CI emulator / simulator); other devices and OS versions are covered by the functional tests, not by screenshots.
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

- **SDK version:** 0.7.12
- **Release:** [GitHub release v0.7.12](https://github.com/MagicFeedback/deepdots-popup-mobile-native-sdk/releases/tag/v0.7.12)
- **Changelog:** [CHANGELOG](https://github.com/MagicFeedback/deepdots-popup-mobile-native-sdk/blob/main/CHANGELOG.md)
- **Test execution:** [CI run](https://github.com/MagicFeedback/deepdots-popup-mobile-native-sdk/actions/runs/44176931068/attempts/1) (logs, screenshots and per-configuration artifacts)
- **Validation date:** 2026-11-17
- **Results file SHA-256:** `887b5c021c0812820f974110ded8049dc3c956cf539328e2e4ea2a49db945856`

### Release notes 0.7.12

Survey SDK supported: 2.2.38–2.2.39 (survey frontend 3.4.1–3.6.0); bundled: 2.2.39. Compatibility table: docs.deepdots.com/popup-native/reference/survey-compatibility/.

#### Added

- **Catalan (`ca`) popup texts.** Buttons, progress, aria-labels and error notices in Catalan;
  `ca`/`ca-ES` resolve to it. Same strings as the web SDK. Catalan is not an official EU language;
  asked for in the v2.2.29 review.

#### Fixed

- **The popup's X, Send, Back and Start respond to a real finger again.** Since 0.7.2 the dimmed
  backdrop consumed every touch so it would not reach the host app. The card with the buttons is
  a child of that backdrop, and when a parent consumes the small movements a finger always makes
  between pressing and lifting, the button cancels the click. On real phones the native buttons
  often did nothing (the survey itself, in the WebView, still answered), so the popup could not
  be closed or moved past a page that needs Send. Automated taps have no movement, which is why
  the test suites did not see it. The backdrop now receives the touches without consuming them:
  they still stop at the popup (the host app does not get them) and the buttons work. Both
  Android and iOS. Reported by Lemvigh-Müller on Galaxy S25 (Android 16 and 17).
- **Tablets: the survey uses the whole height the popup gives it.** The survey's WebView had a
  fixed height of 520dp. On a tablet the popup has more room than that, so the survey stopped at
  520dp, a 0-10 scale shown as a list was cut in the middle (it had to be scrolled inside) and a
  band of white space stayed between it and Send. The WebView now fills its area, up to the
  popup's maximum height as before. Phones were not affected (their area is below 520dp). Both
  Android and iOS. Reported by Lemvigh-Müller on a Samsung tablet.
- **The text box stays in view when the keyboard opens.** With the keyboard open the popup is
  lifted above it and its survey area gets shorter; a text question under a long title (small
  phones, large font sizes) was left below the visible part, so the user typed without seeing
  the text. The survey now scrolls the focused field into view when its area shrinks or a field
  gets focus. Both Android and iOS.
