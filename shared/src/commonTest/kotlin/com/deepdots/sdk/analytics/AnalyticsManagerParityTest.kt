package com.deepdots.sdk.analytics

import kotlinx.serialization.json.jsonPrimitive
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

/**
 * Paridad con el AnalyticsManager Web (src/analytics/analytics-manager.test.ts).
 * Analytics = canal separado del feedback; eventos GA-style vinculados por user_id.
 */
class AnalyticsManagerParityTest {

    private var now: Long = 1000
    private var captured: AnalyticsEnvelope? = null
    private var capturedMeta: AnalyticsFlushMeta? = null
    private val sink: AnalyticsSink = { envelope, meta, _ ->
        captured = envelope
        capturedMeta = meta
    }
    private val identity = AnalyticsIdentity(userId = "u-1", sessionId = "s-1")

    private fun mgr() = AnalyticsManager(
        sink = sink,
        now = { now },
        publicKey = "pk-1",
        platform = "android",
    )

    @Test
    fun buffers_events_with_name_timestamp_and_params() {
        val am = mgr()
        am.track("cta_click", mapOf("label" to "comprar"))
        assertEquals(1, am.pending())

        val p = am.buildPayload(identity)
        assertEquals("cta_click", p.events[0].name)
        assertEquals(1000L, p.events[0].timestamp)
        assertEquals("comprar", p.events[0].params?.get("label")?.jsonPrimitive?.content)
    }

    @Test
    fun merges_user_attributes_into_context() {
        val am = mgr()
        am.setUserAttributes(mapOf("registration_status" to "registered", "pass_type" to "premium"))
        am.setUserAttributes(mapOf("vip" to true))

        val ctx = am.buildPayload(identity).context
        assertEquals("registered", ctx.attributes["registration_status"])
        assertEquals("premium", ctx.attributes["pass_type"])
        assertEquals("true", ctx.attributes["vip"])
        assertEquals("android", ctx.platform)
    }

    @Test
    fun mini_service_tags_following_events() {
        val am = mgr()
        am.enterMiniService("checkout", "home")
        am.track("task_started", mapOf("task_id" to "t-9"))

        val e = am.buildPayload(identity).events
        assertEquals("deepdots_mini_service_enter", e[0].name)
        assertEquals("checkout", e[0].params?.get("mini_service")?.jsonPrimitive?.content)
        assertEquals("home", e[0].params?.get("entry_point_type")?.jsonPrimitive?.content)
        assertEquals("task_started", e[1].name)
        assertEquals("checkout", e[1].params?.get("mini_service")?.jsonPrimitive?.content)
        assertEquals("t-9", e[1].params?.get("task_id")?.jsonPrimitive?.content)
    }

    @Test
    fun builds_envelope_linked_by_user_id() {
        val am = mgr()
        am.track("page_view", mapOf("screen" to "/home"))
        val p = am.buildPayload(identity)

        assertEquals("pk-1", p.publicKey)
        assertEquals("u-1", p.userId)
        assertEquals("s-1", p.sessionId)
        assertEquals(1, p.events.size)
    }

    @Test
    fun flush_sends_via_sink_and_clears() {
        val am = mgr()
        am.track("a")
        am.track("b")
        val p = am.flush(identity)

        assertNotNull(captured)
        assertEquals(p, captured)
        assertEquals(listOf("a", "b"), p!!.events.map { it.name })
        assertEquals(0, am.pending())
    }

    @Test
    fun flush_is_noop_when_empty() {
        val am = mgr()
        val p = am.flush(identity)
        assertNull(p)
        assertNull(captured)
    }

    @Test
    fun exit_mini_service_emits_exit_with_duration_and_stops_tagging() {
        now = 1000
        val am = mgr()
        am.enterMiniService("checkout", "home")
        now += 4000
        am.exitMiniService("checkout")
        am.track("after_exit")

        val events = am.buildPayload(identity).events
        val exit = events.first { it.name == "deepdots_mini_service_exit" }
        assertEquals("checkout", exit.params?.get("mini_service")?.jsonPrimitive?.content)
        assertEquals("4", exit.params?.get("duration_seconds")?.jsonPrimitive?.content)
        val after = events.first { it.name == "after_exit" }
        assertNull(after.params?.get("mini_service"))
        now = 1000
    }

    @Test
    fun supports_concurrent_mini_services_exit_by_name_closes_the_right_one() {
        now = 1000
        val am = mgr()
        am.enterMiniService("checkout", "home")
        now += 1000
        am.enterMiniService("support_chat", "fab")
        assertEquals("support_chat", am.getMiniService()) // el más reciente etiqueta

        now += 2000
        am.exitMiniService("checkout") // cierra el de dentro, no el más reciente
        val exit = am.buildPayload(identity).events.first { it.name == "deepdots_mini_service_exit" }
        assertEquals("checkout", exit.params?.get("mini_service")?.jsonPrimitive?.content)
        assertEquals("3", exit.params?.get("duration_seconds")?.jsonPrimitive?.content)

        assertEquals("support_chat", am.getMiniService()) // sigue activo
        am.track("still_in_support")
        val ev = am.buildPayload(identity).events.first { it.name == "still_in_support" }
        assertEquals("support_chat", ev.params?.get("mini_service")?.jsonPrimitive?.content)
        now = 1000
    }

