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
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * `segments.excludedPaths`: rutas donde el popup NO debe mostrarse.
 * Espejo de `src/core/deepdots-popups.excluded-paths.test.ts` del SDK Web:
 * gana sobre `segments.path`, se aplica igual sin `path`, y usa las mismas
 * formas de candidato (URL absoluta / `/algo` / pathname).
 */
class ExcludedPathsParityTest {

    @Test
    fun excluded_path_blocks_the_popup_even_when_path_includes_it() {
        // El caso del cliente: `path = ["/"]` (= en todas) menos el carrito.
        val sdk = createSdk(listOf(popupWith(Segments(lang = listOf("en"), path = listOf("/"), excludedPaths = listOf("/cart")))))

        sdk.setPath("https://app.test/cart")
        sdk.triggerEvent("ping")

        assertTrue(sdk.debugQueuedPopupIds().isEmpty())
    }

    @Test
    fun the_same_popup_still_shows_on_other_paths() {
        val sdk = createSdk(listOf(popupWith(Segments(lang = listOf("en"), path = listOf("/"), excludedPaths = listOf("/cart")))))

        sdk.setPath("https://app.test/products")
        sdk.triggerEvent("ping")

        assertEquals(listOf("popup-1"), sdk.debugQueuedPopupIds())
    }

    @Test
    fun excluded_paths_apply_without_a_path_list() {
        val sdk = createSdk(listOf(popupWith(Segments(excludedPaths = listOf("/checkout")))))

        sdk.setPath("https://app.test/checkout")
        sdk.triggerEvent("ping")
        assertTrue(sdk.debugQueuedPopupIds().isEmpty())

        sdk.setPath("https://app.test/home")
        sdk.triggerEvent("ping")
        assertEquals(listOf("popup-1"), sdk.debugQueuedPopupIds())
    }

    @Test
    fun exclusion_wins_over_an_exact_path_match() {
        val sdk = createSdk(listOf(popupWith(Segments(path = listOf("/cart"), excludedPaths = listOf("/cart")))))

        sdk.setPath("https://app.test/cart")
        sdk.triggerEvent("ping")

        assertTrue(sdk.debugQueuedPopupIds().isEmpty())
    }

    @Test
    fun excluded_paths_accept_hash_routes() {
        val sdk = createSdk(listOf(popupWith(Segments(excludedPaths = listOf("/#/cart")))))

        sdk.setPath("https://app.test/#/cart")
        sdk.triggerEvent("ping")
        assertTrue(sdk.debugQueuedPopupIds().isEmpty())

        sdk.setPath("https://app.test/#/home")
        sdk.triggerEvent("ping")
        assertEquals(listOf("popup-1"), sdk.debugQueuedPopupIds())
    }

    @Test
    fun an_absolute_url_compares_the_whole_href() {
        val sdk = createSdk(listOf(popupWith(Segments(excludedPaths = listOf("https://app.test/cart")))))

        sdk.setPath("https://app.test/cart")
        sdk.triggerEvent("ping")
        assertTrue(sdk.debugQueuedPopupIds().isEmpty())

        // Mismo pathname en otro host: la URL absoluta no lo excluye.
        sdk.setPath("https://otra.test/cart")
        sdk.triggerEvent("ping")
        assertEquals(listOf("popup-1"), sdk.debugQueuedPopupIds())
    }

    @Test
    fun an_empty_list_changes_nothing() {
        val sdk = createSdk(listOf(popupWith(Segments(path = listOf("/"), excludedPaths = emptyList()))))

        sdk.setPath("https://app.test/cart")
        sdk.triggerEvent("ping")

        assertEquals(listOf("popup-1"), sdk.debugQueuedPopupIds())
    }

    @Test
    fun exit_popup_is_not_queued_when_the_source_path_is_excluded() {
        val sdk = createSdk(
            listOf(
                popupWith(
                    segments = Segments(excludedPaths = listOf("/cart")),
                    triggers = listOf(Trigger.Exit(0.0)),
                ),
            ),
        )

        sdk.setPath("https://app.test/cart")
        sdk.setPath("https://app.test/home")

        assertTrue(sdk.debugQueuedPopupIds().isEmpty())
    }

    @Test
    fun exit_popup_is_not_painted_when_the_destination_path_is_excluded() = runBlocking {
        // Al desencolar se levanta la comprobación de `path` (la ruta cambió a propósito),
        // pero "no mostrar en /cart" es una regla sobre la pantalla en la que se pinta.
        val sdk = createSdk(
            listOf(
                popupWith(
                    segments = Segments(excludedPaths = listOf("/cart")),
                    triggers = listOf(Trigger.Exit(0.05)),
                ),
            ),
        )

        sdk.setPath("https://app.test/home")
        sdk.setPath("https://app.test/cart")

        // Primero que el exit diferido se haya resuelto (si no, el assert pasaria por no haber
        // corrido aun la corrutina) y luego que no se haya encolado.
        assertTrue(waitUntil { sdk.debugDeferredExitQueue().isEmpty() }, "el exit diferido no se resolvio")
        assertTrue(sdk.debugQueuedPopupIds().isEmpty())
    }

    @Test
    fun exit_popup_is_painted_when_the_destination_path_is_not_excluded() = runBlocking {
        val sdk = createSdk(
            listOf(
                popupWith(
                    segments = Segments(excludedPaths = listOf("/cart")),
                    triggers = listOf(Trigger.Exit(0.05)),
                ),
            ),
        )

        sdk.setPath("https://app.test/home")
        sdk.setPath("https://app.test/products")

        waitUntil { sdk.debugQueuedPopupIds().isNotEmpty() }
        assertEquals(listOf("popup-1"), sdk.debugQueuedPopupIds())
    }

    @Test
    fun excluded_paths_are_parsed_from_the_server_payload() {
        val sdk = DeepdotsPopups()

        val popups = sdk.debugParseServerPayload(
            """
            [
              {
                "id": "popup-1",
                "title": "",
                "message": "",
                "triggers": [{"type":"exit","value":0}],
                "cooldown": null,
                "conditions": [],
                "actions": {},
                "style": {"theme":"light","position":"center","imageUrl":null},
                "segments": {"lang":["en"],"path":["/"],"excludedPaths":["/cart"]},
                "surveyId": "survey-1",
                "productId": "product-1"
              }
            ]
            """.trimIndent(),
        )

        assertEquals(listOf("/"), popups.first().segments?.path)
        assertEquals(listOf("/cart"), popups.first().segments?.excludedPaths)
    }

    private fun createSdk(popups: List<PopupDefinition>): DeepdotsPopups {
        return DeepdotsPopups().apply {
            init(
                InitOptions(
                    debug = true,
                    popupOptions = PopupOptions(),
                    storage = InMemoryStorage(),
                ),
            )
            debugLoadPopups(popups)
        }
    }

    private fun popupWith(
        segments: Segments,
        triggers: List<Trigger> = listOf(Trigger.Event("ping")),
    ): PopupDefinition {
        return PopupDefinition(
            id = "popup-1",
            title = "popup-1",
            message = "message",
            triggers = triggers,
            cooldown = emptyList(),
            actions = Actions(),
            surveyId = "survey-1",
            productId = "product-1",
            style = Style(theme = Theme.Light, position = Position.Center),
            segments = segments,
        )
    }
}
