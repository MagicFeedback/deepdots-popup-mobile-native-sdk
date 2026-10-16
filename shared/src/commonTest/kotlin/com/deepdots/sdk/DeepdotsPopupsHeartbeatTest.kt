package com.deepdots.sdk

import com.deepdots.sdk.analytics.AnalyticsEnvelope
import com.deepdots.sdk.analytics.AnalyticsFlushMeta
import com.deepdots.sdk.models.InitOptions
import com.deepdots.sdk.models.PopupOptions
import com.deepdots.sdk.storage.InMemoryStorage
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.longOrNull
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Latido del canal de analytics — paridad con Web
 * (src/core/deepdots-popups.session-timeout.test.ts, bloque del latido).
 *
 * El backend (Run_Jobs `incomplete-surveys`) cierra una sesión que lleva 60 min sin recibir
 * ningún lote. Una app abierta en una sola pantalla no enviaba nada — el page_view sale al salir
 * de la pantalla, el engagement al ir a background — y la sesión se cerraba con solo su
 * session_start. Con el latido, la app en foreground manda su engagement al menos cada 5 min.
 *
 * El reloj (`debugNow`) se fija ANTES de `init()`: el engagement arranca ahí.
 */
class DeepdotsPopupsHeartbeatTest {

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

    private fun engagementEvents() = flushes
        .flatMap { (env, _) -> env.events }
        .filter { it.name == "deepdots_user_engagement" }

    private fun tickAfter(ms: Long, s: DeepdotsPopups) {
        clock += ms
        s.debugAnalyticsFlushTick()
    }

    @Test
    fun foreground_app_sends_its_engagement_every_five_minutes() {
        val s = sdk()
        tickAfter(4 * minute + 30_000, s)
        assertTrue(engagementEvents().isEmpty(), "no más de un latido cada 5 min")

        tickAfter(1 * minute, s)
        assertEquals(1, engagementEvents().size)
        assertEquals(
            5 * minute + 30_000,
            engagementEvents().single().params?.get("engagement_time_msec")?.jsonPrimitive?.longOrNull,
        )

        tickAfter(30_000, s)
        assertEquals(1, engagementEvents().size, "el siguiente latido espera otros 5 min")
        tickAfter(5 * minute, s)
        assertEquals(2, engagementEvents().size)
        assertTrue(flushes.none { (_, meta) -> meta.sessionEnd }, "el latido no cierra la sesión")
    }

    @Test
    fun no_heartbeat_after_the_app_goes_to_background() {
        val s = sdk()
        tickAfter(30_000, s)
        s.onBackground()
        val afterClose = engagementEvents().size // el engagement del cierre

        tickAfter(20 * minute, s)
        tickAfter(20 * minute, s)
        assertEquals(afterClose, engagementEvents().size)
    }

    @Test
    fun back_in_foreground_the_new_session_heartbeats_from_its_own_start() {
        val s = sdk()
        s.onBackground()
        tickAfter(30 * minute, s)
        s.onForeground()
        val before = engagementEvents().size

        tickAfter(4 * minute, s)
        assertEquals(before, engagementEvents().size, "la sesión nueva no hereda el reloj del latido")
        tickAfter(1 * minute + 30_000, s)
        assertEquals(before + 1, engagementEvents().size)
    }
}
