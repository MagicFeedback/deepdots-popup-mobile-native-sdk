package com.deepdots.sdk.ui

import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.platform.Font

// En targets skiko (iOS) Compose puede construir una Font directamente desde bytes.
actual fun fontFamilyFromBytes(family: String, bytes: ByteArray): FontFamily? = try {
    FontFamily(
        Font(
            identity = family,
            data = bytes,
            weight = FontWeight.Normal,
            style = FontStyle.Normal,
        )
    )
} catch (t: Throwable) {
    null
}
