package com.deepdots.sdk.renderer

import android.content.Context
import android.view.KeyEvent
import android.view.View
import android.widget.FrameLayout
import java.lang.ref.WeakReference

/** Qué hacer con un evento de tecla según la ruta legacy del Back (ver [PopupContainer]). */
internal enum class BackKeyDecision {
    /** No es Back o el popup no lo está gestionando: sigue su curso normal. */
    Pass,

    /** Es Back y lo gestiona el popup, pero aún no toca cerrar (DOWN, repeticiones, cancelado). */
    Consume,

    /** Back completo (UP tras un DOWN visto por el popup): cerrar. */
    Close,
}

/**
 * Lógica pura de la ruta legacy del Back, separada para poder testearla en JVM.
 *
 * Solo cierra con el UP de una pulsación cuyo DOWN también llegó al popup: si el popup se
 * enseña justo entre el DOWN (que ya fue al host) y el UP, ese UP no lo cierra al instante.
 */
internal fun backKeyDecision(
    keyCode: Int,
    action: Int,
    canceled: Boolean,
    downTracked: Boolean,
    handling: Boolean,
): BackKeyDecision = when {
    !handling || keyCode != KeyEvent.KEYCODE_BACK -> BackKeyDecision.Pass
    action == KeyEvent.ACTION_UP && downTracked && !canceled -> BackKeyDecision.Close
    else -> BackKeyDecision.Consume
}

/**
 * Contenedor del popup en el decorView. Cubre la ruta LEGACY del Back (apps sin predictive
 * back): ahí el sistema entrega `KEYCODE_BACK` como tecla a la vista con el foco, así que el
 * contenedor toma el foco al hacerse visible y se come el Back mientras [onBackKey] esté puesto.
 * Funciona igual con `ComponentActivity` que con `Activity` a secas, y cubre también al host que
 * sobrescribe `onBackPressed()` sin llamar a `super` (FlutterFragmentActivity): la tecla no
 * llega a la Activity.
 *
 * Se intercepta en `dispatchKeyEvent` del CONTENEDOR, no con un `OnKeyListener`, para que siga
 * funcionando cuando el foco baja a un descendiente (un campo de texto del survey en el WebView).
 *
 * El predictive back (API 33+ con opt-in) y el `OnBackPressedDispatcher` los registra
 * `PopupBackHandler` (ui/PopupBackHandler.android.kt), que es también quien pone [onBackKey].
 */
internal class PopupContainer(context: Context) : FrameLayout(context) {
    /** El cierre del popup mientras está a la vista; `null` = el Back sigue hacia el host. */
    var onBackKey: (() -> Unit)? = null

    private var backDownTracked = false
    private var focusBeforePopup: WeakReference<View>? = null

    init {
        isFocusable = true
        isFocusableInTouchMode = true
    }

    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        val decision = backKeyDecision(
            keyCode = event.keyCode,
            action = event.action,
            canceled = event.isCanceled,
            downTracked = backDownTracked,
            handling = onBackKey != null,
        )
        if (decision != BackKeyDecision.Pass) {
            backDownTracked = event.action == KeyEvent.ACTION_DOWN
        }
        return when (decision) {
            BackKeyDecision.Pass -> super.dispatchKeyEvent(event)
            BackKeyDecision.Consume -> true
            BackKeyDecision.Close -> {
                onBackKey?.invoke()
                true
            }
        }
    }

    override fun onVisibilityChanged(changedView: View, visibility: Int) {
        super.onVisibilityChanged(changedView, visibility)
        // Apertura diferida: el contenedor nace INVISIBLE (no puede tener foco) y el foco se toma
        // al enseñarlo. Popup modal: el foco es suyo mientras está a la vista.
        if (changedView === this && visibility == VISIBLE && isAttachedToWindow && !hasFocus()) {
            focusBeforePopup = rootView.findFocus()?.let { WeakReference(it) }
            requestFocus()
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        // Cualquier camino de cierre acaba quitando el contenedor: ya no hay Back que comerse.
        onBackKey = null
        backDownTracked = false
        // Devolver el foco a donde estaba antes del popup (modal bien educado).
        val previous = focusBeforePopup?.get()
        focusBeforePopup = null
        if (previous != null && previous.isAttachedToWindow) previous.post { previous.requestFocus() }
    }
}
