package com.deepdots.sdk

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.deepdots.sdk.models.Appearance

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

    /**
     * Preferencia del host sobre la barra de progreso. `null` = decide la plataforma
     * (`style.showProgressBar` del survey). Se fija en init desde `InitOptions.showProgressBar`.
     */
    var showProgressBar: Boolean? = null

    /**
     * Light/dark chosen by the host (`InitOptions.appearance` / `setAppearance`). `null` = the
     * survey Style colours decide. Compose state, so an open popup recolours when the app changes it.
     */
    var appearance: Appearance? by mutableStateOf(null)

    /** `InitOptions.debug`: con debug se loguea qué survey SDK pinta cada popup (DD-70). */
    var debug: Boolean = false
}
