package com.deepdots.sdk.ui

import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Enviar en la primera pantalla sin responder una pregunta obligatoria: el WebView manda
 * `validation_error_required` y detrás `after_submit` con `error`. La página no cambia, así que
 * la profundidad tampoco, y el footer no enseña Atrás.
 */
class PageDepthTest {

    @Test
    fun unEnvioCorrectoAvanzaUnaPagina() {
        assertEquals(1, pageDepthAfterSubmit(current = 0, error = null))
        assertEquals(3, pageDepthAfterSubmit(current = 2, error = ""))
    }

    @Test
    fun unaObligatoriaSinResponderNoAvanza() {
        assertEquals(0, pageDepthAfterSubmit(current = 0, error = "SubmitError: No response"))
    }

    @Test
    fun unErrorDeRedNoAvanza() {
        assertEquals(2, pageDepthAfterSubmit(current = 2, error = "Network down"))
    }
}
