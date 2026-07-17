package com.deepdots.sdk.ui

import androidx.compose.material3.Typography
import androidx.compose.ui.text.font.FontFamily

/**
 * Devuelve una copia de la [Typography] con [family] aplicada a todos los estilos,
 * de modo que Text y los Material Button del popup hereden la fuente custom.
 * Si [family] es null, devuelve la misma typography sin cambios (cero regresión).
 */
fun Typography.withFontFamily(family: FontFamily?): Typography {
    if (family == null) return this
    return copy(
        displayLarge = displayLarge.copy(fontFamily = family),
        displayMedium = displayMedium.copy(fontFamily = family),
        displaySmall = displaySmall.copy(fontFamily = family),
        headlineLarge = headlineLarge.copy(fontFamily = family),
        headlineMedium = headlineMedium.copy(fontFamily = family),
        headlineSmall = headlineSmall.copy(fontFamily = family),
        titleLarge = titleLarge.copy(fontFamily = family),
        titleMedium = titleMedium.copy(fontFamily = family),
        titleSmall = titleSmall.copy(fontFamily = family),
        bodyLarge = bodyLarge.copy(fontFamily = family),
        bodyMedium = bodyMedium.copy(fontFamily = family),
        bodySmall = bodySmall.copy(fontFamily = family),
        labelLarge = labelLarge.copy(fontFamily = family),
        labelMedium = labelMedium.copy(fontFamily = family),
        labelSmall = labelSmall.copy(fontFamily = family),
    )
}
