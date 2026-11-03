package com.deepdots.sdk.ui

/**
 * Profundidad de navegación dentro del survey tras un `after_submit` del WebView.
 *
 * `@magicfeedback/native` llama a `afterSubmitEvent` también cuando el envío FALLA (pregunta
 * obligatoria sin responder, error de red), con `error` relleno, y el HTML lo reenvía como
 * `after_submit` justo después de `validation_error_required` / `submit_error`. Ese envío no ha
 * cambiado de página: si contase, el footer pasaba a Atrás + Enviar en la primera pantalla, con
 * un Atrás que no lleva a ninguna parte (reportado por un cliente en iOS con 0.7.5/0.7.6).
 * Paridad con Web (`renderPopup.ts`) y RN (`surveyHtml.ts`), que salen antes de tocar la
 * profundidad cuando hay error.
 */
internal fun pageDepthAfterSubmit(current: Int, error: String?): Int =
    if (error.isNullOrBlank()) current + 1 else current
