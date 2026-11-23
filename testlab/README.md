# Key popup checks on real devices (DD-273)

A few checks that emulators and simulators miss, on real phones and tablets in
[Firebase Test Lab](https://firebase.google.com/docs/test-lab), only on release PRs and by hand.
The 57 Maestro flows stay on emulators (`test-apps/`).

| Check | Android | iOS |
|---|---|---|
| The popup opens with the survey loaded | `popupOpensWithTheSurvey` | `testPopupOpensWithTheSurvey` |
| X responds to a finger-like tap (down, small move, up) | `closeRespondsToAFingerTap` | `testCloseRespondsToAFingerTap` |
| Send responds to a finger-like tap | `sendRespondsToAFingerTap` | `testSendRespondsToAFingerTap` |
| Back responds to a finger-like tap | `backRespondsToAFingerTap` | `testBackRespondsToAFingerTap` |
| Keyboard open on the text question: Send above it, X closes | `keyboardOverTheTextQuestionLeavesSendAndCloseUsable` | `testKeyboardOverTheTextQuestionLeavesSendAndCloseUsable` |
| Tablet: the survey fills its slot (DD-270) | `surveyFillsItsSlotOnATablet` (skips on phones) | `testSurveyFillsItsSlotOnAnIPad` (skips on iPhones) |

- **Android**: `shared/src/androidInstrumentedTest/.../PopupDeviceChecksTest.kt`. The popup goes
  through the real `PopupRenderer` with the system WebView. Finger taps as in DD-269.
- **iOS**: `test-apps/ios/NordlyDeviceChecks/` (XCUITest on the NORDLY test app, scenario
  `help-click`), with the SDK built from the same commit.
- **Data**: DEV only (MAGICFEEDBACK_DEV, survey «SDK Test App · Simple»). No client data. The
  Send check leaves one answer in DEV per run, like the Maestro regression.
- **Devices**: `devices.json` (fixed list, checked against the Test Lab catalog on 19-nov-2026):
  Galaxy S25 (Android 16), Galaxy Tab S8 (14), Xiaomi 14 (15), Pixel 3 (Android 9);
  iPhone 16 Pro (iOS 18.3), iPhone SE 3 (18.4 and 26.3). Test Lab has no iPad since nov-2026; the iPad (A16) simulator job covers it.

## Run locally

```bash
# Android: an emulator or a phone on adb (unlocked screen)
./gradlew :shared:connectedDebugAndroidTest
# or: ./gradlew :shared:assembleDebugAndroidTest, then
adb install -r -t shared/build/outputs/apk/androidTest/debug/shared-debug-androidTest.apk
adb shell am instrument -w -r com.deepdots.sdk.test/androidx.test.runner.AndroidJUnitRunner

# iOS: simulator
testlab/ios-build.sh simulator
cd test-apps/ios && xcodebuild test-without-building \
  -xctestrun build-testlab/Build/Products/*.xctestrun \
  -destination 'platform=iOS Simulator,name=iPad (10th generation)'

# What Test Lab would get (prints the gcloud command, runs nothing)
python3 testlab/testlab.py android --apk x.apk --project P --dry-run
```

## CI (`.github/workflows/testlab.yml`)

- Runs on PRs whose branch is `release/X.Y.Z` (to `dev` or `main`) and on manual dispatch
  (Actions › Test Lab (real devices) › Run workflow, Android, iOS or both).
- `testlab/testlab.py` refuses more than `maxDevices` per platform, a timeout over 10 min and more
  than 1 flaky attempt; it runs with **0 retries**, waits for the matrix, cancels it if it is not
  done after timeout + 10 min, and writes a table with the **billed device minutes** and the cost
  to the job summary.
- A new push cancels the old run, and the job cancels its Test Lab matrix too.
- Without the repo variables `TEST_LAB_PROJECT_ID`, `WIF_PROVIDER` and `WIF_SERVICE_ACCOUNT` the jobs skip with a notice.

## Cost

Physical devices: $5 per device-hour, billed per minute. A run is about 3–6 min per device:
4 Android + 3 iOS ≈ 25–40 device minutes ≈ $2–3.50 per release. With 3–4 releases and some
manual runs, $10–30 a month.

## Setup (once, by Sergi or Fran)

Test Lab runs in the existing **MagicFeedback-Survey-DEV** project (`magicfeedback-c6458-dev`),
already on billing (MagicFeedback OÜ, `0193BB-A5D548-9E3870`). It is a DEV project, so the CI key
never touches prod. Replace the emails. Nothing here runs a test.

1. **APIs** (done on 20-nov-2026):

   ```bash
   gcloud services enable testing.googleapis.com toolresults.googleapis.com --project=magicfeedback-c6458-dev
   ```

2. **Budget of $30/month with alerts at 50/90/100 % to Sergi and Fran.** Budgets only alert, they
   do not stop spending; the guardrails in the workflow are what limit it. The project also hosts
   other DEV things, so filter the budget by project **and** by the Test Lab service, or the alerts
   would count everything else. Easiest in the console: Billing › Budgets & alerts › Create budget ›
   Projects = MagicFeedback-Survey-DEV, Services = "Cloud Testing API" (Firebase Test Lab),
   amount $30, thresholds 50/90/100 %, email alerts to billing admins or to a monitoring channel
   with Sergi and Fran. With gcloud:

   ```bash
   gcloud services enable billingbudgets.googleapis.com monitoring.googleapis.com --project=magicfeedback-c6458-dev
   gcloud beta monitoring channels create --project=magicfeedback-c6458-dev --display-name="Sergi" \
     --type=email --channel-labels=email_address=SERGI_EMAIL
   gcloud beta monitoring channels create --project=magicfeedback-c6458-dev --display-name="Fran" \
     --type=email --channel-labels=email_address=FRAN_EMAIL
   gcloud beta monitoring channels list --project=magicfeedback-c6458-dev --format="value(name)"   # the two channel ids
   gcloud billing budgets create --billing-account=0193BB-A5D548-9E3870 \
     --display-name="Test Lab (DD-273)" --budget-amount=30USD \
     --filter-projects=projects/magicfeedback-c6458-dev \
     --filter-services=services/CLOUD_TESTING_SERVICE_ID \
     --threshold-rule=percent=0.5 --threshold-rule=percent=0.9 --threshold-rule=percent=1.0 \
     --notifications-rule-monitoring-notification-channels=CHANNEL_1,CHANNEL_2
   gcloud billing budgets list --billing-account=0193BB-A5D548-9E3870   # check it
   ```

   `CLOUD_TESTING_SERVICE_ID` is the billing id of "Cloud Testing API"; the console filter picks
   it for you, so prefer the console if you don't have it at hand.

3. **Service account for GitHub Actions** (only Test Lab, in this project only). Done by Fran on
   20-nov: `testlab-ci@magicfeedback-c6458-dev.iam.gserviceaccount.com` with
   `roles/cloudtestservice.testAdmin` and `roles/firebase.analyticsViewer`. **No JSON key:** the
   workflow uses Workload Identity Federation (pool `github`, provider `popup-sdk`), which gives the
   Action a short-lived token on each run and only accepts tokens from
   `MagicFeedback/deepdots-popup-mobile-native-sdk`. Nothing to store or rotate.

   Results go to our own bucket `gs://magicfeedback-c6458-dev-testlab-results` (EU, deleted after
   60 days; testlab-ci has `roles/storage.objectAdmin` only there, set up by Fran). The default
   `test-lab-…` bucket lives in a Google project, nobody here can give it permissions.

4. **Repo variables** (Settings › Secrets and variables › Actions › Variables; not secrets, none
   of them is a credential):

   ```bash
   R=MagicFeedback/deepdots-popup-mobile-native-sdk
   gh variable set TEST_LAB_PROJECT_ID --repo $R --body magicfeedback-c6458-dev
   gh variable set TEST_LAB_RESULTS_BUCKET --repo $R --body magicfeedback-c6458-dev-testlab-results
   gh variable set WIF_PROVIDER --repo $R --body projects/685439469805/locations/global/workloadIdentityPools/github/providers/popup-sdk
   gh variable set WIF_SERVICE_ACCOUNT --repo $R --body testlab-ci@magicfeedback-c6458-dev.iam.gserviceaccount.com
   ```

   The jobs need `id-token: write` (already in the workflow).

5. **First run**: Actions › Test Lab (real devices) › Run workflow (both) on `dev`. Check the job
   summary (billed minutes) and the Billing report the next day; write the cost in DD-273.

## Notes

- iOS zips are signed ad hoc (`SIGN_IDENTITY=-`); Test Lab re-signs the app and the runner with its
  own profile. If Test Lab rejects the zip, import an Apple Development certificate in the job and
  set `SIGN_IDENTITY`.
- Firebase Test Lab is deprecated and shuts down on 30-sep-2027; its successor, Google Cloud's
  Developer Device Platform, takes the same APK / zip.
- Android: the library's self-instrumenting test APK is passed as both `--app` and `--test`.
