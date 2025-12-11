package com.deepdots.sdk.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.interop.UIKitView
import androidx.compose.ui.unit.dp
import kotlinx.cinterop.CValue
import kotlinx.cinterop.ExperimentalForeignApi
import platform.CoreGraphics.CGRect
import platform.CoreGraphics.CGRectMake
import platform.Foundation.NSBundle
import platform.Foundation.NSURL
import platform.UIKit.UIView
import platform.UIKit.UIColor
import platform.WebKit.*
import platform.CoreGraphics.CGRectZero
import platform.darwin.NSObject

@OptIn(ExperimentalForeignApi::class)
private class DeepdotsMessageHandler(private val onEvent: (String) -> Unit) : NSObject(), WKScriptMessageHandlerProtocol {
    override fun userContentController(userContentController: WKUserContentController, didReceiveScriptMessage: WKScriptMessage) {
        val body: Any = didReceiveScriptMessage.body
        val text = if (body is String) body else body.toString()
        onEvent(text)
    }
}

@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun SurveyView(
    surveyId: String,
    productId: String,
    onEvent: (String) -> Unit,
    onController: (SurveyController) -> Unit
) {
    // Keep a reference to WKWebView for controller actions
    var webViewRef by remember { mutableStateOf<WKWebView?>(null) }

    // Expose controller bound to current WKWebView
    onController(object : SurveyController {
        override fun send() { webViewRef?.evaluateJavaScript("window.DeepdotsActions?.send()", null) }
        override fun back() { webViewRef?.evaluateJavaScript("window.DeepdotsActions?.back()", null) }
        override fun close() { webViewRef?.evaluateJavaScript("window.DeepdotsActions?.close()", null) }
        override fun startForm() { webViewRef?.evaluateJavaScript("window.DeepdotsActions?.startForm?.()", null) }
    })

    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        UIKitView(
            factory = {
                // Build WKWebView and wire bridge
                val config = WKWebViewConfiguration()
                val controller = WKUserContentController()
                val handler = DeepdotsMessageHandler(onEvent)
                controller.addScriptMessageHandler(handler, name = "DeepdotsBridge")
                // Inject a script to forward console messages to our bridge
                val consoleForwardScript = """
                    (function(){
                      const origErr = console.error;
                      const origLog = console.log;
                      function forward(type, args){
                        try {
                          const msg = Array.from(args).join(' ');
                          let name = null; let payload = null;
                          const lower = msg.toLowerCase();
                          if (lower.includes('required') || lower.contains("no response")) {
                            name = 'validation_error_required'; payload = { message: 'Please answer the required question to continue.' };
                          } else if (lower.includes('error occurred while submitting')) {
                            const m = msg.match(/error occurred while submitting.*: (.*)/i);
                            const reason = m && m[1] ? m[1].trim() : 'An error occurred while submitting.';
                            name = 'submit_error'; payload = { message: reason };
                          } else if (lower.includes('submit') && lower.includes('error')) {
                            name = 'submit_error'; payload = {};
                          }
                          if (name) {
                            window.webkit?.messageHandlers?.DeepdotsBridge?.postMessage(JSON.stringify({ name, payload }));
                          }
                        } catch(e){}
                      }
                      console.error = function(){ forward('error', arguments); return origErr.apply(console, arguments); };
                      console.log = function(){ forward('log', arguments); return origLog.apply(console, arguments); };
                    })();
                """.trimIndent()
                controller.addUserScript(WKUserScript(source = consoleForwardScript, injectionTime = WKUserScriptInjectionTime.WKUserScriptInjectionTimeAtDocumentEnd, forMainFrameOnly = true))
                config.userContentController = controller
                val wv = WKWebView(frame = CGRectMake(0.0, 0.0, 0.0, 0.0), configuration = config)
                wv.setOpaque(false)
                wv.setBackgroundColor(UIColor.clearColor)
                val html = platformSurveyHtml(surveyId, productId)
                wv.loadHTMLString(html, baseURL = NSURL(string = "https://magicfeedback.app/"))
                webViewRef = wv
                wv as UIView
            },
            modifier = Modifier.fillMaxWidth().height(360.dp),
            update = { /* no-op */ }
        )
    }
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
