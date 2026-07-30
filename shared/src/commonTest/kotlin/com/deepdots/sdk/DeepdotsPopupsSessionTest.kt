package com.deepdots.sdk

import com.deepdots.sdk.analytics.AnalyticsEnvelope
import com.deepdots.sdk.analytics.AnalyticsFlushMeta
import com.deepdots.sdk.models.InitOptions
import com.deepdots.sdk.models.PopupOptions
import com.deepdots.sdk.storage.InMemoryStorage
import com.deepdots.sdk.storage.KeyValueStorage
import kotlinx.serialization.json.jsonPrimitive
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Fin de sesión y cambio de usuario — paridad con Web
 * (src/core/deepdots-popups.analytics.test.ts, bloque de sesión).
 *
 * El último lote de una sesión va con `sessionEnd` (→ `completed:true` en el body) para que el
 * backend cierre el registro; el `sessionId` cacheado se olvida y el lote siguiente abre uno nuevo.
 * El cierre HACE FLUSH, así que se observa por el seam `debugAnalyticsFlushListener` en vez del
 * buffer (que queda vacío).
 */
class DeepdotsPopupsSessionTest {

    private val flushes = mutableListOf<Pair<AnalyticsEnvelope, AnalyticsFlushMeta>>()

    private fun sdk(
        storage: KeyValueStorage = InMemoryStorage(),
        userId: String? = null,
        trackingEnabled: Boolean = true,
    ): DeepdotsPopups = DeepdotsPopups().apply {
        init(
            InitOptions(
                debug = true,
                popupOptions = PopupOptions(publicKey = "pk-1"),
                storage = storage,
                trackingEnabled = trackingEnabled,
                metadata = userId?.let { mapOf<String, Any>("userId" to it) },
            ),
        )
        debugAnalyticsFlushListener = { envelope, meta -> flushes += envelope to meta }
    }

    private fun lastFlush() = flushes.last()

    private fun eventNames(envelope: AnalyticsEnvelope) = envelope.events.map { it.name }

    private fun reason(envelope: AnalyticsEnvelope): String? = envelope.events
        .first { it.name == "deepdots_session_end" }
        .params?.get("reason")?.jsonPrimitive?.content

    // ───────── cierre por lifecycle ─────────

    @Test
    fun on_background_closes_the_session_marking_the_batch_as_the_last_one() {
        val s = sdk()
        s.onBackground()

        val (envelope, meta) = lastFlush()
        assertEquals("background", reason(envelope))
        assertTrue(meta.sessionEnd, "el último lote debe ir marcado → completed:true en el body")
        assertTrue(meta.final, "el cierre no puede esperar: transporte de cierre")
    }

    @Test
    fun closing_flushes_screen_engagement_and_session_end_in_one_batch() {
        val s = sdk()
        s.setPath("/home")
        s.enterMiniService("checkout", "home")
        s.onBackground()

        val names = eventNames(lastFlush().first)
        assertTrue(names.contains("deepdots_page_view"), "cierra la pantalla abierta")
        assertTrue(names.contains("deepdots_mini_service_exit"), "cierra el mini-service abierto")
        assertEquals("deepdots_session_end", names.last(), "session_end cierra el lote")
    }

    @Test
    fun closing_twice_does_not_duplicate_session_end() {
        val s = sdk()
        s.onBackground()
        val flushesAfterFirst = flushes.size
        s.onBackground()
        s.endSession()

        assertEquals(flushesAfterFirst, flushes.size, "los cierres siguientes son no-op")
        assertEquals(1, flushes.sumOf { (env, _) -> env.events.count { it.name == "deepdots_session_end" } })
    }

    @Test
    fun foreground_opens_a_new_session() {
        val s = sdk()
        s.onBackground()
        s.onForeground()

        assertEquals(
            listOf("deepdots_session_start"),
            s.previewAnalytics().events.map { it.name },
        )
    }

    @Test
    fun end_session_is_the_explicit_host_close() {
        val s = sdk()
        s.endSession()
        assertEquals("manual", reason(lastFlush().first))
    }

    @Test
    fun the_next_batch_after_a_close_carries_no_session_id() {
        val s = sdk()
        s.onBackground()
        s.onForeground()
        s.track("cta_click")
        s.flushAnalytics()

        val (envelope, meta) = lastFlush()
        assertNull(envelope.sessionId, "el sessionId viejo pertenecía al registro cerrado")
        assertTrue(!meta.sessionEnd)
    }

    // ───────── consentimiento ─────────

    @Test
    fun revoking_consent_closes_the_session_and_granting_it_opens_one() {
        val s = sdk()
        s.setTrackingEnabled(false)
        assertEquals("tracking_disabled", reason(lastFlush().first))

        s.setTrackingEnabled(true)
        assertEquals(
            listOf("deepdots_session_start"),
            s.previewAnalytics().events.map { it.name },
        )
    }

    @Test
    fun granting_consent_after_init_with_tracking_off_emits_session_start() {
        val s = sdk(trackingEnabled = false)
        assertEquals(0, s.previewAnalytics().events.size)

        s.setTrackingEnabled(true)
        assertEquals(
            listOf("deepdots_session_start"),
            s.previewAnalytics().events.map { it.name },
        )
    }

    // ───────── cambio de usuario ─────────

    @Test
    fun set_user_id_closes_the_previous_session_and_opens_a_new_one() {
        val s = sdk(userId = "user-a")
        assertEquals("user-a", s.getUserId())

        s.setUserId("user-b")

        assertEquals("user-b", s.getUserId())
        assertEquals("user_change", reason(lastFlush().first))
        assertEquals(
            listOf("deepdots_session_start"),
            s.previewAnalytics().events.map { it.name },
        )
    }

    @Test
    fun the_closing_batch_carries_the_previous_user_identity() {
        val s = sdk(userId = "user-a")
        s.setUserId("user-b")

        val (envelope, meta) = lastFlush()
        assertEquals("user-a", envelope.userId, "el lote de cierre es del usuario que se va")
        assertTrue(meta.sessionEnd)
    }

    @Test
    fun set_user_id_without_argument_falls_back_to_the_anonymous_sdk_id() {
        val storage = InMemoryStorage()
        val s = sdk(storage = storage, userId = "user-a")

        s.setUserId(null)

        val anonymous = s.getUserId()
        assertNotNull(anonymous)
        assertTrue(anonymous != "user-a")
        assertEquals(storage.getString("deepdots.user_id"), anonymous)
    }

    @Test
    fun set_user_id_with_the_same_id_is_a_noop() {
        val s = sdk(userId = "user-a")
        s.flushAnalytics() // limpia el session_start del init
        val flushesBefore = flushes.size
        s.setUserId("user-a")

        assertEquals(flushesBefore, flushes.size)
        assertEquals(0, s.previewAnalytics().events.size)
    }

    @Test
    fun set_user_id_forgets_attributes_and_metrics_of_the_previous_user() {
        val s = sdk(userId = "user-a")
        s.setUserAttributes(mapOf("pass_type" to "premium"))
        s.setMetric("cart_value", 42)

        s.setUserId("user-b")

        val ctx = s.previewAnalytics().context
        assertEquals(0, ctx.attributes.size)
        assertEquals(0, ctx.metrics.size)
    }

    @Test
    fun the_closing_batch_still_carries_the_previous_user_metrics() {
        val s = sdk(userId = "user-a")
        s.setMetric("cart_value", 42)

        s.setUserId("user-b")

        assertEquals("42", lastFlush().first.context.metrics["cart_value"])
    }
}