    @Test
    fun exit_all_mini_services_closes_every_active_lifo() {
        val am = mgr()
        am.enterMiniService("a")
        am.enterMiniService("b")
        am.exitAllMiniServices()

        val exits = am.buildPayload(identity).events.filter { it.name == "deepdots_mini_service_exit" }
        assertEquals(listOf("b", "a"), exits.map { it.params?.get("mini_service")?.jsonPrimitive?.content })
        assertNull(am.getMiniService())
    }

    @Test
    fun exit_mini_service_noop_when_that_one_not_active() {
        val am = mgr()
        am.exitMiniService("nope")
        assertEquals(0, am.pending())
    }

    @Test
    fun calls_onFlushNeeded_when_events_reach_maxBatchSize() {
        var called = 0
        val am = AnalyticsManager(sink = { _, _, _ -> }, now = { 1000L }, maxBatchSize = 3, onFlushNeeded = { called++ })
        am.track("e1"); am.track("e2")
        assertEquals(0, called)
        am.track("e3")
        assertEquals(1, called)
    }

    // ───────── setMetric → feedback.metrics (campo dedicado, no metadata) ─────────

    @Test
    fun set_metric_feeds_context_metrics_and_overwrites_by_key() {
        val am = mgr()
        am.setMetric("cart_value", 42)
        am.setMetric("cart_value", 51.5) // sobrescribe
        am.setMetric("items", "3")

        val metrics = am.buildPayload(identity).context.metrics
        assertEquals("51.5", metrics["cart_value"])
        assertEquals("3", metrics["items"])
    }

    @Test
    fun set_metric_ignores_blank_key_and_survives_flushes() {
        val am = mgr()
        am.setMetric("", "x")
        am.setMetric("score", 9)
        am.track("e1")
        am.flush(identity)
        am.track("e2")

        val metrics = am.buildPayload(identity).context.metrics
        assertEquals(1, metrics.size)
        assertEquals("9", metrics["score"]) // persistente: se reenvía en cada flush
    }

    // ───────── Fin de sesión ─────────

    @Test
    fun flush_forwards_meta_to_the_sink() {
        val am = mgr()
        am.track("e1")
        am.flush(identity, AnalyticsFlushMeta(final = true, sessionEnd = true))

        assertEquals(true, capturedMeta?.final)
        assertEquals(true, capturedMeta?.sessionEnd)
    }

    @Test
    fun reset_user_scope_forgets_attributes_and_metrics_not_events() {
        val am = mgr()
        am.setUserAttributes(mapOf("pass_type" to "premium"))
        am.setMetric("cart_value", 42)
        am.track("e1")
        am.resetUserScope()

        val p = am.buildPayload(identity)
        assertEquals(0, p.context.attributes.size)
        assertEquals(0, p.context.metrics.size)
        assertEquals(1, p.events.size) // el buffer no se toca
    }

    // ───────── Fiabilidad de entrega ─────────

    @Test
    fun requeues_the_batch_when_the_sink_reports_a_transient_failure() {
        var attempts = 0
        val am = AnalyticsManager(
            sink = { _, _, requeue ->
                attempts++
                if (attempts == 1) requeue() // primer intento: 5xx
            },
            now = { now },
        )
        am.track("e1")
        am.track("e2")

        am.flush(identity)
        assertEquals(2, am.pending()) // devueltos al buffer

        am.track("e3")
        val p = am.flush(identity)
        // orden cronológico: los re-encolados van delante de lo nuevo
        assertEquals(listOf("e1", "e2", "e3"), p!!.events.map { it.name })
        assertEquals(0, am.pending())
    }

    @Test
    fun requeues_when_the_sink_throws() {
        val am = AnalyticsManager(sink = { _, _, _ -> throw IllegalStateException("boom") }, now = { now })
        am.track("e1")
        am.flush(identity)
        assertEquals(1, am.pending())
    }

    @Test
    fun buffer_cap_drops_the_oldest_events() {
        val am = AnalyticsManager(sink = { _, _, _ -> }, now = { now }, maxBatchSize = 1000, maxBufferedEvents = 3)
        am.track("e1"); am.track("e2"); am.track("e3"); am.track("e4")

        assertEquals(3, am.pending())
        assertEquals(listOf("e2", "e3", "e4"), am.buildPayload(identity).events.map { it.name })
    }
}
