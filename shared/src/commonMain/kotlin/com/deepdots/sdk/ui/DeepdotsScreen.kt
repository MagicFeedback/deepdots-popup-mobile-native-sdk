package com.deepdots.sdk.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.deepdots.sdk.DeepdotsPopups

/**
 * Helper opcional para apps de Compose (Android o Compose Multiplatform en iOS), donde la
 * Activity / el view controller no dicen en qué pantalla está el usuario. Ponlo al principio de
 * cada destino:
 *
 * ```
 * composable("checkout") { TrackDeepdotsScreen(sdk, "checkout"); CheckoutScreen() }
 * ```
 *
 * Cada vez que el destino entra en composición equivale a `sdk.setPath("/checkout")`: cuenta como
 * llamada manual, así que desde la primera la detección por Activity se apaga en esa instancia.
 */
@Composable
fun TrackDeepdotsScreen(sdk: DeepdotsPopups, name: String) {
    LaunchedEffect(sdk, name) {
        sdk.setPath(if (name.startsWith("/")) name else "/$name")
    }
}
