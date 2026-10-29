"""Results matrix of a test-apps-matrix run (MF-3263): flows x rows, from each row's JUnit files.

    python3 summarize.py <dir with one sub-folder per row> > matrix.md

Each row folder (one CI artifact) has row.json (written by the job) and junit/<flow>.xml from
test-apps/scripts/run-maestro.sh. A flow retried and then passed shows as "✅ (retry)".
"""
import json
import sys
import xml.etree.ElementTree as ET
from pathlib import Path

root = Path(sys.argv[1])
rows = []
for meta in sorted(root.glob("*/row.json")):
    row = json.loads(meta.read_text())
    results = {}
    for xml in sorted((meta.parent / "junit").glob("*.xml")):
        cases = ET.parse(xml).getroot().iter("testcase")
        ok = all(c.find("failure") is None and c.find("error") is None for c in cases)
        retried = (meta.parent / "maestro" / f"{xml.stem}.try1.txt").exists()
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
