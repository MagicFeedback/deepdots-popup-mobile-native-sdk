package com.deepdots.sdk

import com.deepdots.sdk.ui.SurveyAutofocus
import com.deepdots.sdk.ui.buildMagicFeedbackHtml
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Foco automático en la primera pregunta de la página cuando es de escribir. Paridad con el SDK
 * Web (`src/ui/autofocus.ts`).
 *
 * - Al abrir: solo con puntero fino. En un móvil el teclado taparía el popup sin que el usuario
 *   haya tocado nada, así que en la práctica en KMP solo actúa al navegar.
 * - Al navegar (Start, Siguiente, Atrás): siempre, porque el usuario acaba de pulsar.
 *
 * El chrome de KMP es nativo, pero el survey vive en el WebView: el foco se pone desde el HTML.
 */
class SurveyAutofocusTest {

    private fun html(): String = buildMagicFeedbackHtml(
        surveyId = "survey-abc",
        productId = "product-xyz",
        localAssetUrl = null,
        assetSize = null,
        bridgeEmitCall = "DeepdotsBridge.emit",
        isIOS = false,
    )

    @Test
    fun mismos_tipos_de_campo_que_el_sdk_web() {
        // Si los dos SDK no coinciden, la misma pregunta se enfoca en web y no en móvil.
        assertEquals(listOf("text", "email", "number", "tel", "url", "search"), SurveyAutofocus.INPUT_TYPES)
        assertTrue(SurveyAutofocus.AUTOFOCUS_JS.contains("""["text","email","number","tel","url","search"]"""))
    }

    @Test
    fun al_abrir_solo_con_puntero_fino() {
        val js = SurveyAutofocus.AUTOFOCUS_JS
        assertTrue(js.contains("function ddAutofocusFirstTextQuestion(root, trigger)"))
        assertTrue(js.contains("window.matchMedia('(pointer: fine)')"))
        assertTrue(js.contains("if(trigger==='open' && !fine){ return false; }"))
    }

    @Test
    fun el_html_lleva_el_helper_y_mira_el_wrapper_estable() {
        // `generate('mf-form')` renombra ese div: el wrapper es lo que sigue en el DOM.
        val h = html()
        assertTrue(h.contains("function ddAutofocusFirstTextQuestion(root, trigger)"))
        assertTrue(h.contains("function ddAutofocusPage(trigger){ ddAutofocusFirstTextQuestion(document.getElementById('dd-form-wrapper'), trigger); }"))
    }

    @Test
    fun la_primera_carga_es_apertura_y_las_siguientes_navegacion() {
        val h = html()
        assertTrue(
            Regex("onLoadedEvent[\\s\\S]*?ddAutofocusPage\\(ddSurveyLoaded \\? 'navigation' : 'open'\\); ddSurveyLoaded = true;").containsMatchIn(h),
            "la carga tras Start es navegación, no apertura",
        )
    }

    @Test
    fun enfoca_tras_siguiente_y_atras_pero_no_al_completar_ni_con_error() {
        val h = html()
        assertTrue(
            h.contains("else { emitJSON('after_submit', { error: err, completed: completed, progress: progress, total: total }); if (!err) { ddAutofocusPage('navigation'); } }"),
            "tras Siguiente, solo si la página ha cambiado",
        )
        assertTrue(
            Regex("onBackEvent[\\s\\S]{0,400}?if \\(!\\(args && args\\.error\\)\\) \\{ ddAutofocusPage\\('navigation'\\); \\}").containsMatchIn(h),
            "tras Atrás, salvo que no haya habido navegación",
        )
    }
}
