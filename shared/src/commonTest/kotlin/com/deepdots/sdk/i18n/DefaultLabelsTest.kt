package com.deepdots.sdk.i18n

import com.deepdots.sdk.i18n.DefaultLabels.Slot
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DefaultLabelsTest {

    @Test
    fun resolvesEnglishByDefault() {
        assertEquals("Send", DefaultLabels.resolve(Slot.ACCEPT, "en"))
        assertEquals("Cancel", DefaultLabels.resolve(Slot.DECLINE, "en"))
        assertEquals("Start survey", DefaultLabels.resolve(Slot.START, "en"))
        assertEquals("Complete survey", DefaultLabels.resolve(Slot.COMPLETE, "en"))
        assertEquals("Back", DefaultLabels.resolve(Slot.BACK, "en"))
    }

    @Test
    fun resolvesSpanish() {
        assertEquals("Enviar", DefaultLabels.resolve(Slot.ACCEPT, "es"))
        assertEquals("Cancelar", DefaultLabels.resolve(Slot.DECLINE, "es"))
        assertEquals("Atrás", DefaultLabels.resolve(Slot.BACK, "es"))
    }

    @Test
    fun resolvesDanish() {
        assertEquals("Send", DefaultLabels.resolve(Slot.ACCEPT, "da"))
        assertEquals("Annuller", DefaultLabels.resolve(Slot.DECLINE, "da"))
        assertEquals("Tilbage", DefaultLabels.resolve(Slot.BACK, "da"))
    }

    @Test
    fun resolvesNorwegianAndVariants() {
        assertEquals("Avbryt", DefaultLabels.resolve(Slot.DECLINE, "no"))
        assertEquals("Avbryt", DefaultLabels.resolve(Slot.DECLINE, "nb"))
        assertEquals("Avbryt", DefaultLabels.resolve(Slot.DECLINE, "nn"))
        assertEquals("Tilbake", DefaultLabels.resolve(Slot.BACK, "nb-NO"))
    }

    @Test
    fun resolvesSwedish() {
        assertEquals("Skicka", DefaultLabels.resolve(Slot.ACCEPT, "sv"))
        assertEquals("Tillbaka", DefaultLabels.resolve(Slot.BACK, "sv-SE"))
    }

    @Test
    fun resolvesFinnish() {
        assertEquals("Lähetä", DefaultLabels.resolve(Slot.ACCEPT, "fi"))
        assertEquals("Peruuta", DefaultLabels.resolve(Slot.DECLINE, "fi-FI"))
    }

    @Test
    fun resolvesSimplifiedChineseVariants() {
        assertEquals("发送", DefaultLabels.resolve(Slot.ACCEPT, "zh-CN"))
        assertEquals("发送", DefaultLabels.resolve(Slot.ACCEPT, "zh"))
        assertEquals("发送", DefaultLabels.resolve(Slot.ACCEPT, "zh-Hans"))
        assertEquals("返回", DefaultLabels.resolve(Slot.BACK, "zh-CN"))
    }

    @Test
    fun acceptsRegionAndUnderscoreVariants() {
        assertEquals("Enviar", DefaultLabels.resolve(Slot.ACCEPT, "es-ES"))
        assertEquals("Enviar", DefaultLabels.resolve(Slot.ACCEPT, "es_419"))
        assertEquals("Annuller", DefaultLabels.resolve(Slot.DECLINE, "DA-dk"))
    }

    @Test
    fun fallsBackToEnglishForUnknownOrBlankLang() {
        assertEquals("Send", DefaultLabels.resolve(Slot.ACCEPT, null))
        assertEquals("Send", DefaultLabels.resolve(Slot.ACCEPT, ""))
        assertEquals("Send", DefaultLabels.resolve(Slot.ACCEPT, "  "))
        assertEquals("Send", DefaultLabels.resolve(Slot.ACCEPT, "xx"))
        assertEquals("Send", DefaultLabels.resolve(Slot.ACCEPT, "fr"))
    }

    @Test
    fun supportedLanguagesListsExpectedTags() {
        val expected = listOf("en", "es", "da", "no", "sv", "fi", "zh-CN")
        assertEquals(expected, DefaultLabels.supportedLanguages)
        // Every advertised locale must resolve to a non-English label for at least one slot
        // (besides English itself), to guarantee the table isn't aliasing back to EN by accident.
        val englishAccept = DefaultLabels.resolve(Slot.ACCEPT, "en")
        val englishDecline = DefaultLabels.resolve(Slot.DECLINE, "en")
        DefaultLabels.supportedLanguages.filter { it != "en" }.forEach { tag ->
            val acceptDiffers = DefaultLabels.resolve(Slot.ACCEPT, tag) != englishAccept
            val declineDiffers = DefaultLabels.resolve(Slot.DECLINE, tag) != englishDecline
            assertTrue(
                acceptDiffers || declineDiffers,
                "Locale '$tag' is aliased to English for ACCEPT and DECLINE — check the table.",
            )
        }
    }
}
