package com.deepdots.sdk.ui

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Los registros del Back se sueltan una sola vez, por el primer camino que llegue (descarte de
 * la composición o detach de la vista), y nada registrado después queda colgado.
 */
class BackRegistrationTest {

    @Test
    fun releasesEverythingOnceInReverseOrder() {
        val calls = mutableListOf<String>()
        val registration = BackRegistration()
        registration.add { calls += "a" }
        registration.add { calls += "b" }

        registration.release()
        registration.release()

        assertEquals(listOf("b", "a"), calls)
        assertTrue(registration.released)
    }

    @Test
    fun aFailingReleaseDoesNotLeaveTheOthersRegistered() {
        val calls = mutableListOf<String>()
        val registration = BackRegistration()
        registration.add { calls += "a" }
        registration.add { error("boom") }
        registration.add { calls += "c" }

        registration.release()

        assertEquals(listOf("c", "a"), calls)
    }

    @Test
    fun addingAfterReleaseReleasesImmediately() {
        var released = false
        val registration = BackRegistration()
        registration.release()

        registration.add { released = true }

        assertTrue(released)
    }
}
