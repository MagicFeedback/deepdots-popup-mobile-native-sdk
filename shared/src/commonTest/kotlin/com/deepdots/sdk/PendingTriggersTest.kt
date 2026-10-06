package com.deepdots.sdk

import com.deepdots.sdk.analytics.AnalyticsFeedbackBody
import com.deepdots.sdk.contact.ContactBody
import com.deepdots.sdk.models.Actions
import com.deepdots.sdk.models.InitOptions
import com.deepdots.sdk.models.PopupDefinition
import com.deepdots.sdk.models.PopupOptions
import com.deepdots.sdk.models.Position
import com.deepdots.sdk.models.Segments
import com.deepdots.sdk.models.Style
import com.deepdots.sdk.models.Theme
import com.deepdots.sdk.models.Trigger
import com.deepdots.sdk.service.PopupsService
import com.deepdots.sdk.storage.InMemoryStorage
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Disparos del host que llegan ANTES de que `init()` termine de bajar las definiciones
 * (`GET /sdk/{publicKey}/popups`). Antes se perdían ("No eligible event popup") aunque el popup
 * llegase milisegundos después; ahora se guardan y se reproducen al cargar, en orden y por la
 * evaluación normal (segmentación, cooldowns y condiciones intactas).
 */
class PendingTriggersTest {

    /** Doble del servicio: el GET de popups se queda en vuelo hasta que el test lo resuelve. */
    private class GatedPopupsService : PopupsService {
        val popupsResponse = CompletableDeferred<String>()

        override suspend fun fetchPopups(publicKey: String, filter: String?): String = popupsResponse.await()

        override suspend fun postPopupEvent(publicKey: String, status: String, popupId: String, userId: String?): String? = null

        override suspend fun postFeedback(body: AnalyticsFeedbackBody): String? = null

        override suspend fun postContact(body: ContactBody) = Unit
    }

    @Test
    fun event_fired_before_the_api_responds_is_shown_once_popups_arrive() = runBlocking {
        val service = GatedPopupsService()
        val sdk = createLoadingSdk(service)

        sdk.triggerEvent("app_opened")
        assertTrue(sdk.debugQueuedPopupIds().isEmpty(), "sin definiciones todavía no hay nada que mostrar")

        // El flujo real de init(): la respuesta de la API se parsea y se cargan las definiciones.
        service.popupsResponse.complete(
            """
            [{"id":"popup-open","title":"","message":"","triggers":[{"type":"event","value":"app_opened"}],
              "actions":{},"style":{"theme":"light","position":"center"},"surveyId":"survey-open","productId":"product-1"}]
            """.trimIndent(),
        )

        awaitUntil { sdk.debugQueuedPopupIds().isNotEmpty() }
        assertEquals(listOf("popup-open"), sdk.debugQueuedPopupIds())
        assertEquals(0, sdk.debugPendingTriggerCount())
    }

    @Test
    fun pending_triggers_are_replayed_in_the_order_they_were_fired() {
        val sdk = createLoadingSdk(GatedPopupsService())

        sdk.triggerEvent("second")
        sdk.triggerClick("buy-button")
        sdk.triggerEvent("first")

        sdk.debugLoadPopups(
            listOf(
                popup("popup-first", listOf(Trigger.Event("first"))),
                popup("popup-second", listOf(Trigger.Event("second"))),
                popup("popup-click", listOf(Trigger.Click("buy-button"))),
            ),
        )

        assertEquals(listOf("popup-second", "popup-click", "popup-first"), sdk.debugQueuedPopupIds())
    }

    @Test
    fun replay_goes_through_normal_evaluation_so_segments_still_apply() {
        val sdk = createLoadingSdk(GatedPopupsService())

        sdk.setPath("https://app.test/#/home")
        sdk.triggerEvent("search")

        sdk.debugLoadPopups(
            listOf(popup("popup-search", listOf(Trigger.Event("search")), Segments(path = listOf("/#/game")))),
        )

        assertTrue(sdk.debugQueuedPopupIds().isEmpty(), "la ruta no casa: el replay no se salta la segmentación")
        assertEquals(0, sdk.debugPendingTriggerCount())
    }

    @Test
    fun exit_from_a_screen_left_before_the_api_responds_is_replayed() = runBlocking {
        val sdk = createLoadingSdk(GatedPopupsService())

        sdk.setPath("https://app.test/#/login")
        sdk.setPath("https://app.test/#/home")

        sdk.debugLoadPopups(
            listOf(popup("popup-exit", listOf(Trigger.Exit(0.0)), Segments(path = listOf("/#/login")))),
        )

        awaitUntil { sdk.debugQueuedPopupIds().isNotEmpty() }
        assertEquals(listOf("popup-exit"), sdk.debugQueuedPopupIds())
    }

