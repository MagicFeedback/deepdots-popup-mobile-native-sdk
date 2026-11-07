package com.deepdots.sdk

import com.deepdots.sdk.analytics.AnalyticsFeedbackBody
import com.deepdots.sdk.analytics.AnalyticsKeys
import com.deepdots.sdk.contact.ContactBody
import com.deepdots.sdk.models.InitOptions
import com.deepdots.sdk.models.PopupOptions
import com.deepdots.sdk.service.PopupsService
import com.deepdots.sdk.storage.InMemoryStorage
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * DD-55, cierre de sesión sin pérdidas. En iOS la app puede quedar suspendida (o morir) con el
 * POST de cierre en vuelo: el cierre se guarda antes de enviarse y el arranque siguiente lo
 * reenvía a su registro, con `completed:true`. El cierre por background con background task lo
 * cubre `FeedbackSinkBackgroundTaskTest`.
 */
class DeepdotsPopupsSessionCloseTest {

    /** Doble del servicio: el primer lote abre "fbk-1"; el cierre puede colgarse (app suspendida). */
    private class Service(private val hangOnClose: Boolean) : PopupsService {
        val bodies = mutableListOf<AnalyticsFeedbackBody>()
        override suspend fun fetchPopups(publicKey: String, filter: String?): String = "[]"
        override suspend fun postPopupEvent(publicKey: String, status: String, popupId: String, userId: String?): String? = null
        override suspend fun postFeedback(body: AnalyticsFeedbackBody): String? {
            bodies += body
            if (body.completed && hangOnClose) CompletableDeferred<String?>().await()
            return body.sessionId ?: "fbk-1"
        }
        override suspend fun postContact(body: ContactBody) = Unit
    }

    private fun sdk(service: PopupsService, storage: InMemoryStorage) = DeepdotsPopups().apply {
        debugTransportScope = CoroutineScope(Dispatchers.Unconfined)
        // Antes de init(): el reenvío de los cierres pendientes sale en el propio init().
        debugSetPopupsService(service)
        init(
            InitOptions(
                popupOptions = PopupOptions(),
                storage = storage,
                analytics = AnalyticsKeys(publicKey = "pub-k", integration = "int-close"),
            ),
        )
    }

    @Test
    fun app_killed_with_the_closing_post_in_flight_resends_it_on_the_next_launch() {
        val storage = InMemoryStorage()
        val before = Service(hangOnClose = true)
        val a = sdk(before, storage)
        a.setPath("/home")
        a.flushAnalytics()
        a.onBackground() // el POST de cierre no responde nunca: la app muere

        val closeBefore = before.bodies.single { it.completed }
        assertEquals("fbk-1", closeBefore.sessionId)

        val after = Service(hangOnClose = false)
        sdk(after, storage)

        val resent = after.bodies.filter { it.completed }
        assertEquals(1, resent.size, "el cierre se reenvía en el arranque siguiente")
        assertEquals("fbk-1", resent[0].sessionId, "a su registro, no al de la sesión nueva")
        assertTrue(resent[0].feedback.metadata.any { it.key == "deepdots_session_end" })
    }

    @Test
    fun a_delivered_close_is_not_resent_on_the_next_launch() {
        val storage = InMemoryStorage()
        val a = sdk(Service(hangOnClose = false), storage)
        a.flushAnalytics()
        a.onBackground()

        val after = Service(hangOnClose = false)
        sdk(after, storage)
        assertTrue(after.bodies.none { it.completed })
    }
}
