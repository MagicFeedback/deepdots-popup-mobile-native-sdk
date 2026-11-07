# Deepdots | Popup SDK Release Validation Report

**SDK:** Deepdots native popup SDK **0.7.11** (Android `com.deepdots.sdk:shared-android:0.7.11` · iOS `DeepdotsSDK-SPM` 0.7.11)  
**Validation date:** 2026-11-07  
**Platforms:** iOS / Android  
**Overall status:** **FAILED**  
**Report type:** Automated release validation, generated from the CI test results (no manual edits)

---

## 1. Executive Summary

SDK 0.7.11 was validated as published (Maven Central and Swift Package Manager) inside dedicated native test applications that integrate the SDK the same way a customer app does.

Results ([CI run](https://github.com/MagicFeedback/deepdots-popup-mobile-native-sdk/actions/runs/42409572820/attempts/1)):

- **400** automated test executions on **16** configurations (7 iOS, 9 Android)
- **373** passed (3 on the automatic retry)
- **4** failed
- **23** not applicable on that platform
- Manual pass on real devices: **pending** (section 7)

**iOS:** FAILED · **Android:** FAILED

**Validation status:** **FAILED**

### Failures

| Platform | Configuration | Test | Detail |
|---|---|---|---|
| iOS | iOS 16.4 iPhone SE (3rd generation) Debug | survey-missing | xcuitest.installer.LocalXCTestInstaller$IOSDriverTimeoutException: iOS driver not ready in time, consider increasing timeout by configuring MAESTRO_DRIVER_STARTUP_TIMEOUT env variable |
| Android | A26 phone debug | survey-sdk-2.2.38 | Assertion is false: "Czy zrobi.*NORDLY.*" is visible |
| Android | A26 phone release | survey-sdk-2.2.38 | Assertion is false: "Czy zrobi.*NORDLY.*" is visible |
| Android | A34 tablet debug | rating-labels | Assertion is false: "Accept all" is visible |

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
| Survey rendering, interactions and submission | PASS | **FAIL** |
| Navigation and app lifecycle (background / foreground, rotation) | PASS | PASS |
| Network failures and recovery | **FAIL** | PASS |
| Detection check: a missing navigation event is caught | PASS | PASS |
| Survey SDK versions assigned in the Admin (DD-70) | PASS | **FAIL** |
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
| lang-da-shows | pass | pass | pass | pass | pass | retry | pass | pass | pass | pass | pass | retry | pass | pass | pass | pass |
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
| rating-labels | pass | pass | pass | pass | pass | pass | fail | pass | pass | retry | pass | pass | pass | pass | pass | pass |
| redisplay | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass |
| release-fixtures | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass |
| slow-survey | skip | skip | pass | pass | pass | pass | pass | pass | pass | skip | skip | skip | skip | skip | skip | skip |
| survey-missing | pass | pass | pass | pass | pass | pass | pass | pass | pass | error | pass | pass | pass | pass | pass | pass |
| survey-sdk-2.2.38 | fail | fail | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass | pass |

</details>

## 3. Tested Environments

| Platform | OS version | Device | Build | SDK resolved | Engine | Result |
|---|---|---|---|---|---|---|
| Android | API 26 | pixel_6 (emulator) | debug | 0.7.11 | WebView 69.0.3497.100 | **FAIL** |
| Android | API 26 | pixel_6 (emulator) | release (R8) | 0.7.11 | WebView 69.0.3497.100 | **FAIL** |
| Android | API 30 | pixel_6 (emulator) | debug | 0.7.11 | WebView 83.0.4103.106 | PASS |
| Android | API 30 | pixel_6 (emulator) | release (R8) | 0.7.11 | WebView 83.0.4103.106 | PASS |
| Android | API 34 | pixel_6 (emulator) | debug | 0.7.11 | WebView 113.0.5672.136 | PASS |
| Android | API 34 | pixel_6 (emulator) | release (R8) | 0.7.11 | WebView 113.0.5672.136 | PASS |
| Android | API 34 | pixel_tablet (emulator) | debug | 0.7.11 | WebView 113.0.5672.136 | **FAIL** |
| Android | API 36 | pixel_6 (emulator) | debug | 0.7.11 | WebView 133.0.6943.137 | PASS |
| Android | API 36 | pixel_6 (emulator) | release (R8) | 0.7.11 | WebView 133.0.6943.137 | PASS |
| iOS | iOS-16-4 | iPhone SE (3rd generation) (simulator) | Debug · Xcode_16.4 | 0.7.11 | — | **FAIL** |
| iOS | iOS-17-5 | iPhone 15 (simulator) | Debug · Xcode_16.4 | 0.7.11 | — | PASS |
| iOS | iOS-18-6 | iPad (A16) (simulator) | Debug · Xcode_16.4 | 0.7.11 | — | PASS |
| iOS | iOS-18-6 | iPhone 16 Pro (simulator) | Debug · Xcode_16.4 | 0.7.11 | — | PASS |
| iOS | iOS-18-6 | iPhone 16 Pro Max (simulator) | Debug · Xcode_16.4 | 0.7.11 | — | PASS |
| iOS | iOS-18-6 | iPhone 16 (simulator) | Release · Xcode_16.4 | 0.7.11 | — | PASS |
| iOS | iOS-26-2 | iPhone 17 (simulator) | Debug · Xcode_26.2 | 0.7.11 | — | PASS |

All automated tests ran against the Deepdots development environment with test companies only. No customer data was used, and each configuration fails if any request reaches the production API.

## 4. Critical Safety Validation

| Scenario | iOS | Android |
|---|---|---|
| Survey that cannot load: the popup can be closed and the app stays usable | **FAIL** | PASS |
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

- **SDK version:** 0.7.11
- **Release:** [GitHub release v0.7.11](https://github.com/MagicFeedback/deepdots-popup-mobile-native-sdk/releases/tag/v0.7.11)
- **Changelog:** [CHANGELOG](https://github.com/MagicFeedback/deepdots-popup-mobile-native-sdk/blob/main/CHANGELOG.md)
- **Test execution:** [CI run](https://github.com/MagicFeedback/deepdots-popup-mobile-native-sdk/actions/runs/42409572820/attempts/1) (logs, screenshots and per-configuration artifacts)
- **Validation date:** 2026-11-07
- **Results file SHA-256:** `aafde0305ebfaed6ecefff386867c4049987b49ab65fbc940fe6973378fd9a77`

### Release notes 0.7.11

Survey SDK supported: 2.2.38–2.2.39 (survey frontend 3.4.1); bundled: 2.2.39. Compatibility table: docs.deepdots.com/popup-native/reference/survey-compatibility/.

0.7.10 was tagged but never published (the release workflow stopped before publishing because
of a release-gate bug, fixed below), so this version is the first with these changes on Maven
Central and SPM. Upgrade from 0.7.9 straight to it.

#### Added

- **Popups follow the survey version assigned in the Admin (DD-70).** `GET /sdk/{publicKey}/popups`
  sends `surveyFrontend.sdkVersion` per popup (latest or pinned). When it is one of
  `SUPPORTED_SURVEY_SDK_VERSIONS` (2.2.38, 2.2.39; also `release/compatibility.json` →
  `surveyRenderer.assignedVersions`) and differs from `MAGICFEEDBACK_VERSION`, the WebView first
  loads that version's survey CSS (the popup-sdk's vendored `survey-css/<v>.css` at
  `POPUP_SDK_CSS_VERSION`, in place of the usual stylesheet) and then that exact
  `@magicfeedback/native` from jsDelivr, then unpkg. If the CSS or the JS fails, the usual CSS and
  the vendored JS come back together (the per-version sheets exist from popup-sdk 1.19.0). An
  invalid version is always logged, the choice only with `debug`.
  New `PopupDefinition.surveySdkVersion` (last parameter, default null).
- **Release gate for the assignable survey SDK versions (DD-70).** Every version of
  `SUPPORTED_SURVEY_SDK_VERSIONS` other than `MAGICFEEDBACK_VERSION` has a Maestro flow
  `test-apps/maestro/flows/survey-sdk-<v>.yaml` and a DEV scenario `survey-sdk-<v>` (survey pinned
  in the Admin to a MagicSurvey release built with that version); `scripts/release/rules.mjs`
  requires both. The flow answers the survey with that version, and `run-maestro.sh` fails it
  unless the device log has `[MagicFeedback] survey-sdk <v> assigned` (the SDK logs it, println /
  NSLog, once
  that version's CSS and JS are in). It runs in the full suite of the release PR (`local`).

- **The popup's own texts in the 24 official EU languages (DD-67).** `DefaultLabels` (buttons,
  progress counter, follow-up badge, accessibility descriptions and notices) now translates
  bg, cs, el, et, ga, hr, hu, it, lt, lv, mt, nl, pl, ro, sk and sl too, 28 locales in all. A
  Polish survey used to show the native Back/Send buttons in English. Machine-translated, pending
  native review (`DD-67-native-review.md`). Mirror of the Web SDK's `src/i18n/labels.ts`.

- **The closing batch survives the app dying (DD-55, parity with Web 1.17).** With storage (the
  default), the batch that closes a session (`completed:true`, `deepdots_session_end`) is stored
  before it is sent. If iOS suspends or kills the app with that request in flight, or it fails
  with a network error or 5xx, the next `init()` resends it to its own record (its `sessionId`).
  A failed closing batch no longer goes back to the event buffer, where it left with the next
  session and in its record. Delivered or rejected (4xx) batches are removed; up to 50 are kept,
  for at most 24 h. The background task around the closing request (iOS) stays as before.

- **The SDK identifies itself on the data it sends (DD-3, Collection dashboard).** Every popup
  response now carries `deepdots_channel=popup` and `deepdots_sdk=android@<version>` /
  `ios@<version>` in its survey metadata, also with tracking off, so a response that came through
  a popup can be told apart from a MagicSurvey link and attributed to an SDK version (same keys as
  the Web SDK). Every analytics batch carries `deepdots_sdk` too. A wrapper declares itself with
  `SdkIdentity.setWrapper(name, version)` before `init` (the Flutter plugin does): `deepdots_sdk`
  becomes `flutter@<version>` and the native core goes in `deepdots_sdk_core`. The version is
  generated at build time from `PUBLISHING_VERSION` (`generateSdkBuildInfo` in
  `shared/build.gradle.kts`), never kept by hand; `SdkIdentity.version` exposes it to hosts.

#### Changed

- **Surveys SDK 2.2.39** (`@magicfeedback/native`, from 2.2.31), vendored on Android and from the
  CDN on iOS (`MAGICFEEDBACK_VERSION`). With it the popup shows "Please enter a valid email
  address." for an email with a typo (`INVALID_EMAIL`), the survey reports send failures apart
  from an empty required question (`SEND_FAILED`, 2.2.37), and it gets the survey changes since
  2.2.31: the 24 official EU languages, the email `Option/Alt + 2` shortcut, typed submit
  errors, rating and choice layout options. The survey stylesheet now comes from popup-sdk
  1.19.0 (`POPUP_SDK_CSS_VERSION`, from 1.10.0), the copy of the 2.2.39 CSS, so the email @ button
  (`assets.atButton`) is styled too.

#### Fixed

- **Error notices in the survey's language (DD-67).** The required-answer, invalid-email and
  submit-error notices stayed in the language the host gave at start-up (`provideLang`, English
  without it), while the buttons and the close label already followed the survey: a Danish survey
  showed "Luk pop op" next to "Please answer the required question to continue.". The WebView
  bridge kept the first event handler, from before the survey's language arrived; it now always
  calls the current one (`rememberUpdatedState`, Android and iOS).

- **The popup understands the survey's error codes.** The surveys SDK (`@magicfeedback/native`
  2.2.37+) sends a `code` with each submit error; the popup only looked at the text, so any
  error containing "No response" read as "required question" (also a send the API did not
  save) and every other error showed the generic message. The popup now reads the code, like the
  Web SDK: `REQUIRED` shows the required message, the new `INVALID_EMAIL` (2.2.39) shows "Please
  enter a valid email address." (translated in all 28 locales), and the rest show the submit
  error. Without a code it keeps matching the text. Asked by Lemvigh-Müller: an email with a typo
  showed "Please answer the question".
- **A failed popups download is retried instead of losing the popup for the session.** At
  start-up the SDK downloads the popups of the public key; taps and events that arrive before
  are kept and replayed. If that request failed once (for example a brief loss of signal at
  start-up), the SDK dropped them and never tried again, so no popup showed until the next app
  start. The request is now retried twice (after 2 s and 5 s) and the pending taps and events
  are kept until the last attempt. Found while investigating the release validation on Android 8.

- **The "Delay (seconds)" set in the Admin is honoured for every trigger type** (reported by
  MOM: an event trigger with a 60 s delay showed the popup at once). `event`, `click`, `scroll`
  and `time_on_page` ignored `trigger.delaySeconds`, and `exit` read its delay from `value`, which
  the Admin saves as 0 (the delay lives in `delaySeconds`), so exit popups made in the Admin never
  waited. Now the delay counts from the moment the trigger fires (for `time_on_page`, after the
  time on page; for `exit`, on the next screen as before). Exit popups made before `delaySeconds`
  existed, with the delay in `value`, keep working. While a popup waits: firing the same trigger
  again neither restarts nor duplicates it; `setPath()` to another screen or `onBackground()`
  cancels it; and when it is due everything is checked again (segments, language, cooldowns), it
  is dropped if the same popup was shown in the meantime, and if another popup is open it is
  skipped without effects, as before. 0 or no delay = at once, as before. Pending delays live in
  memory only. Parity with web 1.19.0. `Trigger.TimeOnPage`, `Scroll`, `Event` and `Click` gain a
  `delaySeconds` property; their old constructors stay (also from Swift). Tests:
  `TriggerDelayTest`, `TriggerDelayApiPayloadTest`; Maestro `rate-delay` and `rate-delay-cancel` (DEV scenario `rate-delay`,
  event + 8 s).

- **Release gate: a skipped check no longer hides the result that was tested.** Editing a release
  pull request after it was merged starts the release check again with its jobs skipped, and the
  publish workflow took that skipped "Release dry run" as the result. The gate now takes the
  latest run that was not skipped (`latestRun` in `scripts/release/metadata.mjs`).
