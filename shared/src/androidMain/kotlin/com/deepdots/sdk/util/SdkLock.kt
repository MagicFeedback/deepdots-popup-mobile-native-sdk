package com.deepdots.sdk.util

import java.util.concurrent.locks.ReentrantLock

actual class SdkLock actual constructor() {
    private val lock = ReentrantLock()

    actual fun <T> withLock(block: () -> T): T {
        lock.lock()
        try {
            return block()
        } finally {
            lock.unlock()
        }
    }
}
