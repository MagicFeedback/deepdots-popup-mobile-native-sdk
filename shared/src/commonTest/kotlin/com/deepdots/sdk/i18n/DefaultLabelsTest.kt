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
        assertEquals("Send", DefaultLabels.resolve(Slot.ACCEPT, "is-IS")) // islandés: sin traducción
    }

    @Test
    fun supportedLanguagesListsExpectedTags() {
        val expected = listOf("en", "es", "da", "no", "sv", "fi", "de", "fr", "pt", "ar", "bn", "zh-CN")
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

    @Test
    fun coversTheElevenLanguagesThePlatformOffersForASurvey() {
        // Lista del selector de idioma de la plataforma. Espejo del test de Web: si aparece un
        // idioma nuevo, sin traducción propia resolvería a 'en' y el chrome saldría en inglés
        // bajo un survey traducido.
        val platform = mapOf(
            "English" to "en", "Danish" to "da", "Finnish" to "fi", "Norwegian" to "no",
            "Spanish" to "es", "Swedish" to "sv", "Arabic" to "ar", "Bengali" to "bn",
            "German" to "de", "Portuguese" to "pt", "French" to "fr",
        )
        platform.forEach { (name, code) ->
            assertEquals(code, DefaultLabels.resolveLocale(code), name)
            assertTrue(DefaultLabels.supportedLanguages.contains(code), name)
        }
    }

    @Test
    fun normalizesRegionalVariantsOfTheNewLanguages() {
        assertEquals("de", DefaultLabels.resolveLocale("de-AT"))
        assertEquals("pt", DefaultLabels.resolveLocale("pt-BR"))
        assertEquals("pt", DefaultLabels.resolveLocale("pt-PT"))
        assertEquals("fr", DefaultLabels.resolveLocale("fr-CA"))
        assertEquals("ar", DefaultLabels.resolveLocale("ar-EG"))
        assertEquals("bn", DefaultLabels.resolveLocale("bn-BD"))
        assertEquals("en", DefaultLabels.resolveLocale("is-IS"))
    }

    @Test
    fun resolvesGerman() {
        val de = DefaultLabels.labels("de-AT")
        assertEquals("Senden", de.accept)
        assertEquals("Zurück", de.back)
        assertEquals("Umfrage starten", de.start)
        assertEquals("Umfrage abschließen", de.complete)
        assertEquals("Frage", de.question)
        assertEquals("von", de.of)
    }

    @Test
    fun resolvesArabicAndBengali() {
        assertEquals("إرسال", DefaultLabels.resolve(Slot.ACCEPT, "ar"))
        assertEquals("رجوع", DefaultLabels.resolve(Slot.BACK, "ar-EG"))
        assertEquals("পাঠান", DefaultLabels.resolve(Slot.ACCEPT, "bn"))
        assertEquals("পিছনে", DefaultLabels.resolve(Slot.BACK, "bn-BD"))
    }

    @Test
    fun chromeLabelsBeyondButtonsAreTranslatedToo() {
        // Los textos que no son botones (progreso, follow-up, errores) también viajan en la
        // tabla: son los que en Web se quedaban en inglés bajo un survey danés.
        val da = DefaultLabels.labels("da")
        assertEquals("Spørgsmål", da.question)
        assertEquals("af", da.of)
        assertEquals("Opfølgning", da.followUp)
        assertEquals("Besvar venligst det obligatoriske spørgsmål for at fortsætte.", da.errorRequired)
        assertTrue(da.errorSubmit.isNotBlank())
    }

    @Test
    fun everySupportedLocaleDefinesEveryField() {
        DefaultLabels.supportedLanguages.forEach { tag ->
            val l = DefaultLabels.labels(tag)
            listOf(
                l.accept, l.decline, l.start, l.complete, l.back, l.question, l.of,
                l.followUp, l.closeAria, l.loadingAria, l.errorRequired, l.errorSubmit,
            ).forEachIndexed { i, value ->
                assertTrue(value.isNotBlank(), "Locale '$tag' field #$i is blank")
            }
        }
    }

    @Test
    fun onlyArabicIsRightToLeft() {
        // Espejo de `RTL_LANGUAGES` de @magicfeedback/native (2.2.22), que voltea el contenedor
        // del survey; el chrome de Compose tiene que voltear con él.
        assertEquals(listOf("ar"), DefaultLabels.rtlLocales)
        assertTrue(DefaultLabels.isRtlLanguage("ar"))
        assertTrue(DefaultLabels.isRtlLanguage("ar-EG"))
        assertTrue(DefaultLabels.isRtlLanguage("AR_eg"))
        DefaultLabels.supportedLanguages.filter { it != "ar" }.forEach { tag ->
            assertTrue(!DefaultLabels.isRtlLanguage(tag), "'$tag' no debería ser RTL")
        }
        assertTrue(!DefaultLabels.isRtlLanguage(null))
        assertTrue(!DefaultLabels.isRtlLanguage("is-IS"))
    }
}
