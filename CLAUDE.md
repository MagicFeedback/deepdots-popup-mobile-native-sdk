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

- Tests unitarios (JVM): `:shared:testDebugUnitTest` — 296 tests
- Tests comunes en el simulador iOS: `:shared:iosSimulatorArm64Test` — 271 (los que faltan
  viven en `androidUnitTest` porque necesitan `runBlocking` o APIs de Android)
- ⚠️ En un worktree nuevo copia `local.properties` (ignorado, lleva `sdk.dir`) del checkout
  principal; sin él Gradle falla con "SDK location not found".
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
- `analytics/FeedbackPayload.kt` ⇔ `src/analytics/feedback-payload.ts` (builder del body +
  `FeedbackSink`, el transporte, espejo de `createFeedbackSink`).
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
`connection_type`; no hay equivalente a `sendBeacon` (en iOS el lote de cierre va dentro de una
background task, `util/BackgroundTask.kt`, que pide `FeedbackSink`); el default de fuente sin
`font` es per-plataforma.

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
- **Pantalla en curso en el engagement (2026-10-16, paridad con Web #28):** el latido
  `deepdots_user_engagement` lleva `screen`, `path` y `screen_seconds` (`NavigationObserver.current()`),
  los tres o ninguno; el engagement del cierre no (el observador ya emitió el `page_view`). El
  `page_view` lleva `path` (ruta real sin query) y dos ítems de la misma pantalla son visitas
  distintas. `openSession` hace `navObserver.restart()` sobre la última ruta de `setPath()`:
  la pantalla a la que se vuelve del background vuelve a contar. Tests:
  `DeepdotsPopupsEngagementScreenTest`, `NavigationObserverParityTest`, `FeedbackSinkBackgroundTaskTest`.
- **Fiabilidad:** 5xx/408/429 re-encola el lote (techo 200 eventos), 4xx se loguea con status y
  cuerpo y se descarta, y los lotes se serializan mientras no se conozca el `sessionId`. Entrega
  **at-least-once** → el backend debe deduplicar por
  `(deepdots_user_id, nombre_evento, timestamp)`.
- **Pendiente:** persistir el buffer y reenviarlo en el arranque siguiente (en móvil el proceso
  puede congelarse tras el background y el POST no completa; en Web esto lo cubre `sendBeacon`).
  Y sigue el bloqueo de backend del `406 Contact not found` con `user_id` autogenerado.
- **Registro de la sesión (`sdkSessionId`):** `getFeedbackSessionId()` y
  `InitOptions.onFeedbackSession` (`FeedbackSession(sessionId, Open|Closed)`) exponen el
  `sessionId` que devuelve `POST /sdk/feedback`, el que la API guarda como `sdkSessionId` en el
  Feedback de la sesión. Lo cachea `FeedbackSink` (bajo `SdkLock`); un contador de generación
  descarta la respuesta tardía de un primer lote cuya sesión ya se cerró. Un 4xx sale del
  servicio como `RejectedFeedbackException` (antes `null`, indistinguible de un lote aceptado
  sin `sessionId`).
- **Seams de test:** `debugLoadPopups(defs)` (popups sin API), `debugAnalyticsFlushListener`
  (observa cada lote antes del sink), `debugSetPopupsService(service)` (doble del transporte) y
  `debugTransportScope` (p. ej. `Dispatchers.Unconfined`, antes de `init()`: cada flush termina
  de enviar al volver, así los tests del transporte van en `commonTest` sin `runBlocking`).

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
- `ready` (ni `loaded`) no toca métricas: `handleSurveyRuntimeEvent` solo actúa sobre
  `popup_clicked` con `action`, `after_submit` y `survey_completed`, así que no marca PARTIAL.
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

## Feature: el survey no acepta toques mientras carga (2026-09-23)

Reportado por el cliente en React Native y arreglado en las tres rutas. Durante la
transición entre páginas se podía seguir marcando opciones, y la respuesta de esa página
ya se había enviado (el rating numérico **auto-avanza** al marcar), así que el cambio no
viajaba con ella: el usuario creía haber corregido su respuesta y no era así.

- `ui/SurveyBusy.kt` (espejo de `src/ui/busy.ts` en Web) lleva el JS que corre dentro del
  WebView: `pointer-events:none` + `aria-busy` + `inert` donde el motor lo soporte.
- ⚠️ Aquí se bloquea el **`<body>` entero**, no un wrapper: el chrome (cabecera, progreso,
  footer) es nativo y vive FUERA del WebView, dentro solo está el survey.
- ⚠️ **El velo de Compose no protege al survey**: el `CircularProgressIndicator` se pinta
  sobre el popup, pero el WebView es una vista de interop que se dibuja por encima del
  canvas y recibe sus propios toques. Por eso el bloqueo tiene que ir dentro del HTML.
- Se bloquea en `beforeSubmitEvent` y se suelta en `afterSubmitEvent` y `onBackEvent`,
  incluido el camino del error de validación: si no, la pantalla se quedaría muerta.
- Tests: `PopupRevealTest` +1 → 210 JVM + 198 en el simulador iOS.
- ⚠️ Verificado con toques reales en la ruta equivalente (el WebView de RN, con Playwright
  y hit-testing de verdad), **no** en el simulador: el mecanismo es el mismo HTML.
  Ojo al comprobarlo, `element.click()` es sintético e **ignora `pointer-events`**.

## Fix: un survey que llega tarde no se responde con el toque que iba a la X (2026-10-29)

Fallo del iPad (A16) / iOS 18 en la matriz (run 40918433857, `redisplay`), que parecía "la X no
cierra con el teclado abierto". No era eso (verificado en el simulador: con el teclado abierto la X
cierra a la primera). Lo que pasó, según `device.log`: el WebContent tardó 4 s en arrancar, el popup
se reveló por el techo (spinner, tarjeta pequeña) y Maestro leyó la X en y=385; el survey llegó
antes del toque, la tarjeta creció y se recentró (la X pasó a y≈170) y el toque cayó en el WebView
("WebKit: Single tap identified") sobre una opción del NPS, que auto-avanza: página 2 de texto
libre, native la enfoca (`autofocus:'navigation'`) y sale el teclado. El foco de apertura del SDK
(`pointer: fine`) no intervino. El fallback `X_POINT` del subflow está medido en iPhone y en iPad
no cae en la X.

