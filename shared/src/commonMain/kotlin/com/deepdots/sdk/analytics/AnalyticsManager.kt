package com.deepdots.sdk.analytics

import com.deepdots.sdk.util.SdkLock
import com.deepdots.sdk.util.currentTimeMillis
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive

/**
 * Capa de Analytics (eventos GA-style) — canal SEPARADO del feedback de survey.
 * Espejo de `src/analytics/analytics-manager.ts` (Web).
 *
 * Se envía como un Feedback a la integración de analytics (`POST /sdk/feedback`), vinculado
 * por `user_id`. Sin claves en `init.analytics` el sink por defecto queda en dry-run
 * (imprime el payload exacto que se enviaría).
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
    /** Métricas del host: valores medibles persistentes → van en `feedback.metrics` del body. */
    val metrics: Map<String, String> = emptyMap(),
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

/** Contexto del flush, para que el sink pueda adaptar el transporte y marcar el cierre. */
data class AnalyticsFlushMeta(
    /**
     * `true` cuando el flush ocurre porque la app se está yendo a background/cerrando. En Web
     * cambia el transporte a `sendBeacon`; en móvil solo indica que no se puede esperar mucho.
     */
    val final: Boolean = false,
    /**
     * `true` en el ÚLTIMO lote de la sesión → el body va con `completed:true` y el sink olvida
     * el `sessionId` cacheado para que el lote siguiente abra un registro nuevo.
     */
    val sessionEnd: Boolean = false,
)

/**
 * Envío del lote. `requeue` lo invoca el sink cuando la entrega falla de forma TRANSITORIA
 * (red, 5xx, 408, 429): el manager devuelve el lote al buffer y se reintenta en el flush
 * siguiente. Es el equivalente en KMP a la promesa que rechaza en Web (aquí el POST es
 * asíncrono en una corrutina, así que no puede señalizar el fallo lanzando).
 *
 * ⚠️ Entrega at-least-once: el backend debe deduplicar por
 * `(deepdots_user_id, nombre_evento, timestamp)`.
 */
typealias AnalyticsSink = (
    envelope: AnalyticsEnvelope,
    meta: AnalyticsFlushMeta,
    requeue: () -> Unit,
) -> Unit

private val dryRunJson = Json { prettyPrint = true; encodeDefaults = true; explicitNulls = false }

/** Sink de dry-run: NO envía nada, solo vuelca por el `log` dado lo que se enviaría. */
fun createDryRunSink(log: (String) -> Unit = { println(it) }): AnalyticsSink = { payload, meta, _ ->
    val suffix = if (meta.sessionEnd) " · completed:true (fin de sesión)" else ""
    log(
        "[DeepdotsAnalytics] (dry-run · NO enviado · sin init.analytics) POST /sdk/feedback$suffix → " +
            dryRunJson.encodeToString(AnalyticsEnvelope.serializer(), payload),
    )
}

