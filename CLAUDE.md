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

## Feature: apertura sin spinner (`PopupReveal`)

Estado: **implementada**, paridad con Web/RN (2026-09-14).

El popup ya no se enseña con el spinner girando mientras carga el survey: se monta invisible y
se enseña cuando está pintado. En móvil la espera es mayor que en web, porque al `GET .../info`
del survey (unos 250 ms de mediana, 775 ms con el backend frío) se le suman el arranque del
WebView y la descarga del bundle del CDN.

- **Contrato compartido:** `ui/PopupReveal.kt` — `REVEAL_TIMEOUT_MS = 1200` (mismo valor que
  `src/ui/reveal.ts` en Web; hay un test que lo fija), `READY_EVENT = "ready"` y `REVEAL_JS`,
  el algoritmo ES5 que espera a las imágenes del survey, espejo de `revealWhenPainted` (DOM web)
  y del `REVEAL_JS` de `surveyHtml.ts` (WebView de RN).
- **WebView:** `MagicFeedbackHtml` instala `ddCreateReveal(document, …)` y llama
  `ddReveal.whenPainted()` en el `onLoadedEvent`; al revelar emite `ready` por el puente. Los
  emojis del rating son SVG y llegan DESPUÉS del `loaded`, de ahí la espera por imágenes.
- **Chrome (Compose):** `PopupView` tiene `revealed` + `onReady`, pinta el Box raíz con
  `alpha(0f)` hasta la revelación (invisible, NO "sin componer": el WebView tiene que estar
  montado para cargar) y arma su propio techo con `LaunchedEffect { delay(REVEAL_TIMEOUT_MS) }`.
  El scrim tampoco se pinta antes de tiempo.
- **Ocultación nativa, no solo Compose:** en Android el contenedor se añade al decorView como
  `View.INVISIBLE` y pasa a `VISIBLE` en `onReady` — mantiene el layout (el WebView carga) pero
  no se pinta ni recibe toques, así que el usuario sigue usando la app mientras espera. En iOS la
  vista del controlador se presenta con `alpha = 0` + `setUserInteractionEnabled(false)`. ⚠️ El
  `alpha` de Compose NO basta en iOS: el survey es un `WKWebView` metido con `UIKitView`, y los
  modificadores de dibujo de Compose no se aplican de forma fiable a las vistas de interop; el
  alpha de `UIView` sí baja por toda la jerarquía.
- **⚠️ Cambio de presentación en iOS (necesario para lo anterior):** el popup se presentaba con el
  estilo modal por defecto, que en iOS 13+ es una **sheet**, y el chrome gris que dibuja UIKit
  para la sheet se ve SIEMPRE, aunque la vista vaya a alpha 0 — durante la espera se veía un
  panel gris vacío, peor que el spinner que se quería quitar (observado en el simulador). Ahora
  se presenta con `UIModalPresentationOverFullScreen` + `view.backgroundColor = clear` +
  `ComposeUIViewController(configure = { opaque = false })`, así que la espera es realmente
  invisible y, de paso, el popup se ve como en Android y en web (scrim + tarjeta) en lugar de
  como una hoja que no cubre la pantalla. `opaque` es API experimental: hace falta
  `@file:OptIn(ExperimentalComposeApi::class, ExperimentalComposeUiApi::class)`, y el opt-in debe
  ir a nivel de FICHERO porque tiene que cubrir la lambda `configure`, no solo la función.
- Diferir la presentación entera no se puede: sin presentar el VC, el WebView no se monta ni
  carga, y el aviso no llegaría nunca (mismo motivo por el que en React Native no vale
  `<Modal visible={ready}>`).
- `ready` no toca métricas: `handleSurveyRuntimeEvent` solo actúa sobre `popup_clicked`,
  `after_submit` y `survey_completed`, así que no marca PARTIAL.
- Tests: `PopupRevealTest` (5, commonTest) → 192 JVM + 180 en el simulador iOS, 0 fallos.
- **Verificado en el simulador iOS** (iPhone 17 Pro, capturas en bucle con `xcrun simctl io …
  screenshot`): el frame anterior a la apertura muestra la pantalla de la app limpia (sin velo,
  sin panel gris, sin spinner) y el siguiente ya trae el popup entero (logo + barra + pregunta +
  botón). También se observó el camino del techo: cuando el survey tarda más de 1200 ms, el popup
  se abre con el spinner, que es el comportamiento de siempre.
