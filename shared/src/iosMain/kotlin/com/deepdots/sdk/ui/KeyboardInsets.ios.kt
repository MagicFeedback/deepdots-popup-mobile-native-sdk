package com.deepdots.sdk.ui

import androidx.compose.foundation.layout.imePadding
import androidx.compose.ui.Modifier

// iOS: antes no se hacía nada, contando con que el ComposeUIViewController sube el contenido
// enfocado por encima del teclado. Pero el campo de texto del survey está en un WKWebView (vista
// de interop), no en Compose: el controlador no lo ve enfocado y no subía nada, y el teclado
// tapaba Atrás/Enviar (iPhone SE e iPhone 17, pruebas E2E). Ahora se resta el inset del teclado
// como en Android, y PopupRenderer pone `onFocusBehavior = DoNothing` para no subirlo dos veces.
internal actual fun Modifier.avoidKeyboard(): Modifier = imePadding()
