package com.deepdots.sdk.tracking

import com.deepdots.sdk.util.currentTimeMillis

/**
 * Observador de navegación (Fase 2). Espejo de `src/tracking/navigation-observer.ts` (Web).
 *
 * Detecta cambios de pantalla y emite "visitas" completadas (screen + entrada/salida/duración)
 * al abandonar una pantalla. En Web se engancha a la History API; en KMP la navegación entra
 * SIEMPRE manualmente por `DeepdotsPopups.setPath()` (el host la llama en cada pantalla), así
 * que aquí solo vive la lógica pura de timing y normalización, con reloj inyectable.
 */

data class ScreenVisit(
    val screen: String,
    val entry: Long,
    val exit: Long,
    val durationSeconds: Long,
)

private val UUID_PREFIX = Regex("^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-")
private val NUMERIC_SEGMENT = Regex("^[0-9]+$")

/** Normaliza una ruta a un nombre de pantalla: path (+hash route), sin query, IDs → :id. */
fun normalizeScreen(href: String): String {
    // Si llega una URL absoluta, quedarse con lo que hay a partir del host.
    var rest = href
    val schemeIdx = rest.indexOf("://")
    if (schemeIdx >= 0) {
        val afterScheme = rest.substring(schemeIdx + 3)
        val slash = afterScheme.indexOf('/')
        rest = if (slash >= 0) afterScheme.substring(slash) else "/"
    }

    val hashIdx = rest.indexOf('#')
    var path = if (hashIdx >= 0) rest.substring(0, hashIdx) else rest
    var hash = if (hashIdx >= 0) rest.substring(hashIdx) else ""

    // Quitar query, tanto del path como de dentro del hash route.
    path = path.substringBefore('?')
    hash = hash.substringBefore('?')
    if (path.isEmpty()) path = "/"
    if (!path.startsWith("/")) path = "/$path"

    val collapsed = (path + hash)
        .split("/")
        .joinToString("/") { seg ->
            if (NUMERIC_SEGMENT.matches(seg) || UUID_PREFIX.containsMatchIn(seg)) ":id" else seg
        }
    return collapsed.ifEmpty { "/" }
}

class NavigationObserver(
    private val now: () -> Long = { currentTimeMillis() },
) {
    private val listeners = mutableListOf<(ScreenVisit) -> Unit>()
    private var currentScreen: String? = null
    private var entryAt: Long = 0

    fun onVisit(listener: (ScreenVisit) -> Unit) {
        listeners += listener
    }

    /** Fija la pantalla inicial (sin emitir visita; su duración se cierra al salir). */
    fun begin(href: String) {
        currentScreen = normalizeScreen(href)
        entryAt = now()
    }

    /** Navega a una pantalla nueva: cierra la anterior (emite visita) y abre la nueva. */
    fun visit(href: String) {
        val next = normalizeScreen(href)
        if (next == currentScreen) return // misma pantalla → no es una visita nueva
        closeCurrent()
        currentScreen = next
        entryAt = now()
    }

    /** Cierra la pantalla actual emitiendo su visita (app a background / fin de sesión). */
    fun stop() {
        closeCurrent()
        currentScreen = null
    }

    private fun closeCurrent() {
        val screen = currentScreen ?: return
        val exit = now()
        val visit = ScreenVisit(
            screen = screen,
            entry = entryAt,
            exit = exit,
            durationSeconds = maxOf(0L, (exit - entryAt + 500) / 1000),
        )
        for (l in listeners.toList()) {
            try {
                l(visit)
            } catch (_: Throwable) {
                /* un listener no debe romper la navegación */
            }
        }
    }
}
