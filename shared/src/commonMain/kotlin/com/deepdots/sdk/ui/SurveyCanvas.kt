package com.deepdots.sdk.ui

import androidx.compose.ui.graphics.Color

/**
 * Fondo del lienzo de la página del survey (DD-248).
 *
 * En iOS el WKWebView es opaco (si no, el hueco de interop deja ver el controlador de debajo) y
 * con la vista opaca su `backgroundColor` no se usa: lo que asoma por los márgenes del survey es
 * el lienzo de la página, blanco en claro y negro con el WebView en oscuro. Sobre una tarjeta
 * oscura eso era una franja encima de la primera pregunta. El lienzo se pinta con el color de la
 * tarjeta poniéndolo en el elemento raíz (su fondo se propaga al lienzo).
 */
internal object SurveyCanvas {
    /** `#RRGGBB` de un color opaco (el alfa se ignora: la tarjeta siempre es opaca). */
    fun hex(color: Color): String {
        fun part(v: Float): String {
            val n = (v.coerceIn(0f, 1f) * 255f + 0.5f).toInt()
            return n.toString(16).padStart(2, '0').uppercase()
        }
        return "#" + part(color.red) + part(color.green) + part(color.blue)
    }

    /** JS que pinta el lienzo; vale antes de que exista `<body>` (solo toca el raíz). */
    fun backgroundJs(color: Color): String =
        "document.documentElement.style.background='${hex(color)}';"
}
