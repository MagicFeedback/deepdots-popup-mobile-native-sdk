package com.deepdots.sdk.storage

/**
 * Storage PERSISTENTE por defecto: `SharedPreferences` en Android, `NSUserDefaults` en iOS.
 * Equivale al `localStorage` que usa el SDK Web — el `user_id` debe sobrevivir entre sesiones
 * o cada arranque contaría como usuario nuevo.
 *
 * Cero configuración para el host: `InitOptions.storage` sigue permitiendo inyectar el suyo.
 * Sin runtime nativo disponible (unit tests en JVM) cae a [InMemoryStorage].
 */
expect fun createDefaultStorage(): KeyValueStorage
