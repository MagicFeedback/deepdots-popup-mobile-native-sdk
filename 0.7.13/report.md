# Deepdots | Popup SDK Release Validation Report

**SDK:** Deepdots native popup SDK **0.7.13** (Android `com.deepdots.sdk:shared-android:0.7.13` · iOS `DeepdotsSDK-SPM` 0.7.13)  
**Validation date:** 2026-11-19  
**Platforms:** iOS / Android  
**Overall status:** **PASSED**  
**Report type:** Automated release validation, generated from the CI test results (no manual edits)

---

## 1. Executive Summary

SDK 0.7.13 was validated as published (Maven Central and Swift Package Manager) inside dedicated native test applications that integrate the SDK the same way a customer app does.

Results ([CI run](https://github.com/MagicFeedback/deepdots-popup-mobile-native-sdk/actions/runs/44549758293/attempts/2)):

- **416** automated test executions on **16** configurations (7 iOS, 9 Android)
- **393** passed (1 on the automatic retry)
- **0** failed
- **23** not applicable on that platform
- Manual pass on real devices: **pending** (section 7)

**iOS:** PASSED · **Android:** PASSED

**Validation status:** **PASSED**

## 2. Test Results

| Test category | iOS | Android |
|---|---|---|
| SDK initialization and version check | PASS | PASS |
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
| 00-version | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass |
| audience-lang | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass |
| checkout-time | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass |
| close-with-keyboard | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass |
| exit-checkout | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass |
| help-click | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass |
| lang-da-shows | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass |
| lang-en-hidden | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass |
| lifecycle | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass |
| missing-navigation | expected | expected | expected | expected | expected | expected | expected | expected | expected | expected | expected | expected | expected | expected | expected | expected |
| not-on-cart | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass |
| offline | pass | pass | pass | pass | pass | pass | pass | pass | pass | skip | skip | skip | skip | skip | skip | skip |
| offline-open-popup | pass | pass | pass | pass | pass | pass | pass | pass | pass | skip | skip | skip | skip | skip | skip | skip |
| path-targeting | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass |
| popups-error | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass |
| rate-delay | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass |
| rate-delay-cancel | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass |
| rate-product | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass |
| rate-product-close | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass |
| rating-labels | pass | pass | pass | pass | pass | pass | pass | pass | pass | retry | pass | pass | pass | pass | pass | pass |
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
| Android | API 26 | pixel_6 (emulator) | debug | 0.7.13 | WebView 69.0.3497.100 | PASS |
| Android | API 26 | pixel_6 (emulator) | release (R8) | 0.7.13 | WebView 69.0.3497.100 | PASS |
| Android | API 30 | pixel_6 (emulator) | debug | 0.7.13 | WebView 83.0.4103.106 | PASS |
| Android | API 30 | pixel_6 (emulator) | release (R8) | 0.7.13 | WebView 83.0.4103.106 | PASS |
| Android | API 34 | pixel_6 (emulator) | debug | 0.7.13 | WebView 113.0.5672.136 | PASS |
| Android | API 34 | pixel_6 (emulator) | release (R8) | 0.7.13 | WebView 113.0.5672.136 | PASS |
| Android | API 34 | pixel_tablet (emulator) | debug | 0.7.13 | WebView 113.0.5672.136 | PASS |
| Android | API 36 | pixel_6 (emulator) | debug | 0.7.13 | WebView 133.0.6943.137 | PASS |
| Android | API 36 | pixel_6 (emulator) | release (R8) | 0.7.13 | WebView 133.0.6943.137 | PASS |
| iOS | iOS-16-4 | iPhone SE (3rd generation) (simulator) | Debug · Xcode_16.4 | 0.7.13 | — | PASS |
| iOS | iOS-17-5 | iPhone 15 (simulator) | Debug · Xcode_16.4 | 0.7.13 | — | PASS |
| iOS | iOS-18-6 | iPad (A16) (simulator) | Debug · Xcode_16.4 | 0.7.13 | — | PASS |
| iOS | iOS-18-6 | iPhone 16 Pro (simulator) | Debug · Xcode_16.4 | 0.7.13 | — | PASS |
| iOS | iOS-18-6 | iPhone 16 Pro Max (simulator) | Debug · Xcode_16.4 | 0.7.13 | — | PASS |
| iOS | iOS-18-6 | iPhone 16 (simulator) | Release · Xcode_16.4 | 0.7.13 | — | PASS |
| iOS | iOS-26-2 | iPhone 17 (simulator) | Debug · Xcode_26.2 | 0.7.13 | — | PASS |

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

**Known blocking issues:** none identified.

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

- **SDK version:** 0.7.13
- **Release:** [GitHub release v0.7.13](https://github.com/MagicFeedback/deepdots-popup-mobile-native-sdk/releases/tag/v0.7.13)
- **Changelog:** [CHANGELOG](https://github.com/MagicFeedback/deepdots-popup-mobile-native-sdk/blob/main/CHANGELOG.md)
- **Test execution:** [CI run](https://github.com/MagicFeedback/deepdots-popup-mobile-native-sdk/actions/runs/44549758293/attempts/2) (logs, screenshots and per-configuration artifacts)
- **Validation date:** 2026-11-19
- **Results file SHA-256:** `3234f0fdbe68b62c2af9e34d3e3a103489176085967c079832504742365c6ffd`

### Release notes 0.7.13

Survey SDK supported: 2.2.38–2.2.44 (survey frontend 3.4.1–3.6.0); bundled: 2.2.44.

#### Added

- **Light / dark popup and survey from the app (DD-240).** New `InitOptions.appearance`
  (`Appearance.System`, `Light`, `Dark`) and `DeepdotsPopups.setAppearance()` / `getAppearance()`.
  `null` (default) keeps today's behaviour: the survey Style colours decide (DD-246). `System`
  follows the device (Android `uiMode`, iOS `traitCollection`); the popup frame also recolours
  while it is open, the survey takes the mode when the popup opens. In dark the card, primary
  button and progress bar use the Style's optional `dark` colours (DD-239), else the Style colour
  if it is already dark, else a default dark palette (light primary button with dark text). The
  survey gets the mode (`mode` option of the survey SDK, DD-238; older survey SDKs ignore it) and
  iOS no longer forces a light WebView when the mode is dark. `appearance` is the last
  `InitOptions` parameter and the previous full constructor stays, so existing Swift
  `InitOptions(...)` calls keep compiling. Asked by Danske Spil (DD-236).

#### Changed

- **Surveys SDK 2.2.44** (`@magicfeedback/native`, from 2.2.39), vendored on Android and from the
  CDN on iOS (`MAGICFEEDBACK_VERSION`), also accepted when the Admin assigns it. It brings the
  survey dark palette (DD-238): a dark Style, the forced mode from `appearance` (DD-240) or the
  Style's `dark` colours (DD-239) render the questions dark. Also Catalan, star colour, subtitle
  alignment, end screens (2.2.40–2.2.43). The survey stylesheet comes from popup-sdk 1.22.0
  (`POPUP_SDK_CSS_VERSION`, from 1.19.0), the first one with the 2.2.44 CSS: **publish popup-sdk
  1.22.0 before releasing this**, or the WebView asks for a stylesheet that does not exist yet.

#### Fixed

- **Band above the survey on a dark card (DD-248).** On iOS the page behind the survey kept its
  default colour (white, or black when the app forces dark), so a strip showed above the first
  question on a dark card. The page and the WebView background now follow the card colour.
- **Thank-you text on a dark card (DD-248).** The default "Thank you for your feedback!" kept the
  survey's light-mode grey on a dark card. It now takes the survey's text colours (light on dark).

- **Popup text follows the Style background, not the theme (DD-246).** Title, close X, progress
  text and the progress track now pick black or white from the luminance of the final card
  background (dark below WCAG luminance 0.2, the same cut the survey uses since DD-238). A dark
  `boxBackgroundColor` with theme Light used to give black text on dark.
  `style.theme` only sets the default background when the Style has none.

#### Tests

- **UI tests on the JVM, no emulator (DD-269, DD-270).** Robolectric + Compose UI test + Roborazzi
  in `androidUnitTest`. The X, Send and Back of the popup are tapped like a real finger (down, a
  small move, up): with the backdrop of 0.7.2–0.7.11 these taps were lost (DD-209), and the tests
  fail on that code. The survey must fill its whole slot (the 520 dp fixed height left white space
  on tablets), and the popup is compared with reference screenshots on a small phone, a tall phone
  and a tablet in portrait and landscape. CI runs `verifyRoborazziDebug`. No SDK behaviour change;
  the backdrop modifier is now `internal` so the tests can use it.
