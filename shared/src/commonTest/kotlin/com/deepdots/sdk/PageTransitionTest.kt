package com.deepdots.sdk

import com.deepdots.sdk.ui.PageTransition
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class PageTransitionTest {

    @Test
    fun while_a_page_is_sent_the_card_keeps_the_height_it_had() {
        // El WebView reporta alturas intermedias (pagina vacia, pagina a medio pintar).
        assertEquals(320, PageTransition.heightSource(reported = 140, frozen = 320, loading = true))
        assertEquals(320, PageTransition.heightSource(reported = 610, frozen = 320, loading = true))
    }

    @Test
    fun once_the_page_arrives_the_reported_height_wins() {
        assertEquals(410, PageTransition.heightSource(reported = 410, frozen = 320, loading = false))
    }

    @Test
    fun without_a_frozen_height_the_reported_one_is_used() {
        // Primera carga: aun no habia altura que conservar.
        assertEquals(200, PageTransition.heightSource(reported = 200, frozen = null, loading = true))
        assertNull(PageTransition.heightSource(reported = null, frozen = null, loading = true))
    }

    @Test
    fun the_spinner_waits_long_enough_to_skip_normal_page_changes() {
        // Una pagina en una red normal llega en 150-300 ms: el spinner no debe llegar a salir.
        assertTrue(PageTransition.SPINNER_DELAY_MS >= 400L)
        // Pero sigue saliendo a tiempo cuando la red va lenta.
        assertTrue(PageTransition.SPINNER_DELAY_MS < PopupRevealLimits.REVEAL_TIMEOUT_MS)
    }
}

private object PopupRevealLimits {
    const val REVEAL_TIMEOUT_MS: Long = com.deepdots.sdk.ui.PopupReveal.REVEAL_TIMEOUT_MS
}
