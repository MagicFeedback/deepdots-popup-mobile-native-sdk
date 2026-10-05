package com.deepdots.sdk.analytics

import com.deepdots.sdk.storage.InMemoryStorage
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/**
 * Paridad con la caché de geo del Web (src/analytics/geo-info.test.ts): el país/ciudad se
 * reutilizan entre arranques (sin llamada, sin gap de timing) y caducan con el TTL.
 */
class GeoInfoParityTest {

    private val now = 1_700_000_000_000

    @Test
    fun round_trips_the_cached_geo() {
        val storage = InMemoryStorage()
        writeCachedGeo(storage, GeoInfo(country = "ES", city = "Barcelona"), now)

        val geo = readCachedGeo(storage, now + 1_000)
        assertEquals("ES", geo?.country)
        assertEquals("Barcelona", geo?.city)
    }

    @Test
    fun ignores_the_cache_once_the_ttl_expires() {
        val storage = InMemoryStorage()
        writeCachedGeo(storage, GeoInfo(country = "ES"), now)

        assertNull(readCachedGeo(storage, now + GEO_TTL_MS + 1))
    }

    @Test
    fun tolerates_a_missing_or_corrupt_cache() {
        val storage = InMemoryStorage()
        assertNull(readCachedGeo(storage, now))

        storage.putString(GEO_STORAGE_KEY, "{not json")
        assertNull(readCachedGeo(storage, now))

        storage.putString(GEO_STORAGE_KEY, "{\"country\":\"ES\"}") // sin ts
        assertNull(readCachedGeo(storage, now))
    }

    @Test
    fun keeps_only_the_fields_that_came_back() {
        val storage = InMemoryStorage()
        writeCachedGeo(storage, GeoInfo(country = "ES"), now)

        val geo = readCachedGeo(storage, now)
        assertEquals("ES", geo?.country)
        assertNull(geo?.city)
    }
}
