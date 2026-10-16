package com.deepdots.sdk.renderer

import android.view.KeyEvent
import kotlin.test.Test
import kotlin.test.assertEquals

/** Ruta legacy del Back (tecla `KEYCODE_BACK`) en el contenedor del popup. */
class BackKeyDecisionTest {

    private fun decide(
        keyCode: Int = KeyEvent.KEYCODE_BACK,
        action: Int = KeyEvent.ACTION_UP,
        canceled: Boolean = false,
        downTracked: Boolean = true,
        handling: Boolean = true,
    ) = backKeyDecision(keyCode, action, canceled, downTracked, handling)

    @Test
    fun fullBackPressClosesOnKeyUp() {
        assertEquals(BackKeyDecision.Consume, decide(action = KeyEvent.ACTION_DOWN, downTracked = false))
        assertEquals(BackKeyDecision.Close, decide(action = KeyEvent.ACTION_UP, downTracked = true))
    }

    @Test
    fun withoutHandlerBackGoesToTheHost() {
        assertEquals(BackKeyDecision.Pass, decide(action = KeyEvent.ACTION_DOWN, handling = false))
        assertEquals(BackKeyDecision.Pass, decide(action = KeyEvent.ACTION_UP, handling = false))
    }

    @Test
    fun otherKeysAreNeverIntercepted() {
        assertEquals(BackKeyDecision.Pass, decide(keyCode = KeyEvent.KEYCODE_ENTER))
        assertEquals(BackKeyDecision.Pass, decide(keyCode = KeyEvent.KEYCODE_DPAD_DOWN, action = KeyEvent.ACTION_DOWN))
    }

    @Test
    fun keyUpWithoutItsDownIsSwallowedButDoesNotClose() {
        // El DOWN fue al host (el popup aún no estaba a la vista): no se cierra al aparecer.
        assertEquals(BackKeyDecision.Consume, decide(action = KeyEvent.ACTION_UP, downTracked = false))
    }

    @Test
    fun canceledKeyUpDoesNotClose() {
        assertEquals(BackKeyDecision.Consume, decide(action = KeyEvent.ACTION_UP, canceled = true))
    }
}
