package com.deepdots.sdk

/**
 * Simple runtime holder for SDK-wide config needed by UI/HTML builders.
 */
object SdkRuntime {
    var publicKey: String? = null
    var env: String = "prod" // magicfeedback env: 'dev' | 'prod'
    var userId: String? = null // optional user identifier from init metadata
    var metadata: Map<String, Any>? = null // raw metadata from InitOptions
    /** Lambda resolving the current UI language (BCP-47). Set from InitOptions.provideLang. */
    var provideLang: (() -> String?)? = null

    /** session_id de navegación devuelto por el backend (POST /sdk/popups). Se inyecta en el survey. */
    var sessionId: String? = null

    /** Mini-service activo si hay: va en la metadata del survey (#33, CSAT por mini-service). */
    var miniService: String? = null

    /** sessionId del registro de analytics (POST /sdk/feedback), para correlacionar survey ↔ analytics. */
    var analyticsFeedbackSessionId: String? = null

    /**
     * Si `PopupView` pinta su scrim + tarjeta (chrome). Default true; false = sin scrim ni
     * tarjeta (el host controla el marco). Se fija en init desde `InitOptions.renderChrome`.
     */
    var renderChrome: Boolean = true
}