- `SurveyBusy.LATE_SURVEY_GUARD_MS` (600) + `ddGuardLateSurvey`: si el survey carga con el popup ya
  revelado (`ddShownBeforeSurvey`), el `<body>` ignora toques ese rato; no suelta un bloqueo de
  envío en curso (`ddSubmitting`). Test: `PopupRevealTest`.
- ⚠️ **Pendiente de paridad:** Web y RN tienen el mismo camino del techo y no llevan el guard.
- Maestro: `flows/close-with-keyboard.yaml` (página 2 de texto, teclado, X), `redisplay` espera al
  survey antes de cerrar, `close-popup` reintenta la etiqueta. `test-apps-ipad-source.yml`: un solo
  job macOS con el SDK compilado desde la rama en un iPad (A16) / iOS 18.

## Fix: el popup sale en cualquier `Activity` (2026-10-06)

Antes `PopupRenderer.show` (Android) hacía `context.activity as? ComponentActivity ?: return`:
con una `Activity` a secas (la `FlutterActivity` por defecto de Flutter) el popup no salía y no
se logueaba nada. `dismissPopup` tenía el mismo cast.

- Si el decorView ya trae `ViewTreeLifecycleOwner` + `ViewTreeSavedStateRegistryOwner`
  (ComponentActivity tras `setContentView`) se usa lo de siempre, sin tocar nada.
- Si no, `renderer/PopupViewTreeOwner.kt` (lifecycle + saved state + view models) se instala
  en el CONTENEDOR del popup: `RESUMED` al engancharse a la ventana, `DESTROYED` + `ViewModelStore`
  limpio al desengancharse. El detach cubre todas las vías de cierre (acción, `dismissPopup`,
  Activity destruida) sin que cada una tenga que acordarse.
- ⚠️ **No basta con los owners en el contenedor:** el Recomposer "de ventana" de Compose se
  cuelga del decorView (el hijo de `android.R.id.content`, y el contenedor va directo al decor)
  y busca el lifecycle desde ahí → "ViewTreeLifecycleOwner not found". Por eso el contenedor
  lleva además su propio `compositionContext = createLifecycleAwareWindowRecomposer(lifecycle)`.
