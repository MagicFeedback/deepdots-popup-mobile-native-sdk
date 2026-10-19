package com.deepdots.sdk.util

/**
 * Android: no-op. El proceso no se congela en `onStop` (el freezer de apps en caché actúa
 * segundos después), y alargarlo exigiría un servicio o WorkManager, fuera de alcance aquí.
 */
internal actual fun beginBackgroundTask(name: String): () -> Unit = {}
