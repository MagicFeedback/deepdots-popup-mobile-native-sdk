package com.deepdots.sdk.tracking

/**
 * Nombre de pantalla a partir del nombre de clase de un view controller de iOS: sin el módulo
 * de Swift (`MyApp.HomeViewController` → `HomeViewController`), y null para lo que no es una
 * pantalla del host: clases de UIKit/SwiftUI (`UI…`, `_…`, genéricos de Swift `_Tt…`), los
 * `UIHostingController` y el host de Compose Multiplatform.
 */
internal fun screenNameForClass(className: String): String? {
    if (className.isBlank()) return null
    if (className.startsWith("_") || className.startsWith("UI") || className.startsWith("SwiftUI")) return null
    if (className.contains("HostingController") || className.contains("ComposeHostingViewController")) return null
    if (className.startsWith("androidx_compose") || className.startsWith("Compose")) return null
    val simple = className.substringAfterLast('.')
    if (simple.isBlank() || simple.startsWith("UI") || simple.startsWith("_")) return null
    return simple
}
