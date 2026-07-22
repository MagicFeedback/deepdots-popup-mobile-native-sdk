# Fuente custom en el chrome nativo del popup (KMP) — Plan de implementación

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Aplicar la fuente custom (`PopupStyle.font = {family, url?}`) al chrome nativo Compose del popup (título, párrafos, botones, banner, ✕, completado), no solo al survey WebView, cerrando el gap de paridad con Web.

**Architecture:** Descarga de la fuente en `commonMain` con Ktor (reutilizando `isSafeFontUrl` de `Font.kt` como guardia), un `FontLoader` cacheado y testeable, y un `expect fun fontFamilyFromBytes` que convierte bytes→`FontFamily` por plataforma (Android: `Typeface.createFromFile`; iOS/skiko: `Font(identity, data)`). `PopupView` carga la fuente en un `LaunchedEffect` y la aplica vía `MaterialTheme(typography=…)` + `LocalTextStyle`. Fallo en cualquier punto → `null` → fuente por defecto (cero regresión).

**Tech Stack:** Kotlin Multiplatform, Compose Multiplatform 1.9.1, Ktor client 2.3.11, kotlinx-coroutines, kotlin.test.

**Ramas / aislamiento:** Trabajar en `feat/popup-font-family` (working copy principal `/Users/sarias/AndroidStudioProjects/DeepdotsPopupSDK`). El repo tiene un gran pile sin commitear no relacionado — **cada `git add` debe listar SOLO los ficheros de la tarea; nunca `git add -A`/`git add .`**.

---

## Estructura de ficheros

- **Crear** `shared/src/commonMain/kotlin/com/deepdots/sdk/ui/FontFamilyFromBytes.kt` — `expect fun fontFamilyFromBytes`.
- **Crear** `shared/src/androidMain/kotlin/com/deepdots/sdk/ui/FontFamilyFromBytes.android.kt` — actual Android.
- **Crear** `shared/src/iosMain/kotlin/com/deepdots/sdk/ui/FontFamilyFromBytes.ios.kt` — actual iOS.
- **Crear** `shared/src/commonMain/kotlin/com/deepdots/sdk/ui/FontLoader.kt` — orquestador + fetch Ktor + instancia compartida.
- **Modificar** `shared/src/commonMain/kotlin/com/deepdots/sdk/ui/Font.kt` — subir visibilidad de `isSafeFontUrl` a `internal`.
- **Crear** `shared/src/commonMain/kotlin/com/deepdots/sdk/ui/Typography.kt` — helper `Typography.withFontFamily`.
- **Modificar** `shared/src/commonMain/kotlin/com/deepdots/sdk/ui/PopupView.kt` — carga + theming.
- **Crear** `shared/src/commonTest/kotlin/com/deepdots/sdk/ui/FontLoaderTest.kt` — tests unitarios.

---

## Task 1: `fontFamilyFromBytes` (expect + actuals)

**Files:**
- Create: `shared/src/commonMain/kotlin/com/deepdots/sdk/ui/FontFamilyFromBytes.kt`
- Create: `shared/src/androidMain/kotlin/com/deepdots/sdk/ui/FontFamilyFromBytes.android.kt`
- Create: `shared/src/iosMain/kotlin/com/deepdots/sdk/ui/FontFamilyFromBytes.ios.kt`

Es código de plataforma/UI: se valida por **compilación**, no por unit test. `FontLoader` (Task 2) lo consume vía referencia inyectable, así que su lógica sí se testea con un fake.

- [ ] **Step 1: Crear el `expect` en commonMain**

`shared/src/commonMain/kotlin/com/deepdots/sdk/ui/FontFamilyFromBytes.kt`:
```kotlin
package com.deepdots.sdk.ui

import androidx.compose.ui.text.font.FontFamily

/**
 * Convierte los bytes de una fuente descargada en un [FontFamily] de Compose.
 * Devuelve null si el formato no es válido o la construcción falla (el caller
 * cae entonces a la fuente por defecto). El paso equivalente en Web es que el
 * navegador cargue el @font-face; aquí hay que materializar los bytes por plataforma.
 */
expect fun fontFamilyFromBytes(family: String, bytes: ByteArray): FontFamily?
```

