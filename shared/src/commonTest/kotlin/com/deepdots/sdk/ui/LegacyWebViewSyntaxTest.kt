package com.deepdots.sdk.ui

import kotlin.test.Test
import kotlin.test.assertFalse

/**
 * El JS que el SDK mete en el WebView tiene que correr en WebViews viejos: Android 8–10 sin
 * actualizar (Chrome < 80) e iOS 13.0–13.3. `?.` y `??` los rompen con un SyntaxError que deja
 * muerto el botón o el script entero.
 */
class LegacyWebViewSyntaxTest {
    private val modern = listOf("?.", "??")

    @Test
    fun nativeButtonActionsAreEs5() {
        listOf(SurveyActionsJs.send, SurveyActionsJs.back, SurveyActionsJs.close, SurveyActionsJs.startForm).forEach { js ->
            modern.forEach { assertFalse(js.contains(it), "\"$it\" in $js") }
        }
    }

    @Test
    fun surveyHtmlScriptsHaveNoOptionalChainingOrNullish() {
        listOf(false, true).forEach { ios ->
            val html = buildMagicFeedbackHtml(
                surveyId = "survey-abc",
                productId = "product-xyz",
                localAssetUrl = null,
                assetSize = null,
                bridgeEmitCall = if (ios) "window.webkit.messageHandlers.DeepdotsBridge.postMessage" else "DeepdotsBridge.emit",
                isIOS = ios,
            )
            val scripts = Regex("<script[^>]*>([\\s\\S]*?)</script>").findAll(html).joinToString("\n") { it.groupValues[1] }
            modern.forEach { assertFalse(scripts.contains(it), "\"$it\" in the survey HTML scripts (isIOS=$ios)") }
        }
    }
}
