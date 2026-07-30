package com.deepdots.sdk.analytics

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/**
 * Paridad con la resolución de idioma del Web (src/analytics/language.test.ts).
 * Cadena: idioma del host > locale de la plataforma (en Web: > navigator.language > Intl).
 */
class LanguageParityTest {

    @Test
    fun the_host_language_wins() {
        assertEquals("es-ES", resolveLanguage("es-ES", platform = "en-US"))
    }

    @Test
    fun falls_back_to_the_platform_locale() {
        assertEquals("en-US", resolveLanguage(null, platform = "en-US"))
    }

    @Test
    fun blank_values_do_not_count() {
        assertEquals("en-US", resolveLanguage("   ", platform = "en-US"))
        assertNull(resolveLanguage("", platform = " "))
    }

    @Test
    fun null_when_nothing_resolves() {
        assertNull(resolveLanguage(null, platform = null))
    }
}
