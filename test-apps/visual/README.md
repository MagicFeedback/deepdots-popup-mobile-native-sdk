# Visual regression · native popup (DD-28)

Detects unexpected changes in how the popup looks on iOS and Android. Maestro flows in
`test-apps/maestro/visual/` open the popup in the NORDLY test apps and take one screenshot per
state (`visual-<state>.png`); the CI compares each with its **approved** baseline. Nothing ever
approves a baseline automatically. The web counterpart is `apps/test-app/visual` in
deepdots-popup-sdk; `scripts/visual/{compare,check,approve}.mjs` are the same files in both repos.

| | |
|---|---|
| Checks (CI) | **`Visual regression · Android · SDK local`** and **`Visual regression · iOS · SDK local`** (`test-apps.yml`, job `visual`): on the release PR (release-check.yml → "Test apps (release build)") and every Monday (visual flows only, SDK built from dev). Manual: Run workflow with versions `["local"]` and flows `visual/*.yaml` |
| Capture | `maestro/visual/*.yaml` + `subflows/visual-shot.yaml`, run by `scripts/run-maestro.sh` |
| Collect | `visual/collect.mjs` (run output → `<device>/<state>.png` + `meta.json`) |
| Compare / approve | `scripts/visual/check.mjs`, `scripts/visual/approve.mjs` (`npm ci --prefix scripts/visual`) |
| Baselines | `visual/baselines/<device>/<state>.png` + `visual/approvals.json` |
| Tolerance and masks | `visual/visual.json` |

## States and devices

| Device folder | Where |
|---|---|
| `ios-iphone-16-ios18` | CI `macos-15`, Xcode 16.4, simulator iPhone 16 (Dynamic Island: safe areas) |
| `ios-iphone-se-3rd-generation-ios18` | the same job, iPhone SE (3rd generation): small screen, no notch |
| `android-pixel-6-api34` | CI `ubuntu-latest`, emulator API 34 google_apis x86_64, profile pixel_6 |
| `android-small-720x1280-api34` | the same emulator at `wm size 720x1280`, `wm density 320`: small screen |

| State | Flow | What it protects |
|---|---|---|
| `complex-1-open` | `visual-complex` (rate-product, centred) | scrim, card size/position, typography, title, X, Send |
| `complex-1-required-error` | idem, Send without answer | required-question notice |
| `complex-2-selected` | page 2: 4 stars + a checkbox | selected states, tallest page (clipping/scroll), Back |
| `complex-3-boolean-email` | page 3 | resize between questions, inputs |
| `complex-4-text` | page 4 (keyboard closed) | textarea |
| `complex-5-thanks` | thank-you | completion screen, "Complete survey" |
| `simple-1-open` | `visual-simple` (help-click) | NPS scale |
| `position-top`, `position-bottom` | `visual-position-*` (E2E scenarios) | top/bottom anchoring, status bar and home indicator safe areas |
| `dark-font` | `visual-dark-font` | dark theme + custom font (Georgia) |
| `text-large` | `visual-text-large` | largest system text (Android 1.3, iOS XXXL) |

Content is fixed: dedicated DEV popup configs (`scenarios.json`) on the DEV surveys "SDK Test App ·
Simple / Complex", and the survey renderer version is pinned in the SDK (`MAGICFEEDBACK_VERSION`,
`POPUP_SDK_CSS_VERSION`). A change to those surveys in DEV shows up as a visual difference.
`visual-complex` sends one answer to MAGICFEEDBACK_DEV per run (like `submit-simple`).

Kept still: the status bar is fixed by run-maestro.sh (iOS `simctl status_bar override`,
Android demo mode: 9:41, full battery and signal, no notification icons, applied again after a
font scale change); the visual flows launch the app with `visual 1`, so the Lab log shows its
lines without the time (the only thing that changed between runs: 0 px elsewhere over 3 iOS and
3 Android local runs); the Lab bar shows the same text on every `local` run; no text is typed
(keyboard suggestions differ between runs). So `visual.json` has no masks; it supports them
(`devices.<device>.masks`, rectangles in screenshot pixels, optionally per state) if a device
ever needs one.

## Tolerance

`threshold 0.01` (per-pixel colour distance, antialiasing ignored), **`maxDiffPixels 300`** per
screenshot, any size change fails. Measured on 4-nov-2026 between two CI runs: 0 px everywhere
except a blinking text cursor (139 px, Android `complex-4-text`) and WebView sub-pixel rendering
(16 px, iPhone SE `simple-1-open`); 300 is about twice the worst. A real layout change is far
above it: two swapped popup buttons changed 6,400–7,150 px per screenshot (web failure test).
Raise it per device (`devices.<device>.compare`) only with a measurement.

## Run it locally

```bash
# iOS (own simulator, published or local SDK built as in test-apps/README.md)
DEVICE=<udid> FLOWS="$(cd test-apps/maestro && ls visual/*.yaml)" \
  test-apps/scripts/run-maestro.sh ios 0.7.9 out/ios
node test-apps/visual/collect.mjs --run out/ios --dest out/visual-actual --sdk 0.7.9
npm ci --prefix scripts/visual
node scripts/visual/check.mjs --suite test-apps/visual --actual out/visual-actual --out out/visual-report
```

⚠️ The baselines come from the CI devices. A Mac simulator with another Xcode/runtime, or an
arm64 emulator, renders slightly differently: locally the check is for finding out what changed
(look at `report.html`), not a gate. The gate is the CI check.

## Approving baselines

1. The check fails; open `report.html` in the `visual-report-<android|ios>-local` artifact
   (reference · current · difference per state).
2. If the new look is intended, from a branch:
   ```bash
   node scripts/visual/approve.mjs --suite test-apps/visual --run <CI run id> \
     --artifact visual-report-ios-local --reason "why the new look is correct"
   ```
   (`--only <device>/<state>`, `--remove <key>`.) It writes the baselines and appends to
   `approvals.json` who approved, when, why, from which run/SDK version, and the sha256 of each
   file (plus the previous one). It refuses to run in CI.
3. Commit and open a PR to dev; the reviewer sees the image diffs. Until it is merged the check
   keeps comparing against the old baselines.

The check also fails when a baseline does not match its approved sha256, when a state has no
baseline and when a baseline has no screenshot.
