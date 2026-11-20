package com.deepdots.sdk.ui

import android.os.SystemClock

import android.view.View
import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.assertIsDisplayed
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.deepdots.sdk.SdkRuntime
import com.deepdots.sdk.models.Action
import com.deepdots.sdk.platform.PlatformContext
import com.deepdots.sdk.renderer.PopupRenderer
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assume.assumeTrue
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.junit.rules.TestRule
import org.junit.rules.Timeout
import java.util.concurrent.TimeUnit
import org.junit.runner.RunWith
import java.util.concurrent.CopyOnWriteArrayList
import kotlin.math.abs
import kotlin.test.assertTrue

/**
 * DD-273: las comprobaciones clave del popup en un dispositivo de verdad (Firebase Test Lab, ver
 * `testlab/devices.yml`) o en un emulador. Son las de DD-269/DD-270, pero aquí el popup sale por
 * el camino real (`PopupRenderer` sobre el decorView de la Activity), con el WebView del sistema
 * y el survey «Simple» de DEV:
 *
 * - el popup se abre con el survey cargado;
 * - X, Enviar y Atrás responden a un toque de dedo (baja, se mueve, sube);
 * - con el teclado abierto en la pregunta de texto, Enviar queda por encima del teclado y la X cierra;
 * - en una tablet el survey llena todo su hueco (DD-270: antes 520 dp fijos y una franja en blanco).
 *
 * Necesitan red (DEV + CDN del survey). Cada una deja como mucho una respuesta en MAGICFEEDBACK_DEV.
 */
@RunWith(AndroidJUnit4::class)
class PopupDeviceChecksTest {

    val rule = createAndroidComposeRule<ComponentActivity>()

    // A hung test fails alone after TEST_TIMEOUT_S instead of eating the whole 10 min Test Lab
    // matrix (Xiaomi 14, run 44774904996: the activity never opened and nothing else ran).
    @get:Rule
    val chain: TestRule = RuleChain
        .outerRule(Timeout.builder().withTimeout(TEST_TIMEOUT_S, TimeUnit.SECONDS).withLookingForStuckThread(true).build())
        .around(rule)

    private val events = CopyOnWriteArrayList<String>()
    private val actions = CopyOnWriteArrayList<Action>()

    @Before
    fun devEnvironment() {
        SdkRuntime.env = DevFixture.ENV
        SdkRuntime.publicKey = DevFixture.PUBLIC_KEY
    }

    private fun showPopup() {
        // Sin foco (pantalla bloqueada, otra ventana encima) el renderer espera, como en una app real.
        try {
            rule.waitUntil(FOCUS_TIMEOUT_MS) { rule.activity.hasWindowFocus() }
        } catch (e: Throwable) {
            throw AssertionError("test activity has no window focus: is the screen locked?", e)
        }
        rule.activityRule.scenario.onActivity { activity ->
            PopupRenderer.show(
                popup = devicePopup(),
                context = PlatformContext(activity),
                onAction = { actions += it },
                onSurveyEvent = { name, _ -> events += name },
                onDismiss = {},
            )
        }
        waitForEvent(PopupReveal.LOADED_EVENT, LOAD_TIMEOUT_MS)
        waitForEvent(PopupReveal.READY_EVENT, LOAD_TIMEOUT_MS)
        rule.waitForIdle()
    }

    private fun waitForEvent(name: String, timeoutMs: Long = EVENT_TIMEOUT_MS, after: Int = 0) {
        try {
            rule.waitUntil(timeoutMs) { events.drop(after).any { it == name || it.startsWith("error") } }
        } catch (e: Throwable) {
            throw AssertionError("survey event '$name' not received in ${timeoutMs}ms; events: $events", e)
        }
        val error = events.drop(after).firstOrNull { it.startsWith("error") }
        if (error != null) throw AssertionError("survey failed to load ($error); events: $events")
    }

