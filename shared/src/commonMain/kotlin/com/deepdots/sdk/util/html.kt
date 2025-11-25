package com.deepdots.sdk.util

// Modelo simple para párrafos y segmentos estilizados
data class HtmlRun(val text: String, val bold: Boolean = false, val italic: Boolean = false)
data class HtmlParagraph(val runs: List<HtmlRun>)

/**
 * Parser HTML simplificado para tags <p>, <b>, <i> sin anidamiento complejo.
 * - Convierte el string en una lista de párrafos con runs estilizados.
 * - Ignora cualquier otra etiqueta.
 * - No soporta atributos ni nested mezclado complejo (bold dentro de italic se marca bold+italic).
 */
fun parsePopupHtml(raw: String): List<HtmlParagraph> {
    if (raw.isBlank()) return emptyList()
    val normalized = raw.replace("\n", " ").trim()
    // Separar por <p> y </p>
    val parts = normalized.split(Regex("<p>|</p>", RegexOption.IGNORE_CASE))
        .map { it.trim() }
        .filter { it.isNotEmpty() }
    return parts.map { paragraph -> parseParagraph(paragraph) }
}

private fun parseParagraph(src: String): HtmlParagraph {
    val runs = mutableListOf<HtmlRun>()
    var i = 0
    val sb = StringBuilder()
    var bold = false
    var italic = false
    while (i < src.length) {
        if (src[i] == '<') {
            // flush actual buffer antes de cambiar estilo
            if (sb.isNotEmpty()) {
                runs += HtmlRun(sb.toString(), bold, italic)
                sb.clear()
            }
            val end = src.indexOf('>', i)
            if (end == -1) break
            val tagContent = src.substring(i + 1, end).lowercase().trim()
            when (tagContent) {
                "b" -> bold = true
                "/b" -> bold = false
                "i" -> italic = true
                "/i" -> italic = false
                else -> { /* ignorar otras tags */ }
            }
            i = end + 1
        } else {
            sb.append(src[i])
            i++
        }
    }
    if (sb.isNotEmpty()) {
        runs += HtmlRun(sb.toString(), bold, italic)
    }
    // Limpiar espacios redundantes en cada run
    val cleaned = runs.map { r -> r.copy(text = r.text.replace(Regex("\\s+"), " ").trim()) }
        .filter { it.text.isNotEmpty() }
    return HtmlParagraph(cleaned)
}
