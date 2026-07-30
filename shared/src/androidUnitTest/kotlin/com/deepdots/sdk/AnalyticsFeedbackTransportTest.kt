package com.deepdots.sdk

import com.deepdots.sdk.analytics.AnalyticsFeedbackBody
import com.deepdots.sdk.analytics.AnalyticsKeys
import com.deepdots.sdk.contact.ContactBody
import com.deepdots.sdk.models.InitOptions
import com.deepdots.sdk.models.PopupOptions
import com.deepdots.sdk.service.PopupsService
import com.deepdots.sdk.service.RetryableFeedbackException
import com.deepdots.sdk.storage.InMemoryStorage
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Transporte real del canal de analytics (`POST /sdk/feedback`) con un doble del servicio:
 * `completed:true` en el cierre, reintento de fallos transitorios y serialización del primer
 * lote. Paridad con `createFeedbackSink` del Web (src/analytics/feedback-payload.test.ts).
 *
 * En el source set de Android porque necesita `runBlocking` para esperar a las corrutinas de envío.
 */
class AnalyticsFeedbackTransportTest {

    private class FakeService(
        private val sessionId: String? = "fbk-1",
        private val failFirst: Boolean = false,
    ) : PopupsService {
        val bodies = mutableListOf<AnalyticsFeedbackBody>()
        /** Se resuelve cuando el test quiere dejar responder al primer POST. */
        val firstResponseGate = CompletableDeferred<Unit>()
        var gateFirstResponse = false
        private var calls = 0

        override suspend fun fetchPopups(publicKey: String, filter: String?): String = "[]"

        override suspend fun postPopupEvent(publicKey: String, status: String, popupId: String, userId: String?): String? = null

        override suspend fun postFeedback(body: AnalyticsFeedbackBody): String? {
            calls++
            if (gateFirstResponse && calls == 1) firstResponseGate.await()
            bodies += body
            if (failFirst && calls == 1) throw RetryableFeedbackException("503 simulado")
            return sessionId
        }

        override suspend fun postContact(body: ContactBody) = Unit
    }

    private fun sdk(service: PopupsService): DeepdotsPopups = DeepdotsPopups().apply {
        init(
            InitOptions(
                debug = true,
                popupOptions = PopupOptions(),
                storage = InMemoryStorage(),
                analytics = AnalyticsKeys(publicKey = "pub-k", integration = "int-1"),
            ),
        )
        debugSetPopupsService(service)
    }

    /** Espera activa corta: las corrutinas de envío no son observables de otra forma. */
    private suspend fun awaitUntil(timeoutMs: Long = 2_000, condition: () -> Boolean) {
        var waited = 0L
        while (!condition() && waited < timeoutMs) {
            delay(20)
            waited += 20
        }
        assertTrue(condition(), "condición no cumplida en ${timeoutMs}ms")
    }

    private fun metadataOf(body: AnalyticsFeedbackBody) =
        body.feedback.metadata.associate { it.key to it.value[0] }

    @Test
    fun the_closing_batch_is_posted_with_completed_true() = runBlocking {
        val service = FakeService()
        val s = sdk(service)
        s.track("cta_click")

        s.onBackground()

        awaitUntil { service.bodies.isNotEmpty() }
        val body = service.bodies.last()
        assertEquals(true, body.completed, "el último lote cierra el registro")
        assertTrue(metadataOf(body).containsKey("deepdots_session_end"))
        assertEquals("int-1", body.integration)
        assertEquals("pub-k", body.publicKey)
    }

    @Test
    fun streaming_batches_are_posted_with_completed_false() = runBlocking {
        val service = FakeService()
        val s = sdk(service)
        s.track("cta_click")
        s.flushAnalytics()

        awaitUntil { service.bodies.isNotEmpty() }
        assertEquals(false, service.bodies.last().completed)
    }

    @Test
    fun the_second_batch_inherits_the_session_id_of_the_first() = runBlocking {
        val service = FakeService(sessionId = "fbk-1")
        val s = sdk(service)
        s.track("e1")
        s.flushAnalytics()
        awaitUntil { service.bodies.size == 1 }
        assertNull(service.bodies[0].sessionId, "el primer POST aún no conoce el sessionId")

        s.track("e2")
        s.flushAnalytics()
        awaitUntil { service.bodies.size == 2 }
        assertEquals("fbk-1", service.bodies[1].sessionId, "un solo registro en backend")
    }

    @Test
    fun batches_are_serialized_until_the_session_id_is_known() = runBlocking {
        val service = FakeService(sessionId = "fbk-1")
        service.gateFirstResponse = true
        val s = sdk(service)

        s.track("e1")
        s.flushAnalytics()
        s.track("e2")
        s.flushAnalytics() // no debe salir antes de que responda el primero

        delay(150)
        assertEquals(0, service.bodies.size, "ambos esperan la respuesta del primero")

        service.firstResponseGate.complete(Unit)
        awaitUntil { service.bodies.size == 2 }
        assertNull(service.bodies[0].sessionId)
        assertEquals("fbk-1", service.bodies[1].sessionId, "el segundo hereda el registro, no crea otro")
    }

    @Test
    fun after_a_close_the_next_batch_omits_the_old_session_id() = runBlocking {
        val service = FakeService(sessionId = "fbk-1")
        val s = sdk(service)
        s.track("e1")
        s.flushAnalytics()
        awaitUntil { service.bodies.size == 1 }

        s.onBackground() // cierre: lleva el sessionId del registro que cierra
        awaitUntil { service.bodies.size == 2 }
        assertEquals("fbk-1", service.bodies[1].sessionId)
        assertEquals(true, service.bodies[1].completed)

        s.onForeground()
        s.track("e2")
        s.flushAnalytics()
        awaitUntil { service.bodies.size == 3 }
        assertNull(service.bodies[2].sessionId, "sesión nueva → registro nuevo")
        assertEquals(false, service.bodies[2].completed)
    }

    @Test
    fun a_transient_failure_requeues_the_batch_for_the_next_flush() = runBlocking {
        val service = FakeService(sessionId = null, failFirst = true)
        val s = sdk(service)
        s.track("e1")
        s.flushAnalytics()

        // El 503 devuelve los eventos al buffer.
        awaitUntil { s.previewAnalytics().events.any { it.name == "e1" } }

        s.flushAnalytics()
        awaitUntil { service.bodies.size == 2 }
        assertTrue(
            metadataOf(service.bodies[1]).containsKey("e1"),
            "el evento del lote fallido se reintenta, no se pierde",
        )
    }
}