    private fun web() = requireNotNull(rule.activity.surveyWebView()) { "no survey WebView on screen" }

    /** Página 1 (NPS) → página 2 (texto libre). Elegir un valor pasa de página solo. */
    private fun goToTextPage() {
        val before = events.size
        val clicked = web().runJs(
            """(function(){
                 var r = document.querySelector('input[type=radio][value="5"]') || document.querySelector('input[type=radio]');
                 if (!r) return 'none'; r.click(); return 'ok';
               })()"""
        )
        assertTrue(clicked == "\"ok\"", "no NPS option in the survey (got $clicked)")
        waitForEvent("after_submit", after = before)
        rule.onNodeWithText("Back").assertIsDisplayed()
    }

    @Test
    fun popupOpensWithTheSurvey() {
        showPopup()

        rule.onNodeWithText(DevFixture.TITLE).assertIsDisplayed()
        rule.onNodeWithContentDescription("Close popup").assertIsDisplayed()
        rule.onNodeWithText("Send").assertIsDisplayed()
        val container = rule.activity.window.decorView.findViewWithTag<View>("DeepdotsPopupContainer")
        assertTrue(container?.visibility == View.VISIBLE, "popup container should be visible")
    }

    @Test
    fun closeRespondsToAFingerTap() {
        showPopup()

        rule.onNodeWithContentDescription("Close popup").fingerTap()
        rule.waitUntil(EVENT_TIMEOUT_MS) { actions.isNotEmpty() }

        assertTrue(actions.single() is Action.Decline, "X should close the popup, got $actions")
    }

    @Test
    fun sendRespondsToAFingerTap() {
        showPopup()
        val before = events.size

        // Página 1 sin responder: el survey recibe el envío (before_submit) y contesta, sea
        // pasando de página o con el aviso de respuesta obligatoria. Lo que importa es que llegue.
        rule.onNodeWithText("Send").fingerTap()

        waitForEvent("before_submit", after = before)
    }

    @Test
    fun backRespondsToAFingerTap() {
        showPopup()
        goToTextPage()
        val before = events.size

        rule.onNodeWithText("Back").fingerTap()

        waitForEvent("back", after = before)
    }

    @Test
    fun keyboardOverTheTextQuestionLeavesSendAndCloseUsable() {
        showPopup()
        goToTextPage()

        // Toque de dedo de verdad en la caja de texto (el foco por JS no abre el teclado). Tras
        // el cambio de página la tarjeta crece y se recentra (content_height) y el survey puede
        // seguir bloqueado (aria-busy): se mide cuando todo está quieto y, si el teclado no sale,
        // se vuelve a medir y tocar (Xiaomi 14 y Tab S8 fallaban con un solo toque, run 44782965531).
        var keyboard = false
        var lastBox = ""
        for (attempt in 1..TAP_ATTEMPTS) {
            val (x, y) = stableTextBoxOnScreen().also { lastBox = "${it.first},${it.second}" }
            fingerTapOnScreen(x, y)
            keyboard = runCatching {
                rule.waitUntil(KEYBOARD_TIMEOUT_MS) { rule.activity.keyboardTopInWindow() != null }
            }.isSuccess
            if (keyboard) break
        }
        val focused = web().runJs("(document.activeElement||{}).tagName")
        assertTrue(keyboard, "no keyboard after $TAP_ATTEMPTS taps on the text box (last tap at $lastBox, focused $focused)")
        rule.waitForIdle()
        val keyboardTop = requireNotNull(rule.activity.keyboardTopInWindow())

        val send = rule.onNodeWithText("Send").assertIsDisplayed().fetchSemanticsNode().boundsInWindow
        assertTrue(send.bottom <= keyboardTop + 1, "Send (bottom ${send.bottom}) is under the keyboard (top $keyboardTop)")
        val close = rule.onNodeWithContentDescription("Close popup").assertIsDisplayed().fetchSemanticsNode().boundsInWindow
        assertTrue(close.top >= 0f, "X is off screen (top ${close.top})")

        rule.onNodeWithContentDescription("Close popup").fingerTap()
        rule.waitUntil(EVENT_TIMEOUT_MS) { actions.isNotEmpty() }
        assertTrue(actions.single() is Action.Decline, "X should close with the keyboard open, got $actions")
    }

