package com.deepdots.sdk.analytics

import com.deepdots.sdk.service.RejectedFeedbackException
import com.deepdots.sdk.util.SdkLock
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
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

/**
 * Transporte real del canal de analytics (`POST /sdk/feedback`). Espejo de `createFeedbackSink`
 * (Web): cachea el `sessionId` del registro abierto y lo lleva en cada lote, y además:
 *  - mientras no se conozca el `sessionId`, los lotes se **serializan** (esperan la primera
 *    respuesta): dos POST a la vez sin `sessionId` crearían dos registros y partirían los datos.
 *    En el flush final no se espera (la app se está yendo): mejor partido que perdido;
 *  - el lote de cierre (`sessionEnd`) lleva el `sessionId` del registro que cierra y lo olvida en
 *    el acto, para que el siguiente abra uno nuevo;
 *  - un fallo transitorio (red, 5xx, 408, 429) llama a `requeue` (el manager re-encola el lote);
 *    un 4xx ([RejectedFeedbackException]) se descarta.
 *
 * El estado se toca desde el hilo del host (`send`, llamado por el flush) y desde la corrutina de
 * envío (la respuesta), así que va bajo [SdkLock]. Los callbacks del host se invocan FUERA del
 * lock y blindados: un fallo suyo no puede convertir un lote entregado en uno fallido (se
 * re-encolaría y el cierre se enviaría dos veces).
 */
internal class FeedbackSink(
    private val keys: AnalyticsKeys,
    private val scope: CoroutineScope,
    private val post: suspend (AnalyticsFeedbackBody) -> String?,
    private val log: (String) -> Unit = {},
    /** El backend aceptó el primer lote de una sesión: `sessionId` nuevo cacheado. */
    private val onSessionId: ((String) -> Unit)? = null,
    /**
     * El backend aceptó el lote de cierre, con el `sessionId` del registro cerrado: el que
     * devuelve la respuesta o, si no trae, el que llevaba el lote. No se re-cachea.
     */
    private val onSessionClosed: ((String) -> Unit)? = null,
    /**
     * Espejo SÍNCRONO del `sessionId` cacheado (nuevo valor o null al cerrar). Se llama CON el
     * lock tomado para que el espejo no pueda quedar desfasado: solo para estado interno del SDK,
     * nunca para código del host.
     */
    private val onCachedSessionIdChanged: ((String?) -> Unit)? = null,
) {
    private val lock = SdkLock()
    private var feedbackSessionId: String? = null
    /** Primer POST (aún sin sessionId) en vuelo: los lotes siguientes lo esperan. */
    private var firstPostInFlight: Job? = null
    /**
     * Sube cada vez que se cierra una sesión. Una respuesta que llega con otra generación es de
     * una sesión ya cerrada (p. ej. el primer POST seguía en vuelo en el `onBackground()`, y el
     * cierre no lo espera) y no puede volver a cachear su sessionId ni avisarlo como abierto.
     */
    private var generation = 0

    /** `sessionId` del registro abierto; null antes del primer lote aceptado y tras cerrar. */
    fun currentSessionId(): String? = lock.withLock { feedbackSessionId }

    val sink: AnalyticsSink = { envelope, meta, requeue -> send(envelope, meta, requeue) }

    fun send(envelope: AnalyticsEnvelope, meta: AnalyticsFlushMeta, requeue: () -> Unit) {
        val closing = meta.sessionEnd
        val job = lock.withLock {
            // El lote de cierre SÍ lleva el sessionId (es el registro que se cierra); es el
            // siguiente el que lo omite para que el backend abra uno nuevo.
            val carried = feedbackSessionId
            val waitFor = if (!closing && !meta.final && feedbackSessionId == null) firstPostInFlight else null
            // LAZY: el Job se registra como barrera antes de arrancar, sin hueco para otro hilo.
            val job = scope.launch(start = CoroutineStart.LAZY) {
                runCatching { waitFor?.join() }
                // La generación se fija junto al sessionId del body: identifica la sesión del lote.
                val (sessionId, postGeneration) = lock.withLock {
                    (if (closing) carried else feedbackSessionId) to generation
                }
                deliver(envelope, closing, sessionId, postGeneration, requeue)
            }
            if (closing) {
                feedbackSessionId = null
                firstPostInFlight = null
                generation++
                onCachedSessionIdChanged?.invoke(null)
            } else if (feedbackSessionId == null) {
                firstPostInFlight = job
            }
            job
        }
        job.start()
    }

    private suspend fun deliver(
        envelope: AnalyticsEnvelope,
        closing: Boolean,
        sessionId: String?,
        postGeneration: Int,
        requeue: () -> Unit,
    ) {
        val body = buildAnalyticsFeedbackBody(envelope, keys, sessionId, BuildBodyOptions(sessionEnd = closing))
        val returned = try {
            post(body)
        } catch (_: RejectedFeedbackException) {
            return // 4xx: ya logueado por el servicio, el lote se descarta
        } catch (t: Throwable) {
            // Fallo transitorio (red/5xx/408/429): devolver el lote al buffer.
            log("analytics · transient failure sending feedback, batch requeued: ${t.message}")
            requeue()
            return
        }

        // El POST de cierre devuelve el sessionId del registro que acabamos de cerrar: NO se
        // re-cachea (el lote siguiente volvería a apuntar al registro cerrado), pero se avisa por
        // onSessionClosed, porque ese registro ya va a ser un Feedback.
        if (closing) {
            val closedId = returned ?: sessionId
            if (closedId != null) notifyHost("onSessionClosed") { onSessionClosed?.invoke(closedId) }
            return
        }

        var stale = false
        val opened = lock.withLock {
            if (postGeneration != generation) {
                stale = true
                null
            } else if (returned != null && returned != feedbackSessionId) {
                feedbackSessionId = returned
                onCachedSessionIdChanged?.invoke(returned)
                returned
            } else {
                null
            }
        }
        if (stale) {
            log("analytics · sessionId of a closed session ignored: $returned")
        } else if (opened != null) {
            log("analytics · feedbackSessionId cached: $opened")
            notifyHost("onSessionId") { onSessionId?.invoke(opened) }
        }
    }

    private inline fun notifyHost(name: String, block: () -> Unit) {
        try {
            block()
        } catch (t: Throwable) {
            log("analytics · $name threw: ${t.message}")
        }
    }
}
