package com.deepdots.sdk

import com.deepdots.sdk.models.Actions
import com.deepdots.sdk.models.CooldownCondition
import com.deepdots.sdk.models.InitOptions
import com.deepdots.sdk.models.PopupDefinition
import com.deepdots.sdk.models.PopupOptions
import com.deepdots.sdk.models.Position
import com.deepdots.sdk.models.Style
import com.deepdots.sdk.models.Theme
import com.deepdots.sdk.models.Trigger
import com.deepdots.sdk.models.TriggerConditionStatus
import com.deepdots.sdk.storage.InMemoryStorage
import com.deepdots.sdk.util.currentTimeMillis
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * "Delay (seconds)" del Admin (`trigger.delaySeconds`) en todos los tipos de trigger (reportado
 * por MOM: event RATING + 60 s salía al momento). Paridad con Web `deepdots-popups.delay.test.ts`.
 * Retrasos cortos (0.3 s) y `waitUntil` para no depender de la velocidad del runner.
 */
class TriggerDelayTest {

    // ---- parseo de la API ----

    @Test
    fun parses_delaySeconds_for_every_trigger_type() {
        val parsed = DeepdotsPopups().debugParseServerPayload(
            """
            [{"id":"p","title":"t","message":"m","surveyId":"s","productId":"x","triggers":[
              {"type":"time_on_page","value":2,"delaySeconds":3},
              {"type":"scroll","value":50,"delaySeconds":4},
              {"type":"event","value":"RATING","delaySeconds":60},
              {"type":"click","value":"help","delaySeconds":5},
              {"type":"exit","value":0,"delaySeconds":6}
            ]}]
            """.trimIndent(),
        )
        assertEquals(
            listOf(
                Trigger.TimeOnPage(2.0, 3.0),
                Trigger.Scroll(50, 4.0),
                Trigger.Event("RATING", 60.0),
                Trigger.Click("help", 5.0),
                Trigger.Exit(6.0),
            ),
            parsed.single().triggers,
        )
    }

    @Test
    fun exit_without_delaySeconds_keeps_the_delay_in_value_older_popups() {
        val parsed = DeepdotsPopups().debugParseServerPayload(
            """[{"id":"p","title":"t","message":"m","surveyId":"s","productId":"x","triggers":[{"type":"exit","value":2}]}]""",
        )
        assertEquals(listOf<Trigger>(Trigger.Exit(2.0)), parsed.single().triggers)
    }

    @Test
    fun missing_negative_or_invalid_delaySeconds_counts_as_zero_and_keeps_the_popup() {
        val parsed = DeepdotsPopups().debugParseServerPayload(
            """
            [{"id":"p","title":"t","message":"m","surveyId":"s","productId":"x","triggers":[
              {"type":"event","value":"a"},
              {"type":"event","value":"b","delaySeconds":-5},
              {"type":"event","value":"c","delaySeconds":"x"},
              {"type":"event","value":"d","delaySeconds":null}
            ]}]
            """.trimIndent(),
        )
        assertEquals(listOf(0.0, 0.0, 0.0, 0.0), parsed.single().triggers.map { it.delaySeconds })
    }

    // ---- 0 = al momento; N s = tras N s ----

    @Test
    fun event_with_zero_delay_queues_at_once() {
        val sdk = createSdk(listOf(popup("p-rating", listOf(Trigger.Event("RATING", 0.0)))))
        sdk.triggerEvent("RATING")
        assertEquals(listOf("p-rating"), sdk.debugQueuedPopupIds())
    }

    @Test
    fun event_with_delay_queues_after_the_delay_not_before() = runBlocking {
        val sdk = createSdk(listOf(popup("p-rating", listOf(Trigger.Event("RATING", 0.3)))))
        sdk.triggerEvent("RATING")
        assertTrue(sdk.debugQueuedPopupIds().isEmpty())
        assertEquals(listOf("p-rating"), sdk.debugDelayedPopupIds())
        delay(100)
        assertTrue(sdk.debugQueuedPopupIds().isEmpty())
        assertTrue(waitUntil { sdk.debugQueuedPopupIds().isNotEmpty() })
        assertEquals(listOf("p-rating"), sdk.debugQueuedPopupIds())
        assertTrue(sdk.debugDelayedPopupIds().isEmpty())
    }

    @Test
    fun click_with_delay_queues_after_the_delay() = runBlocking {
        val sdk = createSdk(listOf(popup("p-click", listOf(Trigger.Click("help", 0.3)))))
        sdk.triggerClick("help")
        assertTrue(sdk.debugQueuedPopupIds().isEmpty())
        assertTrue(waitUntil { sdk.debugQueuedPopupIds().isNotEmpty() })
        assertEquals(listOf("p-click"), sdk.debugQueuedPopupIds())
    }

    @Test
    fun click_with_zero_delay_queues_at_once() {
        val sdk = createSdk(listOf(popup("p-click", listOf(Trigger.Click("help")))))
        sdk.triggerClick("help")
        assertEquals(listOf("p-click"), sdk.debugQueuedPopupIds())
    }

    @Test
    fun scroll_with_delay_queues_after_the_delay() = runBlocking {
        val sdk = createSdk(listOf(popup("p-scroll", listOf(Trigger.Scroll(50, 0.3)))))
        sdk.onScroll(80)
        assertTrue(sdk.debugQueuedPopupIds().isEmpty())
        assertTrue(waitUntil { sdk.debugQueuedPopupIds().isNotEmpty() })
        assertEquals(listOf("p-scroll"), sdk.debugQueuedPopupIds())
    }

