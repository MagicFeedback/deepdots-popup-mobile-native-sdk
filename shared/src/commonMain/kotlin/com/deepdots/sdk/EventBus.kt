package com.deepdots.sdk

import com.deepdots.sdk.models.Event
import com.deepdots.sdk.models.EventData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class EventBus {
    private val mutex = Mutex()
    private val listeners = mutableMapOf<Event, MutableList<(EventData) -> Unit>>()

    /** Registra un listener para un evento (thread-safe). */
    suspend fun on(event: Event, listener: (EventData) -> Unit) {
        mutex.withLock {
            listeners.getOrPut(event) { mutableListOf() }.add(listener)
        }
    }

    /** Emite un evento a todos sus listeners (thread-safe). */
    suspend fun emit(event: Event, data: EventData) {
        // Tomamos snapshot bajo lock para minimizar tiempo bajo mutex
        val snapshot = mutex.withLock { listeners[event]?.toList() ?: emptyList() }
        // Disparamos fuera del lock (posible ejecución costosa)
        snapshot.forEach { callback ->
            // Ejecutamos en Default para no bloquear el caller si lo llama desde Main
            withContext(Dispatchers.Default) {
                callback(data)
            }
        }
    }

    /** Quita un listener previamente registrado. */
    suspend fun off(event: Event, listener: (EventData) -> Unit) {
        mutex.withLock {
            listeners[event]?.remove(listener)
            if (listeners[event]?.isEmpty() == true) {
                listeners.remove(event)
            }
        }
    }

    /** Limpia todos los listeners. */
    suspend fun clear() {
        mutex.withLock { listeners.clear() }
    }
}