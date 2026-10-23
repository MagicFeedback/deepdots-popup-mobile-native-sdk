package com.deepdots.sdk.ui

import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Back con el teclado abierto. El callback de predictive back del popup va con
 * `PRIORITY_OVERLAY`, que gana también al del teclado: un solo Back ocultaba el teclado y
 * cerraba el popup, y el usuario perdía lo que estaba escribiendo. Con el teclado a la vista,
 * el Back solo lo oculta, como en cualquier pantalla de Android.
 */
class PopupBackActionTest {

    @Test
    fun conElTecladoAbiertoSoloLoOculta() {
        assertEquals(PopupBackAction.HideKeyboard, popupBackAction(keyboardVisible = true))
    }

    @Test
    fun sinTecladoCierraElPopup() {
        assertEquals(PopupBackAction.Close, popupBackAction(keyboardVisible = false))
    }
}
