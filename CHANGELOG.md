# Changelog

All notable changes to the Deepdots Popup SDK are documented in this file.

## 0.2.0

### Breaking changes

- `InitOptions.debug` no longer controls the backend base URL. It now only controls
  SDK log output. Integrations that relied on `debug = true` to point the SDK at
  `https://api-dev.deepdots.com` must now opt in explicitly via the new
  `InitOptions.environment` field.

### Added

- `Environment { Production, Development }` enum exposed from
  `com.deepdots.sdk.models`.
- `InitOptions.environment: Environment?` — controls the backend base URL.
  Defaults to `Environment.Production` (`https://api.deepdots.com`). Set
  `Environment.Development` to hit `https://api-dev.deepdots.com`.

### Changed

- `SdkRuntime.env` is now derived from `InitOptions.environment` instead of
  `InitOptions.debug`. Combinations like `debug = true` with
  `environment = Environment.Production` now correctly emit SDK logs while
  talking to the production backend.
- Android and iOS demos (`example-android`, `example-android-local`,
  `iosApp/DeepdotsDemo.swift`) updated to set
  `environment = Environment.Development` so they keep pointing at the dev
  backend.
- Documentation (quickstart, server-mode guide) updated for English, Spanish
  and Danish to reflect the new field and the decoupling of `debug` from the
  environment.

### Migration

If you were relying on `debug = true` to hit the dev backend, add the new
field explicitly:

```kotlin
val options = InitOptions(
    debug = true,
    environment = Environment.Development,   // previously implied by debug = true
    mode = Mode.Server,
    popupOptions = PopupOptions(publicKey = "<your-public-key>")
)
```

To talk to production while keeping SDK logs on (previously impossible):

```kotlin
val options = InitOptions(
    debug = true,                            // logs on
    environment = Environment.Production,    // (or omit — it's the default)
    mode = Mode.Server,
    popupOptions = PopupOptions(publicKey = "<your-public-key>")
)
```

### Release follow-ups

- Publish `com.deepdots.sdk:shared-android:0.2.0` to Maven Central.
- Build `DeepdotsSDK-0.2.0.xcframework.zip`, upload it to the `0.2.0` release
  of `MagicFeedback/DeepdotsSDK-SPM`, then bump `spm/Package.swift` to the new
  URL and checksum.

## 0.1.7

- Last release before the `environment` / `debug` decoupling.
- See git history for prior changes.
