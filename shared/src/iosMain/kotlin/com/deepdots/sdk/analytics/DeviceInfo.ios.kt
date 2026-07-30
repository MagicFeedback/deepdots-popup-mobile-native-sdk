package com.deepdots.sdk.analytics

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.useContents
import platform.Foundation.NSBundle
import platform.Foundation.NSTimeZone
import platform.Foundation.localTimeZone
import platform.UIKit.UIDevice
import platform.UIKit.UIScreen
import platform.UIKit.UIUserInterfaceIdiomPad

/**
 * Device info en iOS. `device_model` usa `UIDevice.model` (genérico: "iPhone"); el
 * identificador preciso (utsname, p. ej. "iPhone15,3") queda como refinamiento futuro.
 */
@OptIn(ExperimentalForeignApi::class)
actual fun collectDeviceInfo(): DeviceInfo {
    val device = UIDevice.currentDevice
    val scale = runCatching { UIScreen.mainScreen.scale }.getOrNull() ?: 1.0

    val viewport = runCatching {
        UIScreen.mainScreen.bounds.useContents {
            "${size.width.toInt()}x${size.height.toInt()}"
        }
    }.getOrNull()

    val resolution = runCatching {
        UIScreen.mainScreen.bounds.useContents {
            "${(size.width * scale).toInt()}x${(size.height * scale).toInt()}"
        }
    }.getOrNull()

    return DeviceInfo(
        deviceType = if (device.userInterfaceIdiom == UIUserInterfaceIdiomPad) "tablet" else "mobile",
        osVersion = runCatching { "iOS ${device.systemVersion}" }.getOrNull(),
        deviceModel = runCatching { device.model }.getOrNull(),
        appVersion = runCatching {
            NSBundle.mainBundle.objectForInfoDictionaryKey("CFBundleShortVersionString") as? String
        }.getOrNull(),
        timezone = runCatching { NSTimeZone.localTimeZone.name }.getOrNull(),
        screenResolution = resolution,
        viewportSize = viewport,
        pixelRatio = runCatching { scale.toString() }.getOrNull(),
    )
}
