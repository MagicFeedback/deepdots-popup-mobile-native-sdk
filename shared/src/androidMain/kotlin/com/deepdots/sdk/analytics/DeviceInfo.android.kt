package com.deepdots.sdk.analytics

import android.os.Build
import com.deepdots.sdk.platform.AppContextHolder
import java.util.TimeZone

/**
 * Device info en Android. Todo es best-effort: en unit tests (JVM, sin runtime de Android
 * ni Context) cada campo cae a null en vez de lanzar.
 */
actual fun collectDeviceInfo(): DeviceInfo {
    val context = AppContextHolder.applicationContext

    val deviceType = runCatching {
        val res = context?.resources ?: return@runCatching null
        if (res.configuration.smallestScreenWidthDp >= 600) "tablet" else "mobile"
    }.getOrNull() ?: "mobile"

    val appVersion = runCatching {
        val ctx = context ?: return@runCatching null
        @Suppress("DEPRECATION")
        ctx.packageManager.getPackageInfo(ctx.packageName, 0).versionName
    }.getOrNull()

    val screenResolution = runCatching {
        val dm = context?.resources?.displayMetrics ?: return@runCatching null
        "${dm.widthPixels}x${dm.heightPixels}"
    }.getOrNull()

    val pixelRatio = runCatching {
        context?.resources?.displayMetrics?.density?.toString()
    }.getOrNull()

    return DeviceInfo(
        deviceType = deviceType,
        osVersion = runCatching { "Android ${Build.VERSION.RELEASE}" }.getOrNull(),
        deviceModel = runCatching { Build.MODEL }.getOrNull(),
        appVersion = appVersion,
        timezone = runCatching { TimeZone.getDefault().id }.getOrNull(),
        screenResolution = screenResolution,
        viewportSize = screenResolution,
        pixelRatio = pixelRatio,
    )
}
