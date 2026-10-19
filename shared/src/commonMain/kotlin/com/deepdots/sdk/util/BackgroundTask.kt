package com.deepdots.sdk.util

/**
 * Pide al sistema unos segundos para terminar un trabajo corto aunque la app pase a background:
 * el POST del lote de cierre, que sale justo en `onBackground()`. Devuelve la función que lo da
 * por terminado; llamarla más de una vez es seguro.
 *
 * - iOS: `UIApplication.beginBackgroundTask`. Sin ella, iOS puede suspender la app nada más
 *   volver de `applicationDidEnterBackground`, con el POST de cierre aún en vuelo, y el
 *   registro se queda sin su último lote (page_view de la última pantalla, session_end).
 * - Android: no-op (ver el `actual`).
 *
 * No tiene equivalente en el SDK Web (allí el cierre va por `sendBeacon`/`keepalive`).
 */
internal expect fun beginBackgroundTask(name: String): () -> Unit