- [ ] **Step 2: Crear el actual de Android**

`shared/src/androidMain/kotlin/com/deepdots/sdk/ui/FontFamilyFromBytes.android.kt`:
```kotlin
package com.deepdots.sdk.ui

import android.graphics.Typeface as AndroidTypeface
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.platform.Typeface
import java.io.File

// Compose en Android necesita un Typeface; Typeface.createFromFile requiere un fichero.
// File.createTempFile usa java.io.tmpdir, así que no hace falta un Context.
actual fun fontFamilyFromBytes(family: String, bytes: ByteArray): FontFamily? = try {
    val file = File.createTempFile("deepdots-font-", ".ttf")
    file.deleteOnExit()
    file.writeBytes(bytes)
    val tf = AndroidTypeface.createFromFile(file)
    FontFamily(Typeface(tf))
} catch (t: Throwable) {
    null
}
```

- [ ] **Step 3: Crear el actual de iOS**

`shared/src/iosMain/kotlin/com/deepdots/sdk/ui/FontFamilyFromBytes.ios.kt`:
```kotlin
package com.deepdots.sdk.ui

import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.platform.Font

// En targets skiko (iOS) Compose puede construir una Font directamente desde bytes.
actual fun fontFamilyFromBytes(family: String, bytes: ByteArray): FontFamily? = try {
    FontFamily(
        Font(
            identity = family,
            data = bytes,
            weight = FontWeight.Normal,
            style = FontStyle.Normal,
        )
    )
} catch (t: Throwable) {
    null
}
```

> Nota: la sobrecarga usada es `androidx.compose.ui.text.platform.Font(identity, data, weight, style)`. Si el compilador reporta ambigüedad, mantener solo ese import.

- [ ] **Step 4: Compilar Android + iOS**

Run:
```bash
cd /Users/sarias/AndroidStudioProjects/DeepdotsPopupSDK && ./gradlew :shared:compileDebugKotlinAndroid :shared:compileKotlinIosSimulatorArm64 --console=plain
```
Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 5: Commit (solo estos 3 ficheros)**

```bash
cd /Users/sarias/AndroidStudioProjects/DeepdotsPopupSDK
git add shared/src/commonMain/kotlin/com/deepdots/sdk/ui/FontFamilyFromBytes.kt \
        shared/src/androidMain/kotlin/com/deepdots/sdk/ui/FontFamilyFromBytes.android.kt \
        shared/src/iosMain/kotlin/com/deepdots/sdk/ui/FontFamilyFromBytes.ios.kt
git commit -m "feat(font): bytes->FontFamily per platform (Android/iOS)

Co-Authored-By: Claude Opus 4.8 <noreply@anthropic.com>"
```

---

## Task 2: `FontLoader` con TDD

**Files:**
- Modify: `shared/src/commonMain/kotlin/com/deepdots/sdk/ui/Font.kt` (visibilidad de `isSafeFontUrl`)
- Create: `shared/src/commonTest/kotlin/com/deepdots/sdk/ui/FontLoaderTest.kt`
- Create: `shared/src/commonMain/kotlin/com/deepdots/sdk/ui/FontLoader.kt`

- [ ] **Step 1: Subir visibilidad de `isSafeFontUrl` a `internal`**

En `shared/src/commonMain/kotlin/com/deepdots/sdk/ui/Font.kt`, cambiar la línea:
```kotlin
private fun isSafeFontUrl(url: String): Boolean =
```
por:
```kotlin
internal fun isSafeFontUrl(url: String): Boolean =
```
(No cambia la lógica; solo permite reutilizarla desde `FontLoader`. Paridad con Web intacta.)

- [ ] **Step 2: Escribir el test que falla**

