package com.deepdots.sdk.platform

import android.content.ContentProvider
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.net.Uri

/**
 * Application context capturado automáticamente, sin pedirle nada al host.
 *
 * Lo puebla [DeepdotsInitProvider], un `ContentProvider` declarado en el manifest de la
 * librería: Android lo instancia antes de `Application.onCreate`, así que el context está
 * disponible desde el primer `init()`. Es el patrón que usan Firebase y WorkManager.
 */
object AppContextHolder {
    @Volatile
    var applicationContext: Context? = null
        internal set
}

/** Provider vacío: su único trabajo es capturar el application context al arrancar el proceso. */
class DeepdotsInitProvider : ContentProvider() {
    override fun onCreate(): Boolean {
        AppContextHolder.applicationContext = context?.applicationContext
        return true
    }

    override fun query(
        uri: Uri,
        projection: Array<out String>?,
        selection: String?,
        selectionArgs: Array<out String>?,
        sortOrder: String?,
    ): Cursor? = null

    override fun getType(uri: Uri): String? = null

    override fun insert(uri: Uri, values: ContentValues?): Uri? = null

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?): Int = 0

    override fun update(
        uri: Uri,
        values: ContentValues?,
        selection: String?,
        selectionArgs: Array<out String>?,
    ): Int = 0
}
