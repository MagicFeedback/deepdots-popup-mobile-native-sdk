#!/usr/bin/env python3
"""Splits a test-apps run into parallel parts (the matrix of test-apps.yml).

    LIST='["local"]' FLOWS='' VISUAL='visual/a.yaml ...' PARTS='' test-apps/scripts/plan-shards.py

env:
  LIST    JSON list of SDK versions.
  FLOWS   space-separated flows relative to test-apps/maestro; empty = the full suite (the same
          folders, in the same order, as run-maestro.sh).
  VISUAL  visual flows of the run: the part that runs them also runs the small-screen pass.
  PARTS   number of parts; empty = auto (4 when the run has more than SPLIT_ABOVE flows, else 1).

Prints a JSON list of matrix entries {sdk, part, parts, label, suffix, flows, visual}. With more
than one part, the visual flows go together in one part (with the small-screen pass, so the
visual job finds every screenshot of a device in the same run) and the rest is dealt round-robin
over the other parts: a stable split (same flows -> same parts) that keeps each part well under
the job timeout. Release PR (57 flows, iOS ~2.1 min/flow): 3 parts of ~17 flows (~36 min) +
the visual part (6 flows + small screen, ~32 min).

Job names: a single part of a published version keeps its old name ("iOS · SDK 0.7.11"); `local`
parts are always "SDK local · i/n", because "iOS · SDK local" / "Android · SDK local" is the job
that sums them up (the exact check name scripts/release/required-checks.mjs requires).
"""
import glob
import json
import os
import sys

SPLIT_ABOVE = 12
DEFAULT_PARTS = 4
SUITE = ["flows", "regression", "failures", "lemu", "basic", "e2e", "visual"]


def all_flows(root):
    out = []
    for d in SUITE:
        out += sorted(os.path.relpath(p, root) for p in glob.glob(os.path.join(root, d, "*.yaml")))
    return out


def plan(sdks, flows, visual, parts=None, root="test-apps/maestro"):
    listed = flows or all_flows(root)
    if parts is None:
        parts = DEFAULT_PARTS if len(listed) > SPLIT_ABOVE else 1
    if parts <= 1:
        groups = [(" ".join(flows), " ".join(visual))]
    else:
        vis = [f for f in listed if f.startswith("visual/")]
        rest = [f for f in listed if not f.startswith("visual/")]
        n = max(1, parts - 1) if vis else parts
        groups = [(" ".join(rest[i::n]), "") for i in range(n)]
        groups = [g for g in groups if g[0]]
        if vis:
            groups.append((" ".join(vis), " ".join(visual)))
    out = []
    for sdk in sdks:
        total = len(groups)
        for i, (fl, vi) in enumerate(groups, 1):
            single = total == 1 and sdk != "local"
            out.append({
                "sdk": sdk,
                "part": i,
                "parts": total,
                "label": f"SDK {sdk}" if single else f"SDK {sdk} · {i}/{total}",
                "suffix": "" if single else f"-p{i}",
                "flows": fl,
                "visual": vi,
            })
    return out


def main():
    sdks = json.loads(os.environ["LIST"])
    flows = os.environ.get("FLOWS", "").split()
    visual = os.environ.get("VISUAL", "").split()
    parts = os.environ.get("PARTS", "").strip()
    print(json.dumps(plan(sdks, flows, visual, int(parts) if parts else None), ensure_ascii=False))


if __name__ == "__main__":
    sys.exit(main())
