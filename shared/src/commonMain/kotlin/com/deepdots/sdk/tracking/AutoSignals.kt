package com.deepdots.sdk.tracking

import com.deepdots.sdk.util.SdkLock
import com.deepdots.sdk.util.WeakRef

/**
 * Lo que una instancia del SDK recibe de la detección automática de la plataforma. Cada
 * instancia decide con su `InitOptions.autoTrack` y con lo que el host ya haya llamado a mano.
 */
internal interface AutoTrackTarget {
    /** App a foreground (Android `ON_START` del proceso, iOS `willEnterForeground`). */
    fun autoForeground()
    /** App a background (Android `ON_STOP` del proceso, iOS `didEnterBackground`): fin de sesión. */
    fun autoBackground()
    /** iOS `willResignActive`: estado transitorio, solo se vacía el buffer. */
    fun autoInactive()
    /** Pantalla detectada (nombre de la Activity / Fragment / view controller). */
    fun autoScreen(name: String)
    /** Hay contexto para pintar (Activity resumed / app activa): reintenta la cola de popups. */
    fun autoContextAvailable()
}

/**
 * Punto único entre la plataforma y las instancias. Las piezas nativas
 * (`installPlatformAutoTracking`) se instalan una vez por proceso y emiten aquí; cada instancia
 * se registra en su `init()`. Referencias débiles: una instancia que el host suelta no se retiene.
 *
 * Guarda la última pantalla detectada: una instancia creada con la app ya en pantalla (init tardío)
 * la recibe al registrarse.
 */
internal object AutoSignals {
    private val lock = SdkLock()
    private val targets = mutableListOf<WeakRef<AutoTrackTarget>>()
    private var lastScreen: String? = null

    fun register(target: AutoTrackTarget) {
        val screen = lock.withLock {
            targets.removeAll { it.get() == null }
            targets += WeakRef(target)
            lastScreen
        }
        if (screen != null) safely { target.autoScreen(screen) }
    }

    fun foreground() = dispatch { it.autoForeground() }
    fun background() = dispatch { it.autoBackground() }
    fun inactive() = dispatch { it.autoInactive() }
    fun contextAvailable() = dispatch { it.autoContextAvailable() }

    fun screen(name: String) {
        if (name.isBlank()) return
        lock.withLock { lastScreen = name }
        dispatch { it.autoScreen(name) }
    }

    private fun dispatch(block: (AutoTrackTarget) -> Unit) {
        val live = lock.withLock {
            targets.removeAll { it.get() == null }
            targets.mapNotNull { it.get() }
        }
        for (t in live) safely { block(t) }
    }

    /** La detección nunca puede tumbar la app del host. */
    private inline fun safely(block: () -> Unit) {
        try {
            block()
        } catch (_: Throwable) {
        }
    }

    internal fun debugReset() = lock.withLock {
        targets.clear()
        lastScreen = null
    }
}

/**
 * Contexto en el que se pinta el popup: el que el host fijó con `attachContext()` si lo hizo
 * (lo manual manda); si no, el detectado (Activity resumed / key window) y, a falta de él, el
 * último usado.
 */
internal fun <T> chooseContext(manualLocked: Boolean, cached: T?, detected: T?): T? =
    if (manualLocked) cached else detected ?: cached
