package com.deepdots.sdk

import com.deepdots.sdk.ui.buildMagicFeedbackHtml
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Foco automático en la primera pregunta de la página cuando es de escribir. Paridad con el SDK
 * Web (`renderPopup.ts` / `surveyHtml.ts`).
 *
 * Lo pone `@magicfeedback/native` con su opción `autofocus`: el HTML le pasa `'navigation'`
 * (tras Start, Siguiente o Atrás) y solo decide la apertura, y únicamente con puntero fino. En un
 * móvil el teclado taparía el popup sin que el usuario haya tocado nada.
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
    fun delega_la_navegacion_en_native() {
        assertTrue(
            Regex("form\\.generate\\('mf-form', \\{[\\s\\S]*?autofocus: 'navigation',").containsMatchIn(html()),
            "native enfoca tras Start, Siguiente y Atrás",
        )
    }

    @Test
    fun al_abrir_pide_el_foco_solo_con_puntero_fino() {
        val h = html()
        assertTrue(h.contains("window.matchMedia('(pointer: fine)').matches"))
        assertTrue(h.contains("if (fine && form && typeof form.focusFirstQuestion === 'function') { form.focusFirstQuestion(); }"))
    }

    @Test
    fun la_primera_carga_es_la_apertura_y_las_siguientes_las_enfoca_native() {
        assertTrue(
            Regex("onLoadedEvent[\\s\\S]*?if \\(!ddSurveyLoaded\\) \\{ ddSurveyLoaded = true; ddAutofocusOnOpen\\(form\\); \\}").containsMatchIn(html()),
        )
    }

    @Test
    fun no_lleva_logica_de_foco_propia() {
        val h = html()
        assertFalse(h.contains("ddAutofocusFirstTextQuestion"), "la lógica vive en native")
        assertFalse(h.contains("ddAutofocusPage"), "el popup no enfoca al navegar")
    }
}
