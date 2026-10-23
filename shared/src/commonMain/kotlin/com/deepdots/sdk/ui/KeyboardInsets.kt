package com.deepdots.sdk.ui

import androidx.compose.ui.Modifier

/**
 * Deja el popup por encima del teclado. En Android el popup va en el `decorView`, fuera del
 * contenido que la Activity redimensiona, y nadie le restaba el espacio del teclado: al escribir
 * en el survey el footer (Send) quedaba debajo y había que cerrar el teclado para enviar.
 */
internal expect fun Modifier.avoidKeyboard(): Modifier
