package com.deepdots.sdk.ui

import androidx.compose.ui.Modifier

// iOS: el ComposeUIViewController ya desplaza el contenido enfocado por encima del teclado;
// sumar aquí el inset lo subiría dos veces.
internal actual fun Modifier.avoidKeyboard(): Modifier = this
