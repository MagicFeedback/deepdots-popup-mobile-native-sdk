package com.deepdots.sdk.ui

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.deepdots.sdk.models.PopupSize

/**
 * Tamaño de la tarjeta (DD-334). Espejo de `src/ui/placement.ts` del SDK web: `regular` es el de
 * siempre; `small` deja sitio a la app alrededor del popup, para una pregunta corta.
 */
internal val REGULAR_CARD_MAX_WIDTH: Dp = 420.dp
internal val SMALL_CARD_MAX_WIDTH: Dp = 300.dp

/** `style.size` de la API. Valores desconocidos o ausentes = `Regular` (comportamiento de siempre). */
internal fun parsePopupSize(value: String?): PopupSize =
    if (value?.trim()?.lowercase() == "small") PopupSize.Small else PopupSize.Regular

internal fun cardMaxWidthFor(size: PopupSize): Dp =
    if (size == PopupSize.Small) SMALL_CARD_MAX_WIDTH else REGULAR_CARD_MAX_WIDTH

/**
 * El WebView puede pedir otro ancho (`popupMaxWidth` del Style del survey). Con `Small` ese valor
 * no puede ensanchar la tarjeta por encima del tamaño elegido en el Admin.
 */
internal fun resolveCardMaxWidth(size: PopupSize, requested: Dp?): Dp {
    val cap = cardMaxWidthFor(size)
    if (requested == null) return cap
    return if (size == PopupSize.Small && requested > cap) cap else requested
}
