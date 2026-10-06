package com.deepdots.sdk

import com.deepdots.sdk.models.Actions
import com.deepdots.sdk.models.Event
import com.deepdots.sdk.models.EventData
import com.deepdots.sdk.models.InitOptions
import com.deepdots.sdk.models.PopupDefinition
import com.deepdots.sdk.models.PopupOptions
import com.deepdots.sdk.models.Position
import com.deepdots.sdk.models.Style
import com.deepdots.sdk.models.Theme
import com.deepdots.sdk.models.Trigger
import com.deepdots.sdk.models.TriggerConditionStatus
import com.deepdots.sdk.storage.InMemoryStorage
import com.deepdots.sdk.ui.PopupReveal
import com.deepdots.sdk.ui.buildMagicFeedbackHtml
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Mensajes del puente del WebView que llegan a los listeners del host.
 *
 * Bug reportado desde un host Flutter: tras `popup_shown` llegaba un `popup_clicked` sin que el
 * usuario tocase nada (también tras un 406 al cargar el survey). Era el aviso de "survey cargado"
 * del WebView, que viajaba con el nombre `popup_clicked` para llevarle a la capa nativa el estilo,
 * el idioma y el progreso; `handleSurveyRuntimeEvent` lo reenviaba al host como un click.
 */
class SurveyBridgeEventsTest {

    private val loadedPayload =
        """{"name":"loaded","payload":{"style":{"buttonPrimaryColor":"#112233"},"surveyLang":"da","progress":0,"total":6}}"""

    @Test
    fun html_avisa_de_la_carga_con_loaded_y_no_con_popup_clicked() {
        val html = html()
        assertFalse(
            html.contains("emitJSON('popup_clicked'"),
            "el aviso de carga no puede viajar como popup_clicked: el host lo recibiría como un click",
        )
        // El payload del aviso de carga lo siguen necesitando el chrome nativo (estilo, idioma
        // de botones y dirección RTL) y la barra de progreso.
        assertTrue(
            html.contains(
                "emitJSON('${PopupReveal.LOADED_EVENT}', { style: style, surveyLang: surveyLang, " +
                    "progress: form.progress || 0, total: form.total || 0 })",
            ),
            "el loaded debe llevar style, surveyLang, progress y total",
        )
    }

    @Test
    fun la_carga_del_survey_no_llega_al_host_como_click() = runBlocking {
        val sdk = createSdk()
        val clicks = listenClicks(sdk)

        sdk.debugHandleSurveyRuntimeEvent("popup-1", PopupReveal.LOADED_EVENT, loadedPayload)
        // Defensa: un popup_clicked del puente sin `action` (la forma del aviso de carga antiguo)
        // tampoco es una interacción del usuario.
        sdk.debugHandleSurveyRuntimeEvent(
            "popup-1",
            "popup_clicked",
            """{"name":"popup_clicked","payload":{"style":null,"surveyLang":"","progress":0,"total":0}}""",
        )
        delay(200)

        assertTrue(clicks.isEmpty(), "la carga no debe emitir PopupClicked, llegó: $clicks")
        assertNull(sdk.debugProgressStatus("survey-1"), "la carga no debe marcar PARTIAL")
    }

    @Test
    fun una_interaccion_real_sigue_llegando_al_host_y_marca_partial() = runBlocking {
        val sdk = createSdk()
        val clicks = listenClicks(sdk)

        sdk.debugHandleSurveyRuntimeEvent(
            "popup-1",
            "popup_clicked",
            """{"name":"popup_clicked","payload":{"action":"partial"}}""",
        )
        waitUntil { clicks.isNotEmpty() }

        assertEquals(1, clicks.size)
        assertEquals("partial", clicks.first().extra["action"])
        assertEquals("popup-1", clicks.first().popupId)
        assertEquals(TriggerConditionStatus.PARTIAL, sdk.debugProgressStatus("survey-1"))
    }

    private fun html(): String = buildMagicFeedbackHtml(
        surveyId = "survey-1",
        productId = "product-1",
        localAssetUrl = null,
        assetSize = null,
        bridgeEmitCall = "DeepdotsBridge.emit",
        isIOS = false,
    )

    /** El registro del listener es asíncrono (`scope.launch`): se espera a que quede puesto. */
    private suspend fun listenClicks(sdk: DeepdotsPopups): MutableList<EventData> {
        val clicks = mutableListOf<EventData>()
        sdk.on(Event.PopupClicked) { clicks.add(it) }
        delay(100)
        return clicks
    }

    private suspend fun waitUntil(timeoutMs: Long = 2000, condition: () -> Boolean) {
        var waited = 0L
        while (!condition() && waited < timeoutMs) {
            delay(20)
            waited += 20
        }
    }

    private fun createSdk(): DeepdotsPopups = DeepdotsPopups().apply {
        init(
            InitOptions(
                debug = true,
                popupOptions = PopupOptions(),
                storage = InMemoryStorage(),
            ),
        )
        debugLoadPopups(
            listOf(
                PopupDefinition(
                    id = "popup-1",
                    title = "popup-1",
                    message = "message",
                    triggers = listOf(Trigger.Event("search")),
                    cooldown = emptyList(),
                    actions = Actions(),
                    surveyId = "survey-1",
                    productId = "product-1",
                    style = Style(theme = Theme.Light, position = Position.Center),
                ),
            ),
        )
    }
}
