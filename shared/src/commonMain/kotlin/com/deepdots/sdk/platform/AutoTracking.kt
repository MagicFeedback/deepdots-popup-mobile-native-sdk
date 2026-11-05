package com.deepdots.sdk.platform

/**
 * Instala (una vez por proceso, idempotente) la detección de la plataforma que emite en
 * [com.deepdots.sdk.tracking.AutoSignals]: ciclo de vida si [lifecycle], pantallas si [screens],
 * y siempre el aviso de "hay contexto" para la cola de popups. Nunca lanza.
 */
internal expect fun installPlatformAutoTracking(lifecycle: Boolean, screens: Boolean)

/**
 * Contexto detectado para pintar un popup: Activity en pantalla (Android) o root view controller
 * de la key window (iOS). En iOS solo responde en el hilo principal (null fuera de él).
 */
internal expect fun detectedPlatformContext(): PlatformContext?

/** ¿Puede haber contexto detectado ahora? Barato y desde cualquier hilo. */
internal expect fun hasDetectedPlatformContext(): Boolean
