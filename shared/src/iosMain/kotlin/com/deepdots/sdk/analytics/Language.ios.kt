package com.deepdots.sdk.analytics

import platform.Foundation.NSLocale
import platform.Foundation.preferredLanguages

/**
 * Locale del dispositivo iOS como BCP-47 (ej. `"es-ES"`).
 *
 * Se usa `NSLocale.preferredLanguages.first` (no `currentLocale.languageCode`) porque devuelve
 * el tag completo con región (`"es-ES"`), a paridad con el `toLanguageTag()` de Android; mientras
 * que `languageCode` daría solo el idioma (`"es"`).
 */
actual fun deviceLanguage(): String? = try {
    (NSLocale.preferredLanguages.firstOrNull() as? String)?.takeUnless { it.isBlank() }
} catch (_: Throwable) {
    null
}
