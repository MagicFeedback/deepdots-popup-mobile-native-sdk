package com.deepdots.sdk.ui

import android.graphics.Typeface as AndroidTypeface
import androidx.compose.ui.text.font.FontFamily
import java.io.File

// Compose en Android necesita un Typeface; Typeface.createFromFile requiere un fichero.
// File.createTempFile usa java.io.tmpdir, así que no hace falta un Context.
actual fun fontFamilyFromBytes(family: String, bytes: ByteArray): FontFamily? = try {
    val file = File.createTempFile("deepdots-font-", ".ttf")
    file.deleteOnExit()
    file.writeBytes(bytes)
    val tf = AndroidTypeface.createFromFile(file)
    FontFamily(tf)
} catch (t: Throwable) {
    null
}
