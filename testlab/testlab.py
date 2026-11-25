#!/usr/bin/env python3
"""DD-273: runs the key popup checks on the fixed real devices of testlab/devices.json in
Firebase Test Lab, waits for them, logs the billed device minutes and fails if a device failed.

  testlab/testlab.py android --apk shared-debug-androidTest.apk --project P
  testlab/testlab.py ios --zip ios-tests.zip --project P
  testlab/testlab.py android --apk x.apk --project P --dry-run   # only prints the gcloud command

Cost guardrails (also in devices.json):
- only the devices in devices.json, at most limits.maxDevices per platform;
- --timeout from the file, never over limits.maxTimeoutMinutes;
- --num-flaky-test-attempts from the file, never over limits.maxFlakyAttempts (no retries: 0);
- the matrix is created with --async, its id is written to $TESTLAB_MATRIX_FILE so the workflow
  can cancel it if the job is cancelled, and it is cancelled here if it is not done after
  timeout + pollSlackMinutes;
- the billed minutes of each device (Tool Results `deviceUsageDuration`) go to the job summary.

Needs gcloud authenticated (the workflow signs in as testlab-ci through Workload Identity Federation). Python
standard library only.
"""
import argparse
import json
import math
import os
import re
import subprocess
import sys
import time
import urllib.request

HERE = os.path.dirname(os.path.abspath(__file__))
PRICE_PER_DEVICE_HOUR = 5.0  # physical devices, https://firebase.google.com/pricing (Test Lab)


def load_devices(platform):
    with open(os.path.join(HERE, "devices.json")) as f:
        cfg = json.load(f)
    limits = cfg["limits"]
    devices = cfg[platform]
    if not devices:
        sys.exit(f"testlab: no {platform} devices in devices.json")
    if len(devices) > limits["maxDevices"]:
        sys.exit(f"testlab: {len(devices)} {platform} devices, the limit is {limits['maxDevices']}")
    minutes = int(re.fullmatch(r"(\d+)m", limits["timeout"]).group(1))
    if minutes > limits["maxTimeoutMinutes"]:
        sys.exit(f"testlab: timeout {limits['timeout']} is over {limits['maxTimeoutMinutes']}m")
    if not 0 <= limits["flakyAttempts"] <= limits["maxFlakyAttempts"] <= 1:
        sys.exit("testlab: flakyAttempts must be 0 or 1 (no automatic retries)")
    for d in devices:
        if not re.fullmatch(r"[A-Za-z0-9_.-]+", d["model"]) or not re.fullmatch(r"[0-9.]+", d["version"]):
            sys.exit(f"testlab: bad device entry {d}")
    return devices, limits, minutes


def gcloud_command(args, devices, limits):
    cmd = ["gcloud", "firebase", "test", args.platform, "run", "--project", args.project, "--async",
           "--timeout", limits["timeout"], "--num-flaky-test-attempts", str(limits["flakyAttempts"]),
           "--results-history-name", f"popup-device-checks-{args.platform}",
           "--client-details", f"matrixLabel=DD-273 {args.label}".strip()]
    if args.results_bucket:
        # Our own bucket (EU, 60-day lifecycle): the default Test Lab bucket lives in a Google
        # project where testlab-ci can't write. One folder per CI run and platform.
        cmd += ["--results-bucket", args.results_bucket]
        if args.results_dir:
            cmd += ["--results-dir", args.results_dir]
    if args.platform == "android":
        # A library module has no app APK: its self-instrumenting test APK is both app and test.
        cmd += ["--type", "instrumentation", "--app", args.apk, "--test", args.apk]
    else:
        cmd += ["--test", args.zip]
    for d in devices:
        cmd += ["--device", f"model={d['model']},version={d['version']},locale=en,orientation=portrait"]
    return cmd


def token():
    return subprocess.run(["gcloud", "auth", "print-access-token"], check=True,
                          capture_output=True, text=True).stdout.strip()


def get(url):
    req = urllib.request.Request(url, headers={"Authorization": f"Bearer {token()}"})
    with urllib.request.urlopen(req, timeout=60) as r:
        return json.load(r)


def cancel(platform, project, matrix):
    # REST, not gcloud: `gcloud firebase test ios` has no `cancel` (only android does), so an iOS
    # matrix waiting for a busy device kept PENDING after the job gave up (run 44774904996).
    url = (f"https://testing.googleapis.com/v1/projects/{project}/testMatrices/{matrix}:cancel")
    req = urllib.request.Request(url, data=b"", method="POST",
                                 headers={"Authorization": f"Bearer {token()}"})
    try:
        with urllib.request.urlopen(req, timeout=60) as r:
            print(f"testlab: cancel {matrix} -> {json.load(r)}")
    except Exception as e:  # never hide the real failure behind a cancel error
        print(f"testlab: could not cancel {matrix}: {e}")