- ⚠️ **El techo se queda corto en la PRIMERA apertura en frío:** el WebView tiene que traerse el
  bundle de `@magicfeedback/native` del CDN (148 KB, medido en 780 ms en frío y 110 ms en
  caliente) antes de pedir el survey, así que la primera vez suele vencer el techo y abrirse con
  spinner; a partir de la segunda (bundle en la caché del WebView) se abre limpia. El equivalente
  móvil del "precalentado del chunk" que hace el SDK Web (traer el bundle a la caché antes del
  primer popup) queda **pendiente**.
- ⚠️ Para probar a mano: el backend deja de devolver un popup ya visto mientras dure su cooldown
  (1 día en la cuenta del demo), así que no se puede repetir la prueba sin cambiar de popup o de
  cuenta; borrar `Library/Preferences/<bundle>.plist` del contenedor solo limpia el cooldown
  local, no el del backend.

## Feature: el popup aprovecha su espacio (2026-09-14)

Reporte del cliente sobre una captura de iOS: el popup desperdiciaba espacio. Eran dos cosas
distintas, las dos en la ruta del WebView.

- **A lo ancho:** el logo y la barra de progreso llegan al borde de la tarjeta (16dp), pero el
  contenido del survey quedaba ~38px más adentro, porque el CSS del paquete mete padding lateral
  en tres capas: `.magicfeedback-container` (12px) + `.magicfeedback-form` (14px) en el breakpoint
  móvil + `.magicfeedback-div` (12px, el bloque de cada pregunta, con fondo blanco sobre tarjeta
  blanca: invisible, solo desalinea). `MagicFeedbackHtml` los anula con un `<style>` **después**
  del `<link>` del CDN (a igualdad de especificidad gana el último; las `@media` no suman
  especificidad). Solo el lateral: el vertical es el que separa las preguntas.
- **A lo alto:** un WebView no tiene tamaño propio, así que el área del survey se estiraba hasta
  el máximo y una sola pregunta dejaba un hueco enorme hasta el footer. Ahora el HTML mide
  `#mf-form` (no el `body`, que va a `height:100%` y siempre devuelve el alto del WebView) y lo
  manda por el puente como `content_height`; `PopupView` dimensiona el área con
  `PopupReveal.resolveSurveyHeightDp` (función pura, testeada), entre un mínimo de 120dp y el 72%
  de la tarjeta — por encima de ese techo el WebView hace su propio scroll. Un `ResizeObserver`
  sobre `#mf-form` cubre carga, cambio de página, follow-ups y avisos de validación sin tener que
  llamarlo en cada evento, y la altura se reenvía justo antes de `ready` para que el popup no
  aparezca y luego dé un salto.
- Tests: `PopupRevealTest` 9 (+4). Verificado en el simulador con el survey de producción.
- **Web y RN llevan las mismas reglas** desde el mismo día (`renderPopup.ts` con un bloque propio
  entre el CSS del paquete y el del host, `surveyHtml.ts` dentro de su `<style>`). ⚠️ Allí van
  **acotadas a `.deepdots-popup`** porque en web la hoja se inyecta en el `<head>` del host; aquí
  no hace falta, el CSS vive aislado en el WebView.

## Feature: color de marca dentro del survey (2026-09-14)

El chrome de Compose ya usaba `buttonPrimaryColor`, pero el survey del WebView pinta sus
controles (chips del rating, bordes, foco, selección) con `--mf-primary`, que
`@magicfeedback/native` rellena **solo** desde `formData.style.primaryColor`. Las
integraciones configuran el color del botón y suelen dejar el primario vacío, así que el
popup mezclaba el color de la marca con el `#5d7bad` por defecto del paquete.

- `ui/SurveyPalette.kt` (espejo de `src/ui/surveyPalette.ts` en Web) lleva el JS que corre
  dentro del WebView: sin `primaryColor`, usa `buttonPrimaryColor`.
- ⚠️ Escribe **las cinco** variables que pone `applyPrimaryColor` del Surveys SDK, no solo
  `--mf-primary`: un `color-mix` declarado en `:root` ya se resolvió allí contra el color por
  defecto y heredaría ese valor ya calculado.
- ⚠️ Solo acepta **hex**: el estilo viene de la API y se interpola en CSS.
- El botón de Material3 (píldora) es ahora el canónico: web y RN se han alineado con él.
- Verificado en el simulador: los chips pasan del gris azulado al azul de la marca.

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
