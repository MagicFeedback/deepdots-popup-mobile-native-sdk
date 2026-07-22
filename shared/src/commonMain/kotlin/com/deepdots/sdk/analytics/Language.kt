package com.deepdots.sdk.analytics

/**
 * Resolución del idioma para el `context.language` del canal de analytics.
 *
 * Paridad con el helper Web `src/analytics/language.ts`. Prioridad:
 *   1. `explicit`  — idioma forzado por el host (InitOptions.provideLang). Máxima prioridad.
 *   2. `deviceLanguage` — locale BCP-47 del dispositivo (fallback automático).
 *   3. `null` — si nada resuelve, el campo se omite del metadata.
 *
 * Función pura: `deviceLanguage` se pasa como parámetro (lo provee [deviceLanguage]) para que
 * sea testeable y determinista, igual que el `LanguageSources` inyectable de la versión Web.
 */
fun resolveLanguage(explicit: String?, deviceLanguage: String?): String? =
    explicit.clean() ?: deviceLanguage.clean()

/** Mirror del `clean()` de Web: trim + descarta cadenas vacías/en blanco. */
private fun String?.clean(): String? = this?.trim()?.ifEmpty { null }

/**
 * Locale BCP-47 del dispositivo (ej. `"es-ES"`). Fallback automático cuando el host no
 * fuerza idioma. Devuelve `null` si la plataforma no puede resolverlo.
 */
expect fun deviceLanguage(): String?
