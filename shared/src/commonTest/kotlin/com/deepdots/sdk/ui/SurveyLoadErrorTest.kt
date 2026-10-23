package com.deepdots.sdk.ui

import com.deepdots.sdk.i18n.DefaultLabels
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Survey que no carga (clave o producto inexistentes, sin red, CDN caído). Antes el popup no se
 * enteraba: `@magicfeedback/native` avisa del fallo por `onLoadedEvent({ error })`, el HTML lo
 * reenviaba como una carga correcta y el popup enseñaba un "Send" sobre un survey vacío, sin
 * forma de cerrarlo salvo el Back de Android (en iOS el usuario quedaba atrapado).
 */
class SurveyLoadErrorTest {

    private fun html(): String = buildMagicFeedbackHtml(
        surveyId = "survey-abc",
        productId = "product-xyz",
        localAssetUrl = null,
        assetSize = null,
        bridgeEmitCall = "DeepdotsBridge.emit",
        isIOS = false,
    )

    @Test
    fun todo_fallo_de_carga_del_webview_cuenta_como_error() {
        listOf(PopupReveal.LOAD_ERROR_EVENT, "error:timeout", "error:init", "error:module", "error:module-load")
            .forEach { assertTrue(isSurveyLoadError(it), it) }
    }

    @Test
    fun los_errores_de_validacion_y_de_envio_no_son_de_carga() {
        // Esos dejan al usuario seguir en el survey: no deben convertir el popup en un error.
        listOf(PopupReveal.LOADED_EVENT, "submit_error", "validation_error_required", "after_submit")
            .forEach { assertFalse(isSurveyLoadError(it), it) }
    }

    @Test
    fun el_html_reenvia_el_error_de_native_en_vez_de_un_loaded() {
        val h = html()
        val errorCheck = h.indexOf("if (args && args.error)")
        val loaded = h.indexOf("emitJSON('${PopupReveal.LOADED_EVENT}'")
        assertTrue(errorCheck >= 0, "onLoadedEvent debe mirar args.error")
        assertTrue(loaded >= 0)
        assertTrue(errorCheck < loaded, "el error se comprueba antes de avisar de la carga")
        assertTrue(h.contains("emitJSON('${PopupReveal.LOAD_ERROR_EVENT}'"), "debe avisar del fallo por el puente")
    }

    @Test
    fun el_aviso_de_error_esta_traducido_en_todos_los_idiomas() {
        val english = DefaultLabels.labels("en").errorLoad
        assertEquals("We couldn't load this survey.", english)
        DefaultLabels.supportedLanguages.forEach { lang ->
            val text = DefaultLabels.labels(lang).errorLoad
            assertTrue(text.isNotBlank(), lang)
            if (lang != "en") assertTrue(text != english, "$lang sigue en inglés")
        }
    }
}
