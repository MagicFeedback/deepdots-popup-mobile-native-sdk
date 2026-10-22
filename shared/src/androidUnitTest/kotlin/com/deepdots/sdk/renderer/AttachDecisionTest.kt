package com.deepdots.sdk.renderer

import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * El popup se monta en la ventana de la Activity. Un diálogo, un bottom sheet o un date picker
 * del host son OTRA ventana, encima: el popup quedaba debajo de su velo, sin poder tocarse. Con
 * uno de ellos abierto la ventana de la Activity no tiene el foco, así que el popup espera a
 * recuperarlo (el host ha cerrado su modal) en vez de pintarse debajo.
 */
class AttachDecisionTest {

    @Test
    fun conLaVentanaEnfocadaSeMuestraYa() {
        assertEquals(AttachDecision.Now, popupAttachDecision(hasWindowFocus = true, finishing = false))
    }

    @Test
    fun conUnModalDelHostEncimaEspera() {
        assertEquals(AttachDecision.WaitForFocus, popupAttachDecision(hasWindowFocus = false, finishing = false))
    }

    @Test
    fun siLaActivitySeEstaCerrandoNoSeMuestra() {
        // Esperar al foco de una Activity que se va no tiene sentido: no volverá.
        assertEquals(AttachDecision.Drop, popupAttachDecision(hasWindowFocus = true, finishing = true))
        assertEquals(AttachDecision.Drop, popupAttachDecision(hasWindowFocus = false, finishing = true))
    }
}
