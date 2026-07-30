# CLAUDE.md — Deepdots Popup SDK (KMP)

Guía para trabajar en este repo. SDK de popups/encuestas Kotlin Multiplatform (Android + iOS)
con UI en Compose Multiplatform y red con Ktor.

## Estructura

- `shared/` — módulo KMP con toda la lógica del SDK.
  - `src/commonMain/kotlin/com/deepdots/sdk/`
    - `ui/` — UI Compose: `PopupView.kt` (chrome nativo del popup), `SurveyView.kt`
      (`expect`, la encuesta va en un WebView), `MagicFeedbackHtml.kt` (genera el HTML
      del WebView), `Font.kt` (saneado/validación de fuentes), `FontLoader.kt`,
      `Typography.kt`, `ImageLoader.kt`.
    - `models/` — modelos (`actions.kt` con `PopupDefinition`, `PopupStyle`, `PopupFont`, `Action`, `Theme`…).
    - `service/` — `PopupsService.kt` (llamadas a la API con Ktor).
    - `analytics/`, `tracking/`, `storage/`, `i18n/`, `util/`.
  - `src/androidMain/`, `src/iosMain/` — `actual`s por plataforma.
  - `src/commonTest/` — tests con `kotlin.test` (+ `kotlinx.coroutines.runBlocking` para suspend).

## Comandos

⚠️ **No hay JDK en el PATH** en la máquina de desarrollo (`java` es el stub de macOS). Usa el
JBR embebido de Android Studio, prefijando todas las tareas:

```
JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home" ./gradlew <tarea>
```

- Tests unitarios (JVM): `:shared:testDebugUnitTest` — 158 tests
- Tests comunes en el simulador iOS: `:shared:iosSimulatorArm64Test` — 146 (los 12 que faltan
  viven en `androidUnitTest` porque necesitan `runBlocking`)
- Compilar iOS: `:shared:compileKotlinIosSimulatorArm64` · `:shared:compileKotlinIosArm64`
- Compilar/ensamblar Android: `:shared:compileDebugKotlinAndroid` · `:shared:assembleDebug`

## Convención clave: PARIDAD CON EL SDK WEB

Este SDK es un port del SDK Web `@magicfeedback/popup-sdk`. Varias piezas son **espejo
exacto** de ficheros del repo Web y cualquier cambio debe replicarse en ambos lados:

- `ui/Font.kt` ⇔ `src/ui/font.ts` (saneado anti-inyección de `family`/`url`).
- `tracking/TrackingManager.kt` ⇔ `src/tracking/tracking-manager.ts` (+ `buildSurveyIdentity`).
- `tracking/NavigationObserver.kt` ⇔ `src/tracking/navigation-observer.ts` (aquí sin hooks de
  History: la navegación entra por `setPath()`).
- `analytics/AnalyticsManager.kt` ⇔ `src/analytics/analytics-manager.ts`.
- `analytics/FeedbackPayload.kt` ⇔ `src/analytics/feedback-payload.ts`.
- `analytics/EngagementTracker.kt` ⇔ `src/analytics/engagement-tracker.ts`.
- `analytics/DeviceInfo.kt` ⇔ `src/analytics/device-info.ts` · `analytics/GeoInfo.kt` ⇔ `geo-info.ts`.
- `analytics/Language.kt` ⇔ `src/analytics/language.ts` · `analytics/Messaging.kt` ⇔ `messaging.ts`.
- `analytics/CrashReporter.kt` ⇔ `src/analytics/crash-reporter.ts`.
- `contact/ContactManager.kt` ⇔ `src/contact/contact-manager.ts`.
- Los tests `*ParityTest` son el espejo del `.test.ts` correspondiente: si añades un caso en un
  lado, añádelo en el otro.

**Divergencias intencionadas** (no son deuda): `util/SdkLock.kt` no existe en Web (JS es
single-thread; aquí el buffer de analytics se toca desde el hilo del host y desde la corrutina
de envío); `DeviceInfo` rellena `os_version`/`device_model` con APIs nativas en vez de mandar un
`user_agent` para que lo parsee el backend, y deja vacíos `referrer`/`entry_type`/`page_load_ms`/
`connection_type`; no hay equivalente a `sendBeacon`; el default de fuente sin `font` es
per-plataforma.

## Tracking y analytics

Estado: **capa completa y al nivel del Web** (reconstruida el 2026-07-30 — el trabajo anterior
nunca llegó a Git y `dev` no compilaba; ver el commit `6f93dc2`).

- **Identidad:** `user_id` propio y **persistente** (`InitOptions.storage = null` →
  `createDefaultStorage()`: SharedPreferences `deepdots_sdk` con el `applicationContext` que
  autocaptura `DeepdotsInitProvider` / `NSUserDefaults`). El `session_id` lo da el BACKEND en la
  respuesta de `POST /sdk/popups` y `/sdk/feedback`; el SDK solo lo cachea.
