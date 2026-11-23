package com.deepdots.sdk.ui

import kotlin.test.Test
import kotlin.test.assertTrue

/**
 * Con el teclado abierto el WebView encoge y el campo enfocado podía quedar bajo su borde
 * (Lemvigh-Müller, iPhone SE y Android con letra grande): el survey lo vuelve a poner a la vista.
 */
class KeyboardFieldVisibleTest {
    @Test
    fun surveyKeepsTheFocusedFieldVisibleWhenTheWebViewShrinks() {
        listOf(false, true).forEach { ios ->
            val html = buildMagicFeedbackHtml(
                surveyId = "survey-abc",
                productId = "product-xyz",
                localAssetUrl = null,
                assetSize = null,
                bridgeEmitCall = if (ios) "window.webkit.messageHandlers.DeepdotsBridge.postMessage" else "DeepdotsBridge.emit",
                isIOS = ios,
            )
            assertTrue(html.contains("function ddKeepFocusedFieldVisible()"), "isIOS=$ios")
            assertTrue(html.contains("addEventListener('resize', function(){ setTimeout(ddKeepFocusedFieldVisible"), "isIOS=$ios")
            assertTrue(html.contains("addEventListener('focusin'"), "isIOS=$ios")
            // El enunciado va con el campo: se sube el bloque de la pregunta, no solo el campo.
            assertTrue(html.contains("contains('magicfeedback-div')"), "isIOS=$ios")
            assertTrue(html.contains("block: 'start'"), "isIOS=$ios")
        }
    }
}
