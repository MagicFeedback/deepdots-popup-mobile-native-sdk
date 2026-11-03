package com.deepdots.sdk.ui

/**
 * JS que los botones nativos (Enviar, Atrás, cerrar, empezar) ejecutan en el WebView del survey.
 *
 * ⚠️ ES5, sin `?.` ni `??`: antes era `window.DeepdotsActions?.send()`, y un WebView anterior a
 * Chrome 80 (Android 8–10 sin actualizar desde Play) o un WKWebView de iOS 13.0–13.3 lo rechaza
 * con "SyntaxError: Unexpected token ." y el botón no hace nada. Visto en el emulador de Android 8
 * (WebView 58): las páginas que se pasan con Enviar no avanzaban; el rating sí, porque avanza solo.
 */
internal object SurveyActionsJs {
    fun call(action: String): String =
        "(function(a){if(a&&typeof a.$action==='function'){a.$action();}})(window.DeepdotsActions)"

    val send = call("send")
    val back = call("back")
    val close = call("close")
    val startForm = call("startForm")
}
