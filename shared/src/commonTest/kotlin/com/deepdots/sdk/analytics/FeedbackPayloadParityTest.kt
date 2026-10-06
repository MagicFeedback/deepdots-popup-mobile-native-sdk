package com.deepdots.sdk.analytics

import com.deepdots.sdk.service.RejectedFeedbackException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Paridad con el builder Web (src/analytics/feedback-payload.test.ts):
 * envelope de analytics → body de POST /sdk/feedback, y el transporte (`FeedbackSink`, espejo
 * de `createFeedbackSink`).
 */
class FeedbackPayloadParityTest {

    private val keys = AnalyticsKeys(publicKey = "pub-k", integration = "int-1")

    private fun envelope(
        userId: String? = "u-1",
        sessionId: String? = "srv-9",
        context: AnalyticsContext = AnalyticsContext(
            platform = "android",
            language = "es-ES",
            device = DeviceInfo(
                deviceType = "mobile",
                userAgent = "UA/1",
                appVersion = "1.2.3",
                timezone = "Europe/Madrid",
                screenResolution = "1080x1920",
                viewportSize = "360x640",
                pixelRatio = "3",
                connectionType = "wifi",
            ),
            attributes = mapOf("pass_type" to "premium"),
        ),
    ) = AnalyticsEnvelope(
        publicKey = "pub-k",
        userId = userId,
        sessionId = sessionId,
        context = context,
        events = listOf(
            AnalyticsEvent("deepdots_page_view", 1000, JsonObject(mapOf("screen" to JsonPrimitive("/home"), "duration_seconds" to JsonPrimitive(5)))),
            AnalyticsEvent("deepdots_user_engagement", 2000, JsonObject(mapOf("engagement_time_msec" to JsonPrimitive(4200)))),
        ),
    )

    /** Helper: metadata como mapa key → value[0] */
    private fun mdMap(body: AnalyticsFeedbackBody) =
        body.feedback.metadata.associate { it.key to it.value[0] }

    @Test
    fun puts_each_event_in_metadata_with_value_array_answers_empty() {
        val body = buildAnalyticsFeedbackBody(envelope(), keys)
        val md = mdMap(body)

        assertNotNull(md["deepdots_page_view"])
        assertTrue(md["deepdots_page_view"]!!.contains("\"timestamp\":1000"))
        assertTrue(md["deepdots_page_view"]!!.contains("\"screen\":\"/home\""))
        assertNotNull(md["deepdots_user_engagement"])

        val evEntry = body.feedback.metadata.first { it.key == "deepdots_page_view" }
        assertEquals(1, evEntry.value.size)

        assertEquals(emptyList(), body.feedback.answers)
    }

    @Test
    fun identity_in_profile_and_deepdots_prefixed_metadata() {
        val body = buildAnalyticsFeedbackBody(envelope(), keys)
        assertEquals(listOf(FeedbackKV("external-user-id", listOf("u-1"))), body.feedback.profile)
        val md = mdMap(body)
        assertEquals("u-1", md["deepdots_user_id"])
        assertEquals("srv-9", md["deepdots_session_id"])
    }

    @Test
    fun context_in_deepdots_prefixed_metadata_attributes_without_prefix() {
        val body = buildAnalyticsFeedbackBody(envelope(), keys)
        val md = mdMap(body)
        assertEquals("android", md["deepdots_platform"])
        assertEquals("es-ES", md["deepdots_language"])
        assertEquals("mobile", md["deepdots_device_type"])
        assertEquals("UA/1", md["deepdots_user_agent"])
        assertEquals("1.2.3", md["deepdots_app_version"])
        assertEquals("Europe/Madrid", md["deepdots_timezone"])
        assertEquals("1080x1920", md["deepdots_screen_resolution"])
        assertEquals("360x640", md["deepdots_viewport_size"])
        assertEquals("3", md["deepdots_pixel_ratio"])
        assertEquals("wifi", md["deepdots_connection_type"])
        assertEquals("premium", md["pass_type"]) // atributo de usuario — sin prefijo deepdots_
    }

