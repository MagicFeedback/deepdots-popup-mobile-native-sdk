package com.deepdots.sdk.tracking

import com.deepdots.sdk.storage.KeyValueStorage
import com.deepdots.sdk.util.currentTimeMillis
import com.deepdots.sdk.util.randomUuid

/**
 * Identidad + sesión para el tracking (Fase 1). Espejo exacto de
 * `src/tracking/tracking-manager.ts` del SDK Web.
 *
 * Modelo: el SDK gestiona el `user_id` (persistente). El `session_id` es propiedad del
 * BACKEND: llega en la respuesta de `POST /sdk/popups` (o `/sdk/feedback`) y se cachea; el
 * backend cose sesiones por `user_id` + ventana. El SDK NO genera ni expira sesiones.
 */

/** Claves de storage — namespace `deepdots.*`. Solo identidad (la sesión no se persiste). */
object StorageKeys {
    const val USER_ID = "deepdots.user_id"
    const val FIRST_SEEN = "deepdots.user.first_seen"
}

/** Par clave/valor con el formato `NativeAnswer` que espera `@magicfeedback/native`. */
data class IdentityAnswer(val key: String, val value: List<String>)

/** `profile` + `metadata` de identidad que se inyectan en el survey. */
data class SurveyIdentity(
    val profile: List<IdentityAnswer>,
    val metadata: List<IdentityAnswer>,
)

/**
 * Construye el `profile` y el `metadata` de identidad para inyectar en el survey
 * (`magicfeedback.form(appId, publicKey, profile, metadata)`), según el contrato §5.
 * Mismas claves que Web: `session_id`, `user_id`, `mini_service`.
 */
fun buildSurveyIdentity(
    userId: String?,
    sessionId: String?,
    miniService: String? = null,
    analyticsFeedbackSessionId: String? = null,
): SurveyIdentity {
    val profile = mutableListOf<IdentityAnswer>()
    if (!userId.isNullOrBlank()) profile += IdentityAnswer("external-user-id", listOf(userId))

    val metadata = mutableListOf<IdentityAnswer>()
    if (!sessionId.isNullOrBlank()) metadata += IdentityAnswer("session_id", listOf(sessionId))
    if (!userId.isNullOrBlank()) metadata += IdentityAnswer("user_id", listOf(userId))
    if (!miniService.isNullOrBlank()) metadata += IdentityAnswer("mini_service", listOf(miniService))
    if (!analyticsFeedbackSessionId.isNullOrBlank()) {
        metadata += IdentityAnswer("deepdots_analytics_feedback_session_id", listOf(analyticsFeedbackSessionId))
    }
    return SurveyIdentity(profile = profile, metadata = metadata)
}

class TrackingManager(
    private val storage: KeyValueStorage,
    /** Id provisto por el cliente; si existe se usa y NO se persiste. */
    private val clientUserId: String? = null,
    private val now: () -> Long = { currentTimeMillis() },
    private val uuid: () -> String = { randomUuid() },
    enabled: Boolean = true,
) {
    private var enabled: Boolean = enabled

    /** session_id cacheado, provisto por el backend (no se genera ni se persiste). */
    private var sessionId: String? = null
    private var userWasJustCreated = false

    fun isTrackingEnabled(): Boolean = enabled

    fun setTrackingEnabled(enabled: Boolean) {
        this.enabled = enabled
    }

    /** Resuelve el user_id según la regla: cliente > persistido > generar+persistir. */
    fun getUserId(): String? {
        if (!enabled) return null
        if (!clientUserId.isNullOrBlank()) return clientUserId

        val persisted = storage.getString(StorageKeys.USER_ID)
        if (!persisted.isNullOrBlank()) return persisted

        val id = uuid()
        storage.putString(StorageKeys.USER_ID, id)
        storage.putString(StorageKeys.FIRST_SEEN, now().toString())
        userWasJustCreated = true
        return id
    }

    /** `true` solo en la primera sesión tras crear el user_id propio del SDK. */
    fun isNewUser(): Boolean = userWasJustCreated

    /** session_id actual provisto por el backend, o null si aún no llegó. */
    fun getSessionId(): String? = if (enabled) sessionId else null

    /** Cachea el session_id devuelto por el backend. */
    fun setSessionId(sessionId: String?) {
        if (!enabled) return
        this.sessionId = sessionId
    }
}
