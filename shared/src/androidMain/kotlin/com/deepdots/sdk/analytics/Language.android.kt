package com.deepdots.sdk.analytics

/**
 * Locale del dispositivo Android como BCP-47 (ej. `"es-ES"`).
 * `toLanguageTag()` devuelve `"und"` cuando el locale es indefinido: se descarta.
 */
actual fun deviceLanguage(): String? = try {
    java.util.Locale.getDefault().toLanguageTag().takeUnless { it.isBlank() || it == "und" }
} catch (_: Throwable) {
    null
}