    @Test
    fun flags_and_no_session_when_not_provided() {
        val body = buildAnalyticsFeedbackBody(envelope(), keys)
        assertEquals(false, body.completed)
        assertEquals(false, body.finished)
        assertEquals(false, body.feedback.finished)
        assertEquals("", body.feedback.text)
        assertEquals("pub-k", body.publicKey)
        assertEquals("int-1", body.integration)
        assertNull(body.sessionId)
    }

    @Test
    fun includes_feedbackSessionId_when_provided() {
        val body = buildAnalyticsFeedbackBody(envelope(), keys, feedbackSessionId = "fbk-sess-1")
        assertEquals("fbk-sess-1", body.sessionId)
    }

    // ───────── Fin de sesión: completed:true ─────────

    @Test
    fun session_end_marks_the_body_completed_keeping_the_session_id() {
        val body = buildAnalyticsFeedbackBody(
            envelope(),
            keys,
            feedbackSessionId = "fbk-sess-1",
            options = BuildBodyOptions(sessionEnd = true),
        )
        // completed:true cierra el registro; el sessionId SÍ va (es el registro que se cierra).
        assertEquals(true, body.completed)
        assertEquals("fbk-sess-1", body.sessionId)
        // `finished` no es la señal de cierre acordada: se queda en false.
        assertEquals(false, body.finished)
        assertEquals(false, body.feedback.finished)
    }

    @Test
    fun streaming_batches_are_not_completed() {
        val body = buildAnalyticsFeedbackBody(envelope(), keys, feedbackSessionId = "fbk-sess-1")
        assertEquals(false, body.completed)
    }

    // ───────── métricas del host → feedback.metrics ─────────

    @Test
    fun host_metrics_go_to_the_dedicated_metrics_field_without_prefix() {
        val body = buildAnalyticsFeedbackBody(
            envelope(
                context = AnalyticsContext(
                    platform = "android",
                    attributes = mapOf("pass_type" to "premium"),
                    metrics = mapOf("cart_value" to "51.5"),
                ),
            ),
            keys,
        )
        assertEquals(listOf(FeedbackKV("cart_value", listOf("51.5"))), body.feedback.metrics)
        // no se cuela en metadata
        assertTrue(body.feedback.metadata.none { it.key == "cart_value" })
    }

    @Test
    fun metrics_is_always_present_and_empty_when_unused() {
        val body = buildAnalyticsFeedbackBody(envelope(), keys)
        assertEquals(emptyList(), body.feedback.metrics)
    }

    @Test
    fun omits_missing_metadata_and_profile() {
        val body = buildAnalyticsFeedbackBody(
            envelope(userId = null, sessionId = null, context = AnalyticsContext(platform = "android", attributes = emptyMap())),
            keys,
        )
        assertEquals(emptyList(), body.feedback.profile)
        val keysMd = body.feedback.metadata.map { it.key }
        assertTrue("deepdots_user_id" !in keysMd)
        assertTrue("deepdots_session_id" !in keysMd)
        assertTrue("deepdots_device_type" !in keysMd)
        assertTrue("deepdots_platform" in keysMd)
        assertNull(body.sessionId)
    }

    // ── FeedbackSink (espejo de `describe('createFeedbackSink')`) ──────────────────────────
    //
    // Con `Dispatchers.Unconfined` el envío corre en el hilo del test hasta su primera
    // suspensión, y al completar una puerta se reanuda en el acto: los tests son síncronos y
    // corren igual en JVM que en el simulador de iOS (sin `runBlocking`).

    private val unconfined = CoroutineScope(Dispatchers.Unconfined)

    /** Doble del POST: devuelve `responses[i]` en la llamada i (la última se repite). */
    private class FakePost(vararg val responses: suspend () -> String?) {
        val bodies = mutableListOf<AnalyticsFeedbackBody>()
        suspend fun post(body: AnalyticsFeedbackBody): String? {
            bodies += body
            return responses[minOf(bodies.size - 1, responses.size - 1)]()
        }
    }

