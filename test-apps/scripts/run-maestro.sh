#!/usr/bin/env bash
# Runs the Maestro suite against the installed NORDLY test app, one flow at a time.
#
#   test-apps/scripts/run-maestro.sh <android|ios> <sdk-version> <out-dir>
#
# env: DEVICE (simulator UDID / adb serial, optional), RECORD=1 (one video per flow),
#      FLOWS (space-separated flow files relative to test-apps/maestro; default: all).
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

# Device log: what CI keeps as "the run's log".
if [ "$PLATFORM" = android ]; then
  adb_cmd logcat -c
  adb_cmd logcat -v time > "$OUT/device.log" 2>&1 &
else
  xcrun simctl spawn "${DEVICE:-booted}" log stream --level info --style compact \
    --predicate 'subsystem == "com.deepdots.testapp" OR process == "NordlyTestApp"' > "$OUT/device.log" 2>&1 &
fi
LOG_PID=$!

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

# `# text-size: large` in a flow: it runs with large system text (Android font_scale 1.3, iOS
# Dynamic Type XXXL), so long titles wrap on any device; back to the default afterwards.
set_text_size() {
  if [ "$PLATFORM" = android ]; then
    adb_cmd shell settings put system font_scale "$([ "$1" = large ] && echo 1.3 || echo 1.0)" > /dev/null 2>&1
  else
    xcrun simctl ui "${DEVICE:-booted}" content_size "$([ "$1" = large ] && echo extra-extra-extra-large || echo large)" > /dev/null 2>&1
  fi
}

run_flow() { # <flow file> -> exit code of maestro
  local name; name=$(basename "$1" .yaml)
  local text; text=$(sed -n 's/^# text-size: *//p' "$1")
  [ -n "$text" ] && { echo "[lab] $name: text size $text"; set_text_size "$text"; }
  start_recording "$name"
  maestro_cmd test $DRIVER_OPTS -e SDK_VERSION="$SDK" --format junit --output "$OUT/junit/$name.xml" \
    --test-output-dir "$OUT/maestro/$name" "$1" > "$OUT/maestro/$name.txt" 2>&1
  local code=$?
  stop_recording "$name"
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
# Warm-up: the first cold start on a CI emulator/simulator is the slow one.
maestro_cmd test -e SCENARIO=checkout-time subflows/launch.yaml > "$OUT/maestro/warm-up.txt" 2>&1 || true
# Android: by default every `maestro test` reinstalls its driver apps, and the emulator only
# started our app once the driver was removed at the end of the flow (+2 min, blank screen).
# The warm-up installed it; keep it for the rest of the run.
DRIVER_OPTS=""
[ "$PLATFORM" = android ] && DRIVER_OPTS="--no-reinstall-driver"

FLOWS=${FLOWS:-$(ls flows/*.yaml failures/*.yaml)}
failed=()
for f in $FLOWS; do
  name=$(basename "$f" .yaml)
  if grep -q 'android-only' "$f" && [ "$PLATFORM" != android ]; then echo "[lab] skip $name (android-only)"; continue; fi
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
  if run_flow "$f"; then
    echo "::warning::[lab] PASS $name on retry (first try: $first)"
  else
    echo "::error::[lab] FAIL $name: $(grep -m1 -E '^\[Failed\]' "$OUT/maestro/$name.txt")"
    failed+=("$name")
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

if [ ${#failed[@]} -gt 0 ]; then echo "[lab] failed: ${failed[*]}"; exit 1; fi
echo "[lab] all flows OK (sdk $SDK, $PLATFORM)"
