package com.deepdots.sdk.storage

import platform.Foundation.NSUserDefaults

private const val KEY_PREFIX = "deepdots_sdk."

/** `KeyValueStorage` sobre `NSUserDefaults` (persistente entre sesiones). */
private class NSUserDefaultsStorage(
    private val defaults: NSUserDefaults = NSUserDefaults.standardUserDefaults,
) : KeyValueStorage {
    private fun k(key: String) = KEY_PREFIX + key

    // Se guarda como string para que ida y vuelta sean simétricas en NSUserDefaults.
    override fun getLong(key: String): Long? = defaults.stringForKey(k(key))?.toLongOrNull()

    override fun putLong(key: String, value: Long) {
        defaults.setObject(value.toString(), k(key))
    }

    override fun getString(key: String): String? = defaults.stringForKey(k(key))

    override fun putString(key: String, value: String) {
        defaults.setObject(value, k(key))
    }

    override fun remove(key: String) {
        defaults.removeObjectForKey(k(key))
    }
}

actual fun createDefaultStorage(): KeyValueStorage =
    runCatching { NSUserDefaultsStorage() as KeyValueStorage }.getOrElse { InMemoryStorage() }