`shared/src/commonTest/kotlin/com/deepdots/sdk/ui/FontLoaderTest.kt`:
```kotlin
package com.deepdots.sdk.ui

import androidx.compose.ui.text.font.FontFamily
import com.deepdots.sdk.models.PopupFont
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertSame

class FontLoaderTest {

    // FontFamily fake no-null para las aserciones (no construimos un typeface real en unit test).
    private val fakeFamily: FontFamily = FontFamily.Monospace

    private class Counter { var calls = 0 }

    private fun loader(
        counter: Counter,
        bytes: ByteArray? = byteArrayOf(1, 2, 3),
        throwOnFetch: Boolean = false,
        build: (String, ByteArray) -> FontFamily? = { _, _ -> FontFamily.Monospace },
    ) = FontLoader(
        fetch = { _ ->
            counter.calls++
            if (throwOnFetch) throw RuntimeException("boom")
            bytes
        },
        buildFamily = build,
    )

    @Test
    fun null_font_returns_null_without_fetch() = runBlocking {
        val c = Counter()
        assertNull(loader(c).load(null))
        assertEquals(0, c.calls)
    }

    @Test
    fun family_only_no_url_returns_null_without_fetch() = runBlocking {
        val c = Counter()
        assertNull(loader(c).load(PopupFont(family = "Inter", url = null)))
        assertEquals(0, c.calls)
    }

    @Test
    fun unsafe_url_returns_null_without_fetch() = runBlocking {
        val c = Counter()
        val font = PopupFont(family = "Inter", url = "javascript:alert(1)")
        assertNull(loader(c).load(font))
        assertEquals(0, c.calls)
    }

    @Test
    fun safe_url_builds_family() = runBlocking {
        val c = Counter()
        val font = PopupFont(family = "Inter", url = "https://cdn.example.com/inter.woff2")
        assertSame(fakeFamily, loader(c).load(font))
        assertEquals(1, c.calls)
    }

    @Test
    fun same_url_fetched_once_thanks_to_cache() = runBlocking {
        val c = Counter()
        val l = loader(c)
        val font = PopupFont(family = "Inter", url = "https://cdn.example.com/inter.woff2")
        l.load(font)
        l.load(font)
        assertEquals(1, c.calls)
    }

    @Test
    fun fetch_failure_returns_null() = runBlocking {
        val c = Counter()
        val font = PopupFont(family = "Inter", url = "https://cdn.example.com/inter.woff2")
        assertNull(loader(c, throwOnFetch = true).load(font))
    }

    @Test
    fun empty_bytes_returns_null() = runBlocking {
        val c = Counter()
        val font = PopupFont(family = "Inter", url = "https://cdn.example.com/inter.woff2")
        assertNull(loader(c, bytes = byteArrayOf()).load(font))
    }

    @Test
    fun null_bytes_returns_null() = runBlocking {
        val c = Counter()
        val font = PopupFont(family = "Inter", url = "https://cdn.example.com/inter.woff2")
        assertNull(loader(c, bytes = null).load(font))
    }
}
```

- [ ] **Step 3: Ejecutar el test y verificar que falla a compilar (no existe `FontLoader`)**

Run:
```bash
cd /Users/sarias/AndroidStudioProjects/DeepdotsPopupSDK && ./gradlew :shared:testDebugUnitTest --tests "com.deepdots.sdk.ui.FontLoaderTest" --console=plain
```
Expected: FAIL — error de compilación "unresolved reference: FontLoader".

- [ ] **Step 4: Implementar `FontLoader` (mínimo para pasar los tests)**

