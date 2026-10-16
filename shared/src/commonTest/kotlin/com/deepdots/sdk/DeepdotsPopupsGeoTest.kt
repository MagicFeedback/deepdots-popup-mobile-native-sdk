package com.deepdots.sdk

import com.deepdots.sdk.analytics.AnalyticsKeys
import com.deepdots.sdk.analytics.GEO_TTL_MS
import com.deepdots.sdk.analytics.GeoInfo
import com.deepdots.sdk.analytics.writeCachedGeo
import com.deepdots.sdk.models.InitOptions
import com.deepdots.sdk.models.PopupOptions
import com.deepdots.sdk.storage.InMemoryStorage
import com.deepdots.sdk.util.currentTimeMillis
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/**
 * Cuándo se hace el lookup de geolocalización por IP (terceros: ipapi.co, ipwho.is, ipinfo.io).
 * Paridad con Web (src/core/deepdots-popups.geo.test.ts). Reportado por un cliente: se hacía en
 * cada init(), sin analytics y con el tracking apagado. Regla: solo si el país/ciudad se va a
 * ENVIAR (analytics configurado + tracking activo + `geolocation` no desactivado) y la caché no
 * está fresca; como mucho una vez por instancia.
 *
 * El lookup real se sustituye por `debugGeoLookup` (sin red); `debugGeoLookupCount` cuenta los
 * lookups arrancados de forma síncrona, así que los tests de "no se hace" no dependen de esperas.
 */
class DeepdotsPopupsGeoTest {

    private val analyticsKeys = AnalyticsKeys(publicKey = "pk", integration = "int")

    private fun sdk(
        storage: InMemoryStorage = InMemoryStorage(),
        analytics: AnalyticsKeys? = analyticsKeys,
        trackingEnabled: Boolean = true,
        geolocation: Boolean? = true,
    ): DeepdotsPopups = DeepdotsPopups().apply {
        debugGeoLookup = { GeoInfo(country = "DK", city = "Aarhus") }
        init(
            InitOptions(
                popupOptions = PopupOptions(publicKey = "pk-1"),
                storage = storage,
                trackingEnabled = trackingEnabled,
                analytics = analytics,
                geolocation = geolocation,
            ),
        )
    }

    private fun DeepdotsPopups.device() = previewAnalytics().context.device

    /** El resultado se aplica desde una corrutina: espera a que llegue (con techo). */
    private fun DeepdotsPopups.awaitCountry(): String? = runBlocking {
        repeat(100) {
            device()?.country?.let { return@runBlocking it }
            delay(10)
        }
        device()?.country
    }

    @Test
    fun without_analytics_the_lookup_is_not_made() {
        val s = sdk(analytics = null)
        assertEquals(0, s.debugGeoLookupCount)
    }

    @Test
    fun with_analytics_and_tracking_on_the_lookup_fills_country_and_city() {
        val s = sdk()
        assertEquals(1, s.debugGeoLookupCount)
        assertEquals("DK", s.awaitCountry())
        assertEquals("Aarhus", s.device()?.city)
    }

    @Test
    fun with_tracking_disabled_the_lookup_waits_for_consent() {
        val s = sdk(trackingEnabled = false)
        assertEquals(0, s.debugGeoLookupCount)

        s.setTrackingEnabled(true)
        assertEquals(1, s.debugGeoLookupCount)
    }

    @Test
    fun at_most_one_lookup_per_instance_even_if_consent_toggles() {
        val s = sdk()
        s.setTrackingEnabled(false)
        s.setTrackingEnabled(true)
        s.setTrackingEnabled(true)
        assertEquals(1, s.debugGeoLookupCount)
    }

    @Test
    fun fresh_cache_skips_the_lookup_and_is_used() {
        val storage = InMemoryStorage()
        writeCachedGeo(storage, GeoInfo(country = "ES", city = "Madrid"), currentTimeMillis())
        val s = sdk(storage = storage)
        assertEquals(0, s.debugGeoLookupCount)
        assertEquals("ES", s.device()?.country)
        assertEquals("Madrid", s.device()?.city)
    }

    @Test
    fun expired_cache_triggers_a_new_lookup() {
        val storage = InMemoryStorage()
        writeCachedGeo(storage, GeoInfo(country = "ES", city = "Madrid"), currentTimeMillis() - GEO_TTL_MS - 1)
        val s = sdk(storage = storage)
        assertEquals(1, s.debugGeoLookupCount)
        assertEquals("DK", s.awaitCountry())
    }

    @Test
    fun geolocation_false_disables_lookup_and_cache() {
        val storage = InMemoryStorage()
        writeCachedGeo(storage, GeoInfo(country = "ES", city = "Madrid"), currentTimeMillis() - GEO_TTL_MS - 1)
        val s = sdk(storage = storage, geolocation = false)
        s.setTrackingEnabled(true)
        assertEquals(0, s.debugGeoLookupCount)
        assertNull(s.device()?.country)
        assertNull(s.device()?.city)
    }

    @Test
    fun geolocation_false_ignores_a_fresh_cache_from_a_previous_version() {
        val storage = InMemoryStorage()
        writeCachedGeo(storage, GeoInfo(country = "ES", city = "Madrid"), currentTimeMillis())
        val s = sdk(storage = storage, geolocation = false)
        assertNull(s.device()?.country)
    }
}
