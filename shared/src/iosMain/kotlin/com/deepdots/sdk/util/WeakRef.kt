package com.deepdots.sdk.util

import kotlin.experimental.ExperimentalNativeApi

@OptIn(ExperimentalNativeApi::class)
internal actual class WeakRef<T : Any> actual constructor(value: T) {
    private val ref = kotlin.native.ref.WeakReference(value)
    actual fun get(): T? = ref.get()
}
