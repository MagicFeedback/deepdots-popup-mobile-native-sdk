#!/usr/bin/env bash
# Runs the Maestro suite against the installed NORDLY test app, one flow at a time.
#
#   test-apps/scripts/run-maestro.sh <android|ios> <sdk-version> <out-dir>
#
# env: DEVICE (simulator UDID / adb serial, optional), RECORD=1 (one video per flow),
#      FLOWS (space-separated flow files relative to test-apps/maestro; default: all),
#      MONITOR_SCENARIO (synthetic monitor failure test, maestro/monitor; passed to the flows).
# Per flow it writes Maestro's output and a JUnit report; for the run, the device log with the
# app's [lab] lines. Exit code != 0 when a flow fails, when the expected-failure test passes, or
# when anything in the log points at the production API.
set -uo pipefail

PLATFORM=$1 SDK=$2 OUT=$(mkdir -p "$3" && cd "$3" && pwd)
RECORD=${RECORD:-0}
DEVICE=${DEVICE:-}
cd "$(dirname "$0")/../maestro"
export MAESTRO_CLI_NO_ANALYTICS=1 MAESTRO_CLI_ANALYSIS_NOTIFICATION_DISABLED=true
mkdir -p "$OUT/maestro" "$OUT/junit" "$OUT/videos"

maestro_cmd() { if [ -n "$DEVICE" ]; then maestro --device "$DEVICE" "$@"; else maestro "$@"; fi; }
adb_cmd() { if [ -n "$DEVICE" ]; then adb -s "$DEVICE" "$@"; else adb "$@"; fi; }
basic_installed() {
  if [ "$PLATFORM" = android ]; then adb_cmd shell pm path com.deepdots.basicapp > /dev/null 2>&1
  else xcrun simctl get_app_container "${DEVICE:-booted}" com.deepdots.basicapp > /dev/null 2>&1; fi
}

# Device log: what CI keeps as "the run's log". Appended, so a restart keeps what came before.
start_device_log() {
  if [ "$PLATFORM" = android ]; then
    adb_cmd logcat -v time >> "$OUT/device.log" 2>&1 &
  else
    xcrun simctl spawn "${DEVICE:-booted}" log stream --level info --style compact \
      --predicate 'subsystem == "com.deepdots.testapp" OR process == "NordlyTestApp"' >> "$OUT/device.log" 2>&1 &
  fi
  LOG_PID=$!
}
# The log stream can die mid-run (iOS 16.4 simulator, 0.7.11: "Child process terminated with
# signal 9" after 10 min while the flows ran 30 more; Android: logcat "read: unexpected EOF!"),
# and the DD-70 check below reads the log. After each flow: if it is gone, append what the device
# still keeps since the flow started (logcat buffer / unified log) and start it again.
log_mark() { if [ "$PLATFORM" = android ]; then adb_cmd shell date +%m-%d\ %H:%M:%S.000 | tr -d '\r'; else date '+%Y-%m-%d %H:%M:%S'; fi; }
ensure_device_log() { # <time the flow started, from log_mark>
  kill -0 "$LOG_PID" 2>/dev/null && return 0
  echo "::warning::[lab] device log stream stopped: recovering the flow's lines and restarting it"
  if [ -n "${1:-}" ]; then
    if [ "$PLATFORM" = android ]; then
      adb_cmd logcat -d -v time -T "$1" >> "$OUT/device.log" 2>&1
    else
      xcrun simctl spawn "${DEVICE:-booted}" log show --info --style compact --start "$1" \
        --predicate 'subsystem == "com.deepdots.testapp" OR process == "NordlyTestApp"' >> "$OUT/device.log" 2>&1
    fi
  fi
  start_device_log
}
[ "$PLATFORM" = android ] && adb_cmd logcat -c
: > "$OUT/device.log"
start_device_log

start_recording() {
  [ "$RECORD" = 1 ] || return 0
  if [ "$PLATFORM" = android ]; then
    adb_cmd shell screenrecord --time-limit 180 "/sdcard/$1.mp4" & REC_PID=$!
  else
    xcrun simctl io "${DEVICE:-booted}" recordVideo --codec h264 --force "$OUT/videos/$1.mp4" > /dev/null 2>&1 & REC_PID=$!
  fi
  sleep 1
}