    private fun feedbackSink(
        fake: FakePost,
        onSessionId: ((String) -> Unit)? = null,
        onSessionClosed: ((String) -> Unit)? = null,
    ) = FeedbackSink(
        keys = keys,
        scope = unconfined,
        post = fake::post,
        onSessionId = onSessionId,
        onSessionClosed = onSessionClosed,
    )

    private val closingMeta = AnalyticsFlushMeta(final = true, sessionEnd = true)

    @Test
    fun sink_reports_on_session_closed_with_the_session_id_of_the_record_it_closes() {
        val closed = mutableListOf<String>()
        // Respuesta de cierre sin sessionId: vale el que llevaba el lote.
        val fake = FakePost({ "fbk-1" }, { null })
        val sink = feedbackSink(fake, onSessionClosed = { closed += it })

        sink.send(envelope(), AnalyticsFlushMeta()) {} // abre fbk-1
        assertEquals(emptyList(), closed)
        sink.send(envelope(), closingMeta) {}

        assertEquals("fbk-1", fake.bodies[1].sessionId)
        assertEquals(listOf("fbk-1"), closed)
    }

    @Test
    fun sink_single_batch_session_reports_the_closing_response_id_but_no_open() {
        val opened = mutableListOf<String>()
        val closed = mutableListOf<String>()
        val sink = feedbackSink(FakePost({ "fbk-9" }), onSessionId = { opened += it }, onSessionClosed = { closed += it })

        sink.send(envelope(), closingMeta) {}

        assertEquals(emptyList(), opened, "el cierre no se re-cachea")
        assertEquals(listOf("fbk-9"), closed)
        assertNull(sink.currentSessionId())
    }

    @Test
    fun sink_ignores_the_first_response_when_the_session_closed_while_it_was_in_flight() {
        // onBackground() con el primer POST aún sin respuesta: el cierre (final) no lo espera.
        val gate = CompletableDeferred<String?>()
        val opened = mutableListOf<String>()
        val fake = FakePost({ gate.await() }, { null })
        val sink = feedbackSink(fake, onSessionId = { opened += it })

        sink.send(envelope(), AnalyticsFlushMeta()) {}
        sink.send(envelope(), closingMeta) {}
        gate.complete("fbk-stale")

        // Ni se avisa como abierto ni se cachea: el lote siguiente (sesión nueva) va sin sessionId.
        assertEquals(emptyList(), opened)
        assertNull(sink.currentSessionId())
        sink.send(envelope(), AnalyticsFlushMeta()) {}
        assertEquals(3, fake.bodies.size)
        assertNull(fake.bodies[2].sessionId)
    }

    @Test
    fun sink_does_not_report_on_session_closed_when_the_backend_rejects_the_closing_batch() {
        val closed = mutableListOf<String>()
        var requeued = 0
        val fake = FakePost({ "fbk-1" }, { throw RejectedFeedbackException("406 Contact not found") })
        val sink = feedbackSink(fake, onSessionClosed = { closed += it })

        sink.send(envelope(), AnalyticsFlushMeta()) {}
        sink.send(envelope(), closingMeta) { requeued++ }

        assertEquals(emptyList(), closed)
        assertEquals(0, requeued, "un 4xx se descarta, no se reintenta")
    }

    /** Solo KMP: aquí el fallo se señaliza con `requeue()`, así que hay que blindarlo aparte. */
    @Test
    fun sink_a_throwing_on_session_closed_does_not_requeue_the_delivered_closing_batch() {
        var requeued = 0
        val fake = FakePost({ "fbk-1" })
        val sink = feedbackSink(fake, onSessionClosed = { throw IllegalStateException("bug del host") })

        sink.send(envelope(), AnalyticsFlushMeta()) {}
        sink.send(envelope(), closingMeta) { requeued++ }

        assertEquals(0, requeued, "el cierre se entregó: re-encolarlo lo enviaría dos veces")
        assertEquals(2, fake.bodies.size)
    }
}
