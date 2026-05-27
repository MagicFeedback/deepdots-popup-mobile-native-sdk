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

    /** Registers a listener for the given event (thread-safe). */
    suspend fun on(event: Event, listener: (EventData) -> Unit) {
        mutex.withLock {
            listeners.getOrPut(event) { mutableListOf() }.add(listener)
        }
    }

    /** Emits an event to all registered listeners (thread-safe). */
    suspend fun emit(event: Event, data: EventData) {
        val snapshot = mutex.withLock { listeners[event]?.toList() ?: emptyList() }
        snapshot.forEach { callback ->
            withContext(Dispatchers.Default) {
                callback(data)
            }
        }
    }

    /** Removes a previously registered listener. */
    suspend fun off(event: Event, listener: (EventData) -> Unit) {
        mutex.withLock {
            listeners[event]?.remove(listener)
            if (listeners[event]?.isEmpty() == true) {
                listeners.remove(event)
            }
        }
    }

    /** Clears every registered listener. */
    suspend fun clear() {
        mutex.withLock { listeners.clear() }
    }
}