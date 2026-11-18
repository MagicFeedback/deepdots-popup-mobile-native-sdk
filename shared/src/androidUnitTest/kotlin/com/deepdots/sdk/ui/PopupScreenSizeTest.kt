package com.deepdots.sdk.ui

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.unit.dp
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import kotlin.math.roundToInt
import kotlin.test.assertEquals

/**
 * DD-270 (sale de DD-209, Lemvigh-Müller): el popup en varios tamaños de pantalla, sin emulador.
 *
 * - La encuesta tiene que llenar todo el hueco que le da el popup. Hasta 0.7.11 el WebView medía
 *   520 dp fijos: en una tablet el hueco era más alto y quedaba una franja en blanco.
 * - Capturas del popup en móvil pequeño, móvil alto y tablet (vertical y horizontal), comparadas
 *   con las de referencia en `shared/src/androidUnitTest/screenshots/`. Grabar de nuevo:
 *   `./gradlew :shared:recordRoborazziDebug`; comprobar: `:shared:verifyRoborazziDebug`.
 *   En la JVM el WebView sale vacío: las capturas cubren el chrome nativo y su tamaño.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [35])
class PopupScreenSizeTest {

    @get:Rule
    val rule = createAndroidComposeRule<ComponentActivity>()

    @Test
    @Config(qualifiers = "w800dp-h1280dp-mdpi")
    fun surveyFillsATallSlot() {
        rule.setContent {
            Box(Modifier.size(600.dp, 900.dp)) {
                SurveyView(
                    surveyId = "survey-test",
                    productId = "product-test",
                    backgroundColor = Color.White,
                    onEvent = {},
                )
            }
        }
        rule.waitForIdle()

        val density = rule.activity.resources.displayMetrics.density
        val web = rule.activity.surveyWebView()
        // measured*: en Robolectric el WebView se mide pero su layout() no guarda el tamaño.
        assertEquals((900 * density).roundToInt(), web.measuredHeight, "survey height in px")
        assertEquals((600 * density).roundToInt(), web.measuredWidth, "survey width in px")
    }

    @Test
    @Config(qualifiers = "w360dp-h640dp-xhdpi")
    fun smallPhone() = capture("small-phone")

    @Test
    @Config(qualifiers = "w412dp-h915dp-xxhdpi")
    fun tallPhone() = capture("tall-phone")

    @Test
    @Config(qualifiers = "w800dp-h1280dp-xhdpi")
    fun tabletPortrait() = capture("tablet-portrait")

    @Test
    @Config(qualifiers = "w1280dp-h800dp-xhdpi")
    fun tabletLandscape() = capture("tablet-landscape")

    /** Popup con una página de encuesta alta (una escala 0–10 en lista, como la de Lemu). */
    private fun capture(name: String) {
        rule.setContent { PopupView(popup = testPopup(), onAction = {}) }
        rule.waitForIdle()
        val web = rule.activity.surveyWebView()
        web.emitFromSurvey("""{"name":"loaded","progress":0,"total":3}""")
        web.emitFromSurvey("""{"name":"content_height","height":760}""")
        web.emitFromSurvey("""{"name":"ready"}""")
        rule.waitForIdle()
        rule.onRoot().captureRoboImage("src/androidUnitTest/screenshots/popup-$name.png")
    }
}
