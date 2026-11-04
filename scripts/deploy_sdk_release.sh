#!/usr/bin/env bash
#
# deploy_sdk_release.sh — RETIRED (DD-27).
#
# This script published the iOS SPM release from a laptop (and left the Maven bundle for a manual
# upload) with no release gate, and SKIP_PREFLIGHT=1 skipped even its own checks: a silent way
# around the tests. Releases go through CI only:
#
#   node scripts/release.mjs prepare patch|minor|major   # release PR (dry run + test apps)
#   node scripts/release.mjs tag                         # tag -> .github/workflows/release.yml
#
# Emergency (a required check cannot go green in time): see docs/VERSIONING.md,
# "Emergency exception". It still publishes through release.yml and is written in the notes.
#
echo "[deploy] scripts/deploy_sdk_release.sh is retired: releases are published only by .github/workflows/release.yml." >&2
echo "[deploy] Run: node scripts/release.mjs prepare <patch|minor|major>, then node scripts/release.mjs tag." >&2
echo "[deploy] Emergency: docs/VERSIONING.md, \"Emergency exception\"." >&2
exit 1
