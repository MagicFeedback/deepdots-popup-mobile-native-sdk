package com.deepdots.sdk.models

/**
 * Motivo del `deepdots_session_start` (param `reason`): por qué EMPIEZA la sesión. Permite a
 * analytics separar un arranque real de la app de una vuelta desde background ("Started here").
 * Espejo del tipo `SessionStartReason` del SDK Web; los valores de wire son idénticos donde el
 * significado coincide.
 *
 *  - [COLD_START]: `init()` (arranque de la app).
 *  - [FOREGROUND]: `onForeground()` tras un cierre (normalmente `onBackground()`; también tras
 *    `endSession()`). Un `onForeground()` con la sesión abierta no emite nada.
 *  - [TRACKING_ENABLED]: `setTrackingEnabled(true)` (consentimiento concedido, también si el
 *    init arrancó con tracking off: entonces es la primera sesión de la app).
 *  - [USER_CHANGE]: `setUserId()` (login/logout del host), tras cerrar la del usuario anterior.
 */
enum class SessionStartReason(val wire: String) {
    COLD_START("cold_start"),
    FOREGROUND("foreground"),
    TRACKING_ENABLED("tracking_enabled"),
    USER_CHANGE("user_change"),
}