- Sin dependencias nuevas (lifecycle/savedstate llegan por `activity-compose`). No hay
  Robolectric: el test (`androidUnitTest/.../PopupViewTreeOwnerTest`, 5) cubre las transiciones
  del owner; el montaje real en una `FlutterActivity` queda para verificar en dispositivo.
- La `Activity` a secas no expone su lifecycle, así que el owner NO sigue pause/stop del host:
  queda `RESUMED` mientras el popup está montado.
## El aviso de carga del survey NO es un click (2026-10-06)

Un host Flutter recibía `popup_shown` y justo después `popup_clicked` sin que el usuario tocase
nada (también tras un 406 al cargar el survey). El `onLoadedEvent` del WebView mandaba el estilo,
el idioma y el progreso con el nombre `popup_clicked`, y `handleSurveyRuntimeEvent` reenviaba
todo `popup_clicked` al host como `PopupClicked`.

- **Ahora el aviso viaja como `loaded`** (`PopupReveal.LOADED_EVENT`), un único mensaje con
  `{ style, surveyLang, progress, total }`; se quitó el `emit('loaded')` desnudo que iba detrás.
  `PopupView` lee estilo/idioma/RTL/progreso solo de ese mensaje y el `SurveyView` de Android
  quita su spinner con él. `handleSurveyRuntimeEvent` no tiene rama para `loaded`.
- **Defensa:** un `popup_clicked` del puente sin `action` se ignora (se loguea); un click real
  siempre dice qué se pulsó. `action: "partial"` sigue llegando al host y marcando PARTIAL.
- Los clicks del chrome nativo (`handleAction`: accept/decline/start/complete/back) no pasan por
  el puente y no cambian.
- Tests: `SurveyBridgeEventsTest` (3).
- ⚠️ **Diferencias con Web/RN (sin tocar):** el popup DOM web también emite al host
  `popup_clicked {action:'loaded'}` (y `before_submit`, `validation_error_required`,
  `submit_error`), etiquetado pero igual de engañoso; y el `ReactNativePopupRenderer` convierte
  el `loaded` del WebView en `popup_clicked {action:'partial'}`, o sea que en RN la carga marca
  PARTIAL en backend sin interacción.

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

## Release

Por CI desde 2026-10-05: `node scripts/release.mjs prepare <X.Y.Z|patch|minor|major>` abre la PR
de release y, tras fusionarla, `node scripts/release.mjs tag` sube el tag `vX.Y.Z` (sobre el merge
en `dev`) y sigue `.github/workflows/release.yml`, que corre los tests y publica Maven Central + `DeepdotsSDK-SPM` + la GitHub Release con el AAR. Los
pasos y los secrets están en la cabecera del workflow y en el README (§15). Maven Central se queda
en 0.3.0 hasta que existan los secrets de Sonatype y de firma GPG: sin ellos el workflow se salta
ese paso con un aviso. Los scripts de `scripts/` son el fallback manual y tienen rutas locales
viejas (`deploy_sdk_release.sh` y el `update_version.sh` del repo SPM).

**Gate de release (MF-3242, 2026-10-28), espejo del web (MF-3241):** reglas en
`scripts/release/` (`rules`, `check`, `manifest`, `smoke`, `metadata`; tests con
`npm test --prefix scripts/release`) y política en `docs/VERSIONING.md`.
- Toda PR que toque `shared/src/*Main`, los `.gradle.kts`, `libs.versions.toml` o
  `gradle.properties` necesita notas en `## Unreleased` (check "Version & changelog" de
  `release-check.yml`). La PR de release corre además "Release dry run" (macOS: gate + tests +
  build + smoke + `release.json`), y `release.mjs tag` / `release.yml` exigen que esté en verde.
- `release/compatibility.json` repite versiones del código y el gate las contrasta: al cambiar
  `MAGICFEEDBACK_VERSION`, `POPUP_SDK_CSS_VERSION`, `minSdk`, el mínimo de iOS o la toolchain,
  actualízalo en la misma PR. `bump_version.sh` sube su `version`.
- Al cambiar `InitOptions` o `ServerPopupDto`: `UPDATE_RELEASE_SCHEMA=1 ./gradlew
  :shared:testDebugUnitTest --tests '*ReleaseContractTest*'` regenera `release/config.schema.json`.
- `release.yml` construye el AAR (staging repo de Maven) y el zip del XCFramework UNA vez; esos
  ficheros son los que se publican y se adjuntan. Xcode y Node van fijados en los dos workflows.
