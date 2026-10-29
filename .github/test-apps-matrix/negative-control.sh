#!/usr/bin/env bash
# Negative control for the Lemu validation (MF-3263): proves the suite detects the 0.7.0 bugs.
#
#   .github/test-apps-matrix/negative-control.sh <sdk-version> <pass|fail> <out-dir>
#
# Runs lemu/rating-labels.yaml through test-apps/scripts/run-maestro.sh on the connected device
# and checks the outcome is the expected one:
#   pass  (0.7.3): the flow passes and the device log has no CalledFromWrongThreadException;
#   fail  (0.7.0): the flow fails ON the rating assertion (labels inside the chips), not on
#                  anything else (app not launching, popup not loading...).
# The invisible popup that blocked taps (also 0.7.0) only happens when the survey is drawn in
# less than 1.2 s, which a CI emulator never does; it is covered by the CalledFromWrongThread
# check on every row and by the recorded repro, not by this script.
set -uo pipefail
SDK=$1 EXPECT=$2 OUT=$3
ROOT=$(cd "$(dirname "$0")/../.." && pwd)
mkdir -p "$ROOT/test-apps/maestro/lemu"
cp -R "$ROOT/.github/test-apps-matrix/lemu/." "$ROOT/test-apps/maestro/lemu/"

FLOWS="lemu/rating-labels.yaml" "$ROOT/test-apps/scripts/run-maestro.sh" android "$SDK" "$OUT"
code=$?
log="$OUT/maestro/rating-labels.txt"
reason=$(grep -m1 -E '^\[Failed\]|Assertion is false' "$log" 2>/dev/null | sed 's/^ *//')
thread=$(grep -ac 'CalledFromWrongThreadException' "$OUT/device.log" 2>/dev/null || true)

if [ "$EXPECT" = pass ]; then
  if [ $code -eq 0 ] && [ "${thread:-0}" = 0 ]; then
    echo "[lab] negative control: SDK $SDK passes (rating chips 1..5, labels outside, no wrong-thread reveal)"; exit 0
  fi
  echo "::error::[lab] negative control: SDK $SDK should pass. flow exit=$code, CalledFromWrongThreadException x${thread:-0}. $reason"; exit 1
fi

if [ $code -eq 0 ]; then
  echo "::error::[lab] negative control: SDK $SDK PASSED but must fail: the 0.7.0 rating bug was NOT detected"; exit 1
fi
if grep -qE 'Assertion is false: "[15] = \.\*" is not visible|"[15] = \.\*" is visible' "$log"; then
  echo "[lab] negative control: SDK $SDK fails as expected: $reason"
  echo "[lab] => the suite detects the 0.7.0 rating bug (scale labels inside the chips)"; exit 0
fi
echo "::error::[lab] negative control: SDK $SDK failed for another reason (not the rating bug): $reason"; exit 1
