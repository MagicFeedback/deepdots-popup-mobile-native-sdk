package com.deepdots.sdk.analytics

import com.deepdots.sdk.storage.InMemoryStorage
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

/** Paridad con el CrashReporter Web (src/analytics/crash-reporter.test.ts). */
class CrashReporterParityTest {

    private fun reporter(
        storage: InMemoryStorage = InMemoryStorage(),
        emitted: MutableList<Map<String, Any?>> = mutableListOf(),
        enabled: Boolean = true,
    ) = CrashReporter(
        storage = storage,
        emit = { emitted.add(it) },
        device = { DeviceSnapshot(appVersion = "1.0.0", osVersion = "17.4", deviceModel = "iPhone14,3") },
        sessionId = { "sess-1" },
        now = { 1_000L },
        enabled = { enabled },
    )

    @Test
    fun report_error_emits_immediately_with_crash_time_context() {
        val emitted = mutableListOf<Map<String, Any?>>()
        val r = reporter(emitted = emitted)
        r.reportError(IllegalStateException("boom"), severity = "error", context = mapOf("screen" to "Checkout"))

        assertEquals(1, emitted.size)
        val p = emitted[0]
        assertEquals(1_000L, p["crashed_at"])
        assertEquals("IllegalStateException", p["crash_type"])
        assertEquals("boom", p["message"])
        assertEquals(false, p["fatal"])
        assertEquals(true, p["handled"])
        assertEquals("error", p["severity"])
        assertEquals("sess-1", p["crashed_session_id"])
        assertEquals("1.0.0", p["crashed_app_version"])
        assertEquals("17.4", p["crashed_os_version"])
        assertEquals("iPhone14,3", p["crashed_device_model"])
        assertEquals("Checkout", p["ctx_screen"])
    }

    @Test
    fun severity_fatal_marks_fatal_true() {
        val emitted = mutableListOf<Map<String, Any?>>()
        val r = reporter(emitted = emitted)
        r.reportError(RuntimeException("a"))
        r.reportError(RuntimeException("b"), severity = "fatal")
        assertEquals(false, emitted[0]["fatal"])
        assertEquals("error", emitted[0]["severity"])
        assertEquals(true, emitted[1]["fatal"])
        assertEquals("fatal", emitted[1]["severity"])
    }

    @Test
    fun disabled_is_a_no_op() {
        val emitted = mutableListOf<Map<String, Any?>>()
        val r = reporter(emitted = emitted, enabled = false)
        r.reportError(RuntimeException("x"))
        assertEquals(0, emitted.size)
    }

    @Test
    fun capture_unhandled_persists_and_drain_returns_and_clears() {
        val storage = InMemoryStorage()
        val r = reporter(storage = storage)
        r.captureUnhandled("NullPointerException", "npe", "stack-trace")
        r.captureUnhandled("RangeError", "oob", "")

        val drained = r.drainPendingCrashes()
        assertEquals(2, drained.size)
        assertEquals("npe", drained[0].message)
        assertEquals(true, drained[0].fatal)
        assertEquals(false, drained[0].handled)
        assertEquals("fatal", drained[0].severity)
        assertTrue(r.drainPendingCrashes().isEmpty())
    }

    @Test
    fun queue_caps_at_20_dropping_oldest() {
        val storage = InMemoryStorage()
        val r = reporter(storage = storage)
        for (i in 0 until 25) r.captureUnhandled("E", "e$i", "")
        val drained = r.drainPendingCrashes()
        assertEquals(20, drained.size)
        assertEquals("e5", drained[0].message)
        assertEquals("e24", drained[19].message)
    }

    @Test
    fun drain_tolerates_corrupt_storage() {
        val storage = InMemoryStorage()
        storage.putString("deepdots.crash.queue", "not json")
        val r = reporter(storage = storage)
        assertTrue(r.drainPendingCrashes().isEmpty())
    }

    @Test
    fun crash_record_to_params_omits_null_optionals() {
        val rec = CrashRecord(
            crashedAt = 5L, crashType = "Error", message = "m", stack = "", fatal = true,
            handled = false, severity = "fatal",
        )
        val p = crashRecordToParams(rec)
        assertNull(p["crashed_session_id"])
        assertNull(p["crashed_app_version"])
        assertEquals(5L, p["crashed_at"])
        assertEquals("Error", p["crash_type"])
        assertEquals(true, p["fatal"])
    }
}
