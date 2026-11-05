package com.deepdots.sdk.ui

import platform.Foundation.NSLog

internal actual fun logSurveySdk(line: String) {
    // Mensaje como único argumento, como el resto del SDK: pasar un String de Kotlin a la parte
    // variádica de NSLog ("%@", line) tumba la app (SIGSEGV). '%' se escapa por ser el formato.
    NSLog(line.replace("%", "%%"))
}
