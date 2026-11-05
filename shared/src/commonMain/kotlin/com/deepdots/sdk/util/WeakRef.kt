package com.deepdots.sdk.util

/** Referencia débil: el registro de [com.deepdots.sdk.tracking.AutoSignals] no retiene instancias. */
internal expect class WeakRef<T : Any>(value: T) {
    fun get(): T?
}