    @Test
    fun scroll_reached_before_the_api_responds_is_replayed_on_the_same_screen() {
        val sdk = createLoadingSdk(GatedPopupsService())

        sdk.setPath("https://app.test/#/article")
        sdk.onScroll(40)
        sdk.onScroll(90)
        sdk.onScroll(60)
        assertEquals(1, sdk.debugPendingTriggerCount(), "el scroll es un nivel: se guarda solo el máximo")

        sdk.debugLoadPopups(listOf(popup("popup-scroll", listOf(Trigger.Scroll(80)))))

        assertEquals(listOf("popup-scroll"), sdk.debugQueuedPopupIds())
    }

    @Test
    fun scroll_before_the_api_responds_is_forgotten_when_the_screen_changes() {
        val sdk = createLoadingSdk(GatedPopupsService())

        sdk.setPath("https://app.test/#/article")
        sdk.onScroll(90)
        sdk.setPath("https://app.test/#/other")

        sdk.debugLoadPopups(listOf(popup("popup-scroll", listOf(Trigger.Scroll(80)))))

        assertTrue(sdk.debugQueuedPopupIds().isEmpty(), "el 90% era de la pantalla anterior")
    }

    @Test
    fun the_pending_buffer_is_capped_and_drops_the_oldest() {
        val sdk = createLoadingSdk(GatedPopupsService())

        val fired = MAX_PENDING_TRIGGERS + 5
        repeat(fired) { i -> sdk.triggerEvent("e$i") }
        assertEquals(MAX_PENDING_TRIGGERS, sdk.debugPendingTriggerCount())

        sdk.debugLoadPopups(
            listOf(
                popup("popup-oldest", listOf(Trigger.Event("e0"))),
                popup("popup-newest", listOf(Trigger.Event("e${fired - 1}"))),
            ),
        )

        assertEquals(listOf("popup-newest"), sdk.debugQueuedPopupIds())
    }

    @Test
    fun a_failed_load_clears_the_buffer_and_stops_buffering() = runBlocking {
        val service = GatedPopupsService()
        val sdk = createLoadingSdk(service)

        sdk.triggerEvent("app_opened")
        assertEquals(1, sdk.debugPendingTriggerCount())

        service.popupsResponse.completeExceptionally(IllegalStateException("503 simulado"))
        awaitUntil { sdk.debugPendingTriggerCount() == 0 }

        // Tras el fallo ya no hay carga en vuelo: un disparo nuevo NO se guarda (no habría replay).
        sdk.triggerEvent("later")
        assertEquals(0, sdk.debugPendingTriggerCount())

        // Y lo descartado no resucita si algo carga definiciones más tarde.
        sdk.debugLoadPopups(listOf(popup("popup-open", listOf(Trigger.Event("app_opened")))))
        assertTrue(sdk.debugQueuedPopupIds().isEmpty())
    }

    @Test
    fun events_after_load_behave_as_before() {
        val sdk = createLoadingSdk(GatedPopupsService())
        sdk.debugLoadPopups(listOf(popup("popup-search", listOf(Trigger.Event("search")))))

        sdk.triggerEvent("unknown")
        assertEquals(0, sdk.debugPendingTriggerCount(), "con definiciones cargadas no se guarda nada")
        assertTrue(sdk.debugQueuedPopupIds().isEmpty())

        sdk.triggerEvent("search")
        assertEquals(listOf("popup-search"), sdk.debugQueuedPopupIds())
    }

    @Test
    fun without_a_fetch_in_flight_nothing_is_buffered() {
        // Sin publicKey init() no llama a la API: no hay carga que esperar.
        val sdk = DeepdotsPopups().apply {
            init(InitOptions(debug = true, popupOptions = PopupOptions(), storage = InMemoryStorage()))
        }

        sdk.triggerEvent("app_opened")

        assertEquals(0, sdk.debugPendingTriggerCount())
    }

    private fun createLoadingSdk(service: PopupsService): DeepdotsPopups = DeepdotsPopups().apply {
        // El servicio se fija ANTES de init() para que el GET de popups lo use (y quede en vuelo).
        debugSetPopupsService(service)
        init(
            InitOptions(
                debug = true,
                popupOptions = PopupOptions(publicKey = "pk-pending"),
                storage = InMemoryStorage(),
            ),
        )
    }

    /** Espera activa corta: la carga corre en una corrutina del SDK, no observable de otra forma. */
    private suspend fun awaitUntil(timeoutMs: Long = 2_000, condition: () -> Boolean) {
        var waited = 0L
        while (!condition() && waited < timeoutMs) {
            delay(20)
            waited += 20
        }
        assertTrue(condition(), "condición no cumplida en ${timeoutMs}ms")
    }

    private fun popup(
        id: String,
        triggers: List<Trigger>,
        segments: Segments? = null,
    ): PopupDefinition = PopupDefinition(
        id = id,
        title = id,
        message = "message",
        triggers = triggers,
        cooldown = emptyList(),
        actions = Actions(),
        surveyId = "survey-$id",
        productId = "product-1",
        style = Style(theme = Theme.Light, position = Position.Center),
        segments = segments,
    )
}
