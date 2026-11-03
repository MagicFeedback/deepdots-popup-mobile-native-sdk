package com.deepdots.sdk.ui

import android.graphics.Rect
import android.os.Build
import android.view.ViewTreeObserver
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView

internal actual fun Modifier.avoidKeyboard(): Modifier =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) imePadding() else composed { legacyKeyboardPadding() }

// Android 10 y anteriores: el inset del IME no llega al popup. Va colgado del decorView, fuera del
// contenido que la Activity redimensiona con adjustResize, y antes de API 30 el teclado no se
// reparte como inset a esa vista: imePadding() valía 0 y el teclado tapaba Atrás/Enviar (visto en
// el emulador de Android 8 con las pruebas E2E). Se mide con el marco visible de la ventana, que
// sí descuenta el teclado, y se le resta la barra de navegación, que ya pone systemBars.
private fun Modifier.legacyKeyboardPadding(): Modifier = composed {
    val view = LocalView.current
    val density = LocalDensity.current
    var keyboardPx by remember { mutableIntStateOf(0) }
    DisposableEffect(view) {
        val root = view.rootView
        val frame = Rect()
        val listener = ViewTreeObserver.OnGlobalLayoutListener {
            root.getWindowVisibleDisplayFrame(frame)
            keyboardPx = legacyKeyboardHeight(root.height, frame.bottom)
        }
        root.viewTreeObserver.addOnGlobalLayoutListener(listener)
        listener.onGlobalLayout()
        onDispose { root.viewTreeObserver.removeOnGlobalLayoutListener(listener) }
    }
    val navigationPx = WindowInsets.navigationBars.getBottom(density)
    padding(bottom = with(density) { (keyboardPx - navigationPx).coerceAtLeast(0).toDp() })
}

/**
 * Alto del teclado a partir del alto de la ventana y del borde inferior de su parte visible. Por
 * debajo del 15 % de la pantalla no es un teclado (la barra de navegación, o nada).
 */
internal fun legacyKeyboardHeight(rootHeight: Int, visibleBottom: Int): Int {
    val hidden = rootHeight - visibleBottom
    return if (hidden > rootHeight * 0.15) hidden else 0
}