stop_recording() {
  [ "$RECORD" = 1 ] || return 0
  if [ "$PLATFORM" = android ]; then
    adb_cmd shell pkill -INT screenrecord || true
    wait "$REC_PID" 2>/dev/null; sleep 1
    adb_cmd pull "/sdcard/$1.mp4" "$OUT/videos/$1.mp4" > /dev/null && adb_cmd shell rm "/sdcard/$1.mp4"
  else
    kill -INT "$REC_PID" 2>/dev/null; wait "$REC_PID" 2>/dev/null
  fi
}

# `# network: <speed> <delay>` in a flow: the Android emulator runs it on a throttled network
# (emulator console `network speed` / `network delay`, e.g. `gsm gprs`), then back to full.
set_network() {
  [ "$PLATFORM" = android ] || return 0
  adb_cmd emu network speed "$1" > /dev/null 2>&1; adb_cmd emu network delay "$2" > /dev/null 2>&1
}

# Fixed status bar (clock 9:41, full battery, full signal, no notification icons), so screenshots
# only change when the app does (visual regression, DD-28). Android: SystemUI demo mode, which a
# font scale change drops, so set_text_size applies it again.
fix_status_bar() {
  if [ "$PLATFORM" = android ]; then
    adb_cmd shell settings put global sysui_demo_allowed 1 > /dev/null 2>&1
    local c
    for c in "enter" "clock -e hhmm 0941" "battery -e level 100 -e plugged false" \
      "network -e wifi show -e level 4 -e mobile show -e datatype none -e level 4" "notifications -e visible false"; do
      # shellcheck disable=SC2086
      adb_cmd shell am broadcast -a com.android.systemui.demo -e command $c > /dev/null 2>&1
    done
  else
    xcrun simctl status_bar "${DEVICE:-booted}" override --time 9:41 --batteryState charged --batteryLevel 100 \
      --cellularMode active --cellularBars 4 --wifiMode active --wifiBars 3 --dataNetwork wifi > /dev/null 2>&1
  fi
}

# Android: wait (up to 90 s) until the device has a default network again. After the airplane-mode
# flows, the API 26 emulator took longer than 30 s to reconnect, so the next throttled flow
# (slow-survey) timed out before the SDK could load its config: a test-order failure, not the SDK's.
wait_online() {
  [ "$PLATFORM" = android ] || return 0
  local i
  for i in $(seq 1 45); do
    adb_cmd shell dumpsys connectivity 2>/dev/null | grep -qE 'Active default network: [0-9]+' && return 0
    sleep 2
  done
  echo "::warning::[lab] device still offline after 90 s"
}

# `# text-size: large` in a flow: it runs with large system text (Android font_scale 1.3, iOS
# Dynamic Type XXXL), so long titles wrap on any device; back to the default afterwards.
set_text_size() {
  if [ "$PLATFORM" = android ]; then
    adb_cmd shell settings put system font_scale "$([ "$1" = large ] && echo 1.3 || echo 1.0)" > /dev/null 2>&1
    sleep 2; fix_status_bar
  else
    xcrun simctl ui "${DEVICE:-booted}" content_size "$([ "$1" = large ] && echo extra-extra-extra-large || echo large)" > /dev/null 2>&1
  fi
}

# JUnit file for a flow Maestro did not report: junit_case <name> <error|skipped> <message>.
junit_case() {
  local msg; msg=$(printf '%s' "$3" | sed -e 's/&/\&amp;/g' -e 's/</\&lt;/g' -e 's/>/\&gt;/g' -e 's/"/\&quot;/g')
  local body="<error message=\"$msg\"/>"
  [ "$2" = skipped ] && body="<skipped message=\"$msg\"/>"
  printf '<?xml version="1.0" encoding="UTF-8"?>\n<testsuites><testsuite name="%s" tests="1"><testcase name="%s">%s</testcase></testsuite></testsuites>\n' \
    "$1" "$1" "$body" > "$OUT/junit/$1.xml"
}

