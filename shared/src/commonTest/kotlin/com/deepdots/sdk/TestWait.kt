package com.deepdots.sdk

import com.deepdots.sdk.util.currentTimeMillis
import kotlinx.coroutines.delay

/**
 * Espera a que se cumpla [condition], con un techo de [timeoutMs]. Para los tests de exits con
 * retraso: esperar un tiempo fijo (p. ej. 80 ms para un retraso de 50 ms) fallaba en el runner de
 * GitHub, mas lento que un portatil, porque la corrutina aun no habia corrido.
 */
internal suspend fun waitUntil(timeoutMs: Long = 2_000, condition: () -> Boolean): Boolean {
    val deadline = currentTimeMillis() + timeoutMs
    while (!condition()) {
        if (currentTimeMillis() >= deadline) return false
        delay(10)
    }
    return true
}
