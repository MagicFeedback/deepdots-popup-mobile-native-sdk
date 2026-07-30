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
     * en dry-run (solo imprime el payload); con ellas hace `POST /sdk/feedback` de verdad.
     */
    val analytics: AnalyticsKeys? = null,
    /**
     * Info interna del usuario (plan, edad, idioma preferido…) que se persiste en el Contact del
     * backend para segmentar/targetear popups. Requiere `metadata["userId"]` (usuario
     * identificado). También se puede llamar después con `setContactAttributes`.
     */
    val contactAttributes: Map<String, Any?>? = null,
    val metadata: Map<String, Any>? = null // arbitrary host-supplied metadata forwarded to the backend
)

data class ShowOptions(
    val surveyId: String,
    val productId: String,
    val data: Map<String, Any>? = null
)
