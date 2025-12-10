package com.deepdots.sdk.ui

import android.annotation.SuppressLint
import android.graphics.Color
import android.util.Log
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView

/**
 * Android implementation using a WebView with a small inline HTML that loads MagicFeedback native bundle.
 * A JS bridge posts events back via window.DeepdotsBridge.emit(type).
 */
@SuppressLint("SetJavaScriptEnabled")
@Composable
actual fun SurveyView(
    surveyId: String,
    productId: String,
    onEvent: (String) -> Unit,
    onController: (SurveyController) -> Unit
) {
    var loading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(360.dp)
    ) {
        AndroidView(factory = { ctx ->
            WebView.setWebContentsDebuggingEnabled(true)
            val assetSize = try { ctx.assets.open("magicfeedback/magicfeedback-sdk.browser.js").use { it.available() } } catch (e: Exception) { -1 }
            val webView = WebView(ctx).apply {
                setBackgroundColor(Color.TRANSPARENT)
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
                settings.cacheMode = WebSettings.LOAD_DEFAULT
                settings.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                settings.allowUniversalAccessFromFileURLs = true
                webChromeClient = WebChromeClient()
                webViewClient = object : WebViewClient() {
                    override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                        // Block any navigation attempts from the survey to avoid leaving the host app
                        return true
                    }
                    override fun onReceivedError(view: WebView?, request: WebResourceRequest?, error: WebResourceError?) {
                        Log.e("SurveyView", "WebView error: ${error?.description}")
                    }
                    override fun onReceivedHttpError(view: WebView?, request: WebResourceRequest?, errorResponse: android.webkit.WebResourceResponse?) {
                        Log.e("SurveyView", "HTTP error: ${errorResponse?.statusCode}")
                    }
                }
                addJavascriptInterface(object {
                    @JavascriptInterface
                    fun emit(event: String) {
                        if (event.startsWith("error")) {
                            loading = false
                            errorMessage = when {
                                event.contains("timeout") -> "Survey load timeout"
                                else -> "Error loading survey"
                            }
                        } else if (event == "popup_clicked") {
                            loading = false
                        }
                        onEvent(event)
                    }
                }, "DeepdotsBridge")
                // Use shared builder
                val assetSize = assetSize
                val html = buildMagicFeedbackHtml(
                    surveyId = surveyId,
                    productId = productId,
                    localAssetUrl = "file:///android_asset/magicfeedback/magicfeedback-sdk.browser.js",
                    assetSize = assetSize,
                    bridgeEmitCall = "DeepdotsBridge.emit",
                    isIOS = false
                )
                loadDataWithBaseURL("https://magicfeedback.app/", html, "text/html", "utf-8", null)
            }
            // Provide controller
            onController(object : SurveyController {
                override fun send() { webView.post { webView.evaluateJavascript("window.DeepdotsActions?.send()", null) } }
                override fun back() { webView.post { webView.evaluateJavascript("window.DeepdotsActions?.back()", null) } }
                override fun close() { webView.post { webView.evaluateJavascript("window.DeepdotsActions?.close()", null) } }
            })
            webView
        }, update = {})
        if (loading && errorMessage == null) {
            Box(modifier = Modifier.fillMaxWidth().height(360.dp), contentAlignment = Alignment.Center) { Text("Loading survey…") }
        } else if (errorMessage != null) {
            Box(modifier = Modifier.fillMaxWidth().height(360.dp), contentAlignment = Alignment.Center) { Text(errorMessage!!) }
        }
        DisposableEffect(Unit) { onDispose { } }
    }
}

// Provide actual implementation for platformSurveyHtml so native Android code could also fetch it if needed.
actual fun platformSurveyHtml(surveyId: String, productId: String): String = buildMagicFeedbackHtml(
    surveyId = surveyId,
    productId = productId,
    localAssetUrl = "file:///android_asset/magicfeedback/magicfeedback-sdk.browser.js",
    assetSize = null,
    bridgeEmitCall = "DeepdotsBridge.emit",
    isIOS = false
)
