package com.deepdots.sdk.ui

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.deepdots.sdk.models.Position
import com.deepdots.sdk.models.PopupSize
import com.deepdots.sdk.models.Style
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive

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

/**
 * ¿El popup tapa la app con el velo y se queda con todos los toques? (DD-334, `style.blockPage`).
 * `center` bloquea siempre: es un modal. Fuera del centro solo deja pasar los toques un `false`
 * explícito; sin el campo el SDK nativo sigue bloqueando como hasta ahora (en web el default es
 * no bloquear, pero allí las esquinas ya no bloqueaban antes de DD-334).
 */
internal fun popupBlocksHost(style: Style): Boolean =
    style.position == Position.Center || style.blockPage != false

/** `style.blockPage` de la API: booleano o su texto. Cualquier otra cosa = no dicho (`null`). */
internal fun parseBlockPage(value: JsonElement?): Boolean? {
    val primitive = value as? JsonPrimitive ?: return null
    return when (primitive.content.trim().lowercase()) {
        "true" -> true
        "false" -> false
        else -> null
    }
}

/** Rectángulo de la tarjeta en puntos (iOS) / dp, relativo a la vista del popup. */
data class CardBounds(val left: Double, val top: Double, val right: Double, val bottom: Double)

/**
 * Popup que no bloquea, en iOS: el toque se lo queda el popup solo si cae en la tarjeta; el resto
 * sigue hacia la app. Sin medir todavía la tarjeta, nada (la vista aún no se ve).
 */
internal fun cardKeepsTouch(card: CardBounds?, x: Double, y: Double): Boolean =
    card != null && x >= card.left && x <= card.right && y >= card.top && y <= card.bottom
