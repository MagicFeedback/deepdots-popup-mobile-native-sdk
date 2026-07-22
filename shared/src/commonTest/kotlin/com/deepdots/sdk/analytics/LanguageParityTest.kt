package com.deepdots.sdk.analytics

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/**
 * Paridad con el helper Web `resolveLanguage` (src/analytics/language.ts + language.test.ts).
 *
 * KMP tiene una única fuente automática (el locale del dispositivo), así que el mirror es
 * `resolveLanguage(explicit, deviceLanguage)` con prioridad: explicit > deviceLanguage > null.
 * `deviceLanguage` se inyecta como String? (fakeado) para que el test sea puro y determinista.
 */
class LanguageParityTest {

    @Test
    fun returns_the_explicit_language_over_the_device_language() {
        assertEquals("fr-CA", resolveLanguage(explicit = "fr-CA", deviceLanguage = "de-DE"))
    }

    @Test
    fun falls_back_to_the_device_language_when_no_explicit_is_given() {
        assertEquals("es-ES", resolveLanguage(explicit = null, deviceLanguage = "es-ES"))
    }

    @Test
    fun falls_back_to_the_device_language_when_explicit_is_blank() {
        assertEquals("en-US", resolveLanguage(explicit = "   ", deviceLanguage = "en-US"))
    }

    @Test
    fun returns_null_when_no_source_yields_a_language() {
        assertNull(resolveLanguage(explicit = null, deviceLanguage = null))
    }

    @Test
    fun ignores_a_blank_device_language() {
        assertNull(resolveLanguage(explicit = "  ", deviceLanguage = "  "))
    }

    @Test
    fun trims_whitespace_from_the_resolved_value() {
        assertEquals("fr-CA", resolveLanguage(explicit = "  fr-CA  ", deviceLanguage = null))
        assertEquals("es-ES", resolveLanguage(explicit = null, deviceLanguage = "  es-ES  "))
    }
}
