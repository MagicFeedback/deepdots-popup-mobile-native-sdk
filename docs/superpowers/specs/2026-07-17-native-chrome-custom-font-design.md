# Fuente custom en el chrome nativo del popup (KMP) — Diseño

**Fecha:** 2026-07-17
**Rama:** `feat/popup-font-family` (integrada en `dev`)
**Estado:** Aprobado, pendiente de implementar

## Problema

`PopupStyle.font = { family, url? }` (espejo de Web `@magicfeedback/popup-sdk`) ya
llega al **survey (WebView)** vía `@font-face`/`font-family` en `MagicFeedbackHtml`.
Pero el **chrome del popup en KMP es UI nativa de Compose**, no HTML: título,
párrafos del mensaje, los botones (Start/Send/Back/Complete/Decline), el banner de
error, la ✕ de cerrar y el mensaje de completado usan la fuente por defecto de
Compose.

**Gap de paridad:** en Web todo el popup es HTML, así que `style.font` aplica por
CSS a título + botones + survey. En KMP la fuente custom hoy solo se ve en el survey.

La dificultad técnica: Compose necesita **bytes reales de la fuente** para construir
un `FontFamily` (a diferencia de CSS, donde basta el nombre). Como `font.url` es una
fuente **remota arbitraria** (no del catálogo Google Fonts), hay que descargarla,
registrarla por plataforma, cachearla y hacer *swap* async.

## Decisiones tomadas

- **Alcance:** diseñar + implementar ahora, **ambas plataformas** (Android + iOS), con TDD.
- **Enfoque A (elegido):** descarga en código común (Ktor, ya en `commonMain`) +
  `expect/actual` solo para el paso bytes→`FontFamily` + cache. Máxima reutilización
  y testeabilidad. (Rechazados: B = todo por plataforma con Downloadable Fonts/CTFontManager,
  que no sirve para url arbitraria; C = reusar Coil, que es Android-only y de imágenes.)
- **Family-only (sin `url`):** mantener la fuente por defecto. Los catálogos de fuentes
  del sistema en móvil no coinciden con los nombres web; intentar resolver por nombre
  rara vez acierta. Se documenta como desviación menor de paridad. El caso principal
  (custom remota) siempre trae `url`.

## Arquitectura y flujo

```
PopupView(popup, ...)
 └─ LaunchedEffect(popup.style.font):
      customFontFamily = FontLoader.load(popup.style.font)   // async; null hasta cargar
 └─ MaterialTheme(typography = typography.withFontFamily(customFontFamily))
     + CompositionLocalProvider(LocalTextStyle provides ...fontFamily = customFontFamily)
     └─ título, párrafos, botones, banner, ✕, completado  ->  heredan la fuente
```

El swap async es el equivalente a `font-display:swap`: se pinta con la fuente por
defecto y, cuando los bytes cargan, recompone con la custom. La **firma pública de
`PopupView` no cambia** (los call-sites no se tocan): la carga vive dentro del composable.

## Componentes nuevos

### `FontLoader` (commonMain) — orquestador testeable

```
suspend fun load(font: PopupFont?): FontFamily?
```

Guardias (paridad con el path CSS de `Font.kt`):
- `font == null` → `null`.
- `font.url == null` → `null` (family-only usa la fuente por defecto).
- `!isSafeFontUrl(url)` → `null` (reutiliza `Font.kt`; **no** se hace fetch).

Descarga con Ktor `HttpClient`, inyectable como `suspend (String) -> ByteArray?`
para poder testear sin red. Fallo de red / bytes vacíos → `null`.

**Cache** en memoria por `url` (una descarga por sesión; dos popups con la misma url
comparten el `FontFamily`). Cache en disco = fuera de alcance por ahora.

Delega el paso final a `fontFamilyFromBytes`.

### `expect fun fontFamilyFromBytes(family: String, bytes: ByteArray): FontFamily?`

- **Android** (`.android.kt`): bytes → fichero en `cacheDir` → `Typeface.createFromFile`
  → `FontFamily(androidx.compose.ui.text.platform.Typeface(tf))`. Excepción → `null`.
- **iOS** (`.ios.kt`): `FontFamily(Font(identity = family, data = bytes))` (skiko renderiza
  desde bytes; sin `CTFontManager`). Excepción → `null`.

### `Typography.withFontFamily(family: FontFamily?): Typography` (commonMain)

Helper que copia cada estilo de la `Typography` con `fontFamily` aplicado, para que
los Material `Button` (que usan la typography del tema) hereden la fuente. Con
`family == null` devuelve la typography sin cambios.

## Integración en `PopupView`

- `var customFontFamily by remember { mutableStateOf<FontFamily?>(null) }`.
- `LaunchedEffect(popup.style.font) { customFontFamily = FontLoader.load(popup.style.font) }`.
- Envolver el contenido del `Surface` en
  `MaterialTheme(typography = MaterialTheme.typography.withFontFamily(customFontFamily))`
  + `CompositionLocalProvider(LocalTextStyle provides LocalTextStyle.current.copy(fontFamily = customFontFamily))`.
- Con `customFontFamily == null` el render es **idéntico al actual** (cero regresión).

## Manejo de errores

Todo fallo — url insegura, red caída, formato no soportado, excepción al construir el
`Typeface` — resuelve a `null` → fuente por defecto. Nunca crashea ni bloquea el
render. Misma filosofía que `buildFontFaceCss` devolviendo `""`.

## Testing (TDD, commonTest)

Nueva suite `FontLoaderTest` con fakes inyectados (`fetch` + `fontFamilyFromBytes` fake):

1. `font == null` → `null`.
2. `font.url == null` (family-only) → `null`.
3. `url` insegura (`javascript:`, comillas/espacios/control chars) → `null`, **sin** llamar al fetch.
4. fetch OK → `FontFamily` no-null.
5. misma `url` dos veces → **una sola** llamada de fetch (cache).
6. fetch lanza excepción o devuelve `null`/vacío → `null`.

El paso Compose real (bytes→Typeface) se valida en compilación
(`compileKotlinIosSimulatorArm64` + Android build); la lógica pura queda cubierta por
unit tests. Se mantiene la paridad con Web reutilizando `isSafeFontUrl`/`fontFormatFromUrl`
de `Font.kt` (ya cubiertos por `FontHtmlParityTest`).

## Fuera de alcance (YAGNI)

- Cache en disco de la fuente descargada.
- Family-only → resolución a fuente del sistema por nombre.
- Precarga de la fuente antes de mostrar el popup.
- Múltiples pesos/estilos (solo Regular; la API entrega una única `url`).