    /**
     * Centro de la caja de texto en coordenadas de pantalla, cuando el survey no está ocupado y
     * ni la caja ni el WebView se han movido entre dos lecturas seguidas (máx. [LAYOUT_TIMEOUT_MS]).
     */
    private fun stableTextBoxOnScreen(): Pair<Float, Float> {
        var previous: Pair<Float, Float>? = null
        val deadline = SystemClock.uptimeMillis() + LAYOUT_TIMEOUT_MS
        while (true) {
            val box = web().runJs(
                """(function(){
                     if (document.body.getAttribute('aria-busy') === 'true') return '';
                     var t = document.querySelector('textarea') || document.querySelector('input[type=text]');
                     if (!t) return '';
                     t.scrollIntoView({block:'center', behavior:'instant'});
                     var r = t.getBoundingClientRect();
                     if (r.width === 0 || r.height === 0) return '';
                     return [r.left + r.width/2, r.top + Math.min(r.height/2, 20), window.innerWidth].join(',');
                   })()"""
            )?.trim('"').orEmpty()
            val point = if (box.isEmpty()) null else {
                val (cssX, cssY, cssWidth) = box.split(',').map { it.toFloat() }
                val webView = web()
                val loc = IntArray(2).also { webView.getLocationOnScreen(it) }
                val scale = webView.width / cssWidth
                Pair(loc[0] + cssX * scale, loc[1] + cssY * scale)
            }
            if (point != null && point == previous) return point
            assertTrue(SystemClock.uptimeMillis() < deadline, "the text box on page 2 never settled (last $box)")
            previous = point
            Thread.sleep(300)
        }
    }

    @Test
    fun surveyFillsItsSlotOnATablet() {
        val sw = rule.activity.resources.configuration.smallestScreenWidthDp
        assumeTrue("tablet only (smallest width $sw dp)", sw >= 600)
        showPopup()

        val webView = web()
        val slot = webView.parent as View
        assertTrue(webView.height > 0, "survey has no height")
        assertTrue(
            abs(webView.height - slot.height) <= 1 && abs(webView.width - slot.width) <= 1,
            "survey ${webView.width}x${webView.height} px does not fill its slot ${slot.width}x${slot.height} px",
        )
    }

    companion object {
        /**
         * HyperOS / MIUI block activity starts from an app without "display pop-up windows while
         * running in the background" (MIUI app op 10021): the Compose rule's activity never opened
         * on the Xiaomi 14 ("MIUILOG- Permission Denied Activity"). Grant it to ourselves through
         * the shell before the rule launches anything. On other Android the op is unknown and the
         * command is a no-op.
         */
        @BeforeClass
        @JvmStatic
        fun allowActivityStartsOnMiui() {
            val instrumentation = InstrumentationRegistry.getInstrumentation()
            val pkg = instrumentation.targetContext.packageName
            val out = instrumentation.uiAutomation.executeShellCommand("appops set $pkg 10021 allow")
            android.os.ParcelFileDescriptor.AutoCloseInputStream(out).use { it.readBytes() }
        }

        const val TEST_TIMEOUT_S = 120L
        // Primera apertura en frío: arranque del WebView + bundle del CDN + survey de DEV.
        const val LOAD_TIMEOUT_MS = 60_000L
        const val EVENT_TIMEOUT_MS = 15_000L
        const val KEYBOARD_TIMEOUT_MS = 8_000L
        const val TAP_ATTEMPTS = 2
        const val LAYOUT_TIMEOUT_MS = 10_000L
        const val FOCUS_TIMEOUT_MS = 10_000L
    }
}
