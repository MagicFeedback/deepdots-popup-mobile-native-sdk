package com.deepdots.sdk

import com.deepdots.sdk.models.Actions
import com.deepdots.sdk.models.InitOptions
import com.deepdots.sdk.models.PopupDefinition
import com.deepdots.sdk.models.PopupOptions
import com.deepdots.sdk.models.Position
import com.deepdots.sdk.models.Segments
import com.deepdots.sdk.models.Style
import com.deepdots.sdk.models.Theme
import com.deepdots.sdk.models.Trigger
import com.deepdots.sdk.storage.InMemoryStorage
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * `segments.lang`: idiomas para los que el popup PUEDE mostrarse.
 * Espejo de `src/core/deepdots-popups.lang.test.ts` del SDK Web: se compara el idioma que
 * reporta analytics (host > locale de la plataforma) por prefijo y sin distinguir mayúsculas;
 * sin lista no filtra. Hasta 0.7.3 el SDK nativo leía `segments.lang` pero no lo aplicaba.
 */
class SegmentsLangParityTest {

    @Test
    fun shows_the_popup_when_the_declared_language_is_in_segments_lang() {
        val sdk = createSdk(lang = "da", popupWith(Segments(lang = listOf("da"))))

        sdk.triggerEvent("ping")

        assertEquals(listOf("popup-1"), sdk.debugQueuedPopupIds())
    }

    @Test
    fun does_not_show_the_popup_when_the_declared_language_does_not_match() {
        val sdk = createSdk(lang = "en", popupWith(Segments(lang = listOf("da"))))

        sdk.triggerEvent("ping")

        assertTrue(sdk.debugQueuedPopupIds().isEmpty())
    }

    @Test
    fun matches_by_prefix_and_ignores_case() {
        val sdk = createSdk(lang = "EN-us", popupWith(Segments(lang = listOf("en"))))

        sdk.triggerEvent("ping")

        assertEquals(listOf("popup-1"), sdk.debugQueuedPopupIds())
    }

    @Test
    fun picks_the_popup_of_the_matching_audience() {
        // El caso de la app de pruebas (audience-lang): dos popups en el mismo evento, uno por idioma.
        val sdk = createSdk(
            lang = "es-ES",
            popupWith(Segments(lang = listOf("en")), id = "popup-en"),
            popupWith(Segments(lang = listOf("es")), id = "popup-es"),
        )

        sdk.triggerEvent("ping")

        assertEquals(listOf("popup-es"), sdk.debugQueuedPopupIds())
    }

    @Test
    fun an_empty_list_does_not_filter() {
        val sdk = createSdk(lang = "fr", popupWith(Segments(lang = emptyList())))

        sdk.triggerEvent("ping")

        assertEquals(listOf("popup-1"), sdk.debugQueuedPopupIds())
    }

    @Test
    fun blank_entries_never_match() {
        val sdk = createSdk(lang = "en", popupWith(Segments(lang = listOf("", " "))))

        sdk.triggerEvent("ping")

        assertTrue(sdk.debugQueuedPopupIds().isEmpty())
    }

    @Test
    fun lang_and_path_must_both_match() {
        // `segments.lang` se suma a `segments.path`: las dos tienen que cumplirse.
        val sdk = createSdk(lang = "en", popupWith(Segments(lang = listOf("da"), path = listOf("/home"))))

        sdk.setPath("/home")
        sdk.triggerEvent("ping")

        assertTrue(sdk.debugQueuedPopupIds().isEmpty())
    }

    private fun createSdk(lang: String, vararg popups: PopupDefinition): DeepdotsPopups {
        return DeepdotsPopups().apply {
            init(
                InitOptions(
                    debug = true,
                    popupOptions = PopupOptions(),
                    storage = InMemoryStorage(),
                    provideLang = { lang },
                ),
            )
            debugLoadPopups(popups.toList())
        }
    }

    private fun popupWith(segments: Segments, id: String = "popup-1"): PopupDefinition {
        return PopupDefinition(
            id = id,
            title = id,
            message = "message",
            triggers = listOf(Trigger.Event("ping")),
            cooldown = emptyList(),
            actions = Actions(),
            surveyId = "survey-$id",
            productId = "product-1",
            style = Style(theme = Theme.Light, position = Position.Center),
            segments = segments,
        )
    }
}
