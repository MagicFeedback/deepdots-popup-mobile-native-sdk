package com.deepdots.sdk.renderer

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner

/**
 * Owners de view tree (lifecycle + saved state + view models) con el ámbito del CONTENEDOR de un
 * popup, para poder montar el `ComposeView` en una `Activity` cualquiera.
 *
 * Un `ComposeView` exige un `ViewTreeLifecycleOwner` y un `ViewTreeSavedStateRegistryOwner` por
 * encima en el árbol de vistas. `ComponentActivity` (y sus hijas: `AppCompatActivity`,
 * `FragmentActivity`, `FlutterFragmentActivity`) los pone en su decorView; una `Activity` a secas
 * NO (p. ej. `io.flutter.embedding.android.FlutterActivity`, el default de Flutter). En ese caso el
 * renderer instala uno de estos en el contenedor: nace al montar el popup, pasa a `RESUMED`
 * mientras se ve y muere (`DESTROYED` + `ViewModelStore` limpio) cuando el contenedor sale de la
 * ventana, sea cual sea la vía (acción del popup, `dismissPopup`, o la Activity destruida).
 *
 * El ciclo es de un solo sentido: una vez destruido, `show()` no lo resucita (el contenedor no se
 * reutiliza, cada popup monta uno nuevo).
 *
 * @param enforceMainThread `false` solo en tests JVM: `LifecycleRegistry` comprueba el hilo
 *   principal vía `Looper`, que en los stubs de `android.jar` no existe.
 */
internal class PopupViewTreeOwner(
    enforceMainThread: Boolean = true,
) : LifecycleOwner, SavedStateRegistryOwner, ViewModelStoreOwner {

    private val registry: LifecycleRegistry =
        if (enforceMainThread) LifecycleRegistry(this) else LifecycleRegistry.createUnsafe(this)
    private val savedStateController = SavedStateRegistryController.create(this)

    override val lifecycle: Lifecycle get() = registry
    override val savedStateRegistry: SavedStateRegistry get() = savedStateController.savedStateRegistry
    override val viewModelStore: ViewModelStore = ViewModelStore()

    init {
        // Hay que restaurar en INITIALIZED (antes de CREATED). Sin estado previo: el popup no
        // sobrevive a una recreación de la Activity. Compose consulta el registro al montarse y
        // falla si nunca se restauró.
        savedStateController.performRestore(null)
    }

    /** Popup montado y a la vista: `RESUMED`. No-op si ya se destruyó. */
    fun show() {
        if (registry.currentState == Lifecycle.State.DESTROYED) return
        registry.currentState = Lifecycle.State.RESUMED
    }

    /** Popup retirado: `DESTROYED` y limpia los view models. Idempotente. */
    fun destroy() {
        if (registry.currentState == Lifecycle.State.DESTROYED) return
        // LifecycleRegistry no deja bajar de INITIALIZED a DESTROYED (popup que nunca llegó a
        // mostrarse): primero a CREATED.
        if (registry.currentState == Lifecycle.State.INITIALIZED) {
            registry.currentState = Lifecycle.State.CREATED
        }
        registry.currentState = Lifecycle.State.DESTROYED
        viewModelStore.clear()
    }
}
