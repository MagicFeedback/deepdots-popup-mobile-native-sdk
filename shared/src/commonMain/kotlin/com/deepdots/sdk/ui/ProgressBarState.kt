package com.deepdots.sdk.ui

import kotlin.math.floor
import kotlin.math.roundToInt

/**
 * Estado de la barra de progreso del popup, en lógica pura para poder testearla igual que en
 * Web (`surveyHtml.ts` / `renderPopup.ts`). Espejo de `LineProgressQuestion` de MagicSurvey.
 *
 * La barra usa el valor real del `progress` (las preguntas de seguimiento dinámicas suman +0.5
 * y avanzan media casilla) y la etiqueta redondea hacia abajo, porque una follow-up es un paso
 * DENTRO de la misma pregunta y no la siguiente.
 */
data class ProgressBarState(
    /** Si la barra debe pintarse. */
    val visible: Boolean,
    /** Texto de la etiqueta, ya formateado. Vacío cuando no hay unidad que mostrar. */
    val label: String,
    /** Fracción rellena, 0f..1f. */
    val fraction: Float,
)

/** Unidad de la etiqueta, tal y como la configura la plataforma en el estilo del survey. */
enum class ProgressUnit { Fraction, Percentage }

/**
 * @param enabled preferencia resuelta: `InitOptions.showProgressBar` si el host se pronunció,
 *   si no el `showProgressBar` del estilo del survey.
 * @param onStartPage la pantalla de inicio no lleva barra.
 * @param completed la pantalla final tampoco.
 */
fun progressBarState(
    enabled: Boolean,
    progress: Double,
    total: Int,
    completed: Boolean = false,
    onStartPage: Boolean = false,
    showUnit: Boolean = true,
    unit: ProgressUnit = ProgressUnit.Fraction,
): ProgressBarState {
    // Con una sola página no hay nada que medir: el mismo corte que hace MagicSurvey.
    if (!enabled || completed || onStartPage || total <= 1) {
        return ProgressBarState(visible = false, label = "", fraction = 0f)
    }

    val current = (progress + 1).coerceIn(1.0, total.toDouble())
    val fraction = (current / total).coerceIn(0.0, 1.0)
    val label = when {
        !showUnit -> ""
        unit == ProgressUnit.Percentage -> "${(fraction * 100).roundToInt()}%"
        else -> {
            val whole = (floor(progress) + 1).coerceIn(1.0, total.toDouble()).toInt()
            "Question $whole of $total"
        }
    }
    return ProgressBarState(visible = true, label = label, fraction = fraction.toFloat())
}
