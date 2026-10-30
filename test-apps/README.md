# Native test apps (MF-3244)

**NORDLY** is a small test shop, in a native Android app (Kotlin + Compose) and a native iOS app
(SwiftUI). Each app integrates the native popup SDK the way a customer's app would, with a
**SDK Lab** bar on top to choose what a run tests. The apps are the base for the native
functional regression (MF-3246). The web counterpart is `apps/test-app` in deepdots-popup-sdk
(MF-3243); scenarios and names are the same.

- They consume the **published** SDK: Android from Maven Central
  (`com.deepdots.sdk:shared-android`), iOS from the Swift package `MagicFeedback/DeepdotsSDK-SPM`.
  The version is chosen at build time. Android also accepts `local`, which builds this repo's
  `:shared` module.
- They always use **DEV** (`Environment.Development`, `api-dev.deepdots.com`) and the company
  **MAGICFEEDBACK_DEV**. There is no production option, and the runner fails if the device log
  mentions `api.deepdots.com`.
- Every run shows the SDK version (Lab bar, Lab panel) and logs it with the `[lab]` prefix
  (logcat tag `lab`, iOS unified log subsystem `com.deepdots.testapp`).
- The same Maestro flows run on both platforms: the apps share the bundle id
  `com.deepdots.testapp` and the accessibility ids (`tab-cart`, `go-checkout`, `help-button`…).

## Run

```bash
# Android (needs JAVA_HOME = Android Studio's JBR, and a running emulator)
cd test-apps/android
./gradlew :app:assembleDebug :app:labSdkInfo -PsdkVersion=0.7.3   # or 0.7.0, local
adb install -r app/build/outputs/apk/debug/app-debug.apk

# iOS (Apple Silicon simulator; the XCFramework has no x86_64 slice)
cd test-apps/ios
SDK_VERSION=0.7.3 xcodegen generate
xcodebuild -project NordlyTestApp.xcodeproj -scheme NordlyTestApp -sdk iphonesimulator \
  -destination 'platform=iOS Simulator,name=iPhone 16' -derivedDataPath build build
xcrun simctl install booted build/Build/Products/Debug-iphonesimulator/NordlyTestApp.app

# Suite (either platform), one video per flow with RECORD=1
RECORD=1 test-apps/scripts/run-maestro.sh android 0.7.3 out/android
DEVICE=<simulator udid> test-apps/scripts/run-maestro.sh ios 0.7.3 out/ios
```

