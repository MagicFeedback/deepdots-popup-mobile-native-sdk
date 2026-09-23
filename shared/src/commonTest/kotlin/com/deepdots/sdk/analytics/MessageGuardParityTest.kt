package com.deepdots.sdk.analytics

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/** Paridad con el MessageGuard Web (src/analytics/messaging.test.ts). Mismos 9 casos. */
class MessageGuardParityTest {

    private fun discard(v: MessageGuardVerdict) = v as MessageGuardVerdict.Discard

    @Test
    fun accepts_full_funnel_of_a_message_on_a_single_channel() {
        val g = MessageGuard()
        assertEquals(MessageGuardVerdict.Emit, g.evaluate("delivered", "m1", "push"))
        assertEquals(MessageGuardVerdict.Emit, g.evaluate("clicked", "m1", "push"))
        assertEquals(MessageGuardVerdict.Emit, g.evaluate("converted", "m1", "push"))
    }

    // Protección 1: channel fuera de la whitelist → descarta.
    @Test
    fun discards_unknown_channel() {
        val g = MessageGuard()
        val v = discard(g.evaluate("delivered", "m1", "PUSH"))
        assertEquals(MessageRejectionReason.INVALID_CHANNEL, v.reason)
        assertTrue(v.detail.contains("PUSH"))
    }

    @Test
    fun invalid_channel_does_not_record_state() {
        val g = MessageGuard()
        g.evaluate("delivered", "m1", "email")
        assertEquals(MessageGuardVerdict.Emit, g.evaluate("delivered", "m1", "push"))
    }

    // Protección 2: idempotencia por (message_id, stage).
    @Test
    fun emits_a_repeated_stage_only_once() {
        val g = MessageGuard()
        assertEquals(MessageGuardVerdict.Emit, g.evaluate("clicked", "m1", "push"))
        val v = discard(g.evaluate("clicked", "m1", "push"))
        assertEquals(MessageRejectionReason.DUPLICATE_STAGE, v.reason)
        assertTrue(v.detail.contains("m1"))
    }

    @Test
    fun idempotency_is_per_message_id() {
        val g = MessageGuard()
        g.evaluate("clicked", "m1", "push")
        assertEquals(MessageGuardVerdict.Emit, g.evaluate("clicked", "m2", "push"))
    }

    // Protección 3: un message_id no puede cambiar de canal.
    @Test
    fun discards_second_channel_for_a_known_message_id() {
        val g = MessageGuard()
        assertEquals(MessageGuardVerdict.Emit, g.evaluate("delivered", "m1", "push"))
        val v = discard(g.evaluate("clicked", "m1", "in_app"))
        assertEquals(MessageRejectionReason.CHANNEL_CONFLICT, v.reason)
        assertTrue(v.detail.contains("in_app"))
    }

    @Test
    fun channel_conflict_does_not_consume_the_stage() {
        val g = MessageGuard()
        g.evaluate("delivered", "m1", "push")
        g.evaluate("clicked", "m1", "in_app") // rechazado
        assertEquals(MessageGuardVerdict.Emit, g.evaluate("clicked", "m1", "push"))
    }

    @Test
    fun conflict_takes_precedence_over_idempotency() {
        val g = MessageGuard()
        g.evaluate("clicked", "m1", "push")
        assertEquals(
            MessageRejectionReason.CHANNEL_CONFLICT,
            discard(g.evaluate("clicked", "m1", "in_app")).reason,
        )
    }

    @Test
    fun evicts_oldest_message_ids_past_the_cap() {
        val g = MessageGuard()
        for (i in 0..MAX_TRACKED_MESSAGES) g.evaluate("clicked", "m$i", "push")
        // m0 fue evictado → su duplicado ya no se detecta (trade-off aceptado del techo)
        assertEquals(MessageGuardVerdict.Emit, g.evaluate("clicked", "m0", "push"))
        // el más reciente sigue vigilado
        assertTrue(g.evaluate("clicked", "m$MAX_TRACKED_MESSAGES", "push") is MessageGuardVerdict.Discard)
    }
}
