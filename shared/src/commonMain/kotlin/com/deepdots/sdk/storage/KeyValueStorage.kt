package com.deepdots.sdk.storage

/** Persistencia sencilla para cooldowns y timestamps multiplataforma */
interface KeyValueStorage {
    fun getLong(key: String): Long?
    fun putLong(key: String, value: Long)
}

/** Implementación en memoria (fallback si la plataforma no provee storage real) */
class InMemoryStorage : KeyValueStorage {
    private val map = mutableMapOf<String, Long>()
    override fun getLong(key: String): Long? = map[key]
    override fun putLong(key: String, value: Long) { map[key] = value }
}

