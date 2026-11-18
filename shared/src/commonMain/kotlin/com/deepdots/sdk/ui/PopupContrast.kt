package com.deepdots.sdk.ui

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import com.deepdots.sdk.models.Theme

/**
 * Colores del chrome nativo (título, X, texto del progreso, pista de la barra) sacados del fondo
 * FINAL de la tarjeta, no de `style.theme` (DD-246). Antes un Style con `boxBackgroundColor`
 * oscuro y theme Light pintaba texto negro sobre oscuro.
 *
 * `theme` solo decide el fondo por defecto cuando el Style no trae `boxBackgroundColor`.
 */
internal data class PopupChromeColors(
    val background: Color,
    val text: Color,
    val progressTrack: Color,
    val isDark: Boolean,
)

internal object PopupContrast {
    val LIGHT_DEFAULT_BG = Color.White
    val DARK_DEFAULT_BG = Color(0xFF2B2B2B)
    val LIGHT_TEXT = Color.Black
    val DARK_TEXT = Color.White
    val LIGHT_TRACK = Color(0xFFE5E7EB)
    /** Blanco al 24 %: se ve sobre cualquier fondo oscuro sin competir con la barra. */
    val DARK_TRACK = Color(0x3DFFFFFF)

    /** Fondo por defecto según el theme, solo cuando el Style no trae uno. */
    fun defaultBackground(theme: Theme): Color = when (theme) {
        Theme.Light -> LIGHT_DEFAULT_BG
        Theme.Dark -> DARK_DEFAULT_BG
    }

    /**
     * Oscuro si el texto blanco contrasta más que el negro (WCAG). El corte cae en una
     * luminancia relativa de ~0,179. Un fondo con alfa se trata por su color opaco: la tarjeta
     * es opaca en la práctica y el velo de detrás es oscuro.
     */
    fun isDark(background: Color): Boolean {
        val l = background.copy(alpha = 1f).luminance()
        val contrastWithWhite = 1.05f / (l + 0.05f)
        val contrastWithBlack = (l + 0.05f) / 0.05f
        return contrastWithWhite > contrastWithBlack
    }

    fun colorsFor(styleBackground: Color?, theme: Theme): PopupChromeColors {
        val bg = styleBackground ?: defaultBackground(theme)
        val dark = isDark(bg)
        return PopupChromeColors(
            background = bg,
            text = if (dark) DARK_TEXT else LIGHT_TEXT,
            progressTrack = if (dark) DARK_TRACK else LIGHT_TRACK,
            isDark = dark,
        )
    }
}
