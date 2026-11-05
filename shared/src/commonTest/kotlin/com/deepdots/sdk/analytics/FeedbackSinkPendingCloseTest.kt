package com.deepdots.sdk.analytics

import com.deepdots.sdk.service.RejectedFeedbackException
import com.deepdots.sdk.storage.InMemoryStorage
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * DD-55, paridad con Web 1.17: el lote de cierre se guarda ANTES de enviarse y, si no llega a
 * entregarse (la app muere o iOS la suspende con el POST en vuelo), el sink del arranque
 * siguiente lo reenvía a SU registro. Espejo de "pendientes persistidos (storage)" en
 * `feedback-payload.test.ts` (Web).
 */
class FeedbackSinkPendingCloseTest {

    private val keys = AnalyticsKeys(publicKey = "pub-a", integration = "int-1")
    private val unconfined = CoroutineScope(Dispatchers.Unconfined)
    private val closingMeta = AnalyticsFlushMeta(sessionEnd = true)
    private val noKeepAlive: (String) -> () -> Unit = { {} }

    private fun envelope(name: String = "deepdots_session_end") = AnalyticsEnvelope(
        context = AnalyticsContext(platform = "ios"),
        events = listOf(AnalyticsEvent(name = name, timestamp = 1L)),
    )

    private fun sink(
        storage: InMemoryStorage?,
        now: () -> Long = { 1_000L },
        onSessionClosed: ((String) -> Unit)? = null,
        post: suspend (AnalyticsFeedbackBody) -> String?,
    ) = FeedbackSink(
        keys = keys, scope = unconfined, post = post, keepAlive = noKeepAlive,
        storage = storage, now = now, onSessionClosed = onSessionClosed,
    )

    @Test
    fun a_closing_batch_that_never_answers_is_resent_on_the_next_launch_to_its_own_record() {
        val storage = InMemoryStorage()
        // Sesión abierta con un lote aceptado: el cierre lleva su sessionId.
        val before = sink(storage) { body -> if (body.completed) CompletableDeferred<String?>().await() else "fbk-1" }
        before.send(envelope("deepdots_page_view"), AnalyticsFlushMeta()) {}
        var requeued = false
        before.send(envelope(), closingMeta) { requeued = true } // la app muere sin respuesta
        assertFalse(requeued)

        val sent = mutableListOf<AnalyticsFeedbackBody>()
        val closed = mutableListOf<String>()
        sink(storage, onSessionClosed = { closed += it }) { body -> sent += body; body.sessionId }
            .resendPendingCloses()

        assertEquals(1, sent.size)
        assertEquals("fbk-1", sent[0].sessionId)
        assertTrue(sent[0].completed)
        assertEquals("deepdots_session_end", sent[0].feedback.metadata.first { it.key.startsWith("deepdots_session") }.key)
        assertEquals(listOf("fbk-1"), closed)

        // Entregado: el arranque siguiente ya no lo repite.
        val again = mutableListOf<AnalyticsFeedbackBody>()
        sink(storage) { body -> again += body; null }.resendPendingCloses()
        assertTrue(again.isEmpty())
    }

    @Test
    fun a_failed_closing_batch_is_kept_for_the_next_launch_instead_of_going_back_to_the_buffer() {
        val storage = InMemoryStorage()
        var requeued = false
        sink(storage) { throw IllegalStateException("offline") }.send(envelope(), closingMeta) { requeued = true }

        // Re-encolarlo lo mandaría con la sesión SIGUIENTE y en su registro.
        assertFalse(requeued)
        assertTrue(storage.getString(pendingCloseKey(keys.integration)) != null)
    }

    @Test
    fun a_delivered_closing_batch_leaves_nothing_pending() {
        val storage = InMemoryStorage()
        sink(storage) { "fbk-1" }.send(envelope(), closingMeta) {}
        assertNull(storage.getString(pendingCloseKey(keys.integration)))
    }

    @Test
    fun a_pending_close_the_api_rejects_is_dropped() {
        val storage = InMemoryStorage()
        sink(storage) { throw IllegalStateException("offline") }.send(envelope(), closingMeta) {}

        sink(storage) { throw RejectedFeedbackException("422 bad") }.resendPendingCloses()
        assertNull(storage.getString(pendingCloseKey(keys.integration)))
    }

    @Test
    fun a_pending_close_that_fails_again_stays_for_the_next_launch() {
        val storage = InMemoryStorage()
        sink(storage) { throw IllegalStateException("offline") }.send(envelope(), closingMeta) {}

        sink(storage) { throw IllegalStateException("still offline") }.resendPendingCloses()
        val sent = mutableListOf<AnalyticsFeedbackBody>()
        sink(storage) { body -> sent += body; null }.resendPendingCloses()
        assertEquals(1, sent.size)
    }

    @Test
    fun a_pending_close_older_than_24_hours_is_not_resent() {
        val storage = InMemoryStorage()
        sink(storage, now = { 0L }) { throw IllegalStateException("offline") }.send(envelope(), closingMeta) {}

        val sent = mutableListOf<AnalyticsFeedbackBody>()
        sink(storage, now = { PENDING_CLOSE_MAX_AGE_MS + 1 }) { body -> sent += body; null }.resendPendingCloses()
        assertTrue(sent.isEmpty())
        assertNull(storage.getString(pendingCloseKey(keys.integration)))
    }

    @Test
    fun without_storage_a_failed_close_is_requeued_as_before() {
        var requeued = false
        sink(null) { throw IllegalStateException("offline") }.send(envelope(), closingMeta) { requeued = true }
        assertTrue(requeued)
    }
}
