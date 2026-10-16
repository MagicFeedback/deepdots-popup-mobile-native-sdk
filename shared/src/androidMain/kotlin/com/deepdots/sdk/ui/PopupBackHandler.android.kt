package com.deepdots.sdk.ui

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.os.Build
import android.view.View
import android.window.OnBackInvokedCallback
import android.window.OnBackInvokedDispatcher
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import com.deepdots.sdk.renderer.PopupContainer

/**
 * Android: el Back del sistema cierra el popup mientras está a la vista. Tres rutas, porque
 * ninguna cubre sola todos los hosts:
 *
 * 1. **Predictive back (API 33+ con opt-in, y por defecto con targetSdk 36):** el sistema ya no
 *    manda tecla ni llama a `onBackPressed()`; solo invoca `OnBackInvokedCallback`s. Se registra
 *    uno con `PRIORITY_OVERLAY` en la ventana de la Activity, que gana a los `PRIORITY_DEFAULT`
 *    (el del `OnBackPressedDispatcher` de androidx y el que registran hosts como Flutter).
 * 2. **Legacy, tecla `KEYCODE_BACK`:** la intercepta el [PopupContainer] enfocado. Vale para
 *    `Activity` a secas y para hosts que sobrescriben `onBackPressed()` sin `super`.
 * 3. **Legacy, `ComponentActivity` sin el foco en el popup:** callback en su
 *    `OnBackPressedDispatcher` (último añadido = primero en recibir).
 *
 * En cada momento solo una de las tres recibe el Back y, aunque llegaran dos, el cierre es
 * idempotente (el renderer ignora la segunda acción).
 */
@Composable
internal actual fun PopupBackHandler(enabled: Boolean, onBack: () -> Unit) {
    val context = LocalContext.current
    val view = LocalView.current
    val currentOnBack by rememberUpdatedState(onBack)
    DisposableEffect(enabled, context, view) {
        val activity = if (enabled) context.findActivity() else null
        val registration = if (activity != null) {
            registerPopupBack(activity, view) { currentOnBack() }
        } else {
            BackRegistration()
        }
        onDispose { registration.release() }
    }
}

/**
 * Registros del Back de un popup, con UNA forma de soltarlos todos. Idempotente: la composición
 * puede descartarse después de que el detach ya lo haya soltado (o al revés).
 */
internal class BackRegistration {
    private val releases = mutableListOf<() -> Unit>()
    var released = false
        private set

    fun add(release: () -> Unit) {
        if (released) runCatching(release) else releases += release
    }

    fun release() {
        if (released) return
        released = true
        releases.asReversed().forEach { runCatching(it) }
        releases.clear()
    }
}

private fun registerPopupBack(activity: Activity, view: View, onBack: () -> Unit): BackRegistration {
    val registration = BackRegistration()

    // 1. Predictive back.
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        val dispatcher = activity.onBackInvokedDispatcher
        val callback = OnBackInvokedCallback { onBack() }
        dispatcher.registerOnBackInvokedCallback(OnBackInvokedDispatcher.PRIORITY_OVERLAY, callback)
        registration.add { dispatcher.unregisterOnBackInvokedCallback(callback) }
    }

    // 2. Tecla Back (legacy) en el contenedor del popup.
    val container = generateSequence(view.parent) { it.parent }
        .filterIsInstance<PopupContainer>()
        .firstOrNull()
    if (container != null) {
        container.onBackKey = onBack
        registration.add { if (container.onBackKey === onBack) container.onBackKey = null }
    }

    // 3. OnBackPressedDispatcher (legacy, ComponentActivity).
    if (activity is ComponentActivity) {
        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() = onBack()
        }
        activity.onBackPressedDispatcher.addCallback(callback)
        registration.add { callback.remove() }
    }

    // Red de seguridad: si la vista sale de la ventana (cualquier camino de cierre, Activity
    // destruida) se suelta todo aunque la composición tarde en descartarse. Un Back tragado
    // después de cerrar dejaría al host sin poder volver atrás.
    val detachListener = object : View.OnAttachStateChangeListener {
        override fun onViewAttachedToWindow(v: View) = Unit
        override fun onViewDetachedFromWindow(v: View) = registration.release()
    }
    view.addOnAttachStateChangeListener(detachListener)
    registration.add { view.removeOnAttachStateChangeListener(detachListener) }

    return registration
}

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}
