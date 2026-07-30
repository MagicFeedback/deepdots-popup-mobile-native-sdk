package com.deepdots.sdk.analytics

import com.deepdots.sdk.storage.KeyValueStorage
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.jsonPrimitive

/**
 * Geolocalización por IP. Espejo de `src/analytics/geo-info.ts` (Web):
 *  - CACHE persistente con TTL (arranques posteriores tienen país/ciudad sin llamada),
 *  - CADENA de proveedores con fallback (bloqueo, 429, caída),
 *  - TIMEOUT por proveedor.
 *
 * Todo es fire-and-forget y tolerante a fallos: ante cualquier error devuelve `null`.
 */

data class GeoInfo(val country: String? = null, val city: String? = null)

/** Clave de cache en storage (misma que Web). */
const val GEO_STORAGE_KEY = "deepdots.geo"

/** El país/ciudad casi no cambian: cache de 30 días. */
const val GEO_TTL_MS = 30L * 24 * 60 * 60 * 1000

/** Timeout por proveedor. */
const val GEO_TIMEOUT_MS = 3_000L

private val geoJson = Json { ignoreUnknownKeys = true; isLenient = true }

/** Lee el geo cacheado si sigue fresco (no supera el TTL). Tolerante a JSON corrupto. */
fun readCachedGeo(storage: KeyValueStorage, now: Long): GeoInfo? = runCatching {
    val raw = storage.getString(GEO_STORAGE_KEY) ?: return null
    val obj = geoJson.parseToJsonElement(raw) as? JsonObject ?: return null
    val ts = obj["ts"]?.jsonPrimitive?.content?.toLongOrNull() ?: return null
    if (now - ts > GEO_TTL_MS) return null
    val country = obj["country"]?.jsonPrimitive?.contentOrNullSafe()
    val city = obj["city"]?.jsonPrimitive?.contentOrNullSafe()
    if (country.isNullOrBlank() && city.isNullOrBlank()) null else GeoInfo(country, city)
}.getOrNull()

/** Persiste el geo con la marca de tiempo actual. Tolerante a fallos de storage. */
fun writeCachedGeo(storage: KeyValueStorage, geo: GeoInfo, now: Long) {
    runCatching {
        val payload = buildString {
            append("{")
            geo.country?.let { append("\"country\":\"").append(it.replace("\"", "")).append("\",") }
            geo.city?.let { append("\"city\":\"").append(it.replace("\"", "")).append("\",") }
            append("\"ts\":").append(now)
            append("}")
        }
        storage.putString(GEO_STORAGE_KEY, payload)
    }
}

/** Proveedor de geo: URL + cómo sacar country/city de su JSON. */
data class GeoProvider(val url: String, val parse: (JsonObject) -> GeoInfo?)

/** Proveedores en orden de preferencia (mismos que Web). Todos devuelven JSON sin auth. */
val DEFAULT_GEO_PROVIDERS: List<GeoProvider> = listOf(
    GeoProvider("https://ipapi.co/json/") { geoFrom(it, "country_code", "city") },
    GeoProvider("https://ipwho.is/") { obj ->
        // devuelve success:false con 200 en errores
        if (obj["success"]?.jsonPrimitive?.booleanOrNull == false) null else geoFrom(obj, "country_code", "city")
    },
    GeoProvider("https://ipinfo.io/json") { geoFrom(it, "country", "city") },
)

private fun geoFrom(obj: JsonObject, countryKey: String, cityKey: String): GeoInfo? {
    val country = obj[countryKey]?.jsonPrimitive?.contentOrNullSafe()
    val city = obj[cityKey]?.jsonPrimitive?.contentOrNullSafe()
    if (country.isNullOrBlank() && city.isNullOrBlank()) return null
    return GeoInfo(country = country?.ifBlank { null }, city = city?.ifBlank { null })
}

private fun kotlinx.serialization.json.JsonPrimitive.contentOrNullSafe(): String? =
    runCatching { if (this.content == "null") null else this.content }.getOrNull()

/**
 * Intenta cada proveedor en orden (con timeout) y devuelve el primer geo válido.
 * `null` si ninguno responde. No lanza.
 */
suspend fun collectGeoInfo(
    client: HttpClient = HttpClient(),
    providers: List<GeoProvider> = DEFAULT_GEO_PROVIDERS,
    timeoutMs: Long = GEO_TIMEOUT_MS,
): GeoInfo? {
    for (provider in providers) {
        val geo = withTimeoutOrNull(timeoutMs) {
            runCatching {
                val body = client.get(provider.url).bodyAsText()
                val obj = geoJson.parseToJsonElement(body) as? JsonObject ?: return@runCatching null
                provider.parse(obj)
            }.getOrNull()
        }
        if (geo != null) return geo
    }
    return null
}
