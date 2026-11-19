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

    /**
     * Colores del chrome. Sin modo forzado ([forcedMode] `null`) manda el Style, como en DD-246.
     * Con modo forzado por la app (DD-240):
     * - Light: el `boxBackgroundColor` del Style o, si no trae, blanco.
     * - Dark: el `dark.boxBackgroundColor` del Style (DD-239); si no, el del Style si ya es oscuro;
     *   si no, el gris oscuro por defecto (un color claro de marca no sirve en oscuro).
     */
    fun colorsFor(
        styleBackground: Color?,
        theme: Theme,
        forcedMode: Theme? = null,
        darkStyleBackground: Color? = null,
    ): PopupChromeColors {
        val bg = when (forcedMode) {
            null -> styleBackground ?: defaultBackground(theme)
            Theme.Light -> styleBackground ?: LIGHT_DEFAULT_BG
            Theme.Dark -> darkStyleBackground ?: styleBackground?.takeIf { isDark(it) } ?: DARK_DEFAULT_BG
        }
        val dark = isDark(bg)
        return PopupChromeColors(
            background = bg,
            text = if (dark) DARK_TEXT else LIGHT_TEXT,
            progressTrack = if (dark) DARK_TRACK else LIGHT_TRACK,
            isDark = dark,
        )
    }

    /** Botón principal por defecto (sin `buttonPrimaryColor`) sobre tarjeta clara. */
    val DEFAULT_PRIMARY = Color(0xFF1E293B)
    /** Y sobre tarjeta oscura: el slate oscuro casi no se ve ahí, así que va uno claro. */
    val DARK_DEFAULT_PRIMARY = Color(0xFFE2E8F0)
    /** Texto del botón cuando el botón es claro. */
    val ON_LIGHT_PRIMARY = Color(0xFF0F172A)

    /**
     * Color del botón principal. En tarjeta oscura: `dark.buttonPrimaryColor` (DD-239), si no el de
     * marca, si no el claro por defecto. En clara: el de marca o el slate de siempre.
     */
    fun primaryFor(stylePrimary: Color?, darkStylePrimary: Color?, darkCard: Boolean): Color =
        if (darkCard) darkStylePrimary ?: stylePrimary ?: DARK_DEFAULT_PRIMARY
        else stylePrimary ?: DEFAULT_PRIMARY

    /**
     * Texto sobre el botón principal. Blanco como siempre; solo en tarjeta oscura con un botón
     * claro pasa a oscuro (si no, el texto blanco no se lee). En clara no cambia nada.
     */
    fun onPrimary(primary: Color, darkCard: Boolean): Color =
        if (darkCard && !isDark(primary)) ON_LIGHT_PRIMARY else Color.White
}
