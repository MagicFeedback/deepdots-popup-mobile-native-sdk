package com.deepdots.sdk.analytics

import com.deepdots.sdk.storage.KeyValueStorage
import com.deepdots.sdk.util.currentTimeMillis
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Crash & error reporting (Stability #14–17) — captura de errores GESTIONADOS.
 * Espejo del SDK Web (src/analytics/crash-reporter.ts).
 *
 * - `reportError()`: API pública del host (app viva) → emite `deepdots_app_crash` ya.
 * - `captureUnhandled()`: lo llaman los handlers de plataforma (uncaught) → persiste a
 *   disco y se reenvía en el siguiente arranque (el proceso puede morir antes del flush).
 *
 * El contexto de device/sesión se captura EN EL MOMENTO del crash y se guarda en el record.
 */

@Serializable
data class CrashRecord(
    val crashedAt: Long,
    val crashType: String,
    val message: String,
    val stack: String,
    val fatal: Boolean,
    val handled: Boolean,
    val severity: String,
    val sessionId: String? = null,
    val appVersion: String? = null,
    val osVersion: String? = null,
    val deviceModel: String? = null,
    val context: Map<String, String>? = null,
)

/** Snapshot de device en el momento del crash. */
data class DeviceSnapshot(
    val appVersion: String? = null,
    val osVersion: String? = null,
    val deviceModel: String? = null,
)

private const val QUEUE_KEY = "deepdots.crash.queue"
private const val MAX_QUEUED = 20
private const val STACK_MAX = 8000

private val crashJson = Json { ignoreUnknownKeys = true }

/** Convierte un CrashRecord en los params del evento `deepdots_app_crash` (omite null). */
fun crashRecordToParams(r: CrashRecord): Map<String, Any?> {
    val params = LinkedHashMap<String, Any?>()
    params["crashed_at"] = r.crashedAt
    params["crash_type"] = r.crashType
    params["message"] = r.message
    params["stack"] = r.stack
    params["fatal"] = r.fatal
    params["handled"] = r.handled
    params["severity"] = r.severity
    r.sessionId?.let { params["crashed_session_id"] = it }
    r.appVersion?.let { params["crashed_app_version"] = it }
    r.osVersion?.let { params["crashed_os_version"] = it }
    r.deviceModel?.let { params["crashed_device_model"] = it }
    r.context?.forEach { (k, v) -> params["ctx_$k"] = v }
    return params
}

class CrashReporter(
    private val storage: KeyValueStorage,
    /** Emite un evento deepdots_app_crash AHORA (app viva). */
    private val emit: (Map<String, Any?>) -> Unit,
    /** Snapshot de device en el momento del crash. */
    private val device: () -> DeviceSnapshot,
    /** session_id en el momento del crash. */
    private val sessionId: () -> String?,
    private val now: () -> Long = { currentTimeMillis() },
    /** Kill-switch de consentimiento. */
    private val enabled: () -> Boolean = { true },
) {
    private fun buildRecord(
        crashType: String,
        message: String,
        stack: String,
        severity: String,
        handled: Boolean,
        fatal: Boolean,
        context: Map<String, String>? = null,
    ): CrashRecord {
        val dev = device()
        return CrashRecord(
            crashedAt = now(),
            crashType = crashType,
            message = message,
            stack = stack.take(STACK_MAX),
            fatal = fatal,
            handled = handled,
            severity = severity,
            sessionId = sessionId(),
            appVersion = dev.appVersion,
            osVersion = dev.osVersion,
            deviceModel = dev.deviceModel,
            context = context,
        )
    }

    /** API pública del host: reporta un error (app viva) → emite el evento ya. */
    fun reportError(
        error: Throwable,
        severity: String = "error",
        handled: Boolean = true,
        context: Map<String, Any?>? = null,
    ) {
        if (!enabled()) return
        val ctx = context?.mapValues { it.value.toString() }
        val record = buildRecord(
            crashType = error::class.simpleName ?: "Error",
            message = error.message ?: "",
            stack = error.stackTraceToString(),
            severity = severity,
            handled = handled,
            fatal = severity == "fatal",
            context = ctx,
        )
        emit(crashRecordToParams(record))
    }

    /** Lo llaman los handlers de plataforma para un crash no capturado (fatal). Persiste a disco. */
    fun captureUnhandled(crashType: String, message: String, stack: String) {
        persist(buildRecord(crashType, message, stack, severity = "fatal", handled = false, fatal = true))
    }

    private fun persist(record: CrashRecord) {
        var queue = readQueue() + record
        if (queue.size > MAX_QUEUED) queue = queue.takeLast(MAX_QUEUED)
        try {
            storage.putString(QUEUE_KEY, crashJson.encodeToString(queue))
        } catch (_: Throwable) {
            /* storage lleno / no disponible — no rompemos la app */
        }
    }

    /** Lee y vacía la cola de crashes pendientes (llamado en init para el replay). */
    fun drainPendingCrashes(): List<CrashRecord> {
        val queue = readQueue()
        if (queue.isNotEmpty()) {
            try { storage.remove(QUEUE_KEY) } catch (_: Throwable) { /* noop */ }
        }
        return queue
    }

    private fun readQueue(): List<CrashRecord> {
        val raw = try { storage.getString(QUEUE_KEY) } catch (_: Throwable) { null } ?: return emptyList()
        return try { crashJson.decodeFromString<List<CrashRecord>>(raw) } catch (_: Throwable) { emptyList() }
    }
}

/** Instala los handlers de errores no capturados de plataforma (managed). No-op si la plataforma no aplica. */
expect fun installCrashHandlers(reporter: CrashReporter, enabled: () -> Boolean)