`shared/src/commonMain/kotlin/com/deepdots/sdk/ui/FontLoader.kt`:
```kotlin
package com.deepdots.sdk.ui

import androidx.compose.ui.text.font.FontFamily
import com.deepdots.sdk.models.PopupFont
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Descarga y cachea una fuente remota como [FontFamily] de Compose para el chrome
 * nativo del popup. Espejo funcional del @font-face del WebView (ver Font.kt):
 * reutiliza [isSafeFontUrl] como guardia y cae a null (fuente por defecto) ante
 * cualquier fallo. Family-only (sin url) => null (usa la fuente por defecto).
 *
 * [fetch] y [buildFamily] son inyectables para poder testear sin red ni Typeface real.
 */
class FontLoader(
    private val fetch: suspend (String) -> ByteArray?,
    private val buildFamily: (String, ByteArray) -> FontFamily? = ::fontFamilyFromBytes,
) {
    private val cache = mutableMapOf<String, FontFamily>()
    private val mutex = Mutex()

    suspend fun load(font: PopupFont?): FontFamily? {
        val url = font?.url ?: return null
        if (!isSafeFontUrl(url)) return null
        mutex.withLock {
            cache[url]?.let { return it }
            val bytes = try { fetch(url) } catch (t: Throwable) { null }
            if (bytes == null || bytes.isEmpty()) return null
            val family = buildFamily(font.family, bytes) ?: return null
            cache[url] = family
            return family
        }
    }
}
```

- [ ] **Step 5: Ejecutar los tests y verificar que pasan**

Run:
```bash
cd /Users/sarias/AndroidStudioProjects/DeepdotsPopupSDK && ./gradlew :shared:testDebugUnitTest --tests "com.deepdots.sdk.ui.FontLoaderTest" --console=plain
```
Expected: PASS (8 tests verdes).

- [ ] **Step 6: Commit (solo estos 3 ficheros)**

```bash
cd /Users/sarias/AndroidStudioProjects/DeepdotsPopupSDK
git add shared/src/commonMain/kotlin/com/deepdots/sdk/ui/Font.kt \
        shared/src/commonMain/kotlin/com/deepdots/sdk/ui/FontLoader.kt \
        shared/src/commonTest/kotlin/com/deepdots/sdk/ui/FontLoaderTest.kt
git commit -m "feat(font): FontLoader with cache + safe-url guard (TDD)

Co-Authored-By: Claude Opus 4.8 <noreply@anthropic.com>"
```

---

## Task 3: Fetch real con Ktor + instancia compartida

**Files:**
- Modify: `shared/src/commonMain/kotlin/com/deepdots/sdk/ui/FontLoader.kt`

Código de red: se valida por compilación (no unit test). Añade el fetch real y una instancia de proceso (cache compartida durante la sesión).

- [ ] **Step 1: Añadir el fetch Ktor y `SharedFontLoader` al final de `FontLoader.kt`**

Añadir estos imports arriba en `FontLoader.kt`:
```kotlin
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.readBytes
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
```

Añadir al final del fichero (nivel superior):
```kotlin
private val fontHttpClient by lazy { HttpClient() }

/** Descarga los bytes de la fuente; null ante cualquier fallo de red. */
internal suspend fun ktorFetchFontBytes(url: String): ByteArray? = try {
    withContext(Dispatchers.Default) { fontHttpClient.get(url).readBytes() }
} catch (t: Throwable) {
    null
}

/**
 * Instancia de proceso: la cache vive durante la sesión, así que popups repetidos
 * con la misma url comparten la fuente ya descargada.
 */
val SharedFontLoader: FontLoader = FontLoader(fetch = ::ktorFetchFontBytes)
```

- [ ] **Step 2: Compilar Android + iOS**

Run:
```bash
cd /Users/sarias/AndroidStudioProjects/DeepdotsPopupSDK && ./gradlew :shared:compileDebugKotlinAndroid :shared:compileKotlinIosSimulatorArm64 --console=plain
```
Expected: `BUILD SUCCESSFUL`.

> Si `readBytes()` aparece como no resuelto en Ktor 2.3.11, usar `import io.ktor.client.call.body` y `get(url).body<ByteArray>()` en su lugar.

- [ ] **Step 3: Commit (solo FontLoader.kt)**

```bash
cd /Users/sarias/AndroidStudioProjects/DeepdotsPopupSDK
git add shared/src/commonMain/kotlin/com/deepdots/sdk/ui/FontLoader.kt
git commit -m "feat(font): Ktor byte fetch + shared session-cached FontLoader

Co-Authored-By: Claude Opus 4.8 <noreply@anthropic.com>"
```

