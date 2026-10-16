package com.deepdots.sdk.renderer

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Ciclo de vida de los owners que el renderer instala en el contenedor del popup cuando la
 * Activity del host no es una ComponentActivity (p. ej. la FlutterActivity por defecto).
 *
 * Solo la lógica del owner: montar el `ComposeView` de verdad necesita un runtime de Android
 * (Robolectric o un dispositivo) y el repo no tiene ninguno en los tests JVM.
 */
class PopupViewTreeOwnerTest {

    private fun owner() = PopupViewTreeOwner(enforceMainThread = false)

    private fun recordEvents(owner: PopupViewTreeOwner): MutableList<Lifecycle.Event> {
        val events = mutableListOf<Lifecycle.Event>()
        owner.lifecycle.addObserver(LifecycleEventObserver { _, event -> events += event })
        return events
    }

    private class ProbeViewModel : ViewModel() {
        var cleared = false
        override fun onCleared() {
            cleared = true
        }
    }

    @Test
    fun starts_initialized_with_the_saved_state_registry_already_restored() {
        val o = owner()
        assertEquals(Lifecycle.State.INITIALIZED, o.lifecycle.currentState)
        // Compose consulta el registro al montarse y falla si no se restauró.
        assertTrue(o.savedStateRegistry.isRestored)
    }

    @Test
    fun show_moves_to_resumed_passing_through_create_and_start() {
        val o = owner()
        val events = recordEvents(o)

        o.show()

        assertEquals(Lifecycle.State.RESUMED, o.lifecycle.currentState)
        assertEquals(
            listOf(Lifecycle.Event.ON_CREATE, Lifecycle.Event.ON_START, Lifecycle.Event.ON_RESUME),
            events,
        )
    }

    @Test
    fun destroy_after_show_tears_down_and_clears_the_view_models() {
        val o = owner()
        o.show()
        val vm = ViewModelProvider(o, object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T = ProbeViewModel() as T
        })[ProbeViewModel::class.java]
        val events = recordEvents(o).also { it.clear() }

        o.destroy()

        assertEquals(Lifecycle.State.DESTROYED, o.lifecycle.currentState)
        assertEquals(
            listOf(Lifecycle.Event.ON_PAUSE, Lifecycle.Event.ON_STOP, Lifecycle.Event.ON_DESTROY),
            events,
        )
        assertTrue(vm.cleared)
    }

    @Test
    fun destroy_without_ever_showing_does_not_throw() {
        // Contenedor que nunca llegó a engancharse a la ventana: INITIALIZED -> DESTROYED no es una
        // transición válida para LifecycleRegistry, el owner pasa antes por CREATED.
        val o = owner()
        o.destroy()
        assertEquals(Lifecycle.State.DESTROYED, o.lifecycle.currentState)
    }

    @Test
    fun destroy_is_idempotent_and_show_does_not_resurrect() {
        val o = owner()
        o.show()
        o.destroy()
        o.destroy()
        o.show()
        assertEquals(Lifecycle.State.DESTROYED, o.lifecycle.currentState)
    }
}
