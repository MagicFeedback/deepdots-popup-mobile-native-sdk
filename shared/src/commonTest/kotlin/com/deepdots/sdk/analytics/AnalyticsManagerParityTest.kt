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
    private val sink: AnalyticsSink = { captured = it }
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
        am.exitMiniService()
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
    fun exit_mini_service_noop_when_none_active() {
        val am = mgr()
        am.exitMiniService()
        assertEquals(0, am.pending())
    }

    @Test
    fun calls_onFlushNeeded_when_events_reach_maxBatchSize() {
        var called = 0
        val am = AnalyticsManager(sink = {}, now = { 1000L }, maxBatchSize = 3, onFlushNeeded = { called++ })
        am.track("e1"); am.track("e2")
        assertEquals(0, called)
        am.track("e3")
        assertEquals(1, called)
    }
}
