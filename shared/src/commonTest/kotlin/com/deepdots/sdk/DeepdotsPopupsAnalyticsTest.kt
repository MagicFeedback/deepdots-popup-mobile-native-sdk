package com.deepdots.sdk

import com.deepdots.sdk.analytics.CrashReporter
import com.deepdots.sdk.analytics.DeviceSnapshot
import com.deepdots.sdk.models.InitOptions
import com.deepdots.sdk.models.PopupOptions
import com.deepdots.sdk.storage.InMemoryStorage
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.long
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Integración de analytics en DeepdotsPopups (canal separado) — paridad con Web
 * (src/core/deepdots-popups.analytics.test.ts).
 */
class DeepdotsPopupsAnalyticsTest {

    private fun sdk(): DeepdotsPopups = DeepdotsPopups().apply {
        init(
            InitOptions(
                debug = true,
                popupOptions = PopupOptions(publicKey = "pk-1"),
                storage = InMemoryStorage(),
            ),
        )
    }

    @Test
    fun track_buffers_events_linked_to_user_id() {
        val s = sdk()
        s.track("cta_click", mapOf("label" to "comprar"))

        val preview = s.previewAnalytics()
        assertTrue(preview.userId != null)
        assertEquals("pk-1", preview.publicKey)
        // Buffer contains deepdots_session_start (emitted at init) + the cta_click.
        val e = preview.events.first { it.name == "cta_click" }
        assertEquals("comprar", e.params?.get("label")?.jsonPrimitive?.content)
    }

    @Test
    fun set_user_attributes_feeds_context() {
        val s = sdk()
        s.setUserAttributes(mapOf("registration_status" to "registered", "pass_type" to "premium"))
        val ctx = s.previewAnalytics().context
        assertEquals("registered", ctx.attributes["registration_status"])
        assertEquals("premium", ctx.attributes["pass_type"])
    }

    @Test
    fun enter_mini_service_tags_following_events() {
        val s = sdk()
        s.enterMiniService("checkout", "home")
        s.track("task_started", mapOf("task_id" to "t-9"))

        // Buffer contains deepdots_session_start + deepdots_mini_service_enter + task_started.
        val events = s.previewAnalytics().events
        val miniEnter = events.first { it.name == "deepdots_mini_service_enter" }
        val taskStarted = events.first { it.name == "task_started" }
        assertEquals("checkout", taskStarted.params?.get("mini_service")?.jsonPrimitive?.content)
        assertTrue(events.indexOf(miniEnter) < events.indexOf(taskStarted))
    }

    @Test
    fun flush_clears_the_buffer() {
        val s = sdk()
        s.track("page_view", mapOf("screen" to "/home"))
        s.flushAnalytics()
        assertEquals(0, s.previewAnalytics().events.size)
    }

    @Test
    fun disabled_tracking_makes_track_noop() {
        val s = sdk()
        s.flushAnalytics() // clear session_start emitted at init
        s.setTrackingEnabled(false)
        s.track("cta_click")
        // No events buffered after disabling (session_start was already flushed).
        assertEquals(0, s.previewAnalytics().events.size)
    }

    @Test
    fun track_search_emits_search_with_findability_convention() {
        val s = sdk()
        s.trackSearch("zapatos", 0)
        val e = s.previewAnalytics().events.first { it.name == "deepdots_search" }
        assertEquals("zapatos", e.params?.get("query")?.jsonPrimitive?.content)
        assertEquals("0", e.params?.get("results_count")?.jsonPrimitive?.content)
        assertEquals("false", e.params?.get("has_results")?.jsonPrimitive?.content)
    }

    @Test
    fun track_findability_friction_emits_event_with_topic() {
        val s = sdk()
        s.trackFindabilityFriction("checkout_address")
        val e = s.previewAnalytics().events.first { it.name == "deepdots_findability_friction" }
        assertEquals("checkout_address", e.params?.get("friction_topic")?.jsonPrimitive?.content)
    }

    @Test
    fun track_funnel_step_emits_funnel_step_with_task_id() {
        val s = sdk()
        s.trackFunnelStep("outstanding_task", "task_started", "task-42")
        val e = s.previewAnalytics().events.first { it.name == "deepdots_funnel_step" }
        assertEquals("outstanding_task", e.params?.get("funnel")?.jsonPrimitive?.content)
        assertEquals("task_started", e.params?.get("step")?.jsonPrimitive?.content)
        assertEquals("task-42", e.params?.get("task_id")?.jsonPrimitive?.content)
    }

    @Test
    fun navigation_via_setPath_emits_page_view() {
        val s = sdk()
        s.setPath("/home")          // begin (no emite)
        s.setPath("/product/123")   // sale de /home → emite page_view de /home

        val pv = s.previewAnalytics().events.filter { it.name == "deepdots_page_view" }
        assertEquals(1, pv.size)
        assertEquals("/home", pv[0].params?.get("screen")?.jsonPrimitive?.content)
    }

    @Test
    fun emits_session_start_at_init_and_app_crash_on_report_error() {
        val s = sdk()
        val names0 = s.previewAnalytics().events.map { it.name }
        assertTrue(names0.contains("deepdots_session_start"))

        s.reportError(IllegalStateException("kaboom"), severity = "error", context = mapOf("screen" to "Home"))
        val crash = s.previewAnalytics().events.first { it.name == "deepdots_app_crash" }
        assertEquals("IllegalStateException", crash.params?.get("crash_type")?.jsonPrimitive?.content)
        assertEquals("kaboom", crash.params?.get("message")?.jsonPrimitive?.content)
        assertEquals("Home", crash.params?.get("ctx_screen")?.jsonPrimitive?.content)
    }

    @Test
    fun replays_pending_crashes_from_disk_at_init() {
        val storage = InMemoryStorage()
        val seeder = CrashReporter(
            storage = storage,
            emit = {},
            device = { DeviceSnapshot(appVersion = "0.9.0") },
            sessionId = { null },
            now = { 111L },
        )
        seeder.captureUnhandled("RangeError", "old crash", "")

        val s = DeepdotsPopups().apply {
            init(InitOptions(debug = true, popupOptions = PopupOptions(publicKey = "pk-1"), storage = storage))
        }
        val crash = s.previewAnalytics().events.first { it.name == "deepdots_app_crash" }
        assertEquals(111L, crash.params?.get("crashed_at")?.jsonPrimitive?.long)
        assertEquals("RangeError", crash.params?.get("crash_type")?.jsonPrimitive?.content)
        assertEquals("0.9.0", crash.params?.get("crashed_app_version")?.jsonPrimitive?.content)
        assertNull(storage.getString("deepdots.crash.queue"))
    }

    @Test
    fun disabled_tracking_emits_no_session_start_nor_crash() {
        val s = DeepdotsPopups().apply {
            init(InitOptions(debug = true, popupOptions = PopupOptions(publicKey = "pk-1"), storage = InMemoryStorage(), trackingEnabled = false))
        }
        s.reportError(RuntimeException("x"))
        val names = s.previewAnalytics().events.map { it.name }
        assertTrue(!names.contains("deepdots_session_start"))
        assertTrue(!names.contains("deepdots_app_crash"))
    }
}
