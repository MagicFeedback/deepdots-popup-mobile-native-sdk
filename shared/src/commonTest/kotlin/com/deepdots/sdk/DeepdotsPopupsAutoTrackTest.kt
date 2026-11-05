package com.deepdots.sdk

import com.deepdots.sdk.analytics.AnalyticsEnvelope
import com.deepdots.sdk.analytics.AnalyticsEvent
import com.deepdots.sdk.analytics.AnalyticsFlushMeta
import com.deepdots.sdk.models.AutoTrack
import com.deepdots.sdk.models.InitOptions
import com.deepdots.sdk.models.PopupOptions
import com.deepdots.sdk.storage.InMemoryStorage
import com.deepdots.sdk.tracking.AutoSignals
import com.deepdots.sdk.tracking.AutoTrackTarget
import com.deepdots.sdk.tracking.chooseContext
import kotlinx.serialization.json.jsonPrimitive
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Detección automática (contrato común `SDK-AUTO-DETECTION-CONTRACT.md`, paso 3: native).
 *
 * Las piezas de plataforma (ProcessLifecycleOwner, ActivityLifecycleCallbacks, NotificationCenter,
 * viewDidAppear) solo traducen a `AutoSignals`, que llama a `autoForeground` / `autoBackground` /
 * `autoInactive` / `autoScreen` de cada instancia. Aquí se prueba esa parte común: activa por
 * defecto, se apaga por señal, y lo manual manda sin duplicar nada.
 */
class DeepdotsPopupsAutoTrackTest {

    private var clock = 1_000_000L
    private val flushes = mutableListOf<Pair<AnalyticsEnvelope, AnalyticsFlushMeta>>()

    private fun sdk(
        autoTrack: AutoTrack = AutoTrack(),
        screenName: ((String) -> String?)? = null,
    ): DeepdotsPopups = DeepdotsPopups().apply {
        debugNow = { clock }
        init(
            InitOptions(
                debug = true,
                popupOptions = PopupOptions(publicKey = "pk-1"),
                storage = InMemoryStorage(),
                autoTrack = autoTrack,
                screenName = screenName,
            ),
        )
        debugAnalyticsFlushListener = { envelope, meta -> flushes += envelope to meta }
    }

    private fun events(s: DeepdotsPopups, name: String): List<AnalyticsEvent> =
        (flushes.flatMap { (env, _) -> env.events } + s.previewAnalytics().events).filter { it.name == name }

    private fun AnalyticsEvent.str(key: String): String? = params?.get(key)?.jsonPrimitive?.content

    private fun sessionEnds(s: DeepdotsPopups) = events(s, "deepdots_session_end")
    private fun sessionStarts(s: DeepdotsPopups) = events(s, "deepdots_session_start")
    private fun pageViews(s: DeepdotsPopups) = events(s, "deepdots_page_view")

    @AfterTest
    fun tearDown() = AutoSignals.debugReset()

    // ───────── ciclo de vida ─────────

    @Test
    fun background_detected_by_the_platform_ends_the_session_by_default() {
        val s = sdk()
        s.autoBackground()
        assertEquals(listOf("background"), sessionEnds(s).map { it.str("reason") })
        assertTrue(flushes.last().second.sessionEnd)
    }

    @Test
    fun foreground_detected_by_the_platform_opens_a_new_session() {
        val s = sdk()
        s.autoBackground()
        s.autoForeground()
        assertEquals(listOf("cold_start", "foreground"), sessionStarts(s).map { it.str("reason") })
    }

    @Test
    fun the_first_foreground_after_init_does_not_open_a_second_session() {
        // ProcessLifecycleOwner entrega ON_START al registrarse si la app ya está arrancada.
        val s = sdk()
        s.autoForeground()
        assertEquals(1, sessionStarts(s).size)
    }

    @Test
    fun inactive_only_flushes_and_never_ends_the_session() {
        val s = sdk()
        s.track("something")
        s.autoInactive()
        assertTrue(sessionEnds(s).isEmpty())
        assertTrue(flushes.isNotEmpty(), "inactive vacía el buffer")
        assertTrue(flushes.none { it.second.sessionEnd })
    }

    @Test
    fun autoTrack_lifecycle_false_ignores_platform_lifecycle() {
        val s = sdk(autoTrack = AutoTrack(lifecycle = false))
        s.autoBackground()
        s.autoInactive()
        assertTrue(sessionEnds(s).isEmpty())
        assertTrue(flushes.isEmpty())
        // La API manual sigue funcionando.
        s.onBackground()
        assertEquals(1, sessionEnds(s).size)
    }

    @Test
    fun a_manual_lifecycle_call_turns_detection_off_for_that_instance() {
        val s = sdk()
        s.onBackground()
        s.autoForeground() // ignorado: el host gestiona el ciclo de vida
        assertEquals(1, sessionStarts(s).size)
        s.onForeground()
        s.autoBackground() // ignorado
        assertEquals(1, sessionEnds(s).size)
        assertEquals(2, sessionStarts(s).size)
    }

    @Test
    fun manual_and_detected_background_together_end_the_session_once() {
        val s = sdk()
        s.autoBackground()
        s.onBackground()
        assertEquals(1, sessionEnds(s).size)
    }

    // ───────── pantallas ─────────

    @Test
    fun detected_screens_feed_page_views_with_the_class_name_as_path() {
        val s = sdk()
        s.autoScreen("HomeActivity")
        clock += 3_000
        s.autoScreen("CheckoutActivity")
        val pv = pageViews(s).single()
        assertEquals("/HomeActivity", pv.str("path"))
        assertEquals("3", pv.str("duration_seconds"))
    }

