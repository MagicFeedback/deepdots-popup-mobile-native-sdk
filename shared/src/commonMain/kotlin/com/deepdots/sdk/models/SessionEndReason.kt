package com.deepdots.sdk.models

/**
 * Motivo del `deepdots_session_end` (último lote de la sesión, enviado con `completed:true`).
 * Espejo del tipo `SessionEndReason` del SDK Web; los valores de wire son idénticos.
 *
 *  - [BACKGROUND]: app a background (`onBackground()`), la única señal fiable en móvil.
 *  - [PAGE_HIDE]: solo Web (cierre de pestaña). Existe aquí para que el enum sea el mismo en
 *    los dos SDKs y el backend no tenga que tratarlos distinto.
 *  - [USER_CHANGE]: `setUserId()` (login/logout del host).
 *  - [TRACKING_DISABLED]: `setTrackingEnabled(false)` (consentimiento revocado).
 *  - [MANUAL]: `endSession()` llamado por el host.
 */
enum class SessionEndReason(val wire: String) {
    PAGE_HIDE("page_hide"),
    BACKGROUND("background"),
    USER_CHANGE("user_change"),
    TRACKING_DISABLED("tracking_disabled"),
    MANUAL("manual"),
}
