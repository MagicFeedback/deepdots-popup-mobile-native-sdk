package com.deepdots.sdk.ui

import android.app.Activity
import android.graphics.Rect
import android.os.SystemClock
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.performTouchInput
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.test.platform.app.InstrumentationRegistry
import com.deepdots.sdk.models.Actions
import com.deepdots.sdk.models.PopupDefinition
import com.deepdots.sdk.models.Position
import com.deepdots.sdk.models.Style
import com.deepdots.sdk.models.Theme
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference

/**
 * Datos de DEV (MAGICFEEDBACK_DEV) que usan las pruebas en dispositivo, los mismos que las test
 * apps (`test-apps/scenarios.json`): survey «SDK Test App · Simple» (página 1 NPS 0–10, que pasa
 * de página sola al elegir; página 2 texto libre «What could we improve?»). Nada de clientes.
 * Cada envío deja una respuesta en DEV, como la regresión de Maestro.
 */
internal object DevFixture {
    const val ENV = "dev"
    const val PUBLIC_KEY = "ll3lGXsyQYiPgeZfaN7HDgIkQBoLj0x2" // escenario help-click
    const val PRODUCT_KEY = "3c75ebc69b72f7cf1e4a6774a8e78711"
    const val SIMPLE_SURVEY = "5581b5a0-d2df-11f1-b287-1be76c3f931d"
    const val TITLE = "Device check (DD-273)"
}

internal fun devicePopup() = PopupDefinition(
    id = "dd-273-device-check",
    title = DevFixture.TITLE,
    message = "",
    triggers = emptyList(),
    actions = Actions(),
    surveyId = DevFixture.SIMPLE_SURVEY,
    productId = DevFixture.PRODUCT_KEY,
    style = Style(theme = Theme.Light, position = Position.Center),
)

/**
 * Un toque como el de un dedo real: baja, se mueve unos px y sube (igual que en DD-269). Aquí
 * llega a la vista de verdad del dispositivo, con su densidad y su `touchSlop`.
 */
internal fun SemanticsNodeInteraction.fingerTap(): SemanticsNodeInteraction = performTouchInput {
    down(center)
    moveBy(Offset(2f, 3f))
    moveBy(Offset(-1f, 1f))
    up()
}

/** Toque de dedo inyectado en la pantalla (coordenadas de pantalla), para tocar dentro del WebView. */
internal fun fingerTapOnScreen(x: Float, y: Float) {
    val inst = InstrumentationRegistry.getInstrumentation()
    val down = SystemClock.uptimeMillis()
    fun send(action: Int, dx: Float, dy: Float, t: Long) {
        val e = MotionEvent.obtain(down, t, action, x + dx, y + dy, 0)
        inst.sendPointerSync(e)
        e.recycle()
    }
    send(MotionEvent.ACTION_DOWN, 0f, 0f, down)
    send(MotionEvent.ACTION_MOVE, 2f, 3f, down + 30)
    send(MotionEvent.ACTION_MOVE, 1f, 4f, down + 60)
    send(MotionEvent.ACTION_UP, 1f, 4f, down + 90)
}

internal fun Activity.surveyWebView(): WebView? = findWebView(window.decorView)

private fun findWebView(v: View): WebView? = when (v) {
    is WebView -> v
    is ViewGroup -> (0 until v.childCount).firstNotNullOfOrNull { findWebView(v.getChildAt(it)) }
    else -> null
}

/** Corre JS en el survey y devuelve el resultado (JSON de `evaluateJavascript`). */
internal fun WebView.runJs(script: String, timeoutSec: Long = 10): String? {
    val result = AtomicReference<String?>()
    val latch = CountDownLatch(1)
    post { evaluateJavascript(script) { result.set(it); latch.countDown() } }
    check(latch.await(timeoutSec, TimeUnit.SECONDS)) { "JS did not answer in ${timeoutSec}s: $script" }
    return result.get()
}

/**
 * Borde superior del teclado en coordenadas de la ventana, o `null` si no hay teclado. API 30+:
 * el inset del IME. Antes: la parte visible de la ventana (como `legacyKeyboardPadding`).
 */
internal fun Activity.keyboardTopInWindow(): Int? {
    val decor = window.decorView
    val ime = ViewCompat.getRootWindowInsets(decor)
    if (ime != null && ime.isVisible(WindowInsetsCompat.Type.ime())) {
        val bottom = ime.getInsets(WindowInsetsCompat.Type.ime()).bottom
        if (bottom > 0) return decor.height - bottom
    }
    val frame = Rect()
    decor.getWindowVisibleDisplayFrame(frame)
    val loc = IntArray(2).also { decor.getLocationOnScreen(it) }
    val visibleBottom = frame.bottom - loc[1]
    return if (legacyKeyboardHeight(decor.height, visibleBottom) > 0) visibleBottom else null
}
