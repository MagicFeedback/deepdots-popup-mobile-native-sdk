package com.deepdots.sdk.tracking

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/**
 * Paridad con el NavigationObserver Web (src/tracking/navigation-observer.test.ts):
 * normalización de pantallas + visitas con duración.
 */
class NavigationObserverParityTest {

    private var now: Long = 1000
    private val visits = mutableListOf<ScreenVisit>()

    private fun observer(): NavigationObserver = NavigationObserver(now = { now }).also { obs ->
        obs.onVisit { visits += it }
    }

    // ───────── normalizeScreen ─────────

    @Test
    fun keeps_the_path_and_drops_the_query() {
        assertEquals("/home", normalizeScreen("/home"))
        assertEquals("/home", normalizeScreen("/home?utm_source=x&a=1"))
        assertEquals("/home", normalizeScreen("https://app.deepdots.com/home?x=1"))
    }

    @Test
    fun collapses_numeric_and_uuid_ids() {
        assertEquals("/product/:id", normalizeScreen("/product/123"))
        assertEquals("/user/:id/orders", normalizeScreen("/user/42/orders"))
        assertEquals(
            "/order/:id",
            normalizeScreen("/order/3f2504e0-4f89-11d3-9a0c-0305e82c3301"),
        )
    }

    @Test
    fun keeps_the_hash_route_without_its_query() {
        assertEquals("/#/settings", normalizeScreen("/#/settings"))
        assertEquals("/#/settings", normalizeScreen("/#/settings?tab=2"))
        assertEquals("/#/user/:id", normalizeScreen("/#/user/7"))
    }

    @Test
    fun empty_paths_normalize_to_root() {
        assertEquals("/", normalizeScreen(""))
        assertEquals("/", normalizeScreen("/"))
        assertEquals("/", normalizeScreen("https://app.deepdots.com"))
    }

    // ───────── visitas ─────────

    @Test
    fun begin_does_not_emit_a_visit() {
        observer().begin("/home")
        assertEquals(0, visits.size)
    }

    @Test
    fun emits_the_previous_screen_with_its_duration_on_navigation() {
        val obs = observer()
        obs.begin("/home")
        now += 5_000
        obs.visit("/product/123")

        assertEquals(1, visits.size)
        assertEquals("/home", visits[0].screen)
        assertEquals(5L, visits[0].durationSeconds)
        assertEquals(1000L, visits[0].entry)
        assertEquals(6000L, visits[0].exit)
    }

    @Test
    fun navigating_to_the_same_screen_is_not_a_new_visit() {
        val obs = observer()
        obs.begin("/home")
        now += 1_000
        obs.visit("/home?utm=x") // normaliza a /home
        obs.visit("/home")

        assertEquals(0, visits.size)
    }

    @Test
    fun stop_closes_the_current_screen_once() {
        val obs = observer()
        obs.begin("/home")
        now += 2_000
        obs.stop()
        obs.stop() // ya no hay pantalla abierta

        assertEquals(1, visits.size)
        assertEquals("/home", visits[0].screen)
        assertEquals(2L, visits[0].durationSeconds)
    }

    // ───────── screenPath y el path de la visita ─────────

    @Test
    fun screen_path_keeps_the_real_ids_dropping_only_the_query() {
        assertEquals(
            "/insights/requests/2f1c9a10-bb5e-11f1-9ff7-b9c354c04f0c",
            screenPath("https://app.test/insights/requests/2f1c9a10-bb5e-11f1-9ff7-b9c354c04f0c?project=X"),
        )
        assertEquals("/#/orders/42", screenPath("https://app.test/#/orders/42?x=1"))
        assertEquals("/Checkout", screenPath("Checkout"))
        assertEquals("/", screenPath(""))
    }

    @Test
    fun each_visit_carries_both_the_normalized_screen_and_the_real_path() {
        val obs = observer()
        obs.begin("/orders/42?tab=1")
        now += 5_000
        obs.visit("/orders/new")

        assertEquals("/orders/:id", visits[0].screen)
        assertEquals("/orders/42", visits[0].path)
        assertEquals(5L, visits[0].durationSeconds)
    }

    @Test
    fun moving_between_two_items_of_the_same_screen_is_a_new_visit() {
        val obs = observer()
        obs.begin("/orders/1")
        now += 3_000
        obs.visit("/orders/2")
        now += 1_000
        obs.visit("/orders/2?tab=2") // misma ruta: solo cambia la query
        obs.stop()

        assertEquals(listOf("/orders/1", "/orders/2"), visits.map { it.path })
        assertEquals(listOf("/orders/:id", "/orders/:id"), visits.map { it.screen })
    }

    // ───────── current() ─────────

    @Test
    fun current_gives_the_screen_in_progress_with_what_its_page_view_would_carry() {
        val obs = observer()
        obs.begin("https://app.test/orders/42?tab=1")
        now += 7_400

        assertEquals(CurrentScreen(screen = "/orders/:id", path = "/orders/42", seconds = 7), obs.current())
        assertEquals(0, visits.size, "no cierra ni emite nada")

        obs.stop()
        assertEquals("/orders/42", visits[0].path)
        assertEquals(7L, visits[0].durationSeconds)
    }

    @Test
    fun current_follows_navigation_from_the_entry_of_the_next_screen() {
        val obs = observer()
        obs.begin("/home")
        now += 10_000
        obs.visit("/cart")
        now += 3_000

        assertEquals(CurrentScreen(screen = "/cart", path = "/cart", seconds = 3), obs.current())
    }

    @Test
    fun current_is_null_before_begin_and_after_stop() {
        val obs = observer()
        assertNull(obs.current())
        obs.begin("/home")
        obs.stop()
        assertNull(obs.current())
    }

    // ───────── restart() ─────────

    @Test
    fun restart_resumes_the_last_screen_after_stop_counting_from_the_reopening() {
        val obs = observer()
        obs.begin("/settings")
        now += 1_000
        obs.stop()
        now += 4_000 // sesión cerrada: este rato no es de nadie
        obs.restart()
        now += 2_000
        obs.stop()

        assertEquals(listOf("/settings" to 1L, "/settings" to 2L), visits.map { it.screen to it.durationSeconds })
    }

    @Test
    fun restart_does_not_override_a_screen_in_progress() {
        val obs = observer()
        obs.begin("/settings")
        now += 1_000
        obs.restart()
        now += 1_000
        obs.stop()

        assertEquals(listOf("/settings" to 2L), visits.map { it.screen to it.durationSeconds })
    }

    @Test
    fun restart_does_nothing_before_any_path() {
        val obs = observer()
        obs.restart()
        obs.stop()

        assertEquals(0, visits.size)
        assertNull(obs.current())
    }
}
