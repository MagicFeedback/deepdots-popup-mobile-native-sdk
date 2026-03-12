package com.deepdots.sdk

import com.deepdots.sdk.models.Actions
import com.deepdots.sdk.models.CooldownCondition
import com.deepdots.sdk.models.InitOptions
import com.deepdots.sdk.models.PopupDefinition
import com.deepdots.sdk.models.PopupOptions
import com.deepdots.sdk.models.Position
import com.deepdots.sdk.models.Segments
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
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class DeepdotsPopupsParityTest {

    @Test
    fun event_trigger_queues_matching_popup_when_path_matches() {
        val sdk = createSdk(
            popups = listOf(
                popup(
                    id = "popup-search",
                    surveyId = "survey-search",
                    triggers = listOf(Trigger.Event("search")),
                    segments = Segments(path = listOf("/#/home")),
                ),
            ),
        )

        sdk.setPath("https://app.test/#/home")
        sdk.triggerEvent("search")

        assertEquals(listOf("popup-search"), sdk.debugQueuedPopupIds())
    }

    @Test
    fun event_trigger_does_not_queue_when_path_does_not_match() {
        val sdk = createSdk(
            popups = listOf(
                popup(
                    id = "popup-search",
                    surveyId = "survey-search",
                    triggers = listOf(Trigger.Event("search")),
                    segments = Segments(path = listOf("/#/game")),
                ),
            ),
        )

        sdk.setPath("https://app.test/#/home")
        sdk.triggerEvent("search")

        assertTrue(sdk.debugQueuedPopupIds().isEmpty())
    }

    @Test
    fun one_of_multiple_triggers_can_queue_the_popup() {
        val sdk = createSdk(
            popups = listOf(
                popup(
                    id = "popup-multi",
                    surveyId = "survey-multi",
                    triggers = listOf(
                        Trigger.Scroll(95),
                        Trigger.Event("search"),
                    ),
                ),
            ),
        )

        sdk.triggerEvent("search")

        assertEquals(listOf("popup-multi"), sdk.debugQueuedPopupIds())
    }

    @Test
    fun exit_trigger_queues_only_the_popup_matching_the_source_path_even_when_survey_is_shared() {
        val sdk = createSdk(
            popups = listOf(
                popup(
                    id = "popup-login-exit",
                    surveyId = "shared-survey",
                    triggers = listOf(Trigger.Exit(0.0)),
                    segments = Segments(path = listOf("/#/login")),
                ),
                popup(
                    id = "popup-game-exit",
                    surveyId = "shared-survey",
                    triggers = listOf(Trigger.Exit(0.0)),
                    segments = Segments(path = listOf("/#/game")),
                ),
            ),
        )

        sdk.setPath("https://app.test/#/game")
        sdk.setPath("https://app.test/#/home")

        assertEquals(listOf("popup-game-exit"), sdk.debugQueuedPopupIds())
        assertTrue(sdk.debugDeferredExitQueue().isEmpty())
    }

    @Test
    fun exit_trigger_respects_delay_before_queueing_on_destination_path() = runBlocking {
        val sdk = createSdk(
            popups = listOf(
                popup(
                    id = "popup-exit-delay",
                    surveyId = "survey-exit-delay",
                    triggers = listOf(Trigger.Exit(0.05)),
                    segments = Segments(path = listOf("/#/login")),
                ),
            ),
        )

        sdk.setPath("https://app.test/#/login")
        sdk.setPath("https://app.test/#/home")

        assertTrue(sdk.debugQueuedPopupIds().isEmpty())
        delay(80)
        assertEquals(listOf("popup-exit-delay"), sdk.debugQueuedPopupIds())
    }

    @Test
    fun showed_cooldown_blocks_popup_when_last_shown_is_recent() {
        val storage = InMemoryStorage()
        storage.putLong("popup_last_shown_popup-showed", currentTimeMillis())

        val sdk = createSdk(
            popups = listOf(
                popup(
                    id = "popup-showed",
                    surveyId = "survey-showed",
                    triggers = listOf(Trigger.Event("search")),
                    cooldown = listOf(
                        CooldownCondition(
                            answered = TriggerConditionStatus.SHOWED,
                            cooldownDays = 7,
                        ),
                    ),
                ),
            ),
            storage = storage,
        )

        assertFalse(sdk.debugShouldShowPopup("popup-showed"))
    }

    @Test
    fun partial_cooldown_blocks_popup_after_partial_progress() {
        val sdk = createSdk(
            popups = listOf(
                popup(
                    id = "popup-partial",
                    surveyId = "survey-partial",
                    triggers = listOf(Trigger.Event("search")),
                    cooldown = listOf(
                        CooldownCondition(
                            answered = TriggerConditionStatus.PARTIAL,
                            cooldownDays = 7,
                        ),
                    ),
                ),
            ),
        )

        sdk.debugHandleSurveyRuntimeEvent(
            popupId = "popup-partial",
            name = "popup_clicked",
            payload = """{"name":"popup_clicked","payload":{"action":"partial"}}""",
        )

        assertEquals(TriggerConditionStatus.PARTIAL, sdk.debugProgressStatus("survey-partial"))
        assertFalse(sdk.debugShouldShowPopup("popup-partial"))
    }

    @Test
    fun completed_cooldown_blocks_popup_after_survey_completed() {
        val sdk = createSdk(
            popups = listOf(
                popup(
                    id = "popup-completed",
                    surveyId = "survey-completed",
                    triggers = listOf(Trigger.Event("search")),
                    cooldown = listOf(
                        CooldownCondition(
                            answered = TriggerConditionStatus.COMPLETED,
                            cooldownDays = 7,
                        ),
                    ),
                ),
            ),
        )

        sdk.surveyCompletedFromJs("survey-completed")

        assertEquals(TriggerConditionStatus.COMPLETED, sdk.debugProgressStatus("survey-completed"))
        assertFalse(sdk.debugShouldShowPopup("popup-completed"))
    }

    @Test
    fun server_payload_with_trigger_array_is_parsed_correctly() {
        val sdk = DeepdotsPopups()

        val popups = sdk.debugParseServerPayload(
            """
            [
              {
                "id": "popup-exit-1",
                "title": "",
                "message": "",
                "triggers": [{"type":"exit","value":0}],
                "cooldown": null,
                "conditions": [],
                "actions": {},
                "style": {"theme":"light","position":"center","imageUrl":null},
                "segments": {"lang":["en"],"path":["/detail/3"]},
                "surveyId": "survey-exit-1",
                "productId": "product-1"
              }
            ]
            """.trimIndent(),
        )

        assertEquals(1, popups.size)
        assertEquals("popup-exit-1", popups.first().id)
        assertEquals(listOf(Trigger.Exit(0.0)), popups.first().triggers)
        assertEquals(listOf("/detail/3"), popups.first().segments?.path)
    }

    private fun createSdk(
        popups: List<PopupDefinition>,
        storage: InMemoryStorage = InMemoryStorage(),
    ): DeepdotsPopups {
        return DeepdotsPopups().apply {
            init(
                InitOptions(
                    debug = true,
                    popupOptions = PopupOptions(popups = popups),
                    storage = storage,
                ),
            )
        }
    }

    private fun popup(
        id: String,
        surveyId: String,
        triggers: List<Trigger>,
        cooldown: List<CooldownCondition> = emptyList(),
        segments: Segments? = null,
    ): PopupDefinition {
        return PopupDefinition(
            id = id,
            title = id,
            message = "message",
            triggers = triggers,
            cooldown = cooldown,
            actions = Actions(),
            surveyId = surveyId,
            productId = "product-1",
            style = Style(theme = Theme.Light, position = Position.Center),
            segments = segments,
        )
    }
}
