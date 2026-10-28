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

run_flow() { # <flow file> -> exit code of maestro
  local name; name=$(basename "$1" .yaml)
  start_recording "$name"
  maestro_cmd test -e SDK_VERSION="$SDK" --format junit --output "$OUT/junit/$name.xml" \
    --test-output-dir "$OUT/maestro/$name" "$1" > "$OUT/maestro/$name.txt" 2>&1
  local code=$?
  stop_recording "$name"
  return $code
}

FLOWS=${FLOWS:-$(ls flows/*.yaml failures/*.yaml)}
failed=()
for f in $FLOWS; do
  name=$(basename "$f" .yaml)
  if grep -q 'android-only' "$f" && [ "$PLATFORM" != android ]; then echo "[lab] skip $name (android-only)"; continue; fi
  if grep -q 'known-issue' "$f"; then
    if run_flow "$f"; then
      echo "::error::[lab] $name PASSED: the known issue looks fixed, drop its known-issue tag"
      failed+=("$name")
    else
      echo "::warning::[lab] $name still fails (known issue): $(grep -m1 -E 'Assertion|Element' "$OUT/maestro/$name.txt" | sed 's/^ *//')"
    fi
    continue
  fi
  if grep -q 'expected-failure' "$f"; then
    if run_flow "$f"; then
      echo "::error::[lab] $name PASSED but must fail: the missing navigation event was NOT detected"
      failed+=("$name")
    else
      echo "[lab] $name failed as expected: $(grep -m1 -E 'Assertion|not visible|Timeout|Element' "$OUT/maestro/$name.txt" | sed 's/^ *//')"
      echo "[lab] => the test system detects the missing trigger"
    fi
    continue
  fi
  if run_flow "$f"; then echo "[lab] PASS $name"; else echo "::error::[lab] FAIL $name"; tail -5 "$OUT/maestro/$name.txt"; failed+=("$name"); fi
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