    @Test
    fun the_same_detected_screen_twice_is_one_visit() {
        val s = sdk()
        s.autoScreen("HomeActivity")
        s.autoScreen("HomeActivity")
        s.autoScreen("CheckoutActivity")
        assertEquals(1, pageViews(s).size)
    }

    @Test
    fun screenName_renames_the_detected_screen_and_null_keeps_it() {
        val s = sdk(screenName = { name -> if (name == "HomeActivity") "home" else null })
        s.autoScreen("HomeActivity")
        s.autoScreen("CheckoutActivity")
        s.autoScreen("/x") // nombre que ya parece una ruta: se respeta
        assertEquals(listOf("/home", "/CheckoutActivity"), pageViews(s).map { it.str("path") })
    }

    @Test
    fun screenName_that_throws_falls_back_to_the_detected_name() {
        val s = sdk(screenName = { error("boom") })
        s.autoScreen("HomeActivity")
        s.autoScreen("Other")
        assertEquals(listOf("/HomeActivity"), pageViews(s).map { it.str("path") })
    }

    @Test
    fun autoTrack_screens_false_ignores_detected_screens() {
        val s = sdk(autoTrack = AutoTrack(screens = false))
        s.autoScreen("HomeActivity")
        s.autoScreen("CheckoutActivity")
        assertTrue(pageViews(s).isEmpty())
    }

    @Test
    fun the_first_setPath_takes_over_without_a_page_view_for_the_detected_screen() {
        // Un host que ya llama a setPath (o el plugin de Flutter) no ve un page_view de
        // "MainActivity": la pantalla detectada se descarta y desde ahí manda setPath.
        val s = sdk()
        s.autoScreen("MainActivity")
        clock += 2_000
        s.setPath("/home")
        s.autoScreen("OtherActivity") // ignorado
        clock += 1_000
        s.setPath("/settings")
        assertEquals(listOf("/home"), pageViews(s).map { it.str("path") })
    }

    @Test
    fun background_closes_the_detected_screen_and_foreground_resumes_it() {
        val s = sdk()
        s.autoScreen("HomeActivity")
        clock += 4_000
        s.autoBackground()
        s.autoForeground()
        clock += 1_000
        s.autoScreen("Other")
        assertEquals(listOf("/HomeActivity", "/HomeActivity"), pageViews(s).map { it.str("path") })
    }

    // ───────── despacho desde la plataforma ─────────

    private class FakeTarget : AutoTrackTarget {
        val calls = mutableListOf<String>()
        override fun autoForeground() { calls += "fg" }
        override fun autoBackground() { calls += "bg" }
        override fun autoInactive() { calls += "inactive" }
        override fun autoScreen(name: String) { calls += "screen:$name" }
        override fun autoContextAvailable() { calls += "ctx" }
    }

    @Test
    fun signals_reach_every_registered_instance() {
        val a = FakeTarget()
        val b = FakeTarget()
        AutoSignals.register(a)
        AutoSignals.register(b)
        AutoSignals.background()
        AutoSignals.screen("Home")
        AutoSignals.foreground()
        AutoSignals.inactive()
        AutoSignals.contextAvailable()
        val expected = listOf("bg", "screen:Home", "fg", "inactive", "ctx")
        assertEquals(expected, a.calls)
        assertEquals(expected, b.calls)
    }

    @Test
    fun an_instance_registered_late_gets_the_current_screen() {
        // init() después de que la primera Activity ya está en pantalla.
        AutoSignals.screen("SplashActivity")
        AutoSignals.screen("HomeActivity")
        val t = FakeTarget()
        AutoSignals.register(t)
        assertEquals(listOf("screen:HomeActivity"), t.calls)
    }

    @Test
    fun a_failing_instance_does_not_stop_the_others() {
        val bad = object : AutoTrackTarget {
            override fun autoForeground() = error("boom")
            override fun autoBackground() = error("boom")
            override fun autoInactive() = error("boom")
            override fun autoScreen(name: String) = error("boom")
            override fun autoContextAvailable() = error("boom")
        }
        val ok = FakeTarget()
        AutoSignals.register(bad)
        AutoSignals.register(ok)
        AutoSignals.background()
        assertEquals(listOf("bg"), ok.calls)
    }

    // ───────── contexto ─────────

    @Test
    fun the_detected_context_is_used_until_the_host_attaches_one() {
        assertEquals("auto", chooseContext(manualLocked = false, cached = "old", detected = "auto"))
        assertEquals("old", chooseContext(manualLocked = false, cached = "old", detected = null))
        assertEquals("manual", chooseContext(manualLocked = true, cached = "manual", detected = "auto"))
        assertEquals(null, chooseContext<String>(manualLocked = false, cached = null, detected = null))
    }
}

class ScreenNameForClassTest {
    @Test
    fun swift_module_prefix_is_dropped() {
        assertEquals("HomeViewController", com.deepdots.sdk.tracking.screenNameForClass("MyApp.HomeViewController"))
        assertEquals("ProfileVC", com.deepdots.sdk.tracking.screenNameForClass("ProfileVC"))
    }

    @Test
    fun system_generic_and_compose_classes_are_not_screens() {
        listOf(
            "UINavigationController",
            "UIAlertController",
            "_UIRemoteInputViewController",
            "_TtGC7SwiftUI19UIHostingControllerV5MyApp8HomeView_",
            "SwiftUI.UIHostingController<Home>",
            "ComposeHostingViewController",
            "androidx_compose_ui_window_ComposeHostingViewController",
            "",
        ).forEach { assertEquals(null, com.deepdots.sdk.tracking.screenNameForClass(it), it) }
    }
}
