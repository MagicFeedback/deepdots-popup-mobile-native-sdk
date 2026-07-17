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
