package com.deepdots.sdk.renderer

import android.app.Activity
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.webkit.WebView
import android.widget.FrameLayout
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.compositionContext
import androidx.compose.ui.platform.createLifecycleAwareWindowRecomposer
import androidx.lifecycle.findViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.findViewTreeSavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.deepdots.sdk.models.Action
import com.deepdots.sdk.models.PopupDefinition
import com.deepdots.sdk.platform.PlatformContext
import com.deepdots.sdk.ui.PopupView
import java.util.WeakHashMap
import java.util.concurrent.atomic.AtomicBoolean

/** Qué hacer con un popup según el estado de la ventana de la Activity. */
internal enum class AttachDecision {
    /** Montarlo ya. */
    Now,

    /** Hay otra ventana del host encima (diálogo, bottom sheet, picker): esperar a que se cierre. */
    WaitForFocus,

    /** La Activity se está cerrando: no tiene sentido montarlo ni esperar. */
    Drop,
}

/**
 * Lógica pura de cuándo montar el popup, separada para poder testearla en JVM.
 *
 * El popup va en la ventana de la Activity; un diálogo o un bottom sheet del host son otra
 * ventana ENCIMA, y el popup quedaba debajo de su velo, sin poder tocarse. Con uno de ellos
 * abierto la ventana de la Activity pierde el foco, así que se espera a que lo recupere.
 */
internal fun popupAttachDecision(hasWindowFocus: Boolean, finishing: Boolean): AttachDecision = when {
    finishing -> AttachDecision.Drop
    hasWindowFocus -> AttachDecision.Now
    else -> AttachDecision.WaitForFocus
}

