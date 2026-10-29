#!/usr/bin/env bash
# Stability guard for every matrix row (MF-3263): fails when the device log of the run shows the
# signature of the 0.7.0 invisible popup (the reveal touching the view from the JavaBridge
# thread) or a crash of the test app, even if every flow passed.
#
#   .github/test-apps-matrix/check-device-log.sh <device.log>
set -uo pipefail
LOG=$1
PATTERNS='CalledFromWrongThreadException|FATAL EXCEPTION|ANR in com\.deepdots\.testapp|Terminating app due to uncaught exception|NordlyTestApp.*(crashed|EXC_BAD_ACCESS|EXC_CRASH)'
if grep -aE "$PATTERNS" "$LOG" > "$(dirname "$LOG")/stability.txt"; then
  echo "::error::[lab] device log shows a crash / wrong-thread reveal:"; head -20 "$(dirname "$LOG")/stability.txt"; exit 1
fi
echo "[lab] stability OK: no crash, ANR or CalledFromWrongThreadException in the device log"
