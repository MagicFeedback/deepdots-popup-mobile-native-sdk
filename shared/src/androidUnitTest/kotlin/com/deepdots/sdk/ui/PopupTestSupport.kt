package com.deepdots.sdk.ui

import android.os.Looper
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import androidx.activity.ComponentActivity
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.performTouchInput
import com.deepdots.sdk.models.Actions
import com.deepdots.sdk.models.PopupDefinition
import com.deepdots.sdk.models.Position
import com.deepdots.sdk.models.Style
import com.deepdots.sdk.models.Theme
import org.robolectric.Shadows.shadowOf

/**
 * Un toque como el de un dedo real: baja, se mueve unos px y sube. Maestro, `adb input tap` y el
 * toque del simulador no tienen ese movimiento, y por eso no vieron que la X, Enviar y Atrás no
 * respondían en móviles reales (DD-209, Lemvigh-Müller). `performClick()` tampoco lo tiene.
 */
internal fun SemanticsNodeInteraction.fingerTap(): SemanticsNodeInteraction = performTouchInput {
    down(center)
    moveBy(Offset(2f, 3f))
    moveBy(Offset(-1f, 1f))
    up()
}

internal fun testPopup(position: Position = Position.Center) = PopupDefinition(
    id = "popup-test",
    title = "How was your visit?",
    message = "",
    triggers = emptyList(),
    actions = Actions(),
    surveyId = "survey-test",
    productId = "product-test",
    style = Style(theme = Theme.Light, position = position),
)

/** El WebView del survey dentro de la Activity de la prueba. */
internal fun ComponentActivity.surveyWebView(): WebView =
    requireNotNull(findWebView(window.decorView)) { "no WebView in the test activity" }

private fun findWebView(v: View): WebView? = when (v) {
    is WebView -> v
    is ViewGroup -> (0 until v.childCount).firstNotNullOfOrNull { findWebView(v.getChildAt(it)) }
    else -> null
}

/**
 * Manda un evento por el puente JS como lo haría el survey (`DeepdotsBridge.emit`). En la JVM no
 * corre JavaScript, así que la prueba hace de survey: `loaded`, `after_submit`, `ready`…
 */
internal fun WebView.emitFromSurvey(json: String) {
    val bridge = requireNotNull(shadowOf(this).getJavascriptInterface("DeepdotsBridge"))
    val emit = bridge.javaClass.getMethod("emit", String::class.java).apply { isAccessible = true }
    emit.invoke(bridge, json)
    shadowOf(Looper.getMainLooper()).idle()
}

/** Lo último que el popup pidió al survey (`send`, `back`…), vía `evaluateJavascript`. */
internal fun WebView.lastSurveyCall(): String? {
    shadowOf(Looper.getMainLooper()).idle()
    return shadowOf(this).lastEvaluatedJavascript
}