actual object PopupRenderer {
    actual fun show(
        popup: PopupDefinition,
        context: PlatformContext,
        onAction: (Action) -> Unit,
        onSurveyEvent: (name: String, payload: String?) -> Unit,
        onDismiss: () -> Unit
    ) {
        // Cualquier Activity, no solo ComponentActivity: la FlutterActivity por defecto de Flutter
        // es una Activity a secas y antes el popup no salía (y no se logueaba nada).
        val activity = context.activity
        val root = activity.window?.decorView as? ViewGroup
        if (root == null) {
            Log.w(LOG_TAG, "Popup ${popup.id} not shown: ${activity.javaClass.name} has no window/decorView yet")
            return
        }
        val dismissed = AtomicBoolean(false)
        // PopupContainer: FrameLayout que además se come el Back legacy (ver PopupBackHandler).
        val container = PopupContainer(activity).apply {
            layoutParams = FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            tag = "DeepdotsPopupContainer"
            // Apertura diferida: INVISIBLE, no GONE. Mantiene el layout (el WebView se monta y
            // carga el survey) pero no se pinta NI recibe toques, así que mientras se espera el
            // usuario sigue usando la app; con la vista visible y solo transparente, el overlay
            // se comería sus toques sin enseñar nada. `PopupView` la enseña en `onReady`.
            visibility = android.view.View.INVISIBLE
        }
        // ComponentActivity (y AppCompat/FragmentActivity/FlutterFragmentActivity) pone los owners
        // de view tree en su decorView (en `setContentView`): entonces se usa lo de siempre. Si
        // faltan (Activity a secas), se instalan unos propios en el contenedor, ANTES de añadir
        // el ComposeView.
        val hostHasViewTreeOwners = root.findViewTreeLifecycleOwner() != null &&
            root.findViewTreeSavedStateRegistryOwner() != null
        if (!hostHasViewTreeOwners) installOwnViewTreeOwners(container)
        val composeView = ComposeView(activity).apply {
            setContent {
                PopupView(
                    popup = popup,
                    onAction = actionHandler@{ action ->
                        if (dismissed.getAndSet(true)) return@actionHandler
                        onAction(action)
                        // Defer teardown and removal to avoid detaching AndroidView during pointer event
                        container.post {
                            try {
                                (0 until container.childCount)
                                    .asSequence()
                                    .map { container.getChildAt(it) }
                                    .forEach { child ->
                                        if (child is WebView) {
                                            try {
                                                child.stopLoading()
                                            } catch (_: Throwable) {}
                                            child.onPause()
                                            child.loadUrl("about:blank")
                                            child.clearHistory()
                                            child.removeAllViews()
                                            child.destroy()
                                        }
                                    }
                            } catch (_: Throwable) { }
                            try { root.removeView(container) } catch (_: Throwable) { }
                            onDismiss()
                        }
                    },
                    onSurveyEvent = onSurveyEvent,
                    // Por `post`: una vista solo se toca desde el hilo principal, y `onReady`
                    // puede llegar desde el puente del WebView (ver SurveyView.android).
                    onReady = { container.post { container.visibility = android.view.View.VISIBLE } },
                )
            }
        }
        container.addView(composeView)
        when (popupAttachDecision(activity.hasWindowFocus(), activity.isFinishing)) {
            AttachDecision.Now -> root.addView(container)
            AttachDecision.WaitForFocus -> attachWhenFocused(activity, root, container)
            AttachDecision.Drop ->
                Log.w(LOG_TAG, "Popup ${popup.id} not shown: ${activity.javaClass.name} is finishing")
        }
    }

    /** Popups esperando a que el host cierre su modal, uno por ventana. */
    private val pending = WeakHashMap<View, ViewTreeObserver.OnWindowFocusChangeListener>()

    private fun attachWhenFocused(activity: Activity, root: ViewGroup, container: View) {
        cancelPending(root)
        Log.i(LOG_TAG, "Host window on top (dialog or sheet): popup waits until it closes")
        val listener = object : ViewTreeObserver.OnWindowFocusChangeListener {
            override fun onWindowFocusChanged(hasFocus: Boolean) {
                if (!hasFocus) return
                cancelPending(root)
                // Fuera del despacho del foco: añadir vistas dentro de él no es seguro.
                root.post {
                    if (!activity.isFinishing && !activity.isDestroyed) root.addView(container)
                }
            }
        }
        pending[root] = listener
        root.viewTreeObserver.addOnWindowFocusChangeListener(listener)
    }

    /** ¿Hay un popup esperando en [root] a que el host cierre su modal? */
    internal fun hasPending(root: View): Boolean = pending.containsKey(root)

    /** Olvida el popup que esperaba en [root] (`close()` / `Deepdots.dismiss` antes de mostrarse). */
    internal fun cancelPending(root: View) {
        val listener = pending.remove(root) ?: return
        val observer = root.viewTreeObserver
        if (observer.isAlive) observer.removeOnWindowFocusChangeListener(listener)
    }

    /**
     * Owners propios para una Activity que no los trae (ver [PopupViewTreeOwner]). Viven lo que
     * vive el contenedor en la ventana: se destruyen al desengancharlo, así que cubre TODAS las
     * vías de cierre (acción del popup, `dismissPopup` y la Activity destruida) sin que cada una
     * tenga que acordarse.
     */
    private fun installOwnViewTreeOwners(container: FrameLayout) {
        val owner = PopupViewTreeOwner()
        container.setViewTreeLifecycleOwner(owner)
        container.setViewTreeSavedStateRegistryOwner(owner)
        container.setViewTreeViewModelStoreOwner(owner)
        // El Recomposer "de ventana" de Compose se cuelga del decorView y saca el lifecycle de
        // ahí, donde una Activity a secas no tiene ninguno ("ViewTreeLifecycleOwner not found").
        // Le damos uno propio en el contenedor, atado a este lifecycle: el ComposeView lo
        // encuentra antes al subir por el árbol, y se cancela al destruir el owner.
        container.compositionContext =
            container.createLifecycleAwareWindowRecomposer(lifecycle = owner.lifecycle)
        container.addOnAttachStateChangeListener(object : View.OnAttachStateChangeListener {
            override fun onViewAttachedToWindow(v: View) {
                owner.show()
            }

            override fun onViewDetachedFromWindow(v: View) {
                // Los hijos (el ComposeView) se desenganchan antes que el contenedor, así que la
                // composición ya se ha liberado cuando el lifecycle llega a DESTROYED.
                v.removeOnAttachStateChangeListener(this)
                owner.destroy()
            }
        })
    }

    private const val LOG_TAG = "DeepdotsPopups"
}
