package com.deepdots.sdk.util

/**
 * UUID v4 aleatorio. Espejo de `defaultUuid` del SDK Web (crypto.randomUUID con fallback).
 * Se usa para el `user_id` persistente que genera el SDK cuando el host no provee uno.
 */
expect fun randomUuid(): String
