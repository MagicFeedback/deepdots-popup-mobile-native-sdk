package com.deepdots.sdk.analytics

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Device info para el context de analytics (Technology #11–13). Espejo de
 * `src/analytics/device-info.ts` (Web), con las mismas claves de wire (snake_case).
 *
 * Divergencia intencionada con Web: aquí NO hay user agent que el backend pueda parsear, así
 * que las APIs nativas rellenan `os_version` y `device_model` de verdad (en Web van vacíos y
 * el backend los deriva del `user_agent`, como hace GA).
 */
@Serializable
data class DeviceInfo(
    @SerialName("device_type") val deviceType: String? = null,
    @SerialName("os_version") val osVersion: String? = null,
    @SerialName("device_model") val deviceModel: String? = null,
    @SerialName("app_version") val appVersion: String? = null,
    @SerialName("user_agent") val userAgent: String? = null,
    @SerialName("timezone") val timezone: String? = null,
    @SerialName("referrer") val referrer: String? = null,
    @SerialName("viewport_size") val viewportSize: String? = null,
    @SerialName("screen_resolution") val screenResolution: String? = null,
    @SerialName("pixel_ratio") val pixelRatio: String? = null,
    @SerialName("entry_type") val entryType: String? = null,
    @SerialName("page_load_ms") val pageLoadMs: Long? = null,
    @SerialName("connection_type") val connectionType: String? = null,
    val country: String? = null,
    val city: String? = null,
)

/** Recoge la info del dispositivo con las APIs nativas de cada plataforma. Nunca lanza. */
expect fun collectDeviceInfo(): DeviceInfo