---

## Task 4: Helper `Typography.withFontFamily`

**Files:**
- Create: `shared/src/commonMain/kotlin/com/deepdots/sdk/ui/Typography.kt`

Mapper mecánico (copia cada estilo con la familia). Se valida por compilación + su uso en `PopupView` (Task 5). Los Material `Button` toman la fuente de la `Typography` del tema, por eso hay que cubrir los 15 estilos.

- [ ] **Step 1: Crear el helper**

`shared/src/commonMain/kotlin/com/deepdots/sdk/ui/Typography.kt`:
```kotlin
package com.deepdots.sdk.ui

import androidx.compose.material3.Typography
import androidx.compose.ui.text.font.FontFamily

/**
 * Devuelve una copia de la [Typography] con [family] aplicada a todos los estilos,
 * de modo que Text y los Material Button del popup hereden la fuente custom.
 * Si [family] es null, devuelve la misma typography sin cambios (cero regresión).
 */
fun Typography.withFontFamily(family: FontFamily?): Typography {
    if (family == null) return this
    return copy(
        displayLarge = displayLarge.copy(fontFamily = family),
        displayMedium = displayMedium.copy(fontFamily = family),
        displaySmall = displaySmall.copy(fontFamily = family),
        headlineLarge = headlineLarge.copy(fontFamily = family),
        headlineMedium = headlineMedium.copy(fontFamily = family),
        headlineSmall = headlineSmall.copy(fontFamily = family),
        titleLarge = titleLarge.copy(fontFamily = family),
        titleMedium = titleMedium.copy(fontFamily = family),
        titleSmall = titleSmall.copy(fontFamily = family),
        bodyLarge = bodyLarge.copy(fontFamily = family),
        bodyMedium = bodyMedium.copy(fontFamily = family),
        bodySmall = bodySmall.copy(fontFamily = family),
        labelLarge = labelLarge.copy(fontFamily = family),
        labelMedium = labelMedium.copy(fontFamily = family),
        labelSmall = labelSmall.copy(fontFamily = family),
    )
}
```

- [ ] **Step 2: Compilar**

Run:
```bash
cd /Users/sarias/AndroidStudioProjects/DeepdotsPopupSDK && ./gradlew :shared:compileDebugKotlinAndroid --console=plain
```
Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 3: Commit (solo Typography.kt)**

```bash
cd /Users/sarias/AndroidStudioProjects/DeepdotsPopupSDK
git add shared/src/commonMain/kotlin/com/deepdots/sdk/ui/Typography.kt
git commit -m "feat(font): Typography.withFontFamily helper for native chrome

Co-Authored-By: Claude Opus 4.8 <noreply@anthropic.com>"
```

---

## Task 5: Integrar en `PopupView`

**Files:**
- Modify: `shared/src/commonMain/kotlin/com/deepdots/sdk/ui/PopupView.kt`

Cargar la fuente async y aplicarla al chrome envolviendo el contenido del `Surface` en `MaterialTheme(typography=…)` + `CompositionLocalProvider(LocalTextStyle=…)`.

- [ ] **Step 1: Añadir imports**

En `PopupView.kt` añadir los imports que no cubre el wildcard `material3.*` existente:
```kotlin
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.text.font.FontFamily
```
(`LocalTextStyle` y `MaterialTheme` ya vienen por `androidx.compose.material3.*`.)

- [ ] **Step 2: Añadir estado + carga async (dentro de `PopupView`, junto a los otros `remember`)**

Tras la línea `var surveyController: SurveyController? by remember { mutableStateOf(null) }` añadir:
```kotlin
    var customFontFamily by remember { mutableStateOf<FontFamily?>(null) }
    LaunchedEffect(popup.style.font) {
        customFontFamily = SharedFontLoader.load(popup.style.font)
    }
```

- [ ] **Step 3: Envolver el contenido en el theming**

