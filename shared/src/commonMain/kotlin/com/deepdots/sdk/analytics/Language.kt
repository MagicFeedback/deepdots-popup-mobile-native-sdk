package com.deepdots.sdk.analytics

/**
 * Resolución del idioma para el context de analytics. Espejo de `src/analytics/language.ts` (Web).
 *
 * Prioridad: idioma del host (`InitOptions.provideLang`) > locale de la plataforma. Si nada
 * resuelve, `null` y el campo se omite del metadata. En Web la cadena es
 * explícito > `navigator.language` > `Intl`; aquí el segundo escalón es el locale nativo.
 */
fun resolveLanguage(explicit: String?, platform: String? = platformLanguage()): String? {
    explicit?.trim()?.takeIf { it.isNotEmpty() }?.let { return it }
    return platform?.trim()?.takeIf { it.isNotEmpty() }
}

/** Locale del dispositivo en formato BCP-47 (es-ES). Null si no se puede resolver. */
expect fun platformLanguage(): String?
