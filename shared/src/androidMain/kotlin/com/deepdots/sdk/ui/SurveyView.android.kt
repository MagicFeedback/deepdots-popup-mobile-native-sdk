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
import android.view.MotionEvent
import android.view.View
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import android.webkit.ConsoleMessage
import org.json.JSONObject

/**
 * Android implementation using a WebView with a small inline HTML that loads MagicFeedback native bundle.
 * A JS bridge posts events back via window.DeepdotsBridge.emit(JSON.stringify({name,payload}))).
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
                // Make inner scrolling visible and smooth
                isVerticalScrollBarEnabled = true
                overScrollMode = View.OVER_SCROLL_IF_CONTENT_SCROLLS
                // Prevent parent (Compose) from intercepting vertical scroll while interacting
                setOnTouchListener { v, event ->
                    if (event.action == MotionEvent.ACTION_DOWN || event.action == MotionEvent.ACTION_MOVE) {
                        v.parent?.requestDisallowInterceptTouchEvent(true)
                    }
                    false
                }
                webChromeClient = object : WebChromeClient() {
                    override fun onConsoleMessage(consoleMessage: ConsoleMessage?): Boolean {
                        val msg = consoleMessage?.message() ?: return super.onConsoleMessage(consoleMessage)
                        // Detect MagicFeedback logs and synthesize events with payload message
                        val lower = msg.lowercase()
                        fun emitJson(name: String, message: String?) {
                            val json = JSONObject()
                            json.put("name", name)
                            if (message != null) {
                                val payload = JSONObject()
                                payload.put("message", message)
                                json.put("payload", payload)
                            }
                            onEvent(json.toString())
                        }
                        if (lower.contains("required") || lower.contains("no response")) {
                            emitJson("validation_error_required", "Please answer the required question to continue.")
                        } else if (lower.contains("error occurred while submitting")) {
                            // Try to extract a trailing reason like 'No response'
                            val reason = Regex("error occurred while submitting.*: (.*)", RegexOption.IGNORE_CASE)
                                .find(msg)?.groupValues?.get(1)?.trim()
                            emitJson("submit_error", reason ?: "An error occurred while submitting.")
                        } else if (lower.contains("submit") && lower.contains("error")) {
                            emitJson("submit_error", null)
                        }
                        return super.onConsoleMessage(consoleMessage)
                    }
                }
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
                        // Accept plain string or JSON {name,payload}
                        val name = try {
                            if (event.trim().startsWith("{")) {
                                val regex = Regex("\"name\"\\s*:\\s*\"(.*?)\"")
                                regex.find(event)?.groupValues?.get(1) ?: event
                            } else event
                        } catch (e: Exception) { event }
                        if (name.startsWith("error")) {
                            loading = false
                            errorMessage = if (name.contains("timeout")) "Survey load timeout" else "Error loading survey"
                        } else if (name == "popup_clicked" || name == "loaded") {
                            loading = false
                        } else if (name == "before_submit") {
                            loading = true
                        }
                        onEvent(event)
                    }
                }, "DeepdotsBridge")
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
                override fun startForm() { webView.post { webView.evaluateJavascript("window.DeepdotsActions?.startForm?.()", null) } }
            })
            webView
        }, update = {})
        if (errorMessage != null) {
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
