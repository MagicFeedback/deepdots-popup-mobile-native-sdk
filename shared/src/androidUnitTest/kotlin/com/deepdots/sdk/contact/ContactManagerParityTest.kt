package com.deepdots.sdk.contact

import com.deepdots.sdk.storage.InMemoryStorage
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Paridad con el ContactManager Web (src/contact/contact-manager.test.ts): los atributos del
 * host solo se envían cuando cambian.
 *
 * Vive en el source set de Android (no en commonTest) porque `setAttributes` es `suspend` y
 * `runBlocking` no existe en common; la clase bajo test es común.
 */
class ContactManagerParityTest {

    private val sent = mutableListOf<ContactBody>()

    private fun mgr(storage: InMemoryStorage = InMemoryStorage()) = ContactManager(
        storage = storage,
        publicKey = "pk-1",
        userId = "user-a",
        post = { body -> sent += body },
    )

    @Test
    fun sends_the_attributes_the_first_time() = runBlocking {
        val m = mgr()
        val didSend = m.setAttributes(mapOf("plan" to "premium", "age" to 34))

        assertTrue(didSend)
        assertEquals(1, sent.size)
        assertEquals("pk-1", sent[0].publicKey)
        assertEquals("user-a", sent[0].userId)
        assertEquals(mapOf("plan" to "premium", "age" to "34"), sent[0].userAttributes)
    }

    @Test
    fun does_not_resend_unchanged_attributes() = runBlocking {
        val m = mgr()
        m.setAttributes(mapOf("plan" to "premium"))
        val second = m.setAttributes(mapOf("plan" to "premium"))

        assertTrue(!second)
        assertEquals(1, sent.size)
    }

    @Test
    fun key_order_does_not_count_as_a_change() = runBlocking {
        val m = mgr()
        m.setAttributes(mapOf("a" to "1", "b" to "2"))
        val second = m.setAttributes(mapOf("b" to "2", "a" to "1"))

        assertTrue(!second)
        assertEquals(1, sent.size)
    }

    @Test
    fun resends_when_a_value_changes() = runBlocking {
        val m = mgr()
        m.setAttributes(mapOf("plan" to "premium"))
        val second = m.setAttributes(mapOf("plan" to "free"))

        assertTrue(second)
        assertEquals(2, sent.size)
        assertEquals("free", sent[1].userAttributes["plan"])
    }

    @Test
    fun the_diff_survives_between_sessions() = runBlocking {
        val storage = InMemoryStorage()
        mgr(storage).setAttributes(mapOf("plan" to "premium"))
        val second = mgr(storage).setAttributes(mapOf("plan" to "premium"))

        assertTrue(!second, "el diff se persiste en storage, no en memoria")
        assertEquals(1, sent.size)
    }

    @Test
    fun ignores_blank_keys_and_null_values() = runBlocking {
        val m = mgr()
        m.setAttributes(mapOf("" to "x", "plan" to null, "age" to 20))

        assertEquals(mapOf("age" to "20"), sent[0].userAttributes)
    }
}
