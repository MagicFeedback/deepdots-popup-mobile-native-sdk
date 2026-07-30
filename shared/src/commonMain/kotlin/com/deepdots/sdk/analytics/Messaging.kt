package com.deepdots.sdk.analytics

/**
 * Messaging (#18–22): tracking de notificaciones del host (push / in-app).
 * Un único evento reservado `deepdots_message` con un campo `stage` discriminador;
 * el funnel se correlaciona por `message_id` y se agrupa por `message_title`.
 * Espejo del SDK Web (src/analytics/messaging.ts).
 */
fun buildMessageParams(
    stage: String,
    id: String,
    title: String,
    channel: String,
    campaign: String? = null,
    value: Double? = null,
    currency: String? = null,
    params: Map<String, Any?>? = null,
): Map<String, Any?> {
    val p = LinkedHashMap<String, Any?>()
    p["stage"] = stage
    p["message_id"] = id
    p["message_title"] = title
    p["channel"] = channel
    campaign?.let { p["campaign"] = it }
    value?.let { p["value"] = it }
    currency?.let { p["currency"] = it }
    params?.let { p.putAll(it) }
    return p
}

/** Canales válidos. Un mensaje tiene UN canal: se valida en runtime porque el host puede no ser Kotlin. */
val MESSAGE_CHANNELS = listOf("push", "in_app")

/** Techo de `message_id` vigilados por sesión; los más antiguos se evictan (memoria acotada). */
const val MAX_TRACKED_MESSAGES = 500

enum class MessageRejectionReason { INVALID_CHANNEL, DUPLICATE_STAGE, CHANNEL_CONFLICT }

/** Veredicto del guard: emitir, o descartar con razón + detalle para el log. */
sealed class MessageGuardVerdict {
    object Emit : MessageGuardVerdict()
    data class Discard(val reason: MessageRejectionReason, val detail: String) : MessageGuardVerdict()
}

/**
 * Protege el funnel de Messaging de las formas imposibles que el host puede producir por error
 * (visto en producción: CTR > 100% por falta de `delivered` + el mismo `message_id` reportado en
 * dos canales). Tres reglas, todas dentro de la sesión (se reinicia en cada `init()`):
 *
 *  1. `channel` fuera de `MESSAGE_CHANNELS` → se descarta el evento.
 *  2. Un `(message_id, stage)` ya emitido → se emite una sola vez.
 *  3. Un `message_id` ya visto en un canal → se descartan los eventos de otro canal.
 *
 * Los eventos rechazados NO mutan el estado: un rechazo nunca "consume" el stage bueno.
 * Lo que el SDK no puede arreglar es la ausencia de `delivered`: eso lo instrumenta el host.
 * Espejo del SDK Web (src/analytics/messaging.ts).
 */
class MessageGuard(private val maxTrackedMessages: Int = MAX_TRACKED_MESSAGES) {

    private class Entry(val channel: String, val stages: MutableSet<String> = mutableSetOf())

    /** message_id → canal fijado + stages ya emitidos. El orden de inserción marca la eviction. */
    private val seen = LinkedHashMap<String, Entry>()

    fun evaluate(stage: String, id: String, channel: String): MessageGuardVerdict {
        if (channel !in MESSAGE_CHANNELS) {
            return MessageGuardVerdict.Discard(
                MessageRejectionReason.INVALID_CHANNEL,
                "channel \"$channel\" no válido (esperado ${MESSAGE_CHANNELS.joinToString(" | ")})",
            )
        }

        val entry = seen[id]
        if (entry == null) {
            remember(id, channel, stage)
            return MessageGuardVerdict.Emit
        }
        if (entry.channel != channel) {
            return MessageGuardVerdict.Discard(
                MessageRejectionReason.CHANNEL_CONFLICT,
                "message_id \"$id\" ya se reportó en channel \"${entry.channel}\"; se descarta \"$channel\"",
            )
        }
        if (stage in entry.stages) {
            return MessageGuardVerdict.Discard(
                MessageRejectionReason.DUPLICATE_STAGE,
                "stage \"$stage\" ya emitido para message_id \"$id\"",
            )
        }
        entry.stages.add(stage)
        return MessageGuardVerdict.Emit
    }

    /** Olvida todo lo vigilado (nueva sesión). */
    fun reset() {
        seen.clear()
    }

    private fun remember(id: String, channel: String, stage: String) {
        seen[id] = Entry(channel, mutableSetOf(stage))
        while (seen.size > maxTrackedMessages) {
            val oldest = seen.keys.firstOrNull() ?: break
            seen.remove(oldest)
        }
    }
}
