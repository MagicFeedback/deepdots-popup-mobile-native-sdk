package com.deepdots.sdk.analytics

import com.deepdots.sdk.util.currentTimeMillis

/**
 * Mide el tiempo ACTIVO (en primer plano), estilo GA4 (engagement time). Espejo de
 * `src/analytics/engagement-tracker.ts` (Web).
 *
 * Acumula mientras está "resumed" (app en foreground) y pausa en background. `consume()`
 * devuelve los ms activos desde la última lectura y reinicia, dejando el timer corriendo.
 * El consumidor emite `deepdots_user_engagement` con `engagement_time_msec`; el backend lo
 * suma por sesión → "Average Time Spent per Session" (#8).
 */
class EngagementTracker(
    private val now: () -> Long = { currentTimeMillis() },
) {
    private var activeMs: Long = 0
    private var lastResumeAt: Long? = null

    /** Reanuda el conteo (app en foreground). Idempotente. */
    fun resume() {
        if (lastResumeAt == null) lastResumeAt = now()
    }

    /** Pausa el conteo (app en background), acumulando el tramo activo. */
    fun pause() {
        val since = lastResumeAt ?: return
        activeMs += now() - since
        lastResumeAt = null
    }

    /** Ms activos acumulados desde la última lectura; reinicia sin parar el timer. */
    fun consume(): Long {
        val since = lastResumeAt
        if (since != null) {
            val t = now()
            activeMs += t - since
            lastResumeAt = t
        }
        val ms = activeMs
        activeMs = 0
        return ms
    }

    fun isActive(): Boolean = lastResumeAt != null
}
