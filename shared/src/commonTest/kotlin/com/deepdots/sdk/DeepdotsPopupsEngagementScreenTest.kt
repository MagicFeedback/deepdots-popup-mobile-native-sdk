package com.deepdots.sdk

import com.deepdots.sdk.analytics.AnalyticsEnvelope
import com.deepdots.sdk.analytics.AnalyticsEvent
import com.deepdots.sdk.analytics.AnalyticsFlushMeta
import com.deepdots.sdk.models.InitOptions
import com.deepdots.sdk.models.PopupOptions
import com.deepdots.sdk.storage.InMemoryStorage
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.longOrNull
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * La pantalla en curso viaja en cada `deepdots_user_engagement` (`screen`, `path`,
 * `screen_seconds`) — paridad con Web (src/core/deepdots-popups.engagement-screen.test.ts).
 *
 * El `page_view` sale al SALIR de una pantalla o al cerrar la sesión (`onBackground()`). Si el
 * cierre no llega nunca (app matada en foreground: crash, OOM, cerrada desde el selector de apps
 * en iOS sin pasar por background; o suspendida antes de que salga el lote de cierre), el backend
 * cierra el registro con lo que tenga: una visita de una sola pantalla se quedaba sin ningún
 * page_view y una de varias perdía la última.
 *
 * Y al volver del background el observador no retomaba la pantalla: la pantalla a la que volvía
 * el usuario no contaba.
 *
 * El reloj (`debugNow`) se fija ANTES de `init()`: el engagement y el observador arrancan ahí.
 */
class DeepdotsPopupsEngagementScreenTest {

    private val minute = 60_000L
    private var clock = 1_000_000L
    private val flushes = mutableListOf<Pair<AnalyticsEnvelope, AnalyticsFlushMeta>>()

    private fun sdk(): DeepdotsPopups = DeepdotsPopups().apply {
        debugNow = { clock }
        init(
            InitOptions(
                debug = true,
                popupOptions = PopupOptions(publicKey = "pk-1"),
                storage = InMemoryStorage(),
            ),
        )
        debugAnalyticsFlushListener = { envelope, meta -> flushes += envelope to meta }
    }

    private fun tickAfter(ms: Long, s: DeepdotsPopups) {
        clock += ms
        s.debugAnalyticsFlushTick()
    }

    /** Lo enviado más lo que queda en el buffer, en orden. */
    private fun events(s: DeepdotsPopups, name: String): List<AnalyticsEvent> =
        (flushes.flatMap { (env, _) -> env.events } + s.previewAnalytics().events).filter { it.name == name }

    private fun engagements(s: DeepdotsPopups) = events(s, "deepdots_user_engagement")
    private fun pageViews(s: DeepdotsPopups) = events(s, "deepdots_page_view")

    private fun AnalyticsEvent.str(key: String): String? = params?.get(key)?.jsonPrimitive?.content
    private fun AnalyticsEvent.long(key: String): Long? = params?.get(key)?.jsonPrimitive?.longOrNull
    private fun AnalyticsEvent.has(key: String): Boolean = params?.containsKey(key) == true

    /** Los tres params de pantalla van juntos o ninguno. */
    private fun AnalyticsEvent.assertScreenParamsAllOrNone() {
        val present = listOf("screen", "path", "screen_seconds").map { has(it) }
        assertTrue(present.all { it } || present.none { it }, "screen/path/screen_seconds: todos o ninguno ($params)")
    }

    @Test
    fun the_heartbeat_carries_the_screen_its_real_path_and_the_seconds_spent_on_it() {
        val s = sdk()
        s.setPath("/orders/42?tab=1")
        tickAfter(5 * minute + 30_000, s)

        val heartbeat = engagements(s).single()
        assertEquals("/orders/:id", heartbeat.str("screen"))
        assertEquals("/orders/42", heartbeat.str("path"))
        // Mismo reloj que el duration_seconds del page_view: desde que se entró en la pantalla.
        assertEquals(5L * 60 + 30, heartbeat.long("screen_seconds"))
        assertEquals(5 * minute + 30_000, heartbeat.long("engagement_time_msec"))
        assertTrue(pageViews(s).isEmpty(), "sin page_view de más: la pantalla sigue abierta")
    }

    @Test
    fun after_navigating_the_heartbeat_reports_the_new_screen_counted_from_its_entry() {
        val s = sdk()
        s.setPath("/home")
        clock += 2 * minute
        s.setPath("/cart")
        tickAfter(3 * minute + 30_000, s)

        val heartbeat = engagements(s).single()
        assertEquals("/cart", heartbeat.str("screen"))
        assertEquals("/cart", heartbeat.str("path"))
        assertEquals(3L * 60 + 30, heartbeat.long("screen_seconds"))
        assertEquals(listOf("/home"), pageViews(s).map { it.str("screen") }, "solo la que se abandonó")
    }

