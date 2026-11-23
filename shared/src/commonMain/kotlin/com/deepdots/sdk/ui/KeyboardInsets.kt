package com.deepdots.sdk.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * Deja el popup por encima del teclado. En Android el popup va en el `decorView`, fuera del
 * contenido que la Activity redimensiona, y nadie le restaba el espacio del teclado: al escribir
 * en el survey el footer (Send) quedaba debajo y había que cerrar el teclado para enviar.
 */
internal expect fun Modifier.avoidKeyboard(): Modifier

/**
 * Si el teclado está abierto. Con el teclado la tarjeta encoge y el logo se pliega para dejar a la
 * vista el enunciado y el campo (Lemvigh-Müller, 23-nov-2026: en los pasos de texto el logo se
 * quedaba y el enunciado salía de la vista).
 */
@Composable
internal expect fun isKeyboardOpen(): Boolean
