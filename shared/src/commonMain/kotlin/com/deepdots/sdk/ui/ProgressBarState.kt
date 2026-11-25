package com.deepdots.sdk.ui

import com.deepdots.sdk.i18n.DefaultLabels

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
 * Qué cuenta la barra (DD-210, `style.progressMode`). `Current` = la pregunta en la que está
 * (por defecto, también si falta); `Completed` = las ya respondidas, al 100 % en la pantalla final.
 */
enum class ProgressMode {
    Current, Completed;

    companion object {
        /** Cualquier valor que no sea "completed" es la barra de siempre. */
        fun fromStyle(value: String?): ProgressMode = if (value == "completed") Completed else Current
    }
}

/**
 * @param enabled preferencia resuelta: `InitOptions.showProgressBar` si el host se pronunció,
 *   si no el `showProgressBar` del estilo del survey.
 * @param onStartPage la pantalla de inicio no lleva barra.
 * @param completed la pantalla final tampoco.
 * @param labels textos del chrome ya resueltos (idioma del survey > host > inglés).
 */
fun progressBarState(
    enabled: Boolean,
    progress: Double,
    total: Int,
    completed: Boolean = false,
    onStartPage: Boolean = false,
    showUnit: Boolean = true,
    unit: ProgressUnit = ProgressUnit.Fraction,
    labels: DefaultLabels.Labels = DefaultLabels.labels(null),
    mode: ProgressMode = ProgressMode.Current,
): ProgressBarState {
    val hidden = ProgressBarState(visible = false, label = "", fraction = 0f)
    // Con una sola página no hay nada que medir: el mismo corte que hace MagicSurvey.
    if (!enabled || onStartPage || total <= 1) return hidden

    if (mode == ProgressMode.Completed) {
        // Espejo de `progressView` (Web) y `src/utils/progress.ts` (MagicSurvey). En la pantalla
        // final la página en pantalla también se envió, y el total pasa a ser lo respondido (una
        // rama puede haber saltado páginas): siempre acaba en "N of N". La follow-up abierta
        // (+0.5) no mueve la barra.
        val answered = if (completed) {
            (floor(progress) + 1).coerceIn(1.0, total.toDouble()).toInt()
        } else {
            floor(progress).coerceIn(0.0, total.toDouble()).toInt()
        }
        val shownTotal = if (completed) answered else total
        val fraction = if (completed) 1.0 else (answered.toDouble() / total).coerceIn(0.0, 1.0)
        val label = when {
            !showUnit -> ""
            unit == ProgressUnit.Percentage -> "${(fraction * 100).roundToInt()}%"
            else -> labels.answeredOf
                .replace("{answered}", answered.toString())
                .replace("{total}", shownTotal.toString())
        }
        return ProgressBarState(visible = true, label = label, fraction = fraction.toFloat())
    }

    if (completed) return hidden

    val current = (progress + 1).coerceIn(1.0, total.toDouble())
    val fraction = (current / total).coerceIn(0.0, 1.0)
    val label = when {
        !showUnit -> ""
        unit == ProgressUnit.Percentage -> "${(fraction * 100).roundToInt()}%"
        else -> {
            val whole = (floor(progress) + 1).coerceIn(1.0, total.toDouble()).toInt()
            "${labels.question} $whole ${labels.of} $total"
        }
    }
    return ProgressBarState(visible = true, label = label, fraction = fraction.toFloat())
}