Localizar el `Surface( … ) { BoxWithConstraints( … ) { … } }` (hijo directo del `Box` raíz). Envolver el `BoxWithConstraints` — abrir justo dentro de la lambda del `Surface` y cerrar justo antes de cerrar esa lambda:

```kotlin
        Surface(
            modifier = Modifier
                .padding(16.dp)
                .widthIn(max = popupMaxWidth)
                .wrapContentHeight(),
            shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
            color = bgColor,
            tonalElevation = 6.dp,
            shadowElevation = 8.dp
        ) {
            MaterialTheme(typography = MaterialTheme.typography.withFontFamily(customFontFamily)) {
                CompositionLocalProvider(
                    LocalTextStyle provides LocalTextStyle.current.copy(fontFamily = customFontFamily)
                ) {
                    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                        // … TODO el cuerpo actual del BoxWithConstraints se mantiene SIN cambios …
                    }
                }
            }
        }
```

> Con `customFontFamily == null`: `withFontFamily(null)` devuelve la typography sin cambios y `copy(fontFamily = null)` deja el `LocalTextStyle` sin familia explícita → render idéntico al actual.

- [ ] **Step 4: Compilar Android + iOS**

Run:
```bash
cd /Users/sarias/AndroidStudioProjects/DeepdotsPopupSDK && ./gradlew :shared:compileDebugKotlinAndroid :shared:compileKotlinIosSimulatorArm64 --console=plain
```
Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 5: Commit (solo PopupView.kt)**

```bash
cd /Users/sarias/AndroidStudioProjects/DeepdotsPopupSDK
git add shared/src/commonMain/kotlin/com/deepdots/sdk/ui/PopupView.kt
git commit -m "feat(font): apply custom font to native popup chrome (title/buttons/etc)

Co-Authored-By: Claude Opus 4.8 <noreply@anthropic.com>"
```

---

## Task 6: Verificación final

**Files:** ninguno (solo verificación).

- [ ] **Step 1: Suite completa de tests**

Run:
```bash
cd /Users/sarias/AndroidStudioProjects/DeepdotsPopupSDK && ./gradlew :shared:testDebugUnitTest --console=plain
```
Expected: `BUILD SUCCESSFUL`; FontLoaderTest (8), FontHtmlParityTest (6), MagicFeedbackHtmlTest (3) verdes; sin regresiones.

- [ ] **Step 2: Compilación iOS**

Run:
```bash
cd /Users/sarias/AndroidStudioProjects/DeepdotsPopupSDK && ./gradlew :shared:compileKotlinIosSimulatorArm64 --console=plain
```
Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 3: Confirmar que el pile sigue sin commitear**

Run:
```bash
cd /Users/sarias/AndroidStudioProjects/DeepdotsPopupSDK && git status --short | wc -l && git log --oneline -6
```
Expected: el pile no relacionado sigue como untracked/modified (mismo recuento que antes de empezar); los últimos commits son los 5 de esta feature (Tasks 1–5) sobre `a7293c7` (spec).

---

## Self-review notes

- **Cobertura del spec:** descarga común Ktor (Task 3) ✓; expect/actual bytes→FontFamily (Task 1) ✓; FontLoader + cache + guardias (Task 2) ✓; family-only→default (Task 2, test) ✓; url insegura sin fetch (Task 2, test) ✓; theming Text+Button (Tasks 4+5) ✓; swap async / firma pública intacta (Task 5) ✓; manejo de errores→null (Tasks 1–3) ✓; fuera de alcance respetado (sin cache en disco, sin system-font, sin multi-peso) ✓.
- **Consistencia de tipos:** `fontFamilyFromBytes(family, bytes): FontFamily?` usado igual en Task 1, en la firma por defecto de `FontLoader.buildFamily` (Task 2) y por `SharedFontLoader` (Task 3). `SharedFontLoader.load(popup.style.font)` (Task 5) coincide con `FontLoader.load(font: PopupFont?)` (Task 2). `withFontFamily(FontFamily?)` (Task 4) usado en Task 5.
- **Sin placeholders de código:** todos los steps de código traen el código completo.
