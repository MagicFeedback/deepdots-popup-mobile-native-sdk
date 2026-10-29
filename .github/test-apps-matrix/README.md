# Test apps matrix (MF-3263)

`.github/workflows/test-apps-matrix.yml` runs the MF-3244 test apps (`test-apps/`) and their Maestro
suite on every OS version, device and build type we promise, against one **published** SDK version,
plus two negative controls that must catch the 0.7.0 bugs Lemvigh-Müller hit. `test-apps.yml` stays
the quick per-PR check (one emulator, one simulator).

Run it from Actions → "Test apps matrix" (inputs: `sdk`, `control`, `flows`, `record`), or with
`gh workflow run test-apps-matrix.yml -f sdk=0.7.3 -f record=true` once the workflow is on the
default branch. The `report` job writes the flows × rows table to the run summary and to the
`results-matrix` artifact.

## Rows

| Platform | OS | Device | Build | Runner |
|---|---|---|---|---|
| Android | API 26, 30, 34, 36 | Pixel 6 emulator | debug + release (R8, minify, shrinkResources) | ubuntu-latest, `google_apis` x86_64 |
| Android | API 34 | Pixel Tablet emulator | debug | ubuntu-latest |
| iOS | 16.4 | iPhone SE (3rd gen) simulator | Debug, Xcode 16.4 | macos-15, runtime downloaded |
| iOS | 17.5 | iPhone 15 simulator | Debug, Xcode 16.4 | macos-15, runtime downloaded |
| iOS | 18.x | iPhone 16 Pro Max, iPad (A16) | Debug, Xcode 16.4 | macos-15 (18.5/18.6 preinstalled) |
| iOS | 18.x | iPhone 16 | Release, Xcode 16.4 | macos-15 |
| iOS | 18.x | iPhone 16 Pro, **no `CADisableMinimumFrameDurationOnPhone`** (scenario 12) | Debug, Xcode 16.4 | macos-15 |
| iOS | 26.x | iPhone 17 | Debug, **Xcode 26.2** | macos-15 (26.x preinstalled) |

Every row also fails when the device log shows `CalledFromWrongThreadException` (the 0.7.0
invisible popup), a crash or an ANR (`check-device-log.sh`).

## Negative controls

- **Rating labels** (`negative-control.sh` + `lemu/rating-labels.yaml`, Android API 34): on 0.7.0
  the vendored survey bundle 2.2.4 puts "1 = Meget dårlig" inside the first chip. The flow must
  fail on that assertion with the control version and pass with the tested one. Needs the `lemu`
  scenario (replica of Lemu's popup in MAGICFEEDBACK_DEV, `da`, 1–5 rating with labels) in
  `test-apps/scenarios.json` (MF-3244).
- **R8 build** (`r8-control` job, no emulator): 0.7.0 shipped without consumer ProGuard rules, so a
  minified release build fails with `Missing class org.slf4j.impl.StaticLoggerBinder`; 0.7.2+
  builds. Verified locally on 29-oct: 0.7.0 fails, 0.7.3 builds (1.9 MB APK).
- **Not automatable on CI: the invisible popup that blocked taps.** It only happens when the
  survey is drawn in under 1.2 s, so the WebView's `ready` wins over the native reveal timeout and
  touches the view from the JavaBridge thread. A CI emulator takes ~1.5 s, so the timeout always
  wins. Evidence for it: the recorded repro in `Deepdots/lemvigh-android-popups/` (0.7.0 with the
  timeout raised to simulate a fast phone) and the real-device pass. The per-row wrong-thread check
  would catch it if it ever happened.

## Runner gaps (checked 29-oct-2026 against actions/runner-images)

- **iOS 16 and 17:** macos-15 has only iOS 18.5/18.6 and 26.x runtimes. macos-14 has 17.x but
  no Xcode 16.4, and no runner image has iOS 16. Both runtimes are downloaded with
  `xcodebuild -downloadPlatform iOS -buildVersion` (adds ~5–10 min per job). If Apple's CDN fails,
  that row fails at the download step, not in the tests.
- **iPhone 15 on iOS 18 / 16 Pro Max on iOS 17:** device types follow the runtime; iPhone 15 is
  covered on 17.5, the 16 family on 18.x.
- **iOS 26 needs Xcode 26:** Xcode 16.4 can't run iOS 26 simulators. That row builds with Xcode
  26.2, as App Store builds have to since April 2026. The other rows stay on 16.4 (same pin as
  MF-3242).
- **Android API 26 WebView:** `google_apis` images ship the WebView of their release and there's no
  Play Store to update it. Real Android 8 phones get WebView updates, so a failure that only
  shows on API 26 can be an emulator artifact. The row records the WebView version (`row.json`).
- **Physical devices:** none on GitHub runners. Covered by the manual real-device checklist.
- **Not covered:** x86_64 iOS simulators (the XCFramework has no x86_64 slice), Android 7 (API
  24–25: the SDK's minSdk is 24, but they're not in the promised matrix).
