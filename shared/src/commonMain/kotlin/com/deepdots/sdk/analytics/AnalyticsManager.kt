package com.deepdots.sdk.analytics

import com.deepdots.sdk.util.currentTimeMillis
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive

/**
 * Capa de Analytics (eventos GA-style) — canal SEPARADO del feedback de survey.
 * Réplica del SDK Web (src/analytics/analytics-manager.ts).
 *
 * El feedback de survey se envía con el Surveys SDK. La analítica de
 * navegación/comportamiento va por un ENDPOINT PROPIO, vinculada por `user_id`.
 *
 * De momento NO se hace la llamada: el `sink` por defecto imprime por consola el
 * payload exacto que se enviaría (dry-run). Cuando exista el endpoint, se sustituye
 * el sink por un POST.
 */

@Serializable
data class AnalyticsEvent(
    val name: String,
    val timestamp: Long,
    val params: JsonObject? = null,
)

@Serializable
data class AnalyticsContext(
    val platform: String,
    val language: String? = null,
    /** Info de dispositivo (Technology #11–13). */
    val device: DeviceInfo? = null,
    /** User attributes del cliente para breakdowns (registration_status, pass_type, …). */
    val attributes: Map<String, String> = emptyMap(),
)

@Serializable
data class AnalyticsEnvelope(
    val publicKey: String? = null,
    val userId: String? = null,
    val sessionId: String? = null,
    val context: AnalyticsContext,
    val events: List<AnalyticsEvent>,
)

/** Identidad resuelta por el tracking, inyectada al construir el payload. */
data class AnalyticsIdentity(val userId: String?, val sessionId: String?)

typealias AnalyticsSink = (AnalyticsEnvelope) -> Unit

private val dryRunJson = Json { prettyPrint = true; encodeDefaults = true }

/** Sink por defecto: NO envía nada, solo imprime lo que se enviaría. */
val dryRunSink: AnalyticsSink = { payload ->
    println(
        "[DeepdotsAnalytics] (dry-run · NO enviado) POST /sdk/analytics → " +
            dryRunJson.encodeToString(AnalyticsEnvelope.serializer(), payload),
    )
}

private fun anyToJsonElement(value: Any?): JsonElement = when (value) {
    null -> JsonNull
    is String -> JsonPrimitive(value)
    is Boolean -> JsonPrimitive(value)
    is Number -> JsonPrimitive(value)
    else -> JsonPrimitive(value.toString())
}

class AnalyticsManager(
    private val sink: AnalyticsSink = dryRunSink,
    private val now: () -> Long = { currentTimeMillis() },
    private val publicKey: String? = null,
    private val platform: String = "unknown",
    private val language: String? = null,
    private var device: DeviceInfo? = null,
    /** Nº máximo de eventos en buffer antes de solicitar un flush automático (default 20). */
    private val maxBatchSize: Int = 20,
    /** Callback invocado cuando el buffer alcanza `maxBatchSize`. El caller hace el flush real. */
    private val onFlushNeeded: (() -> Unit)? = null,
) {
    private val events = mutableListOf<AnalyticsEvent>()
    private val attributes = mutableMapOf<String, String>()
    private var miniService: String? = null
    private var miniServiceEnteredAt: Long = 0

    /** Actualiza country/city en el DeviceInfo cuando resuelve la geolocalización async. */
    fun updateDevice(geo: GeoInfo) {
        device = device?.copy(country = geo.country, city = geo.city)
    }

    /** Mezcla user attributes (se coercionan a string). Mutable en runtime. */
    fun setUserAttributes(attrs: Map<String, Any?>) {
        for ((k, v) in attrs) {
            if (k.isBlank() || v == null) continue
            attributes[k] = v.toString()
        }
    }

    /** Marca el inicio de un mini-service; etiqueta los eventos siguientes con `mini_service`. */
    fun enterMiniService(name: String, entryPointType: String? = null) {
        miniService = name
        miniServiceEnteredAt = now()
        track("deepdots_mini_service_enter", mapOf("entry_point_type" to entryPointType))
    }

    /** Cierra el mini-service activo emitiendo `mini_service_exit` con su duración (#27). No-op si no hay ninguno. */
    fun exitMiniService() {
        val name = miniService ?: return
        val durationSeconds = maxOf(0L, (now() - miniServiceEnteredAt) / 1000)
        miniService = null // dejar de etiquetar antes de emitir el evento de salida
        track("deepdots_mini_service_exit", mapOf("mini_service" to name, "duration_seconds" to durationSeconds))
    }

    /** Registra un evento de analítica (modelo GA: nombre + params). */
    fun track(name: String, params: Map<String, Any?>? = null) {
        val merged = LinkedHashMap<String, Any?>()
        miniService?.let { merged["mini_service"] = it }
        params?.let { merged.putAll(it) }
        val obj = if (merged.isEmpty()) null else JsonObject(merged.mapValues { anyToJsonElement(it.value) })
        events.add(AnalyticsEvent(name = name, timestamp = now(), params = obj))
        if (events.size >= maxBatchSize) {
            onFlushNeeded?.invoke()
        }
    }

    /** Mini-service activo (para inyectarlo en la metadata del survey, #33). */
    fun getMiniService(): String? = miniService

    /** Nº de eventos pendientes de flush. */
    fun pending(): Int = events.size

    /** Construye el envelope que se enviaría al endpoint de analytics. */
    fun buildPayload(identity: AnalyticsIdentity): AnalyticsEnvelope = AnalyticsEnvelope(
        publicKey = publicKey,
        userId = identity.userId,
        sessionId = identity.sessionId,
        context = AnalyticsContext(platform = platform, language = language, device = device, attributes = attributes.toMap()),
        events = events.toList(),
    )

    /** Envía (vía sink) el lote acumulado y vacía el buffer. No-op si no hay eventos. */
    fun flush(identity: AnalyticsIdentity): AnalyticsEnvelope? {
        if (events.isEmpty()) return null
        val payload = buildPayload(identity)
        sink(payload)
        events.clear()
        return payload
    }
}
