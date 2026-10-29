"""Results matrix of a test-apps-matrix run (MF-3263): flows x rows, from each row's JUnit files.

    python3 summarize.py <dir with one sub-folder per row> [<test-apps/maestro>] > matrix.md

Each row folder (one CI artifact) has row.json (written by the job) and junit/<flow>.xml from
test-apps/scripts/run-maestro.sh. A flow retried and then passed shows as "✅ (retry)". With the
Maestro folder, flows that are meant to fail read as such: an `expected-failure` that failed is
"✅ (fails as expected)", a `known-issue` (up to `known-issue-until`, if set) is "⚠️ known issue".
"""
import json
import sys
import xml.etree.ElementTree as ET
from pathlib import Path

root = Path(sys.argv[1])
maestro = Path(sys.argv[2]) if len(sys.argv) > 2 else None


def version_key(v):
    return [int(x) if x.isdigit() else 0 for x in v.split(".")]


def flow_kind(name, sdk):
    """'expected', 'known' or None, from the flow file's tags."""
    if not maestro:
        return None
    files = list(maestro.glob(f"*/{name}.yaml"))
    if not files:
        return None
    text = files[0].read_text()
    if "expected-failure" in text:
        return "expected"
    if "known-issue" in text:
        until = next((l.split(":", 1)[1].strip() for l in text.splitlines() if l.startswith("# known-issue-until:")), None)
        if until is None or (sdk != "local" and version_key(sdk) <= version_key(until)):
            return "known"
    return None


rows = []
for meta in sorted(root.glob("*/row.json")):
    row = json.loads(meta.read_text())
    results = {}
    for xml in sorted((meta.parent / "junit").glob("*.xml")):
        cases = ET.parse(xml).getroot().iter("testcase")
        ok = all(c.find("failure") is None and c.find("error") is None for c in cases)
        retried = (meta.parent / "maestro" / f"{xml.stem}.try1.txt").exists()
        kind = flow_kind(xml.stem, row["sdk"])
        if kind == "expected":
            results[xml.stem] = "✅ (fails as expected)" if not ok else "❌ (should fail)"
        elif kind == "known":
            results[xml.stem] = "⚠️ known issue" if not ok else "❌ (known issue passes: drop the tag)"
        else:
            results[xml.stem] = "✅ (retry)" if ok and retried else "✅" if ok else "❌"
    row["results"] = results
    rows.append(row)

flows = sorted({f for r in rows for f in r["results"]})
print("| Flow | " + " | ".join(r["label"] for r in rows) + " |")
print("|---|" + "---|" * len(rows))
for f in flows:
    print(f"| {f} | " + " | ".join(r["results"].get(f, "–") for r in rows) + " |")
print("| **Job** | " + " | ".join("✅" if r.get("outcome") == "success" else "❌" for r in rows) + " |")
print()
print("| Row | Platform | OS | Device | Build | SDK requested → resolved | WebView / runtime |")
print("|---|---|---|---|---|---|---|")
for r in rows:
    print(f"| {r['label']} | {r['platform']} | {r['os']} | {r['device']} | {r['build']} | "
          f"{r['sdk']} → {r.get('resolved', '?')} | {r.get('engine', '')} |")