    @Test
    fun scroll_with_zero_delay_queues_at_once() {
        val sdk = createSdk(listOf(popup("p-scroll", listOf(Trigger.Scroll(50)))))
        sdk.onScroll(80)
        assertEquals(listOf("p-scroll"), sdk.debugQueuedPopupIds())
    }

    @Test
    fun time_on_page_with_delay_waits_value_plus_delay() = runBlocking {
        val sdk = createSdk(listOf(popup("p-time", listOf(Trigger.TimeOnPage(0.2, 0.3)))), autoLaunch = true)
        val start = currentTimeMillis()
        sdk.setPath("https://app.test/#/home")
        assertTrue(waitUntil { sdk.debugQueuedPopupIds().isNotEmpty() })
        assertTrue(currentTimeMillis() - start >= 500, "queued after ${currentTimeMillis() - start} ms")
    }

    @Test
    fun exit_uses_delaySeconds_as_the_admin_saves_it() = runBlocking {
        val sdk = createSdk(listOf(popup("p-exit", listOf(Trigger.Exit(0.3)))))
        sdk.setPath("https://app.test/#/login")
        sdk.setPath("https://app.test/#/home")
        assertTrue(sdk.debugQueuedPopupIds().isEmpty())
        assertTrue(waitUntil { sdk.debugQueuedPopupIds().isNotEmpty() })
        assertEquals(listOf("p-exit"), sdk.debugQueuedPopupIds())
    }

    // ---- espera: repeticiones, cancelación y re-evaluación ----

    @Test
    fun firing_again_while_waiting_neither_restarts_nor_duplicates() = runBlocking {
        val sdk = createSdk(listOf(popup("p-rating", listOf(Trigger.Event("RATING", 0.4)))))
        val start = currentTimeMillis()
        sdk.triggerEvent("RATING")
        delay(200)
        sdk.triggerEvent("RATING")
        assertTrue(waitUntil { sdk.debugQueuedPopupIds().isNotEmpty() })
        assertTrue(currentTimeMillis() - start < 550, "first due time kept")
        delay(400)
        assertEquals(listOf("p-rating"), sdk.debugQueuedPopupIds())
    }

    @Test
    fun changing_route_while_waiting_cancels_it() = runBlocking {
        val sdk = createSdk(listOf(popup("p-rating", listOf(Trigger.Event("RATING", 0.3)))))
        sdk.setPath("https://app.test/#/rating")
        sdk.triggerEvent("RATING")
        sdk.setPath("https://app.test/#/home")
        assertTrue(sdk.debugDelayedPopupIds().isEmpty())
        delay(500)
        assertTrue(sdk.debugQueuedPopupIds().isEmpty())
    }

    @Test
    fun same_route_reported_again_keeps_it_waiting() = runBlocking {
        val sdk = createSdk(listOf(popup("p-rating", listOf(Trigger.Event("RATING", 0.3)))))
        sdk.setPath("https://app.test/#/rating")
        sdk.triggerEvent("RATING")
        sdk.setPath("https://app.test/#/rating")
        assertTrue(waitUntil { sdk.debugQueuedPopupIds().isNotEmpty() })
    }

    @Test
    fun app_to_background_cancels_it() = runBlocking {
        val sdk = createSdk(listOf(popup("p-rating", listOf(Trigger.Event("RATING", 0.3)))))
        sdk.triggerEvent("RATING")
        sdk.onBackground()
        sdk.onForeground()
        delay(500)
        assertTrue(sdk.debugQueuedPopupIds().isEmpty())
    }

    @Test
    fun dropped_if_the_survey_was_completed_while_waiting() = runBlocking {
        val sdk = createSdk(
            listOf(
                popup(
                    "p-rating",
                    listOf(Trigger.Event("RATING", 0.3)),
                    cooldown = listOf(CooldownCondition(TriggerConditionStatus.COMPLETED, 30)),
                ),
            ),
        )
        sdk.triggerEvent("RATING")
        sdk.markSurveyAnswered("survey-p-rating")
        delay(500)
        assertTrue(sdk.debugQueuedPopupIds().isEmpty())
        assertTrue(sdk.debugDelayedPopupIds().isEmpty())
    }

    @Test
    fun dropped_if_the_same_popup_was_shown_while_waiting() = runBlocking {
        val storage = InMemoryStorage()
        val sdk = createSdk(listOf(popup("p-rating", listOf(Trigger.Event("RATING", 0.3)))), storage = storage)
        sdk.triggerEvent("RATING")
        delay(20)
        storage.putLong("popup_last_shown_p-rating", currentTimeMillis())
        delay(500)
        assertTrue(sdk.debugQueuedPopupIds().isEmpty())
    }

    private fun createSdk(
        popups: List<PopupDefinition>,
        storage: InMemoryStorage = InMemoryStorage(),
        autoLaunch: Boolean = false,
    ): DeepdotsPopups = DeepdotsPopups().apply {
        init(
            InitOptions(
                debug = true,
                popupOptions = PopupOptions(),
                storage = storage,
                autoLaunch = autoLaunch,
            ),
        )
        debugLoadPopups(popups)
    }

    private fun popup(
        id: String,
        triggers: List<Trigger>,
        cooldown: List<CooldownCondition> = emptyList(),
    ): PopupDefinition = PopupDefinition(
        id = id,
        title = id,
        message = "message",
        triggers = triggers,
        cooldown = cooldown,
        actions = Actions(),
        surveyId = "survey-$id",
        productId = "product-1",
        style = Style(theme = Theme.Light, position = Position.Center),
    )
}
