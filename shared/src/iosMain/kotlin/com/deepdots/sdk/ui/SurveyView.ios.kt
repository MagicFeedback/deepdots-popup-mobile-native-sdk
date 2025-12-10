package com.deepdots.sdk.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import platform.Foundation.NSBundle

@Composable
actual fun SurveyView(
    surveyId: String,
    productId: String,
    onEvent: (String) -> Unit,
    onController: (SurveyController) -> Unit
) {
    var loading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    Box(modifier = Modifier.fillMaxWidth().height(360.dp), contentAlignment = Alignment.Center) {
        when {
            loading && errorMessage == null -> Text("Loading survey…")
            errorMessage != null -> Text(errorMessage!!)
        }
    }
    val bundle = NSBundle.mainBundle
    val resPath = bundle.pathForResource("magicfeedback/magicfeedback-sdk.browser", ofType = "js")
    val localScript = resPath?.let { "file://${it}" }
    val html = buildMagicFeedbackHtml(
        surveyId = surveyId,
        productId = productId,
        localAssetUrl = localScript,
        assetSize = null,
        bridgeEmitCall = "window.webkit.messageHandlers.DeepdotsBridge.postMessage",
        isIOS = true
    )
    // Provide a placeholder controller (native side should inject JS execution to call these)
    onController(object : SurveyController {
        override fun send() { /* WKWebView JS eval from native side */ }
        override fun back() { /* WKWebView JS eval from native side */ }
        override fun close() { /* WKWebView JS eval from native side */ }
    })
}

actual fun platformSurveyHtml(surveyId: String, productId: String): String {
    val bundle = NSBundle.mainBundle
    val resPath = bundle.pathForResource("magicfeedback/magicfeedback-sdk.browser", ofType = "js")
    val localScript = resPath?.let { "file://${it}" }
    return buildMagicFeedbackHtml(
        surveyId = surveyId,
        productId = productId,
        localAssetUrl = localScript,
        assetSize = null,
        bridgeEmitCall = "window.webkit.messageHandlers.DeepdotsBridge.postMessage",
        isIOS = true
    )
}
