"""Results matrix of a test-apps-matrix run (MF-3263): flows x rows, from each row's JUnit files.

    python3 summarize.py <dir with one sub-folder per row> [<test-apps/maestro>] > matrix.md

Each row folder (one CI artifact) has row.json (written by the job) and junit/<flow>.xml from
test-apps/scripts/run-maestro.sh. A flow retried and then passed shows as "✅ (retry)". With the
Maestro folder, flows that are meant to fail read as such: an `expected-failure` that failed is
"✅ (fails as expected)", a `known-issue` (up to `known-issue-until`, if set) is "⚠️ known issue".
A flow that ran without a JUnit file (Maestro did not start) is "❌", and a skipped one "n/a".

    python3 summarize.py <rows> <test-apps/maestro> --json results.json > matrix.md

also writes every row with its per-flow status (pass, retry, fail, error, skip, known, expected)
for the release validation report.
"""
import json
import sys
import xml.etree.ElementTree as ET
from pathlib import Path

args = sys.argv[1:]
json_out = None
if "--json" in args:
    i = args.index("--json")
    json_out = Path(args[i + 1])
    del args[i:i + 2]
root = Path(args[0])
maestro = Path(args[1]) if len(args) > 1 else None


def version_key(v):
    return [int(x) if x.isdigit() else 0 for x in v.split(".")]


def flow_kind(name, sdk, platform):
    """'expected', 'known' or None, from the flow file's tags. Same rule as run-maestro.sh:
    `# known-issue-until-ios:` / `-android:` overrides `# known-issue-until:` on that platform, and
    a flow marked only for the other platform is a normal flow here."""
    if not maestro:
        return None
    files = list(maestro.glob(f"*/{name}.yaml"))
    if not files:
        return None
    text = files[0].read_text()
    if "expected-failure" in text:
        return "expected"
    if "known-issue" in text:
        def marker(prefix):
            return next((l.split(":", 1)[1].strip() for l in text.splitlines() if l.startswith(prefix)), None)
        until = marker(f"# known-issue-until-{platform.lower()}:") or marker("# known-issue-until:")
        if until is None and "# known-issue-until-" in text:
            return None
        if until is None or (sdk != "local" and version_key(sdk) <= version_key(until)):
            return "known"
    return None


rows = []
for meta in sorted(root.glob("*/row.json")):
    row = json.loads(meta.read_text())
    results, status, messages = {}, {}, {}
    junit = {x.stem: x for x in (meta.parent / "junit").glob("*.xml")}
    # Flows Maestro ran but did not report (older runs without the synthetic JUnit): failed.
    ran = {t.stem for t in (meta.parent / "maestro").glob("*.txt")} - {"warm-up"}
    ran = {n for n in ran if not n.endswith(".try1")}
    for name in sorted(set(junit) | ran):
        cases = list(ET.parse(junit[name]).getroot().iter("testcase")) if name in junit else []
        skipped = bool(cases) and all(c.find("skipped") is not None for c in cases)
        errored = not cases or any(c.find("error") is not None for c in cases)
        ok = bool(cases) and all(c.find("failure") is None and c.find("error") is None for c in cases)
        retried = (meta.parent / "maestro" / f"{name}.try1.txt").exists()
        bad = [e for c in cases for e in (c.find("failure"), c.find("error")) if e is not None]
        if bad:
            text = bad[0].get("message") or bad[0].text or ""
            messages[name] = (text.strip().splitlines() or [""])[0][:300]
        elif not cases:
            messages[name] = "Maestro did not report this flow"
        kind = flow_kind(name, row["sdk"], row["platform"])
        if skipped:
            results[name], status[name] = "n/a", "skip"
        elif kind == "expected":
            results[name] = "✅ (fails as expected)" if not ok else "❌ (should fail)"
            status[name] = "expected" if not ok else "fail"
        elif kind == "known":
            results[name] = "⚠️ known issue" if not ok else "❌ (known issue passes: drop the tag)"
            status[name] = "known" if not ok else "fail"
        elif ok:
            results[name], status[name] = ("✅ (retry)", "retry") if retried else ("✅", "pass")
        else:
            results[name], status[name] = ("❌ (did not run)", "error") if errored else ("❌", "fail")
    row["results"], row["status"], row["messages"] = results, status, messages
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

if json_out:
    json_out.write_text(json.dumps([{k: v for k, v in r.items() if k != "results"} for r in rows], indent=2))
