package com.deepdots.reference

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** Rutas que la app pasa a `setPath()` y nombres de trigger que usa. */
object Catalog {
    /**
     * Las fichas usan `/detail/{id}` porque son las rutas que segmentan los popups de la
     * integración de pruebas en api-dev:
     *  - `/detail/1` → time on page (2 s)
     *  - `/detail/2` → scroll (50 %)
     *  - `/detail/3` → exit (al salir de la ficha)
     *  - evento `custom-event` → cualquier ruta
     */
    data class Product(val id: Int, val name: String, val price: String, val note: String)

    val products = listOf(
        Product(1, "Cordless drill 18V", "1.299 kr", "Popup by time on page (2 s)"),
        Product(2, "Wood screws 4x40 (200)", "89 kr", "Popup by scroll (50 %)"),
        Product(3, "Safety gloves", "129 kr", "Popup on exit (when you leave)"),
        Product(4, "Spirit level 60 cm", "249 kr", "No popup configured"),
        Product(5, "Measuring tape 5 m", "119 kr", "No popup configured"),
        Product(6, "Work light LED", "399 kr", "No popup configured"),
    )

    fun product(id: Int) = products.firstOrNull { it.id == id } ?: products.first()
    fun productPath(id: Int) = "/detail/$id"

    const val HOME = "/home"
    const val SEARCH = "/search"
    const val ACCOUNT = "/account"
    const val CHECKOUT = "/checkout"
    const val DIAGNOSTICS = "/diagnostics"
    const val TEST_CASES = "/test-cases"

    const val CUSTOM_EVENT = "custom-event"
    const val ADD_TO_CART_CLICK = "add-to-cart"
}

/**
 * Registro de lo que pasa en la app y en el SDK, visible en la pantalla de diagnóstico y en
 * logcat (`adb logcat -s DeepdotsRef`). Los listeners del SDK pueden llegar desde un hilo de
 * fondo: el registro es thread-safe y avisa a la UI en el hilo principal.
 */
object HostLog {
    const val TAG = "DeepdotsRef"
    private val lines = ArrayDeque<String>()
    private val listeners = mutableSetOf<() -> Unit>()
    private val main = Handler(Looper.getMainLooper())
    private val clock = SimpleDateFormat("HH:mm:ss.SSS", Locale.US)

    fun add(message: String) {
        Log.i(TAG, message)
        synchronized(lines) {
            lines.addFirst("${clock.format(Date())}  $message")
            while (lines.size > 200) lines.removeLast()
        }
        main.post { listeners.toList().forEach { it() } }
    }

    fun snapshot(): List<String> = synchronized(lines) { lines.toList() }

    fun listen(listener: () -> Unit) { listeners += listener }
    fun unlisten(listener: () -> Unit) { listeners -= listener }
}

/**
 * Sonda de respuesta: un contador que cualquier pantalla puede incrementar. Si tras abrir y
 * cerrar un popup el contador deja de subir al pulsar, la app ha quedado bloqueada (el bug de
 * la 0.7.0, cuando un contenedor invisible se comía los toques).
 */
object Probe {
    var taps = 0
        private set

    fun tap(): Int {
        taps += 1
        HostLog.add("probe tap #$taps")
        return taps
    }

    fun label() = "Probe taps: $taps"
}

/** Usuario "logueado" de la app (persistido, como haría un cliente). */
object Session {
    private const val PREFS = "reference_app"
    private const val KEY_USER = "user_id"

    fun userId(context: Context): String? =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(KEY_USER, null)

    fun setUserId(context: Context, userId: String?) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().apply {
            if (userId == null) remove(KEY_USER) else putString(KEY_USER, userId)
        }.apply()
    }
}

object Settings {
    private const val PREFS = "reference_app"
    private const val KEY_COLD_EVENT = "fire_event_on_cold_start"

    fun fireEventOnColdStart(context: Context): Boolean =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getBoolean(KEY_COLD_EVENT, false)

    fun setFireEventOnColdStart(context: Context, value: Boolean) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putBoolean(KEY_COLD_EVENT, value).apply()
    }
}
