package com.deepdots.sdk.analytics

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Paridad con el EngagementTracker Web (src/analytics/engagement-tracker.test.ts):
 * tiempo activo en foreground (#8).
 */
class EngagementTrackerParityTest {

    private var now: Long = 0
    private fun tracker() = EngagementTracker(now = { now })

    @Test
    fun accumulates_only_while_resumed() {
        val t = tracker()
        t.resume()
        now += 3_000
        t.pause()
        now += 10_000 // en background no cuenta

        assertEquals(3_000L, t.consume())
    }

    @Test
    fun consume_resets_but_keeps_the_timer_running() {
        val t = tracker()
        t.resume()
        now += 2_000
        assertEquals(2_000L, t.consume())

        now += 1_500
        assertEquals(1_500L, t.consume())
        assertTrue(t.isActive())
    }

    @Test
    fun resume_is_idempotent() {
        val t = tracker()
        t.resume()
        now += 1_000
        t.resume() // no reinicia el tramo en curso
        now += 1_000

        assertEquals(2_000L, t.consume())
    }

    @Test
    fun consume_is_zero_when_it_never_ran() {
        val t = tracker()
        assertEquals(0L, t.consume())
        assertTrue(!t.isActive())
    }
}
