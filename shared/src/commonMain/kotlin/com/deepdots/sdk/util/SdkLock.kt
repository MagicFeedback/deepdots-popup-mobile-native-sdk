package com.deepdots.sdk.util

/**
 * Exclusión mutua mínima. NO tiene equivalente en el SDK Web porque JS es single-thread;
 * en KMP el buffer de analytics se toca desde el hilo del host (`track`) y desde la corrutina
 * de envío (re-encolado de un lote que falló), así que necesita protección real.
 */
expect class SdkLock() {
    fun <T> withLock(block: () -> T): T
}
