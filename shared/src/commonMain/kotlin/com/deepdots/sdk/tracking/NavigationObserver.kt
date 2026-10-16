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
    /** Pantalla normalizada (IDs → `:id`): agrupa las visitas a la misma pantalla. */
    val screen: String,
    /**
     * La ruta real, sin query: `/orders/42` donde `screen` dice `/orders/:id`. Es lo que permite
     * enlazar la visita al elemento concreto.
     */
    val path: String,
    val entry: Long,
    val exit: Long,
    val durationSeconds: Long,
)

/**
 * La pantalla en la que está el usuario AHORA, aún sin `page_view` (sale al abandonarla).
 * Mismos `screen`/`path` que llevará su visita y `seconds` medidos igual que su
 * `durationSeconds` (tiempo de reloj desde que se entró), pero hasta este momento.
 */
data class CurrentScreen(
    val screen: String,
    val path: String,
    val seconds: Long,
)

private val UUID_PREFIX = Regex("^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-")
private val NUMERIC_SEGMENT = Regex("^[0-9]+$")

/** La ruta de una URL o path: path (+hash route), sin query. Sin normalizar los IDs. */
fun screenPath(href: String): String {
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

    return (path + hash).ifEmpty { "/" }
}

/** Normaliza una ruta a un nombre de pantalla: path (+hash route), sin query, IDs → :id. */
fun normalizeScreen(href: String): String {
    val collapsed = screenPath(href)
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
    private var currentPath: String? = null
    private var entryAt: Long = 0
    /**
     * Última ruta recibida (`begin`/`visit`), aunque ya no haya pantalla en curso. Es la que
     * retoma `restart()`: en Web se lee `window.location`; aquí la navegación solo entra por
     * `setPath()`, así que la ruta actual es la última que dio el host.
     */
    private var lastHref: String? = null

    fun onVisit(listener: (ScreenVisit) -> Unit) {
        listeners += listener
    }

    /** Fija la pantalla inicial (sin emitir visita; su duración se cierra al salir). */
    fun begin(href: String) {
        lastHref = href
        currentScreen = normalizeScreen(href)
        currentPath = screenPath(href)
        entryAt = now()
    }

    /** Navega a una pantalla nueva: cierra la anterior (emite visita) y abre la nueva. */
    fun visit(href: String) {
        lastHref = href
        val nextPath = screenPath(href)
        // Misma ruta (solo cambia la query) → no es una visita nueva. Se compara la ruta y no
        // la pantalla normalizada: pasar de un pedido a otro (`/orders/1` → `/orders/2`) sí es
        // otra visita, aunque las dos sean `/orders/:id`.
        if (currentScreen != null && nextPath == currentPath) return
        closeCurrent()
        currentScreen = normalizeScreen(href)
        currentPath = nextPath
        entryAt = now()
    }

    /** Cierra la pantalla actual emitiendo su visita (app a background / fin de sesión). */
    fun stop() {
        closeCurrent()
        currentScreen = null
        currentPath = null
    }

    /**
     * Retoma el seguimiento en la pantalla actual tras un `stop()`, cuando se abre una sesión
     * nueva sin navegar (vuelta del background, cambio de usuario, tracking reactivado), con la
     * entrada en el momento de la reapertura. Sin esto, la pantalla a la que vuelve el usuario
     * no tenía `page_view` hasta que el host llamase a `setPath()` con otra ruta, y entonces la
     * nueva pantalla se tomaba como la primera. No-op si nunca se recibió una ruta o si ya hay
     * pantalla en curso. Espejo de `restart()` en Web.
     */
    fun restart() {
        val href = lastHref ?: return
        if (currentScreen != null) return
        begin(href)
    }

    /**
     * La pantalla en curso, sin cerrarla ni emitir nada; null si no hay (antes de `begin`, tras
     * `stop`). Para que el `user_engagement` lleve la pantalla: si el cierre de la sesión no
     * llega nunca (app matada en foreground, o congelada antes de que salga el lote de cierre),
     * la última pantalla no tiene `page_view` y solo queda esto.
     */
    fun current(): CurrentScreen? {
        val screen = currentScreen ?: return null
        return CurrentScreen(
            screen = screen,
            path = currentPath ?: screen,
            seconds = secondsSinceEntry(now()),
        )
    }

    /** Segundos desde la entrada, redondeados (`Math.round` en Web). */
    private fun secondsSinceEntry(at: Long): Long = maxOf(0L, (at - entryAt + 500) / 1000)

    private fun closeCurrent() {
        val screen = currentScreen ?: return
        val exit = now()
        val visit = ScreenVisit(
            screen = screen,
            path = currentPath ?: screen,
            entry = entryAt,
            exit = exit,
            durationSeconds = secondsSinceEntry(exit),
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
