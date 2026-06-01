# Package size analysis

Question from integrator (Slack): "Did you optimize the size of the package?"

Measured on `0.2.x` release artifacts (iPhone 17 Pro simulator, arm64, Release config).

## Numbers that matter

| Metric | Size | What it is |
|---|---|---|
| **App bundle delta (Release, stripped)** | **~37 MB** | The real on-device impact: demo app `.app` linking the SDK statically, after dead-strip. |
| └ main executable | ~38 MB file / **~26 MB `__TEXT`** | Actual machine code linked in (`size` segment breakdown). |
| iOS download (xcframework `.zip`) | ~66 MB | What integrators download via SPM. Compressed, contains device **+ simulator** slices. |
| iOS per-arch release framework (static `ar` archive) | ~107 MB | Pre-link, pre-strip static library. **Not** representative of app size. |
| Android AAR (`shared-release.aar`) | ~396 KB | Our SDK code only. Compose/Ktor are pulled transitively via Gradle, not bundled here. |

### How to read this
- The scary 107 MB / 66 MB figures are the **static library / download**, not app growth.
  After the linker dead-strips unused code and Apple applies App Thinning, the actual
  install delta is roughly the **~37 MB bundle / ~26 MB of code** measured above.
- The floor is dominated by the **Compose Multiplatform runtime + Skiko (Skia)** that
  renders the popup. This is inherent to the stack; there is no small subset of it.
- Android is cheap on disk for the AAR because the heavy runtime is a normal Gradle
  dependency the host app already resolves.

## Optimization levers (if we want to push it down later)

- `isStatic = true` is already set (good: enables dead-stripping into the host binary).
- Ship **device-only** slices to integrators (the simulator slice doubles the download;
  keep it for local dev only).
- Confirm Release uses full LTO/strip (`-Xstrip-debug-info`-equivalent already implied by
  Release framework).
- Longer term: evaluate whether any bundled MagicFeedback fallback assets / unused Ktor
  engines can be trimmed.

## Honest answer to give the integrator

> The download is ~66 MB (it includes both device and simulator slices), but the real
> app-size impact after linking and App Thinning is on the order of ~25–40 MB, dominated
> by the Compose/Skia rendering runtime that the popup is built on. We can trim the
> download by shipping device-only slices and will look at further reductions, but the
> runtime floor is inherent to the cross-platform UI stack.