- **Canal de analytics:** eventos GA-style → `POST /sdk/feedback` como Feedback de la integración
  (`InitOptions.analytics = AnalyticsKeys(publicKey, integration)`). Sin esas claves queda en
  **dry-run** (imprime el payload). Los eventos van en `feedback.metadata`; `setMetric` va al
  campo dedicado `feedback.metrics`.
- **Fin de sesión:** el último lote va con **`completed:true`** + evento `deepdots_session_end`
  (`SessionEndReason`), y los `sessionId` se olvidan para que el siguiente abra registro nuevo.
  Lo disparan `onBackground()`, `setUserId()`, `setTrackingEnabled(false)` y `endSession()`.
  ⚠️ **`onBackground()` es fin de sesión** → llámalo en `onStop`/`didEnterBackground`, NUNCA en
  `onPause`/`willResignActive` (en iOS `inactive` es transitorio y partiría la sesión en dos).
  `onForeground()` abre una nueva.
- **Fiabilidad:** 5xx/408/429 re-encola el lote (techo 200 eventos), 4xx se loguea con status y
  cuerpo y se descarta, y los lotes se serializan mientras no se conozca el `sessionId`. Entrega
  **at-least-once** → el backend debe deduplicar por
  `(deepdots_user_id, nombre_evento, timestamp)`.
- **Pendiente:** persistir el buffer y reenviarlo en el arranque siguiente (en móvil el proceso
  puede congelarse tras el background y el POST no completa; en Web esto lo cubre `sendBeacon`).
  Y sigue el bloqueo de backend del `406 Contact not found` con `user_id` autogenerado.
- **Seams de test:** `debugLoadPopups(defs)` (popups sin API), `debugAnalyticsFlushListener`
  (observa cada lote antes del sink) y `debugSetPopupsService(service)` (doble del transporte).

La encuesta en KMP se renderiza en un **WebView** (`SurveyView` + `MagicFeedbackHtml`),
mientras que el **chrome del popup (título, mensaje, botones, ✕, banner, completado) es
UI nativa de Compose** (`PopupView`) — no HTML. Al portar features de estilo del Web hay
que cubrir **ambos** caminos (CSS del WebView + tema nativo Compose).

## Feature: fuente custom (`PopupStyle.font = { family, url? }`)

Estado: **implementada en survey + chrome nativo**, ambas plataformas.

- Contrato (espejo del Web): `family` = nombre saneado (whitelist `[A-Za-z0-9 ._-]`) +
  fallback `-apple-system, system-ui, sans-serif`; `url` opcional (woff2/ttf/otf), valida
  `http(s):`/`data:` sin comillas/`<>`/`\`/whitespace/control chars → `@font-face`.
- **Survey (WebView):** `Font.kt` genera `@font-face`/`font-family`, inyectados por
  `MagicFeedbackHtml`/`SurveyView`.
- **Chrome nativo (Compose):** `FontLoader` descarga los bytes con Ktor (reutilizando
  `isSafeFontUrl` de `Font.kt` como guardia) y los cachea por url; `fontFamilyFromBytes`
  (`expect`/`actual`: Android `Typeface.createFromFile`; iOS/skiko `Font(identity, data)`)
  los convierte en `FontFamily`; `PopupView` los carga en un `LaunchedEffect` y los aplica
  vía `MaterialTheme(typography = …withFontFamily(...))` + `LocalTextStyle`. Cualquier fallo
  (url insegura, red, formato, etc.) → `null` → fuente por defecto (cero regresión). Swap
  async = equivalente a `font-display:swap`.
- **Fuera de alcance actual:** cache en disco, family-only→fuente del sistema por nombre,
  precarga antes de mostrar, múltiples pesos/estilos.
- Diseño y plan: `docs/superpowers/specs/2026-07-17-native-chrome-custom-font-design.md`
  y `docs/superpowers/plans/2026-07-17-native-chrome-custom-font.md`.

## Ramas

- `main` — base.
- `dev` — rama de integración (espejo del `dev` del repo Web). Se trabaja aquí.
- `feat/popup-font-family` — desarrollo de la feature de font.

## Lecciones (2026-07-30)

- **Lo que no está commiteado, no existe.** Este repo llegó a `dev` con el cableado de
  tracking/analytics pero sin los ficheros que lo implementaban: no compilaba y hubo que
  reescribir la capa entera desde el Web. Commitea antes de cerrar sesión, aunque quede a medias.
- **`PopupOptions.popups` es un resto que nada lee**: los popups vienen SIEMPRE de la API (igual
  que en Web desde el 2026-06-19). Los tests inyectan por `debugLoadPopups`.
- **Cuidado con `binaries.all { name }`** en `shared/build.gradle.kts`: ahí `name` es el del
  BINARIO, no el del target. Por eso `-mios-version-min` se colaba en el simulador y `ld` fallaba
  (arreglado en `3ba8377`).
