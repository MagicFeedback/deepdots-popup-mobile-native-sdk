package com.deepdots.sdk.util

internal actual class WeakRef<T : Any> actual constructor(value: T) {
    private val ref = java.lang.ref.WeakReference(value)
    actual fun get(): T? = ref.get()
}
