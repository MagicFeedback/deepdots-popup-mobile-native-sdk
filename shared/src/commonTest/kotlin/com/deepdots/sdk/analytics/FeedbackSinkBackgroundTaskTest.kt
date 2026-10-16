package com.deepdots.sdk.analytics

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * El lote de cierre sale justo en `onBackground()`; en iOS la app puede suspenderse con ese POST
 * en vuelo. `FeedbackSink` lo envuelve en una background task (`beginBackgroundTask`) que termina
 * al acabar el envío. Divergencia con Web, sin espejo (allí lo cubre `sendBeacon`).
 */
class FeedbackSinkBackgroundTaskTest {

    private val keys = AnalyticsKeys(publicKey = "pub-a", integration = "int-1")
    private val unconfined = CoroutineScope(Dispatchers.Unconfined)
    private val closingMeta = AnalyticsFlushMeta(final = true, sessionEnd = true)

    private val begun = mutableListOf<String>()
    private var ended = 0
    private val keepAlive: (String) -> () -> Unit = { name ->
        begun += name
        { ended++ }
    }

    private fun envelope() = AnalyticsEnvelope(
        context = AnalyticsContext(platform = "ios"),
        events = listOf(AnalyticsEvent(name = "deepdots_session_end", timestamp = 1L)),
    )

    private fun sink(post: suspend (AnalyticsFeedbackBody) -> String?) =
        FeedbackSink(keys = keys, scope = unconfined, post = post, keepAlive = keepAlive)

    @Test
    fun the_closing_batch_keeps_the_app_alive_until_its_post_completes() {
        val response = CompletableDeferred<String?>()
        val s = sink { response.await() }

        s.send(envelope(), closingMeta) {}
        assertEquals(1, begun.size, "abre una background task para el cierre")
        assertEquals(0, ended, "sigue abierta con el POST en vuelo")

        response.complete("fbk-1")
        assertEquals(1, ended, "termina al acabar el envío")
    }

    @Test
    fun a_failed_closing_post_ends_the_task_too() {
        val s = sink { throw IllegalStateException("offline") }
        var requeued = false

        s.send(envelope(), closingMeta) { requeued = true }

        assertEquals(true, requeued)
        assertEquals(1, begun.size)
        assertEquals(1, ended)
    }

    @Test
    fun regular_batches_do_not_open_a_background_task() {
        val s = sink { "fbk-1" }

        s.send(envelope(), AnalyticsFlushMeta()) {}
        s.send(envelope(), AnalyticsFlushMeta(final = true)) {}

        assertEquals(0, begun.size)
    }
}
