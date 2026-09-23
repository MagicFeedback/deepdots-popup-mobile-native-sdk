package com.deepdots.sdk.tracking

import kotlin.test.Test
import kotlin.test.assertEquals

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
}
