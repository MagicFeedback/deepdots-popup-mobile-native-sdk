package com.deepdots.sdk.ui

import android.os.Looper
import android.os.SystemClock
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.activity.ComponentActivity
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import com.deepdots.sdk.models.Action
import com.deepdots.sdk.models.Position
import com.deepdots.sdk.renderer.PopupContainer
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * DD-334: el popup en una esquina con `blockPage = false` deja la app usable. Se monta como lo
 * hace el renderer (vistas del host debajo, el ComposeView del popup encima a pantalla completa)
 * y se toca con un dedo de verdad (baja, se mueve, sube) fuera de la tarjeta.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class PopupBlockPageTouchTest {

    @get:Rule
    val rule = createAndroidComposeRule<ComponentActivity>()

    private var hostClicks = 0

    private fun showOverHost(blockPage: Boolean?, position: Position = Position.TopRight, onAction: (Action) -> Unit = {}) {
        rule.runOnUiThread {
            val activity = rule.activity
            val host = View(activity).apply {
                layoutParams = FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
                setOnClickListener { hostClicks++ }
            }
            val popup = ComposeView(activity).apply {
                layoutParams = FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
                setContent {
                    PopupView(
                        popup = testPopup(position).let { it.copy(style = it.style.copy(blockPage = blockPage)) },
                        onAction = onAction,
                    )
                }
            }
            // Como el renderer: el ComposeView va dentro de PopupContainer (enfocable, se come el Back).
            val container = PopupContainer(activity).apply {
                layoutParams = FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
                addView(popup)
            }
            activity.setContentView(FrameLayout(activity).apply { addView(host); addView(container) })
        }
        rule.waitForIdle()
        val web = rule.activity.surveyWebView()
        web.emitFromSurvey("""{"name":"loaded","progress":0,"total":3}""")
        web.emitFromSurvey("""{"name":"ready"}""")
        rule.waitForIdle()
    }

    /** Dedo real en la esquina de abajo a la izquierda, lejos de la tarjeta (arriba a la derecha). */
    private fun fingerTapBottomLeft() {
        val root = rule.activity.window.decorView
        val x = 20f
        val y = root.height - 40f
        val down = SystemClock.uptimeMillis()
        fun send(action: Int, dx: Float, dy: Float, t: Long) {
            val ev = MotionEvent.obtain(down, down + t, action, x + dx, y + dy, 0)
            rule.runOnUiThread { root.dispatchTouchEvent(ev) }
            ev.recycle()
        }
        send(MotionEvent.ACTION_DOWN, 0f, 0f, 0)
        send(MotionEvent.ACTION_MOVE, 2f, 3f, 16)
        send(MotionEvent.ACTION_MOVE, 1f, 4f, 32)
        send(MotionEvent.ACTION_UP, 1f, 4f, 48)
        shadowOf(Looper.getMainLooper()).idle()
        rule.waitForIdle()
    }

    @Test
    fun nonBlockingCornerPopupLetsTheAppBehindBeTapped() {
        showOverHost(blockPage = false)
        fingerTapBottomLeft()
        assertEquals(1, hostClicks, "a tap outside the card must reach the app")
    }

    @Test
    fun blockingPopupKeepsTheTap() {
        showOverHost(blockPage = true)
        fingerTapBottomLeft()
        assertEquals(0, hostClicks)
    }

    @Test
    fun withoutBlockPageItBlocksLikeBefore() {
        showOverHost(blockPage = null)
        fingerTapBottomLeft()
        assertEquals(0, hostClicks)
    }

    @Test
    fun centerBlocksEvenWithBlockPageFalse() {
        showOverHost(blockPage = false, position = Position.Center)
        fingerTapBottomLeft()
        assertEquals(0, hostClicks)
    }

    @Test
    fun theCloseButtonStillAnswersAFingerOnANonBlockingPopup() {
        val actions = mutableListOf<Action>()
        showOverHost(blockPage = false) { actions += it }
        rule.onNodeWithContentDescription("Close popup").fingerTap()
        rule.waitForIdle()
        assertTrue(actions.single() is Action.Decline, "X should close the popup, got $actions")
        assertEquals(0, hostClicks)
    }
}