# iOS: when the Maestro driver did not come up in time (seen on the downloaded iOS 16.4 runtime),
# reboot the simulator before the next try; retrying on the same simulator hit the same timeout.
reboot_if_driver_timeout() { # <maestro output>
  [ "$PLATFORM" = ios ] && grep -q 'IOSDriverTimeoutException' "$1" || return 0
  echo "::warning::[lab] Maestro iOS driver did not start: rebooting the simulator"
  local dev=${DEVICE:-booted}
  [ "$dev" = booted ] && dev=$(xcrun simctl list devices booted -j | python3 -c "import json,sys;print([d['udid'] for r in json.load(sys.stdin)['devices'].values() for d in r][0])")
  xcrun simctl shutdown "$dev" > /dev/null 2>&1
  xcrun simctl boot "$dev" && xcrun simctl bootstatus "$dev" -b > /dev/null
}

run_flow() { # <flow file> -> exit code of maestro
  local name; name=$(basename "$1" .yaml)
  ensure_device_log
  local mark; mark=$(log_mark)
  local net; net=$(sed -n 's/^# network: *//p' "$1")
  [ -n "$net" ] && { echo "[lab] $name: network $net"; wait_online; set_network $net; }
  local text; text=$(sed -n 's/^# text-size: *//p' "$1")
  [ -n "$text" ] && { echo "[lab] $name: text size $text"; set_text_size "$text"; }
  start_recording "$name"
  rm -f "$OUT/junit/$name.xml"
  maestro_cmd test $DRIVER_OPTS -e SDK_VERSION="$SDK" -e ANDROID_API="${ANDROID_API:-}" -e MONITOR_SCENARIO="${MONITOR_SCENARIO:-}" --format junit --output "$OUT/junit/$name.xml" \
    --test-output-dir "$OUT/maestro/$name" "$1" > "$OUT/maestro/$name.txt" 2>&1
  local code=$?
  # Maestro writes no JUnit when it cannot start (e.g. the iOS driver did not come up): write one
  # with the error, so the flow reads as failed and not as "not run" in the results.
  if [ "$code" != 0 ] && [ ! -s "$OUT/junit/$name.xml" ]; then
    # The exception line, else the first line Maestro printed (e.g. "Unknown Property: ...").
    local msg; msg=$(grep -m1 -E 'Exception|Error' "$OUT/maestro/$name.txt" | sed 's/^ *//')
    [ -n "$msg" ] || msg=$(grep -m1 -v '^[[:space:]]*$' "$OUT/maestro/$name.txt" | sed 's/^ *//')
    junit_case "$name" error "${msg:-Maestro exited with code $code and no output}"
  fi
  stop_recording "$name"
  ensure_device_log "$mark"
  [ -n "$net" ] && set_network full none
  [ -n "$text" ] && set_text_size default
  return $code
}

# A debug APK runs interpreted/JIT; on a CI emulator the first frame took 64 s. Compile it
# ahead of time (clearState keeps the compiled code).
if [ "$PLATFORM" = android ]; then
  adb_cmd shell cmd package compile -m speed -f com.deepdots.testapp > /dev/null 2>&1 || true
  # A busy CI emulator raises "<system app> isn't responding" dialogs over the app (seen:
  # Pixel Launcher), which hid the cookie dialog in every flow. Don't show them.
  adb_cmd shell settings put global hide_error_dialogs 1 > /dev/null 2>&1 || true
fi
# What device this run is, for the visual report: test-apps/visual/collect.mjs reads device.json.
if [ "$PLATFORM" = android ]; then
  printf '{"platform":"android","model":"%s","profile":"%s","os":"%s","density":"%s","size":"%s"}\n' \
    "$(adb_cmd shell getprop ro.product.model | tr -d '\r')" "${AVD_PROFILE:-}" "$(adb_cmd shell getprop ro.build.version.sdk | tr -d '\r')" \
    "$(adb_cmd shell wm density | tr -d '\r' | awk '{print $NF}' | tail -1)" "$(adb_cmd shell wm size | tr -d '\r' | awk '{print $NF}' | tail -1)" > "$OUT/device.json"
else
  xcrun simctl list devices -j | python3 -c "
import json, sys
want = sys.argv[1]
for runtime, devs in json.load(sys.stdin)['devices'].items():
    for d in devs:
        if d['state'] == 'Booted' and want in ('booted', d['udid']):
            os = runtime.rsplit('iOS-', 1)[-1].replace('-', '.')
            model = d.get('deviceTypeIdentifier', d['name']).rsplit('.', 1)[-1].replace('-', ' ')
            print(json.dumps({'platform': 'ios', 'model': model, 'os': os, 'name': d['name']}))
            raise SystemExit
