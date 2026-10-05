package com.deepdots.sdk.storage

import android.content.SharedPreferences
import com.deepdots.sdk.platform.AppContextHolder

private const val PREFS_NAME = "deepdots_sdk"

/** `KeyValueStorage` sobre `SharedPreferences` (persistente entre sesiones). */
private class SharedPreferencesStorage(private val prefs: SharedPreferences) : KeyValueStorage {
    override fun getLong(key: String): Long? =
        if (prefs.contains(key)) prefs.getLong(key, 0L) else null

    override fun putLong(key: String, value: Long) {
        prefs.edit().putLong(key, value).apply()
    }

    override fun getString(key: String): String? = prefs.getString(key, null)

    override fun putString(key: String, value: String) {
        prefs.edit().putString(key, value).apply()
    }

    override fun remove(key: String) {
        prefs.edit().remove(key).apply()
    }
}

actual fun createDefaultStorage(): KeyValueStorage {
    val context = AppContextHolder.applicationContext ?: return InMemoryStorage()
    return runCatching {
        SharedPreferencesStorage(context.getSharedPreferences(PREFS_NAME, android.content.Context.MODE_PRIVATE))
    }.getOrElse { InMemoryStorage() }
}
