package com.deepdots.sdk.ui

import androidx.compose.ui.unit.LayoutDirection
import com.deepdots.sdk.i18n.DefaultLabels

/**
 * Dirección de escritura del chrome del popup para [lang].
 *
 * En Web y RN esto es el atributo `dir` del contenedor; aquí el chrome es Compose, así que la
 * decisión se traduce a [LayoutDirection] y se inyecta con `LocalLayoutDirection`. La regla es
 * la misma en las tres rutas, y el idioma se resuelve igual que los textos: el del survey
 * manda sobre el del host.
 *
 * Se aplica siempre, también `Ltr`, para no arrastrar la dirección del layout de la app host.
 */
fun popupLayoutDirection(lang: String?): LayoutDirection =
    if (DefaultLabels.isRtlLanguage(lang)) LayoutDirection.Rtl else LayoutDirection.Ltr