" "${DEVICE:-booted}" > "$OUT/device.json"
fi
fix_status_bar
# Warm-up: the first cold start on a CI emulator/simulator is the slow one.
maestro_cmd test -e SCENARIO=checkout-time subflows/launch.yaml > "$OUT/maestro/warm-up.txt" 2>&1 || true
# Android: by default every `maestro test` reinstalls its driver apps, and the emulator only
# started our app once the driver was removed at the end of the flow (+2 min, blank screen).
# The warm-up installed it; keep it for the rest of the run.
DRIVER_OPTS=""
[ "$PLATFORM" = android ] && DRIVER_OPTS="--no-reinstall-driver"

# lemu/: a client popup and survey, exact copies in DEV. e2e/: the E2E scenarios in DEV. basic/: the minimal host app (com.deepdots.basicapp,
# installed next to NORDLY): skipped when it is not installed.
FLOWS=${FLOWS:-$(ls flows/*.yaml regression/*.yaml failures/*.yaml lemu/*.yaml basic/*.yaml e2e/*.yaml visual/*.yaml)}
failed=()
ANDROID_API=
[ "$PLATFORM" = android ] && ANDROID_API=$(adb_cmd shell getprop ro.build.version.sdk 2>/dev/null | tr -d '\r')
for f in $FLOWS; do
  name=$(basename "$f" .yaml)
  if grep -q 'android-only' "$f" && [ "$PLATFORM" != android ]; then
    echo "[lab] skip $name (android-only)"; junit_case "$name" skipped "Android only"; continue
  fi
  # `# min-android-api: N`: the flow cannot run on older Android versions (the reason is in the flow).
  min_api=$(sed -n 's/^# min-android-api: *//p' "$f")
  if [ -n "$min_api" ] && [ -n "$ANDROID_API" ] && [ "$ANDROID_API" -lt "$min_api" ]; then
    echo "[lab] skip $name (API $ANDROID_API < $min_api)"; junit_case "$name" skipped "Android API $ANDROID_API < $min_api"; continue
  fi
  # `# min-sdk: X.Y.Z`: the flow covers a setup that only exists from that version (always run on `local`).
  min_v=$(sed -n 's/^# min-sdk: *//p' "$f")
  if [ -n "$min_v" ] && [ "$SDK" != local ] && [ "$(printf '%s\n%s\n' "$SDK" "$min_v" | sort -V | head -1)" != "$min_v" ]; then
    echo "[lab] skip $name (SDK $SDK < $min_v)"; junit_case "$name" skipped "SDK $SDK < $min_v"; continue
  fi
  if [ "${f%%/*}" = basic ] && ! basic_installed; then
    echo "[lab] skip $name (minimal host app not installed)"; junit_case "$name" skipped "minimal host app not installed"; continue
  fi
  # `# known-issue-until: X.Y.Z`: a known issue only for SDK versions up to X.Y.Z.
  # `# known-issue-until-ios: X.Y.Z` / `-android:` overrides it on that platform.
  until_v=$(sed -n "s/^# known-issue-until-$PLATFORM: *//p" "$f")
  [ -n "$until_v" ] || until_v=$(sed -n 's/^# known-issue-until: *//p' "$f")
  known=0
  if grep -q 'tags:.*known-issue' "$f"; then
    known=1
    # Marked only for the other platform: a normal flow here.
    if [ -z "$until_v" ] && grep -q '^# known-issue-until-' "$f"; then known=0; fi
    if [ -n "$until_v" ] && { [ "$SDK" = local ] || [ "$(printf '%s\n%s\n' "$SDK" "$until_v" | sort -V | tail -1)" != "$until_v" ]; }; then
      known=0
    fi
  fi
  if [ "$known" = 1 ]; then
    if run_flow "$f"; then
      echo "::error::[lab] $name PASSED: the known issue looks fixed, drop its known-issue tag"
      failed+=("$name")
    else
      echo "::warning::[lab] $name still fails (known issue): $(grep -m1 -E 'Assertion|Element' "$OUT/maestro/$name.txt" | sed 's/^ *//')"
    fi
    continue
  fi
  if grep -q 'expected-failure' "$f"; then
    # Up to two tries: a failure for another reason (e.g. the driver did not start) is retried.
    for try in 1 2; do
      if run_flow "$f"; then
        echo "::error::[lab] $name PASSED but must fail: the missing navigation event was NOT detected"
        failed+=("$name")
      elif grep -q 'Assertion is false: "Why did you leave the checkout?" is visible' "$OUT/maestro/$name.txt"; then
        echo "[lab] $name failed as expected: $(grep -m1 -E '^\[Failed\]' "$OUT/maestro/$name.txt")"
        echo "[lab] => the test system detects the missing trigger"
      elif [ "$try" = 1 ]; then
        cp "$OUT/maestro/$name.txt" "$OUT/maestro/$name.try1.txt"
        reboot_if_driver_timeout "$OUT/maestro/$name.try1.txt"
        echo "::warning::[lab] $name failed for another reason, retrying: $(grep -m1 -E '^\[Failed\]' "$OUT/maestro/$name.txt")"
        continue
      else
        echo "::error::[lab] $name failed for another reason (not the missing popup): $(grep -m1 -E '^\[Failed\]' "$OUT/maestro/$name.txt")"
        failed+=("$name")
      fi
      break
    done
    continue
  fi
  if run_flow "$f"; then echo "[lab] PASS $name"; continue; fi
  # One retry, so a slow CI device does not hide real results; the retry is reported.
  first=$(grep -m1 -E '^\[Failed\]' "$OUT/maestro/$name.txt")
  mv "$OUT/maestro/$name.txt" "$OUT/maestro/$name.try1.txt"
  reboot_if_driver_timeout "$OUT/maestro/$name.try1.txt"
  if run_flow "$f"; then
    echo "::warning::[lab] PASS $name on retry (first try: $first)"
  else
    echo "::error::[lab] FAIL $name: $(grep -m1 -E '^\[Failed\]' "$OUT/maestro/$name.txt")"
    failed+=("$name")
    reboot_if_driver_timeout "$OUT/maestro/$name.txt"
  fi
done

sleep 2; kill "$LOG_PID" 2>/dev/null
echo "----- [lab] lines from the device log -----"
grep -a '\[lab\]' "$OUT/device.log" | sed -E 's/.*(\[lab\])/\1/' | tee "$OUT/lab.log"
echo "-------------------------------------------"

# Isolation: the apps only know DEV; fail loudly if anything reached production.
if grep -aE 'https?://api\.deepdots\.com' "$OUT/device.log" > "$OUT/prod-calls.txt"; then
  echo "::error::[lab] production API referenced in the device log:"; head "$OUT/prod-calls.txt"; failed+=("prod-isolation")
else
  echo "[lab] isolation OK: no request to api.deepdots.com, only $(grep -ac 'api-dev.deepdots.com' "$OUT/device.log") log lines with api-dev.deepdots.com"
fi

# DD-70: a passing survey-sdk-<v> flow must really have drawn the survey with <v> (JS and CSS).
# The WebView logs "[MagicFeedback] survey-sdk <v> assigned" only once that version's CSS and JS
# are in; any fallback logs "<vendored> bundled". Maestro cannot read that from the screen.
# SDKs before 0.7.10 skip those flows (they ignore surveyFrontend.sdkVersion).
knows_survey_sdk() { [ "$SDK" = "local" ] && return 0; local IFS=.; set -- $SDK; [ "${1:-0}" -gt 0 ] || [ "${2:-0}" -gt 7 ] || { [ "${2:-0}" -eq 7 ] && [ "${3:-0}" -ge 10 ]; }; }
if knows_survey_sdk; then
  for f in $FLOWS; do
    name=$(basename "$f" .yaml)
    case "$name" in survey-sdk-*) ;; *) continue ;; esac
    v=${name#survey-sdk-}
    printf '%s\n' "${failed[@]}" | grep -qx "$name" && continue
    if grep -aqF "[MagicFeedback] survey-sdk $v assigned" "$OUT/device.log"; then
      echo "[lab] survey SDK $v assigned (JS + CSS) in $name"
    else
      echo "::error::[lab] FAIL $name: the survey was not drawn with $v ($(grep -aoE '\[MagicFeedback\] survey-sdk [0-9.]+ [a-z]+' "$OUT/device.log" | tail -1))"
      failed+=("$name")
    fi
  done
fi

if [ ${#failed[@]} -gt 0 ]; then echo "[lab] failed: ${failed[*]}"; exit 1; fi
echo "[lab] all flows OK (sdk $SDK, $PLATFORM)"