Tools: [Maestro](https://maestro.mobile.dev) (`brew install mobile-dev-inc/tap/maestro`) and
XcodeGen (`brew install xcodegen`).

## Launch arguments

A run fits in the launch arguments (Maestro `launchApp: arguments`, `adb shell am start --es`,
or `-key value` on iOS). They are saved, so a relaunch without arguments keeps the same run.
The **SDK Lab** panel (gear icon) shows them and can change them (Android restarts by itself;
iOS quits, and the new run starts on the next launch).

| Argument | Values | What it does |
|---|---|---|
| `scenario` | see below | Which DEV popup config (public key) the SDK loads. |
| `nav` | `history` · `silent` | `silent`: screens change but the app never calls `setPath()`, so the SDK does not see the navigation. |
| `lang` | `en` · `es` | `provideLang` of the SDK (audience segmentation). |
| `load` | `immediate` · `consent` | Init the SDK at launch, or after "Accept all" in the cookie dialog. |
| `user` | text | `setUserId()` after init. Without it the SDK uses its anonymous id. |
| `start` | `home` · `shop` · `cart` · `checkout` · `account` | First screen (not saved). |

**Reset** in the Lab panel clears the SDK's storage (cooldowns, user id) and the cookie choice.

## Screens and SDK calls

- Tabs **Home**, **Shop**, **Cart**, **Account**; **Product** and **Checkout** open on top.
  Every screen change calls `setPath()` with `/`, `/shop`, `/product/<id>`, `/cart`,
  `/checkout`, `/account`. Leaving a screen fires its exit popups (the SDK does it in `setPath`).
- **Product › Rate** opens a bottom sheet; "Submit rating" calls `triggerEvent("rate_product")`.
- **Account › Help** calls `triggerClick("help-button")`.
- **Checkout › Place order** opens a confirmation dialog; a cookie dialog shows on first launch.
- Lifecycle: Android `ProcessLifecycleOwner` onStop/onStart, iOS `scenePhase` background/active
  → `onBackground()` / `onForeground()` (never on pause / inactive: `onBackground()` ends the
  SDK session).
- `attachContext()` with the Activity / the root view controller. On iOS the popup is presented
  from the root controller, so the app closes its own sheet first and fires the event 0.6 s later.

## Scenarios (DEV)

One popup config per scenario in MAGICFEEDBACK_DEV, so a run only gets the popups it tests (the
native SDK always loads popups from the API: there is no local fixture mode like the web app's
service worker). Keys and ids are in `scenarios.json`, which both apps read at build time.
Surveys are the MF-3243 ones: «SDK Test App · Simple» `5581b5a0-…` and «Complex» `7c78d670-…`.

| Scenario | Trigger | Targeting | Survey |
|---|---|---|---|
| `checkout-time` | `time_on_page` 3 s | `path: /checkout` | simple |
| `exit-checkout` | `exit` 2 s | leaving `/checkout` | simple |
| `rate-product` | `event` `rate_product` (bottom sheet) | — | complex |
| `help-click` | `click` `help-button` | — | simple |
| `redisplay` | `time_on_page` 2 s | `path: /account`, cooldown `SHOWED` 1 day | simple |
| `not-on-cart` | `time_on_page` 2 s | `excludedPaths: /cart, /checkout` | simple |
| `audience-lang` | `event` `rate_product` | two popups, `segments.lang` `en` / `es` | simple |
| `release-fixtures` | events `fixture_*` (Lab panel) | `release/fixtures/popups` (MF-3242) | simple |
| `fault-survey-missing` | `event` `fault_survey` | popup whose survey id does not exist | — |
| `fault-popups-error` | — | unknown public key: the popups GET answers 406 | — |

`release-fixtures` stores the four fixtures through the API with the real simple survey; the API
keeps them in its current shape, so the legacy `trigger`/`condition` parsing is still only
covered by the SDK's unit tests (`ReleaseContractTest`). Path segments (`/`, `/#/game`) and the
es-only language were dropped so all four show from the Lab panel.

If you change a popup, change it in the DEV Admin Panel too (Integrations › Popups, configs
named "Native Test Apps (MF-3244) · <scenario>").

## Suite (`maestro/`)

- `flows/`: version and environment; one flow per trigger and targeting type (time, path, exit,
  event, click, excludedPaths, language); re-display after relaunch; background/foreground;
  release fixtures.
- `regression/`: the functional regression (MF-3246, native side of MF-3197; web: `apps/test-app/e2e/regression/`
  in deepdots-popup-sdk). Each flow asserts on the Lab bar counters (`lab-counts`: `shown N · done N`,
  the SDK's `PopupShown` / `SurveyCompleted` events in this process) and ends with real taps on the host.

  | Flow | What it checks |
  |---|---|
  | `submit-simple` | Full answer to the «Simple» survey (NPS, text, Send): `survey_completed`, "Complete survey", host usable. Leaves one answer in MAGICFEEDBACK_DEV. |
  | `repeated-init` | `init()` called twice more (Lab "Init again"): one popup per trigger, listeners not duplicated. |
  | `duplicate-trigger` | `rate_product` twice in a row and once more with the popup open (Lab "×3"): one popup. Known issue up to 0.7.5. |
  | `rapid-exit` | Cart ⇄ Checkout three times at 250 ms (Lab "Rapid nav"): the exit popup shows once, on Cart. |
  | `time-cancel` | Same rapid navigation with the 3 s `time_on_page` on /checkout: never shows. |
  | `overlay-touches` | After closing a popup, taps on the spot it covered reach the host (the popup shows again, `shown 2`). |
  | `background-open-popup` | Background and back with the popup open: closable, no second popup, host usable. |
  | `rotate-open-popup` | Rotate with the popup open: still on screen and closable. Known issue on Android up to 0.7.5 (the recreated Activity loses the popup). |

  Counters are checked after closing the popup: on iOS the popup is a full-screen modal and the
  Lab bar under it is hidden from accessibility. Flows wait for the survey before tapping the X,
  so a late survey cannot take that tap. A known issue can be limited to one platform with
  `# known-issue-platform: android|ios` next to `# known-issue-until:`.

  The Lab buttons exist because Maestro needs 2–4 s per tap on a CI emulator: a double trigger or
  a quick in-and-out of a screen has to come from the app.
- `failures/`: the mandatory failure tests.
  - **Missing navigation event** (`missing-navigation.yaml`, `nav=silent`): the exit trigger does
    not fire. The flow is expected to fail; the runner reports it as detected and turns red if it
    ever passes. `flows/exit-checkout.yaml` is the control with normal navigation.
  - **Popup load failure**: survey that does not load (`survey-missing.yaml`), popups request
    rejected (`popups-error.yaml`), no network (`offline.yaml`, Android). After each one the flow
    checks the app with real taps: tabs, a product, add to cart, checkout.
  - **Slow survey** (`slow-survey.yaml`, Android): the runner throttles the emulator network
    (`# network: gsm gprs` in the flow → `adb emu network speed/delay`, back to full afterwards).
    The popup opens after the 1.2 s reveal ceiling with the survey still loading, closes, and the
    host works. iOS has no scriptable network throttling on CI.
  - **Network lost with the popup open** (`offline-open-popup.yaml`, Android): airplane mode right
    before the trigger; the popup closes and the host works.

CI: `.github/workflows/test-apps.yml`, one job per platform and SDK version. The versions come
from `sdk-versions.json` (latest published, the ones customers run in production, oldest
supported); add the new version there after each release, and the workflow warns when Maven
Central has one that is not in the list. *Run workflow* takes another list and can record
videos. Android runs on an
`ubuntu-latest` emulator (API 34), iOS on `macos-15` (Xcode 16.4, iPhone 16). Each job uploads
the device log, the `[lab]` lines, JUnit, Maestro output and, with recording, one MP4 per flow.

## Versions

- `0.7.0` and `0.7.3` have the same `InitOptions`, so both apps build with either. iOS from Swift
  has to pass every `InitOptions` parameter (Kotlin defaults are not visible), and 0.6.x lacks
  `onFeedbackSession`, so the iOS app does not build with 0.6.x as is. The Android app does
  (`-PsdkVersion=0.6.2`), since Kotlin keeps the defaults.
- The iOS app sets `CADisableMinimumFrameDurationOnPhone` in its Info.plist, as the SDK docs
  recommend. Without it, **0.7.0 crashes the host when it presents a popup** (Compose's
  frame-rate check; fixed in 0.7.1). The first CI run of this suite reproduced it (run
  40729713599, iOS · 0.7.0: "The app is not running" on every popup).
- Known issue found here (≤ 0.7.3): the native SDK parses `segments.lang` but does not filter by
  it (the web SDK does). Fixed in 0.7.4. `flows/audience-lang.yaml` is tagged `known-issue` with
  `# known-issue-until: 0.7.3`: up to that version the runner expects it to fail (warning) and
  turns red if it passes; from 0.7.4 (and `local`) it runs as a normal flow.
- iOS has no `local` mode: it always uses the published XCFramework.
