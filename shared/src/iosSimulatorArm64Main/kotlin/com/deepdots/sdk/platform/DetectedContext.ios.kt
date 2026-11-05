package com.deepdots.sdk.platform

internal actual fun detectedPlatformContext(): PlatformContext? =
    detectedRootViewController()?.let { PlatformContext(it) }
