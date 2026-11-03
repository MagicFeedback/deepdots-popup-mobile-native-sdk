package com.deepdots.sdk

import com.deepdots.sdk.models.Trigger
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Un popup que la API manda con un solo trigger se carga con un solo trigger. El parser ya
 * devuelve la definición normalizada y la carga la normaliza otra vez; antes esa segunda pasada
 * sumaba `trigger` a `triggers`, así que un `time_on_page` de 5 s programaba dos temporizadores
 * que saltaban a la vez y, hasta 0.7.5, el popup salía dos veces al arrancar la app.
 */
class LoadedTriggersTest {

    private val payload = """
        [{"id":"popup-time","title":"","message":"",
          "triggers":[{"type":"time_on_page","value":5,"delaySeconds":0}],
          "cooldown":[{"answered":"SHOWED","cooldownDays":7}],
          "conditions":[{"answered":false,"cooldownDays":3}],
          "actions":{},"style":{"theme":"light","position":"center"},
          "segments":{"lang":["en"],"path":["/"]},
          "surveyId":"survey-time","productId":"product-1"}]
    """.trimIndent()

    @Test
    fun a_single_api_trigger_is_loaded_once() {
        val sdk = DeepdotsPopups()
        sdk.debugLoadPopups(sdk.debugParseServerPayload(payload))

        val loaded = sdk.debugLoadedPopup("popup-time")!!
        assertEquals(listOf<Trigger>(Trigger.TimeOnPage(5.0)), loaded.triggers)
        assertEquals(1, loaded.legacyConditions.size)
        assertEquals(1, loaded.conditions.size)
    }

    @Test
    fun loading_the_same_popup_again_does_not_add_triggers() {
        val sdk = DeepdotsPopups()
        val parsed = sdk.debugParseServerPayload(payload)
        sdk.debugLoadPopups(parsed)
        sdk.debugLoadPopups(listOf(sdk.debugLoadedPopup("popup-time")!!))

        assertEquals(1, sdk.debugLoadedPopup("popup-time")!!.triggers.size)
    }

    @Test
    fun a_legacy_trigger_and_a_different_trigger_are_both_kept() {
        val sdk = DeepdotsPopups()
        val base = sdk.debugParseServerPayload(payload).single()
        sdk.debugLoadPopups(listOf(base.copy(trigger = Trigger.Event("app_opened"))))

        assertEquals(
            listOf(Trigger.Event("app_opened"), Trigger.TimeOnPage(5.0)),
            sdk.debugLoadedPopup("popup-time")!!.triggers,
        )
    }
}
