package com.deepdots.sdk.util

import platform.UIKit.UIApplication
import platform.UIKit.UIBackgroundTaskIdentifier
import platform.UIKit.UIBackgroundTaskInvalid

/**
 * iOS: `beginBackgroundTaskWithName` (se puede llamar desde cualquier hilo). Se termina al
 * acabar el envío o, si iOS agota el tiempo antes, en su expiration handler; lo que llegue
 * primero, y una sola vez.
 */
internal actual fun beginBackgroundTask(name: String): () -> Unit {
    val task = BackgroundTask(UIApplication.sharedApplication)
    task.start(name)
    return task::end
}

private class BackgroundTask(private val app: UIApplication) {
    private val lock = SdkLock()
    private var id: UIBackgroundTaskIdentifier = UIBackgroundTaskInvalid
    private var ended = false

    fun start(name: String) {
        val started = app.beginBackgroundTaskWithName(name) { end() }
        // El expiration handler puede haber llegado antes de guardar el id: terminarla aquí.
        val endNow = lock.withLock {
            if (ended) {
                true
            } else {
                id = started
                false
            }
        }
        if (endNow && started != UIBackgroundTaskInvalid) app.endBackgroundTask(started)
    }

    fun end() {
        val toEnd = lock.withLock {
            if (ended) {
                null
            } else {
                ended = true
                id
            }
        }
        if (toEnd != null && toEnd != UIBackgroundTaskInvalid) app.endBackgroundTask(toEnd)
    }
}
