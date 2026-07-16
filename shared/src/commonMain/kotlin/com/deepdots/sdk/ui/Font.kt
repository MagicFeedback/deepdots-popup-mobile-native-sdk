package com.deepdots.sdk.ui

/**
 * Espejo EXACTO de `src/ui/font.ts` (repo Web). Cualquier cambio se replica allí.
 * family/url vienen de la API y se interpolan en CSS y en el <style> del WebView,
 * por eso se sanean/validan para evitar inyección CSS/script.
 */

private val FORMAT_BY_EXT = mapOf(
    "woff2" to "woff2",
    "woff" to "woff",
    "ttf" to "truetype",
    "otf" to "opentype",
)

fun fontFormatFromUrl(url: String): String? {
    val clean = url.substringBefore('?').substringBefore('#')
    val ext = clean.substringAfterLast('.', "").lowercase()
    return FORMAT_BY_EXT[ext]
}

private val UNSAFE_FAMILY = Regex("[^A-Za-z0-9 ._-]")
private fun sanitizeFamily(family: String): String =
    UNSAFE_FAMILY.replace(family, "").trim()

private val SCHEME = Regex("^(https?:|data:)", RegexOption.IGNORE_CASE)

// Control chars 0x00-0x20 + quote/angle-bracket/backslash (mirrors Web font.ts UNSAFE_URL_RE).
private val UNSAFE_URL = Regex("[\\x00-\\x20\"<>\\\\]")
private fun isSafeFontUrl(url: String): Boolean =
    SCHEME.containsMatchIn(url) && !UNSAFE_URL.containsMatchIn(url)

fun buildFontFamilyValue(family: String): String =
    "\"${sanitizeFamily(family)}\", -apple-system, system-ui, sans-serif"

fun buildFontFaceCss(family: String, url: String?): String {
    if (url == null || !isSafeFontUrl(url)) return ""
    val safe = sanitizeFamily(family)
    val fmt = fontFormatFromUrl(url)
    val src = if (fmt != null) "url(\"$url\") format(\"$fmt\")" else "url(\"$url\")"
    return "@font-face{font-family:\"$safe\";src:$src;font-display:swap;}"
}
