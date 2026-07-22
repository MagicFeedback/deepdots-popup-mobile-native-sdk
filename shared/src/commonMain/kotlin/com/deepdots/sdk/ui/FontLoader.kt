package com.deepdots.sdk.ui

import androidx.compose.ui.text.font.FontFamily
import com.deepdots.sdk.models.PopupFont
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.readBytes
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

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
