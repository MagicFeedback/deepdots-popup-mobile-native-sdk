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
     * Debajo de esta luminancia relativa (WCAG, 0 = negro, 1 = blanco) el fondo es oscuro.
     * Mismo corte que el survey (magicfeedback-sdk DD-238, `DARK_LUMINANCE_THRESHOLD`) y que el
     * aviso del Admin (DD-237): así el chrome nativo y el survey cambian de paleta a la vez.
     */
    const val DARK_LUMINANCE_THRESHOLD = 0.2f

    /**
     * Un fondo con alfa se trata por su color opaco: la tarjeta es opaca en la práctica y el velo
     * de detrás es oscuro.
     */
    fun isDark(background: Color): Boolean =
        background.copy(alpha = 1f).luminance() < DARK_LUMINANCE_THRESHOLD

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
