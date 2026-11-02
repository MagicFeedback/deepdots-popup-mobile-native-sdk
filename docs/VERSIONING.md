# Versioning, releases and supported versions

The native popup SDK (Android AAR `com.deepdots.sdk:shared-android` and the iOS
XCFramework `DeepdotsSDK` on [DeepdotsSDK-SPM](https://github.com/MagicFeedback/DeepdotsSDK-SPM))
is released on its own schedule. Its version has nothing to do with the Survey Renderer
(MagicSurvey, `vX.Y.Z` of the survey frontend) or with the web popup SDK
(`@magicfeedback/popup-sdk`). Each release states, in its compatibility manifest, the
`@magicfeedback/native` version its WebView runs and the popup SDK stylesheet it loads.

Android and iOS always share one version number: one tag `vX.Y.Z` publishes both.

## Version numbers

[Semantic Versioning](https://semver.org/spec/v2.0.0.html), `MAJOR.MINOR.PATCH`:

| Bump  | When |
|-------|------|
| MAJOR | Something a host app has to change: a removed or renamed option, method or event, a different meaning for an existing one, a higher Android `minSdk` or iOS minimum. While the SDK is `0.x`, these go in a MINOR. |
| MINOR | New options, methods or events; a new `@magicfeedback/native` with visible changes; new supported hosts. |
| PATCH | Fixes that keep the public API and the behaviour host apps rely on. |

Prereleases (`X.Y.Z-beta.N`) are never marked as the latest GitHub Release and are not merged
into `main`.

## What every release carries

Every published version has, and the publish workflow refuses to publish without:

1. **A version bump**: `PUBLISHING_VERSION` in `gradle.properties`, higher than anything
   already released on its `MAJOR.MINOR` line.
2. **Release notes**: its `## X.Y.Z - YYYY-MM-DD` section in [`CHANGELOG.md`](../CHANGELOG.md).
3. **A Git tag** `vX.Y.Z` on the release commit in `dev`.
4. **A valid compatibility manifest**, [`release/compatibility.json`](../release/compatibility.json)
   (schema: [`release/compatibility.schema.json`](../release/compatibility.schema.json)) with:
   - platform and SDK version (must equal `gradle.properties` and the tag);
   - the artifacts: Maven coordinates and the SPM package;
   - the survey renderer the WebView runs (`MAGICFEEDBACK_VERSION` in `MagicFeedbackHtml.kt`,
     vendored on Android, from the CDN on iOS) and the popup SDK stylesheet
     (`POPUP_SDK_CSS_VERSION`);
   - supported hosts: Android `minSdk` / `compileSdk` / `targetSdk`, the iOS minimum and the
     XCFramework slices, the frameworks (Compose, Views, SwiftUI, UIKit, KMP, Flutter) and the
     test suites that run before publishing;
   - the toolchain (Kotlin, Compose Multiplatform, AGP);
   - the configuration schema, [`release/config.schema.json`](../release/config.schema.json),
     generated from the code: `InitOptions` and the popup definition the API sends
     (`GET /sdk/popups`);
   - known-good popup fixtures ([`release/fixtures/`](../release/fixtures)), validated against
     that schema and parsed by the SDK itself in `ReleaseContractTest`;
   - known issues.

   Every version the manifest states is checked against the code, so it cannot drift.
5. **Immutable artifacts**: the AAR and the XCFramework zip are built once, smoke-tested
   (classes and headers present, `minSdk`, slices, renderer version), and **those same files**
   are published: the AAR is the one in the Maven Central bundle, and the zip's sha256 is the
   SPM checksum. Neither Maven Central nor an SPM tag can be overwritten.
6. **Traceability**: the GitHub Release `vX.Y.Z` has the AAR, the XCFramework zip,
   `SHA256SUMS`, the manifest, the config schema and `release.json`, which records the
   commit, each artifact's sha256 (checked against the Maven bundle and the SPM checksum),
   where each was published, the smoke test result, the toolchain, the "Release dry run" that
   passed on the release pull request, that pull request with its approvals and who merged
   it, and who pushed the tag.

## How a release happens

```text
feature PR ──► dev       notes under "## Unreleased"                (Version & changelog)
node scripts/release.mjs prepare patch|minor|major
release PR ──► dev       version bump + "## X.Y.Z - date"            (Version & changelog
                         + release/compatibility.json version         + Release dry run)
node scripts/release.mjs tag     tag vX.Y.Z on dev
release.yml              gate ─► tests ─► build once ─► smoke ─► Maven Central + SPM ─► GitHub Release ─► merge to main
                         └─► Slack notice in #sdk-releases (slack-notify.yml)
```

- A pull request to `dev` that changes the SDK (`shared/src/*Main`, the build scripts, the
  version catalog or `gradle.properties`) must add release notes under `## Unreleased`.
- Only the release pull request changes the version. Its "Release dry run" job (macOS) runs
  the same gate, tests and build as the publish workflow, with the same pinned tools, and
  smoke-tests the artifacts, so a broken release is caught before the tag exists.
- A pull request to `main` that changes the SDK is a hotfix and must bump the version and add
  its own changelog section.
- If `InitOptions` or the popup definition change, regenerate the schema:
  `UPDATE_RELEASE_SCHEMA=1 ./gradlew :shared:testDebugUnitTest --tests '*ReleaseContractTest*'`.
- If `MAGICFEEDBACK_VERSION`, `POPUP_SDK_CSS_VERSION`, `minSdk`, the iOS minimum or the
  toolchain change, update `release/compatibility.json` in the same pull request.
- Run the pull request check locally:
  `npm ci --prefix scripts/release && node scripts/release/check.mjs pr`.
  Tests of the gate: `npm test --prefix scripts/release`.
- Once the GitHub Release exists, `slack-notify.yml` posts the version, where it was published
  (Maven Central only if the POM is there), links and up to six changelog entries to
  #sdk-releases. The webhook is the `SDK_RELEASES_SLACK_WEBHOOK_URL` secret; without it the job
  only warns. Dry runs and releases that fail before publishing post nothing. To test it, run
  the workflow by hand ("Run workflow"): it posts the latest release with a "🧪 TEST" prefix.

## Supported versions

| Line | Support |
|------|---------|
| Latest minor of the current major (e.g. `0.8.x`) | Fixes and security fixes. Upgrade to the newest patch to get them. |
| Previous minor of the current major (e.g. `0.7.x`) | Security fixes and fixes for data loss (analytics or survey responses) for **3 months** after the next minor ships, as patches on its line (`0.7.4`). |
| Latest minor of the previous major | Security fixes for **6 months** after the new major ships. |
| Anything older | Not supported. |

Maven Central and SPM have no dist-tags: a patch for an older line is published under its own
version (`0.7.4` after `0.8.0`), is not marked as the latest GitHub Release and is not merged
into `main`. Host apps pin an exact version (Gradle, `exact:` in SPM), so nobody gets it
without asking for it. The survey renderer and stylesheet are supported as shipped: to get a
newer renderer, upgrade the SDK.
