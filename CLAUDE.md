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

- Tests unitarios: `./gradlew :shared:testDebugUnitTest`
- Compilar iOS: `./gradlew :shared:compileKotlinIosSimulatorArm64`
- Compilar Android: `./gradlew :shared:compileDebugKotlinAndroid`

## Convención clave: PARIDAD CON EL SDK WEB

Este SDK es un port del SDK Web `@magicfeedback/popup-sdk`. Varias piezas son **espejo
exacto** de ficheros del repo Web y cualquier cambio debe replicarse en ambos lados:

- `ui/Font.kt` ⇔ `src/ui/font.ts` (saneado anti-inyección de `family`/`url`).
- `analytics/Language.kt` ⇔ `src/analytics/language.ts` (`resolveLanguage`: explicit > device > null).

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

## Feature: idioma del context de analytics (auto-detección)

Estado: **implementada**, ambas plataformas. Espejo del Web `src/analytics/language.ts`.

- `analytics/Language.kt` (commonMain): `resolveLanguage(explicit, deviceLanguage)` — función
  pura con prioridad **`explicit` (InitOptions.provideLang) > `deviceLanguage` (locale del
  dispositivo) > `null`**; hace `trim` y descarta cadenas en blanco (mirror del `clean()` Web).
- `expect fun deviceLanguage(): String?` con `actual`s: Android
  `Locale.getDefault().toLanguageTag()`; iOS `NSLocale.preferredLanguages.first` (ambos dan
  BCP-47 tipo `"es-ES"`). Cualquier fallo → `null` (el campo se omite del metadata).
- Cableado en `DeepdotsPopups.init()`: `AnalyticsManager(language = resolveLanguage(
  options.provideLang.invoke(), deviceLanguage()))`. `provideLang` sigue siendo el override
  explícito de máxima prioridad; el fallback al device es nuevo.
- Tests (paridad con `language.test.ts`): `analytics/LanguageParityTest.kt` (`deviceLanguage`
  inyectado/fakeado para tests puros).

## Ramas

- `main` — base.
- `dev` — rama de integración (espejo del `dev` del repo Web).
- `feat/popup-font-family` — desarrollo de la feature de font.
