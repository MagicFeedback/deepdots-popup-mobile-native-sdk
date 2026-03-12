package com.deepdots.sdk.storage

/** Persistencia sencilla para cooldowns y timestamps multiplataforma */
interface KeyValueStorage {
    fun getLong(key: String): Long?
    fun putLong(key: String, value: Long)
    fun getString(key: String): String?
    fun putString(key: String, value: String)
    fun remove(key: String)
}

/** Implementación en memoria (fallback si la plataforma no provee storage real) */
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
