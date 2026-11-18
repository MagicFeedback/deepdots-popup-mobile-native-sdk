package com.deepdots.sdk.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.activity.ComponentActivity
import androidx.compose.foundation.gestures.awaitEachGesture
import com.deepdots.sdk.models.Action
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * DD-269 (sale de DD-209, Lemvigh-Müller): la X, Enviar y Atrás del popup tienen que responder a
 * un dedo real, que siempre se mueve un poco entre bajar y subir. Hasta 0.7.11 el velo consumía
 * esos MOVE y Compose cancelaba el clic; las pruebas con toques sin movimiento no lo veían.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class PopupFingerTapTest {

    @get:Rule
    val rule = createAndroidComposeRule<ComponentActivity>()

    private fun showLoadedPopup(onAction: (Action) -> Unit = {}) {
        rule.setContent { PopupView(popup = testPopup(), onAction = onAction) }
        rule.waitForIdle()
        val web = rule.activity.surveyWebView()
        web.emitFromSurvey("""{"name":"loaded","progress":0,"total":3}""")
        web.emitFromSurvey("""{"name":"ready"}""")
        rule.waitForIdle()
    }

    @Test
    fun closeButtonRespondsToAFingerTap() {
        val actions = mutableListOf<Action>()
        showLoadedPopup { actions += it }

        rule.onNodeWithContentDescription("Close popup").fingerTap()
        rule.waitForIdle()

        assertTrue(actions.single() is Action.Decline, "X should close the popup, got $actions")
    }

    @Test
    fun sendRespondsToAFingerTap() {
        showLoadedPopup()

        rule.onNodeWithText("Send").fingerTap()

        assertEquals(SurveyActionsJs.send, rule.activity.surveyWebView().lastSurveyCall())
    }

    @Test
    fun backRespondsToAFingerTap() {
        showLoadedPopup()
        val web = rule.activity.surveyWebView()
        web.emitFromSurvey("""{"name":"after_submit","progress":1,"total":3}""")
        rule.waitForIdle()

        rule.onNodeWithText("Back").fingerTap()

        assertEquals(SurveyActionsJs.back, web.lastSurveyCall())
    }

    @Test
    fun aFingerTapOnTheBackdropDoesNotReachTheHostApp() {
        var hostClicks = 0
        rule.setContent {
            Box(Modifier.fillMaxSize()) {
                Box(Modifier.fillMaxSize().testTag("host").clickable { hostClicks++ })
                Box(Modifier.fillMaxSize().testTag("backdrop").popupBackdropTouches())
            }
        }

        rule.onNodeWithTag("host").fingerTap()
        rule.waitForIdle()

        assertEquals(0, hostClicks)
    }

    /**
     * Comprueba la propia prueba: con el velo de 0.7.2–0.7.11 (consumía todo), el toque de dedo
     * cancela el botón y el toque sin movimiento no. Si un día `fingerTap` deja de mover, esta
     * prueba falla y avisa de que las de arriba ya no protegen nada.
     */
    @Test
    fun theFingerTapReproducesTheOldBackdropBug() {
        var clicks = 0
        rule.setContent {
            Box(Modifier.fillMaxSize().pointerInput(Unit) { consumeAllLikeBefore() }) {
                Button(onClick = { clicks++ }, modifier = Modifier.size(120.dp, 48.dp)) { Text("Old send") }
            }
        }

        rule.onNodeWithText("Old send").fingerTap()
        rule.waitForIdle()

        assertEquals(0, clicks, "with the old backdrop a finger tap must be lost")
    }
}

/** El velo hasta 0.7.11 (`consumeAllTouches`), solo para comprobar la prueba. */
private suspend fun androidx.compose.ui.input.pointer.PointerInputScope.consumeAllLikeBefore() {
    awaitEachGesture {
        do {
            val event = awaitPointerEvent()
            event.changes.forEach { it.consume() }
        } while (event.changes.any { it.pressed })
    }
}