    @Test
    fun before_any_screen_the_engagement_carries_none_of_the_screen_params() {
        val s = sdk()
        tickAfter(5 * minute, s)

        val heartbeat = engagements(s).single()
        assertFalse(heartbeat.has("screen"))
        assertFalse(heartbeat.has("path"))
        assertFalse(heartbeat.has("screen_seconds"))
        heartbeat.assertScreenParamsAllOrNone()
    }

    @Test
    fun the_page_view_carries_the_real_path_next_to_the_normalized_screen() {
        val s = sdk()
        s.setPath("/orders/1")
        clock += 4_000
        s.setPath("/orders/2?tab=2") // otro pedido: otra visita, aunque sea la misma pantalla
        clock += 6_000
        s.setPath("/home")

        val pv = pageViews(s)
        assertEquals(listOf("/orders/:id", "/orders/:id"), pv.map { it.str("screen") })
        assertEquals(listOf("/orders/1", "/orders/2"), pv.map { it.str("path") })
        assertEquals(listOf(4L, 6L), pv.map { it.long("duration_seconds") })
    }

    @Test
    fun closing_the_session_emits_the_page_view_once_and_an_engagement_without_screen() {
        val s = sdk()
        s.setPath("/orders/42")
        clock += 40_000
        s.onBackground()

        val (closing, meta) = flushes.last()
        assertTrue(meta.sessionEnd)
        val pv = closing.events.single { it.name == "deepdots_page_view" }
        assertEquals("/orders/:id", pv.str("screen"))
        assertEquals("/orders/42", pv.str("path"))
        assertEquals(40L, pv.long("duration_seconds"))

        // La pantalla ya tiene su page_view: el engagement del cierre no la repite.
        val engagement = closing.events.single { it.name == "deepdots_user_engagement" }
        assertEquals(40_000L, engagement.long("engagement_time_msec"))
        assertFalse(engagement.has("screen"))
        assertFalse(engagement.has("path"))
        assertFalse(engagement.has("screen_seconds"))
        assertEquals(1, pageViews(s).size)
    }

    @Test
    fun back_from_background_the_resumed_screen_gets_its_page_view_when_left() {
        val s = sdk()
        s.setPath("/home")
        clock += 10_000
        s.onBackground() // page_view de /home (10 s)
        clock += 30 * minute // en background: este rato no es de ninguna pantalla
        s.onForeground() // el host no vuelve a llamar a setPath: sigue en /home
        clock += 7_000
        s.setPath("/cart")

        val pv = pageViews(s)
        assertEquals(listOf("/home" to 10L, "/home" to 7L), pv.map { it.str("screen") to it.long("duration_seconds") })
        assertEquals("/home", pv.last().str("path"))
    }

    @Test
    fun back_from_background_the_resumed_screen_is_closed_by_the_next_background_too() {
        val s = sdk()
        s.setPath("/home")
        clock += 10_000
        s.onBackground()
        clock += minute
        s.onForeground()
        clock += 5_000
        s.onBackground()

        assertEquals(listOf(10L, 5L), pageViews(s).map { it.long("duration_seconds") })
        val secondClose = flushes.last().first.events.map { it.name }
        assertTrue("deepdots_page_view" in secondClose, "el lote de cierre de la sesión reanudada lleva su pantalla")
    }

    @Test
    fun back_from_background_the_heartbeat_carries_the_resumed_screen_from_the_reopening() {
        val s = sdk()
        s.setPath("/home")
        clock += 10_000
        s.onBackground()
        clock += 30 * minute
        s.onForeground()
        val before = engagements(s).size
        tickAfter(5 * minute, s)

        val heartbeat = engagements(s).drop(before).single()
        assertEquals("/home", heartbeat.str("screen"))
        assertEquals(5L * 60, heartbeat.long("screen_seconds"), "cuenta desde la reapertura, no desde la visita anterior")
    }

    @Test
    fun a_user_change_resumes_the_current_screen_in_the_new_session() {
        val s = sdk()
        s.setPath("/settings")
        clock += 3_000
        s.setUserId("user-2")
        clock += 2_000
        s.setPath("/home")

        assertEquals(listOf("/settings" to 3L, "/settings" to 2L), pageViews(s).map { it.str("screen") to it.long("duration_seconds") })
    }

    @Test
    fun every_engagement_carries_the_screen_params_all_or_none() {
        val s = sdk()
        tickAfter(5 * minute, s)
        s.setPath("/home")
        tickAfter(5 * minute, s)
        s.onBackground()
        s.onForeground()
        tickAfter(5 * minute, s)

        val all = engagements(s)
        assertTrue(all.size >= 3)
        all.forEach { it.assertScreenParamsAllOrNone() }
    }
}
