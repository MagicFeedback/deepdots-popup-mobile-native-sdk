# Deepdots | Popup SDK Release Validation Report

**SDK:** Deepdots native popup SDK **0.7.14** (Android `com.deepdots.sdk:shared-android:0.7.14` · iOS `DeepdotsSDK-SPM` 0.7.14)  
**Validation date:** 2026-11-23  
**Platforms:** iOS / Android  
**Overall status:** **FAILED**  
**Report type:** Automated release validation, generated from the CI test results (no manual edits)

---

## 1. Executive Summary

SDK 0.7.14 was validated as published (Maven Central and Swift Package Manager) inside dedicated native test applications that integrate the SDK the same way a customer app does.

Results ([CI run](https://github.com/MagicFeedback/deepdots-popup-mobile-native-sdk/actions/runs/45250872853/attempts/1)):

- **416** automated test executions on **16** configurations (7 iOS, 9 Android)
- **392** passed (4 on the automatic retry)
- **1** failed
- **23** not applicable on that platform
- Manual pass on real devices: **pending** (section 7)

**iOS:** PASSED · **Android:** FAILED

**Validation status:** **FAILED**

### Failures

| Platform | Configuration | Test | Detail |
|---|---|---|---|
| Android | A26 phone release | 00-version | maestro.android.DeviceServerDiedException: Device server died during 'deviceInfo' on emulator-5554 (179ms since last byte, connection age 179ms): StatusRuntimeException: UNAVAILABLE |

## 2. Test Results

| Test category | iOS | Android |
|---|---|---|
| SDK initialization and version check | PASS | **FAIL** |
| Path-based triggers and excluded screens | PASS | PASS |
| Time-based triggers | PASS | PASS |
| Event and click triggers | PASS | PASS |
| Exit triggers | PASS | PASS |
| Re-display rules (cooldown) | PASS | PASS |
| Audience segmentation by language | PASS | PASS |
| Popup opening and closing | PASS | PASS |
| Survey rendering, interactions and submission | PASS | PASS |
| Navigation and app lifecycle (background / foreground, rotation) | PASS | PASS |
| Network failures and recovery | PASS | PASS |
| Detection check: a missing navigation event is caught | PASS | PASS |
| Survey SDK versions assigned in the Admin (DD-70) | PASS | PASS |
| Visual regression | AUTOMATED: screenshots of the popup states (iOS + Android, standard and small screen) compared with approved baselines by the job "Visual regression · <platform> · SDK local" (test-apps/visual/README.md). | AUTOMATED: screenshots of the popup states (iOS + Android, standard and small screen) compared with approved baselines by the job "Visual regression · <platform> · SDK local" (test-apps/visual/README.md). |

`—` = not part of that platform's suite.

<details><summary>Every test on every configuration</summary>

| Test | A26 phone debug | A26 phone release | A30 phone debug | A30 phone release | A34 phone debug | A34 phone release | A34 tablet debug | A36 phone debug | A36 phone release | iOS 16.4 iPhone SE (3rd generation) Debug | iOS 17.5 iPhone 15 Debug | iOS 18 iPad (A16) Debug | iOS 18 iPhone 16 Pro Debug no-frame-key | iOS 18 iPhone 16 Pro Max Debug | iOS 18 iPhone 16 Release | iOS 26 iPhone 17 Debug |
|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|
| 00-version | pass | fail | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass |
| audience-lang | pass | retry | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass |
| checkout-time | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass |
| close-with-keyboard | pass | pass | pass | pass | pass | pass | pass | pass | retry | pass | pass | pass | pass | pass | pass | pass |
| exit-checkout | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass |
| help-click | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass |
| lang-da-shows | pass | pass | pass | pass | pass | pass | pass | pass | pass | retry | pass | pass | pass | pass | pass | pass |
| lang-en-hidden | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass |
| lifecycle | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass |
| missing-navigation | expected | expected | expected | expected | expected | expected | expected | expected | expected | expected | expected | expected | expected | expected | expected | expected |
| not-on-cart | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass |
| offline | pass | pass | pass | pass | pass | pass | pass | pass | pass | skip | skip | skip | skip | skip | skip | skip |
| offline-open-popup | pass | pass | pass | pass | pass | retry | pass | pass | pass | skip | skip | skip | skip | skip | skip | skip |
| path-targeting | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass |
| popups-error | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass |
| rate-delay | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass |
| rate-delay-cancel | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass |
| rate-product | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass |
| rate-product-close | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass |
| rating-labels | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass |
| redisplay | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass |
| release-fixtures | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass |
| slow-survey | skip | skip | pass | pass | pass | pass | pass | pass | pass | skip | skip | skip | skip | skip | skip | skip |
| survey-missing | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass |
| survey-sdk-2.2.38 | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass |
| survey-sdk-2.2.39 | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass |

</details>

## 3. Tested Environments

| Platform | OS version | Device | Build | SDK resolved | Engine | Result |
|---|---|---|---|---|---|---|
| Android | API 26 | pixel_6 (emulator) | debug | 0.7.14 | WebView 69.0.3497.100 | PASS |
| Android | API 26 | pixel_6 (emulator) | release (R8) | 0.7.14 | WebView 69.0.3497.100 | **FAIL** |
| Android | API 30 | pixel_6 (emulator) | debug | 0.7.14 | WebView 83.0.4103.106 | PASS |
| Android | API 30 | pixel_6 (emulator) | release (R8) | 0.7.14 | WebView 83.0.4103.106 | PASS |
| Android | API 34 | pixel_6 (emulator) | debug | 0.7.14 | WebView 113.0.5672.136 | PASS |
| Android | API 34 | pixel_6 (emulator) | release (R8) | 0.7.14 | WebView 113.0.5672.136 | PASS |
| Android | API 34 | pixel_tablet (emulator) | debug | 0.7.14 | WebView 113.0.5672.136 | PASS |
| Android | API 36 | pixel_6 (emulator) | debug | 0.7.14 | WebView 133.0.6943.137 | PASS |
| Android | API 36 | pixel_6 (emulator) | release (R8) | 0.7.14 | WebView 133.0.6943.137 | PASS |
| iOS | iOS-16-4 | iPhone SE (3rd generation) (simulator) | Debug · Xcode_16.4 | 0.7.14 | — | PASS |
| iOS | iOS-17-5 | iPhone 15 (simulator) | Debug · Xcode_16.4 | 0.7.14 | — | PASS |
| iOS | iOS-18-6 | iPad (A16) (simulator) | Debug · Xcode_16.4 | 0.7.14 | — | PASS |
| iOS | iOS-18-6 | iPhone 16 Pro (simulator) | Debug · Xcode_16.4 | 0.7.14 | — | PASS |
| iOS | iOS-18-6 | iPhone 16 Pro Max (simulator) | Debug · Xcode_16.4 | 0.7.14 | — | PASS |
| iOS | iOS-18-6 | iPhone 16 (simulator) | Release · Xcode_16.4 | 0.7.14 | — | PASS |
| iOS | iOS-26-2 | iPhone 17 (simulator) | Debug · Xcode_26.2 | 0.7.14 | — | PASS |

All automated tests ran against the Deepdots development environment with test companies only. No customer data was used, and each configuration fails if any request reaches the production API.

## 4. Critical Safety Validation

| Scenario | iOS | Android |
|---|---|---|
| Survey that cannot load: the popup can be closed and the app stays usable | PASS | PASS |
| Popup configuration that cannot load: no popup, the app stays usable | PASS | PASS |
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

- **SDK version:** 0.7.14
- **Release:** [GitHub release v0.7.14](https://github.com/MagicFeedback/deepdots-popup-mobile-native-sdk/releases/tag/v0.7.14)
- **Changelog:** [CHANGELOG](https://github.com/MagicFeedback/deepdots-popup-mobile-native-sdk/blob/main/CHANGELOG.md)
- **Test execution:** [CI run](https://github.com/MagicFeedback/deepdots-popup-mobile-native-sdk/actions/runs/45250872853/attempts/1) (logs, screenshots and per-configuration artifacts)
- **Validation date:** 2026-11-23
- **Results file SHA-256:** `49deb426ba16304f936500ced6d2191f8b5add9f94edd73b3349bd367f458f9d`

### Release notes 0.7.14

Survey SDK supported: 2.2.38–2.2.44 (survey frontend 3.4.1–3.6.0); bundled: 2.2.44.

#### Added

- **The progress bar can count answered questions (DD-210).** With `style.progressMode:
  "completed"` the bar starts at 0 %, shows "2 of 5 answered" on question 3, does not move while
  an AI follow-up is open, and shows 100 % ("5 of 5 answered") on the thank-you screen. Missing or
  `"current"` keeps today's bar. Same rules as the web popup and the survey link.

#### Fixed

- **The question stays in view with the keyboard open on small phones.** With the survey fitted
  to the card (see below), centring the focused text field pushed the question text out of
  view on a 360x640 screen. The survey now scrolls the question block (question text and the
  first line of the field) to the top, and only the field when even that does not fit.
- **The end of the survey is no longer hidden when the popup has a logo.** The survey area took
  72 % of the card without counting the logo, progress bar, message or error banner. With a logo
  the middle of the card overflowed and scrolled on its own, but a finger on the survey only
  scrolls the WebView, so the bottom of the survey stayed under the footer: the last number of a
  0-10 list, the last option of a question, and on iPhone the text field with the keyboard open
  (Lemvigh-Müller, Galaxy S21/S25 and iPhone 17). The survey now gets the height that is left in
  the card and scrolls inside. Android and iOS.
- **The logo folds away while the keyboard is open.** On text questions the card shrinks with the
  keyboard and the logo kept its space, so the question text went out of view. The logo comes back
  when the keyboard closes. Android and iOS.
- **iPads are reported as iOS.** On iPad `UIDevice.systemName` is "iPadOS", so `deepdots_sdk`
  said `android@<version>` and analytics batches had `platform: android`. Both now say `ios`.

#### Tests

- **Key popup checks on real devices (DD-273).** Open popup, X / Send / Back with a finger-like
  tap, keyboard over the text question and the survey filling its slot on a tablet, as Android
  instrumented tests with the real WebView (`shared/src/androidInstrumentedTest`) and as XCUITest
  on NORDLY (`test-apps/ios/NordlyDeviceChecks`). New workflow `testlab.yml` runs them in Firebase
  Test Lab on a fixed device list (`testlab/devices.json`) only on release PRs and by hand, with a
  10 min timeout, no retries and the billed minutes in the job summary. No change to the SDK
  itself: the build only gains the instrumented test set-up.
