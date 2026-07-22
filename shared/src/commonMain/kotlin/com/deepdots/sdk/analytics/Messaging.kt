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