/** Sink por defecto: NO envía nada, solo imprime lo que se enviaría. */
val dryRunSink: AnalyticsSink = createDryRunSink()

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
    /**
     * Techo de eventos retenidos en memoria contando los re-encolados por fallo de envío
     * (default 200). Al superarlo se descartan los más antiguos.
     */
    private val maxBufferedEvents: Int = 200,
    /** Callback invocado cuando el buffer alcanza `maxBatchSize`. El caller hace el flush real. */
    private val onFlushNeeded: (() -> Unit)? = null,
) {
    private val lock = SdkLock()
    private val events = mutableListOf<AnalyticsEvent>()
    private var attributes = mutableMapOf<String, String>()
    /** Métricas del host (valores medibles persistentes). Sobrescriben por key; se reenvían en cada flush. */
    private var metrics = mutableMapOf<String, String>()
    /** Mini-services activos: nombre → timestamp de entrada. El orden de inserción marca el "más reciente". */
    private val activeMiniServices = LinkedHashMap<String, Long>()

    /** Actualiza country/city en el DeviceInfo cuando resuelve la geolocalización async. */
    fun updateDevice(geo: GeoInfo) {
        device = device?.copy(country = geo.country, city = geo.city)
            ?: DeviceInfo(country = geo.country, city = geo.city)
    }

    /** Mezcla user attributes (se coercionan a string). Mutable en runtime. */
    fun setUserAttributes(attrs: Map<String, Any?>) {
        for ((k, v) in attrs) {
            if (k.isBlank() || v == null) continue
            attributes[k] = v.toString()
        }
    }

    /**
     * Registra/actualiza una métrica (valor medible). Persistente: se reenvía en cada flush.
     * Repetir la misma key SOBRESCRIBE; el valor se coerciona a string; ignora key vacía.
     */
    fun setMetric(key: String, value: Any?) {
        if (key.isBlank() || value == null) return
        metrics[key] = value.toString()
    }

    /**
     * Marca el inicio de un mini-service; etiqueta los eventos siguientes con `mini_service`.
     * Admite varios activos a la vez (concurrencia); el "actual" para etiquetar es el más reciente.
     * Reentrar con un nombre ya activo refresca su orden y su tiempo de entrada.
     */
    fun enterMiniService(name: String, entryPointType: String? = null) {
        activeMiniServices.remove(name) // reinsertar = pasa a ser el más reciente
        activeMiniServices[name] = now()
        track("deepdots_mini_service_enter", mapOf("entry_point_type" to entryPointType))
    }

    /**
     * Cierra el mini-service `name` emitiendo `mini_service_exit` con su duración (#27).
     * No-op si ese nombre no está activo. Cierre coherente con concurrencia.
     */
    fun exitMiniService(name: String) {
        val enteredAt = activeMiniServices[name] ?: return
        // Math.round como en Web (media hacia arriba), no truncamiento.
        val durationSeconds = maxOf(0L, (now() - enteredAt + 500) / 1000)
        activeMiniServices.remove(name) // dejar de etiquetar con este antes de emitir
        track("deepdots_mini_service_exit", mapOf("mini_service" to name, "duration_seconds" to durationSeconds))
    }

    /** Cierra TODOS los mini-services activos (orden LIFO). Para el cierre por lifecycle. */
    fun exitAllMiniServices() {
        for (name in activeMiniServices.keys.toList().asReversed()) exitMiniService(name)
    }

    /**
     * Olvida lo que pertenecía al usuario anterior (user attributes + métricas) al cambiar de
     * usuario. NO toca el buffer de eventos: el cierre de sesión ya lo vació con la identidad
     * vieja, y lo que llegue después es del usuario nuevo.
     */
    fun resetUserScope() {
        attributes = mutableMapOf()
        metrics = mutableMapOf()
    }

    /** Registra un evento de analítica (modelo GA: nombre + params). */
    fun track(name: String, params: Map<String, Any?>? = null) {
        val merged = LinkedHashMap<String, Any?>()
        getMiniService()?.let { merged["mini_service"] = it }
        params?.let { merged.putAll(it) }
        val obj = if (merged.isEmpty()) null else JsonObject(merged.mapValues { anyToJsonElement(it.value) })
        val reachedBatch = lock.withLock {
            events.add(AnalyticsEvent(name = name, timestamp = now(), params = obj))
            enforceBufferCapLocked()
            events.size >= maxBatchSize
        }
        if (reachedBatch) onFlushNeeded?.invoke()
    }

    /** Mini-service actual (el más reciente aún activo) para etiquetar eventos + metadata del survey (#33). */
    fun getMiniService(): String? = activeMiniServices.keys.lastOrNull()

    /** Nº de eventos pendientes de flush. */
    fun pending(): Int = lock.withLock { events.size }

    /** Construye el envelope que se enviaría al endpoint de analytics. */
    fun buildPayload(identity: AnalyticsIdentity): AnalyticsEnvelope = AnalyticsEnvelope(
        publicKey = publicKey,
        userId = identity.userId,
        sessionId = identity.sessionId,
        context = AnalyticsContext(
            platform = platform,
            language = language,
            device = device,
            attributes = attributes.toMap(),
            metrics = metrics.toMap(),
        ),
        events = lock.withLock { events.toList() },
    )

    /**
     * Envía (vía sink) el lote acumulado y vacía el buffer. No-op si no hay eventos.
     *
     * Si el sink señala un fallo transitorio (`requeue()`) o lanza, el lote se devuelve al
     * principio del buffer y se reintenta en el flush siguiente: un error de red o un 5xx no
     * debe hacer desaparecer los eventos.
     */
    fun flush(identity: AnalyticsIdentity, meta: AnalyticsFlushMeta = AnalyticsFlushMeta()): AnalyticsEnvelope? {
        val batch = lock.withLock {
            val copy = events.toList()
            events.clear()
            copy
        }
        if (batch.isEmpty()) return null
        val payload = buildPayloadWithEvents(identity, batch)
        try {
            sink(payload, meta) { requeue(batch) }
        } catch (_: Throwable) {
            requeue(batch)
        }
        return payload
    }

    private fun buildPayloadWithEvents(identity: AnalyticsIdentity, batch: List<AnalyticsEvent>) = AnalyticsEnvelope(
        publicKey = publicKey,
        userId = identity.userId,
        sessionId = identity.sessionId,
        context = AnalyticsContext(
            platform = platform,
            language = language,
            device = device,
            attributes = attributes.toMap(),
            metrics = metrics.toMap(),
        ),
        events = batch,
    )

    /**
     * Devuelve un lote fallido al buffer, delante de lo que haya llegado mientras se enviaba
     * (orden cronológico). Con el buffer lleno se descartan los eventos más antiguos.
     */
    private fun requeue(batch: List<AnalyticsEvent>) {
        lock.withLock {
            events.addAll(0, batch)
            enforceBufferCapLocked()
        }
    }

    /** Techo del buffer: si el envío lleva rato fallando, se sacrifica lo más antiguo. */
    private fun enforceBufferCapLocked() {
        val overflow = events.size - maxBufferedEvents
        if (overflow > 0) repeat(overflow) { events.removeAt(0) }
    }
}
