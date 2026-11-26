package com.deepdots.sdk

import com.deepdots.sdk.analytics.AnalyticsFeedbackBody
import com.deepdots.sdk.contact.ContactBody
import com.deepdots.sdk.models.InitOptions
import com.deepdots.sdk.models.PopupOptions
import com.deepdots.sdk.service.PopupsService
import com.deepdots.sdk.storage.InMemoryStorage
import com.deepdots.sdk.storage.KeyValueStorage
import com.deepdots.sdk.util.SdkLock
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * DD-561: el servidor aplica las exclusiones por email, dominio y atributo (DD-333) desde el
 * Contact, así que el GET de popups va después del POST de contact de `init(contactAttributes)`,
 * pero nunca espera más de [CONTACT_SYNC_TIMEOUT_MS]. Paridad con Web
 * `deepdots-popups.contact-order.test.ts`.
 */
class ContactBeforePopupsTest {

    /** Doble del servicio: registra el orden y deja el POST de contact en vuelo hasta que el test lo suelte. */
    private class OrderedService(private val gateContact: Boolean) : PopupsService {
        private val lock = SdkLock()
        private val events = mutableListOf<String>()
        val contactGate = CompletableDeferred<Boolean>()

        fun log(): List<String> = lock.withLock { events.toList() }
        private fun add(e: String) = lock.withLock { events.add(e) }

        override suspend fun fetchPopups(publicKey: String, filter: String?): String {
            add("popups")
            return "[]"
        }

        override suspend fun postPopupEvent(publicKey: String, status: String, popupId: String, userId: String?): String? = null

        override suspend fun postFeedback(body: AnalyticsFeedbackBody): String? = null

        override suspend fun postContact(body: ContactBody) {
            add("contact:start")
            if (gateContact && !contactGate.await()) {
                add("contact:fail")
                throw IllegalStateException("offline (simulado)")
            }
            add("contact:end")
        }
    }

    private fun start(
        service: PopupsService,
        storage: KeyValueStorage = InMemoryStorage(),
        attrs: Map<String, Any?>? = mapOf("email" to "bo@shop.dd333-internal.com"),
        timeoutMs: Long? = null,
    ): DeepdotsPopups = DeepdotsPopups().apply {
        debugSetPopupsService(service)
        debugPopupsFetchRetryDelaysMs = listOf(0, 0)
        debugContactSyncTimeoutMs = timeoutMs
        init(
            InitOptions(
                popupOptions = PopupOptions(publicKey = "pk-dd561"),
                storage = storage,
                metadata = mapOf("userId" to "new-user"),
                contactAttributes = attrs,
            ),
        )
    }

    @Test
    fun new_attributes_the_popups_get_waits_for_the_contact_post() = runBlocking<Unit> {
        val service = OrderedService(gateContact = true)
        start(service)

        assertTrue(waitUntil { "contact:start" in service.log() })
        delay(150)
        assertTrue("popups" !in service.log(), "the GET must wait for the contact POST")

        service.contactGate.complete(true)
        assertTrue(waitUntil { "popups" in service.log() })
        val log = service.log()
        assertTrue(log.indexOf("popups") > log.indexOf("contact:end"), "order: $log")
    }

    @Test
    fun a_failed_contact_post_does_not_block_the_popups() = runBlocking<Unit> {
        val service = OrderedService(gateContact = true)
        start(service)
        assertTrue(waitUntil { "contact:start" in service.log() })

        service.contactGate.complete(false)
        assertTrue(waitUntil { "popups" in service.log() })
        assertTrue(service.log().indexOf("popups") > service.log().indexOf("contact:fail"))
    }

    @Test
    fun a_slow_contact_post_the_popups_get_goes_after_the_timeout() = runBlocking<Unit> {
        val service = OrderedService(gateContact = true)
        start(service, timeoutMs = 300)

        assertTrue(waitUntil { "contact:start" in service.log() })
        delay(100)
        assertTrue("popups" !in service.log(), "still inside the timeout")
        assertTrue(waitUntil(timeoutMs = 2_000) { "popups" in service.log() }, "the GET goes after the timeout")
        assertTrue("contact:end" !in service.log(), "the POST is still pending")
        service.contactGate.complete(true)
    }

    @Test
    fun the_default_timeout_is_at_most_one_and_a_half_seconds() {
        assertEquals(1_500L, CONTACT_SYNC_TIMEOUT_MS)
    }

    @Test
    fun same_attributes_as_last_time_no_post_and_no_wait() = runBlocking<Unit> {
        val storage = InMemoryStorage()
        val first = OrderedService(gateContact = false)
        start(first, storage)
        assertTrue(waitUntil { "popups" in first.log() })
        assertTrue("contact:end" in first.log())

        val second = OrderedService(gateContact = true) // would hang if it posted
        start(second, storage)
        assertTrue(waitUntil(timeoutMs = 1_000) { "popups" in second.log() })
        assertTrue("contact:start" !in second.log())
    }

    @Test
    fun without_contact_attributes_there_is_no_wait() = runBlocking<Unit> {
        val service = OrderedService(gateContact = true)
        start(service, attrs = null)
        assertTrue(waitUntil(timeoutMs = 1_000) { "popups" in service.log() })
        assertTrue("contact:start" !in service.log())
    }
}
