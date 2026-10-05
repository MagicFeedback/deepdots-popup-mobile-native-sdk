package com.deepdots.sdk.analytics

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive

/**
 * Mapeo del envelope de analytics → body de `POST /sdk/feedback`. Espejo de
 * `src/analytics/feedback-payload.ts` (Web).
 *
 * La analítica se envía como un Feedback del modelo del Surveys SDK, agrupado por una
 * INTEGRACIÓN creada en la plataforma. Se manda en streaming con `completed:false`; el
 * backend cose por `sessionId` + `user_id`. El ÚLTIMO lote de una sesión (app a background,
 * cambio de usuario, cierre explícito) va con `completed:true`: cierra el registro y el lote
 * siguiente omite el `sessionId` viejo para que el backend abra uno nuevo.
 * `feedback.finished` se queda siempre en `false` (no es la señal de cierre acordada).
 *
 * Encoding:
 *  - todo va en `feedback.metadata`: contexto (user_id, session_id, platform…) + eventos
 *  - cada entrada usa `value: List<String>` (lista de un elemento)
 *  - eventos: {key: nombre_evento, value: [JSON(timestamp + params)]}
 *  - identidad (user_id) → `profile` como `external-user-id`
 *  - métricas del host (setMetric) → `feedback.metrics` (mismo shape, sin prefijo)
 *  - `answers` y `text` vacíos
 */

/** Claves de la integración de analytics creada en la plataforma. */
data class AnalyticsKeys(
    val publicKey: String,
    val integration: String,
)

@Serializable
data class FeedbackKV(
    val key: String,
    val value: List<String>,
)

@Serializable
data class AnalyticsFeedback(
    val text: String = "",
    val answers: List<FeedbackKV> = emptyList(),
    val metrics: List<FeedbackKV> = emptyList(),
    val metadata: List<FeedbackKV> = emptyList(),
    val profile: List<FeedbackKV> = emptyList(),
    val finished: Boolean = false,
)

@Serializable
data class AnalyticsFeedbackBody(
    val feedback: AnalyticsFeedback,
    val publicKey: String,
    val integration: String,
    val completed: Boolean = false,
    val finished: Boolean = false,
    /** sessionId devuelto por el primer POST; agrupa todos los eventos en un solo registro. */
    val sessionId: String? = null,
)

/** Opciones del builder (espejo de `BuildBodyOptions` en Web). */
data class BuildBodyOptions(
    /**
     * `true` en el último lote de la sesión → `completed:true` (cierra el registro en backend).
     * El body SÍ lleva el `sessionId` actual (es el registro que se cierra); es el lote
     * SIGUIENTE el que lo omite.
     */
    val sessionEnd: Boolean = false,
)

/** Json del canal de analytics: los defaults SÍ viajan (paridad de wire con Web), los nulls no. */
internal val analyticsFeedbackJson = Json {
    encodeDefaults = true
    explicitNulls = false
}

/** Añade un par key/value solo si el valor está definido y no es vacío. */
private fun MutableList<FeedbackKV>.pushKV(key: String, value: Any?) {
    if (value == null) return
    val text = value.toString()
    if (text.isEmpty()) return
    add(FeedbackKV(key, listOf(text)))
}

/** Serializa los params de un evento junto a su timestamp, como hace Web con JSON.stringify. */
private fun eventValue(event: AnalyticsEvent): String {
    val fields = linkedMapOf<String, JsonElement>("timestamp" to JsonPrimitive(event.timestamp))
    event.params?.let { fields.putAll(it) }
    return analyticsFeedbackJson.encodeToString(JsonObject.serializer(), JsonObject(fields))
}

fun buildAnalyticsFeedbackBody(
    envelope: AnalyticsEnvelope,
    keys: AnalyticsKeys,
    feedbackSessionId: String? = null,
    options: BuildBodyOptions = BuildBodyOptions(),
): AnalyticsFeedbackBody {
    val context = envelope.context

    val profile = mutableListOf<FeedbackKV>()
    profile.pushKV("external-user-id", envelope.userId)

    // Todo va en metadata: contexto del sistema (prefijo deepdots_) + atributos + eventos.
    val metadata = mutableListOf<FeedbackKV>()
    metadata.pushKV("deepdots_user_id", envelope.userId)
    metadata.pushKV("deepdots_session_id", envelope.sessionId)
    metadata.pushKV("deepdots_platform", context.platform)
    metadata.pushKV("deepdots_language", context.language)
    context.device?.let { d ->
        metadata.pushKV("deepdots_device_type", d.deviceType)
        metadata.pushKV("deepdots_os_version", d.osVersion)
        metadata.pushKV("deepdots_device_model", d.deviceModel)
        metadata.pushKV("deepdots_app_version", d.appVersion)
        metadata.pushKV("deepdots_user_agent", d.userAgent)
        metadata.pushKV("deepdots_timezone", d.timezone)
        metadata.pushKV("deepdots_referrer", d.referrer)
        metadata.pushKV("deepdots_viewport_size", d.viewportSize)
        metadata.pushKV("deepdots_screen_resolution", d.screenResolution)
        metadata.pushKV("deepdots_pixel_ratio", d.pixelRatio)
        metadata.pushKV("deepdots_entry_type", d.entryType)
        metadata.pushKV("deepdots_page_load_ms", d.pageLoadMs)
        metadata.pushKV("deepdots_connection_type", d.connectionType)
        metadata.pushKV("deepdots_country", d.country)
        metadata.pushKV("deepdots_city", d.city)
    }
    for ((k, v) in context.attributes) metadata.pushKV(k, v)
    for (event in envelope.events) metadata.add(FeedbackKV(event.name, listOf(eventValue(event))))

    // Métricas del host → campo dedicado `feedback.metrics` (sin prefijo).
    val metrics = mutableListOf<FeedbackKV>()
    for ((k, v) in context.metrics) metrics.pushKV(k, v)

    return AnalyticsFeedbackBody(
        feedback = AnalyticsFeedback(
            text = "",
            answers = emptyList(),
            metrics = metrics,
            metadata = metadata,
            profile = profile,
            finished = false,
        ),
        publicKey = keys.publicKey,
        integration = keys.integration,
        // Único marcador de cierre acordado con backend: el último lote de la sesión.
        completed = options.sessionEnd,
        finished = false,
        sessionId = feedbackSessionId,
    )
}
