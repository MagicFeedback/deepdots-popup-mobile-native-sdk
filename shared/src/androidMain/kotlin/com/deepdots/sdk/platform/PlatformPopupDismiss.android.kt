package com.deepdots.sdk.platform

import android.view.ViewGroup
import com.deepdots.sdk.renderer.PopupRenderer

actual fun dismissPopup(context: PlatformContext) {
    // Cualquier Activity (igual que `PopupRenderer.show`). Si el renderer instaló owners propios en
    // el contenedor, quitarlo de la ventana los destruye (listener de detach en el renderer).
    val root = context.activity.window?.decorView as? ViewGroup ?: return
    // Un popup que esperaba a que el host cerrase su modal tampoco debe aparecer después.
    PopupRenderer.cancelPending(root)
    // Buscar por tag
    val target = (0 until root.childCount)
        .map { root.getChildAt(it) }
        .firstOrNull { it.tag == "DeepdotsPopupContainer" }
    target?.let { root.removeView(it) }
}