def seconds(duration):
    if not duration:
        return 0.0
    return float(duration.get("seconds", 0)) + duration.get("nanos", 0) / 1e9


def device_name(execution, devices):
    env = execution.get("environment", {})
    dev = env.get("androidDevice") or env.get("iosDevice") or {}
    model = dev.get("androidModelId") or dev.get("iosModelId") or "?"
    version = dev.get("androidVersionId") or dev.get("iosVersionId") or "?"
    for d in devices:
        if d["model"] == model and d["version"] == version:
            return d["name"]
    return f"{model} / {version}"


def summary(lines):
    path = os.environ.get("GITHUB_STEP_SUMMARY")
    text = "\n".join(lines) + "\n"
    print(text)
    if path:
        with open(path, "a") as f:
            f.write(text)


def main():
    p = argparse.ArgumentParser()
    p.add_argument("platform", choices=["android", "ios"])
    p.add_argument("--project", required=True)
    p.add_argument("--apk")
    p.add_argument("--zip")
    p.add_argument("--label", default="")
    p.add_argument("--results-bucket", default="")
    p.add_argument("--results-dir", default="")
    p.add_argument("--dry-run", action="store_true")
    args = p.parse_args()
    if args.platform == "android" and not args.apk:
        sys.exit("testlab: --apk is required for android")
    if args.platform == "ios" and not args.zip:
        sys.exit("testlab: --zip is required for ios")

    devices, limits, timeout_min = load_devices(args.platform)
    cmd = gcloud_command(args, devices, limits)
    print("testlab: " + " ".join(cmd))
    if args.dry_run:
        return 0

    run = subprocess.run(cmd, capture_output=True, text=True)
    out = run.stdout + run.stderr
    print(out)
    m = re.search(r"matrix-[a-z0-9]+", out)
    if run.returncode != 0 or not m:
        sys.exit(f"testlab: could not create the test matrix (exit {run.returncode})")
    matrix = m.group(0)
    if os.environ.get("TESTLAB_MATRIX_FILE"):
        with open(os.environ["TESTLAB_MATRIX_FILE"], "a") as f:
            f.write(f"{args.platform} {matrix}\n")

    url = f"https://testing.googleapis.com/v1/projects/{args.project}/testMatrices/{matrix}"
    deadline = time.time() + (timeout_min + limits["pollSlackMinutes"]) * 60
    state = None
    while time.time() < deadline:
        tm = get(url)
        state = tm.get("state")
        if state in ("FINISHED", "ERROR", "INVALID", "CANCELLED"):
            break
        time.sleep(20)
    else:
        print(f"testlab: {matrix} not done after {timeout_min + limits['pollSlackMinutes']} min, cancelling it")
        cancel(args.platform, args.project, matrix)
        tm = get(url)

    rows, total_min, estimated = [], 0, False
    for ex in tm.get("testExecutions", []):
        step = ex.get("toolResultsStep")
        billed, outcome = 0.0, ex.get("state", "?")
        if step:
            s = get("https://toolresults.googleapis.com/toolresults/v1beta3/projects/"
                    f"{step['projectId']}/histories/{step['historyId']}/executions/{step['executionId']}"
                    f"/steps/{step['stepId']}")
            billed = seconds(s.get("deviceUsageDuration"))
            if not billed:
                # Tool Results fills deviceUsageDuration a while after the step ends (run
                # 44795094317 logged 0 for all devices). Fall back to the step's run time, which is
                # what a physical device is billed for, so the summary never says $0 by mistake.
                billed = seconds(s.get("runDuration"))
                estimated = estimated or bool(billed)
            outcome = s.get("outcome", {}).get("summary", outcome)
        minutes = math.ceil(billed / 60) if billed else 0
        total_min += minutes
        rows.append(f"| {device_name(ex, devices)} | {outcome} | {billed / 60:.1f} | {minutes} |")

    result = tm.get("outcomeSummary") or tm.get("invalidMatrixDetails") or state
    console = f"https://console.firebase.google.com/project/{args.project}/testlab/histories"
    summary([
        f"### Test Lab · {args.platform} · {result}",
        "",
        f"Matrix `{matrix}` · state {state} · [results]({console})",
        "",
        "| Device | Outcome | Device minutes | Billed minutes |",
        "|---|---|---|---|",
        *rows,
        "",
        f"**Total billed: {total_min} device minutes ≈ ${total_min * PRICE_PER_DEVICE_HOUR / 60:.2f}**"
        f" (at ${PRICE_PER_DEVICE_HOUR:.0f}/device-hour)"
        + (" · estimated from the run time: Tool Results had no device usage yet" if estimated else ""),
    ])
    return 0 if result == "SUCCESS" else 1


if __name__ == "__main__":
    sys.exit(main())
