package com.deepdots.sdk.models

import com.deepdots.sdk.analytics.AnalyticsKeys
import com.deepdots.sdk.storage.KeyValueStorage

enum class Mode { Client, Server }

/**
 * Backend environment the SDK should talk to.
 *
 * Defaults to [Production] (https://api.deepdots.com). Set [Development] to point
 * the SDK at https://api-dev.deepdots.com. This is independent of [InitOptions.debug],
 * which only controls SDK log output.
 */
enum class Environment { Production, Development }

data class InitOptions(
    val debug: Boolean? = false, // controls SDK log verbosity only
    val environment: Environment? = Environment.Production, // controls backend base URL
    val mode: Mode? = Mode.Client,
    val popupOptions: PopupOptions = PopupOptions(),
    val provideLang: () -> String? = { null }, // resolver for the current UI language
    val autoLaunch: Boolean? = false, // when true, triggers start evaluating immediately after init
    /**
     * Storage del host. Si es null, el SDK usa el PERSISTENTE por defecto
     * (SharedPreferences/NSUserDefaults): el `user_id` tiene que sobrevivir entre sesiones o
     * cada arranque contaría como usuario nuevo. Inyecta el tuyo solo para controlar dónde se
     * guarda (o `InMemoryStorage()` en tests).
     */
    val storage: KeyValueStorage? = null,
    /** Arranca el tracking activado (default) o desactivado, a la espera de consentimiento. */
    val trackingEnabled: Boolean? = true,
    /**
     * Claves de la integración de analytics creada en la plataforma. Sin ellas el canal queda
     * en dry-run (no envía nada; el payload solo se imprime con `debug`); con ellas hace `POST /sdk/feedback` de verdad.
     */
    val analytics: AnalyticsKeys? = null,
    /**
     * Called when the analytics channel opens or closes a session (needs [analytics]), with the
     * id the Deepdots API stores as `sdkSessionId` on the feedback the session becomes: send it
     * to your backend to attach data to that feedback later. A session whose only batch is the
     * closing one reports just `Closed`. `Closed` does not come when the app is killed before
     * the closing request returns, nor when the closing request fails (the API closes those
     * itself, later): their id already came with `Open`. Key by `sessionId`, not by order: the
     * next session's `Open` can arrive before the previous one's `Closed`. Same value as
     * `DeepdotsPopups.getFeedbackSessionId()` while the session is open. Called from a
     * background thread: hop to the main thread before touching UI. Paridad con Web
     * `DeepdotsInitParams.onFeedbackSession`.
     */
    val onFeedbackSession: ((FeedbackSession) -> Unit)? = null,
    /**
     * Geolocalización por IP (país/ciudad) añadida a analytics. Default `true`. El lookup llama
     * a servicios de terceros (ipapi.co, luego ipwho.is y luego ipinfo.io como fallback, 3 s de
     * timeout cada uno) y solo se hace con `analytics` configurado, el tracking activo y la
     * caché de 30 días ausente o caducada. Con `false` no se llama nunca ni se adjunta
     * país/ciudad. Paridad con Web `DeepdotsInitParams.geolocation`.
     */
    val geolocation: Boolean? = true,
    /**
     * Info interna del usuario (plan, edad, idioma preferido…) que se persiste en el Contact del
     * backend para segmentar/targetear popups. Requiere `metadata["userId"]` (usuario
     * identificado). También se puede llamar después con `setContactAttributes`.
     */
    val contactAttributes: Map<String, Any?>? = null,
    /**
     * Si el SDK pinta el "modal" del popup (scrim + tarjeta con sombra/bordes redondeados en
     * `PopupView`). Default `true`. Con `false` el popup se pinta sin scrim ni tarjeta
     * (transparente, a pantalla completa), para que el host controle el marco visual. El survey
     * sigue funcional (header con cerrar + footer con back/start/complete/send). Paridad con
     * Web/RN `DeepdotsInitParams.renderChrome`.
     * ⚠️ En KMP el SDK sigue auto-montando el overlay a pantalla completa; el flag solo quita el
     * scrim + la tarjeta, no cede el montaje del contenedor al host.
     */
    val renderChrome: Boolean? = true,
    /**
     * Barra de progreso ("Question X of Y" + barra) bajo la cabecera del popup. `null` respeta
     * el `showProgressBar` que la plataforma configure en el estilo del survey; `true`/`false`
     * lo fuerzan desde el host. Solo se pinta con más de una página, fuera de la pantalla de
     * inicio y antes de completar. Paridad con Web/RN `DeepdotsInitParams.showProgressBar`.
     */
    val showProgressBar: Boolean? = null,
    val metadata: Map<String, Any>? = null // arbitrary host-supplied metadata forwarded to the backend
)

data class ShowOptions(
    val surveyId: String,
    val productId: String,
    val data: Map<String, Any>? = null
)
