package com.deepdots.sdk.storage

/** Lightweight cross-platform key/value store used internally for cooldown caching. */
interface KeyValueStorage {
    fun getLong(key: String): Long?
    fun putLong(key: String, value: Long)
    fun getString(key: String): String?
    fun putString(key: String, value: String)
    fun remove(key: String)
}

/** In-memory implementation used as the default within-session cache. */
class InMemoryStorage : KeyValueStorage {
    private val longMap = mutableMapOf<String, Long>()
    private val stringMap = mutableMapOf<String, String>()

    override fun getLong(key: String): Long? = longMap[key]

    override fun putLong(key: String, value: Long) {
        longMap[key] = value
    }

    override fun getString(key: String): String? = stringMap[key]

    override fun putString(key: String, value: String) {
        stringMap[key] = value
    }

    override fun remove(key: String) {
        longMap.remove(key)
        stringMap.remove(key)
    }
}
