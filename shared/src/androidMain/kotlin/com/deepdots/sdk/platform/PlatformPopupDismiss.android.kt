package com.deepdots.sdk.platform

import android.view.ViewGroup
import androidx.activity.ComponentActivity

actual fun dismissPopup(context: PlatformContext) {
    val activity = context.activity as? ComponentActivity ?: return
    val root = activity.window?.decorView as? ViewGroup ?: return
    // Buscar por tag
    val target = (0 until root.childCount)
        .map { root.getChildAt(it) }
        .firstOrNull { it.tag == "DeepdotsPopupContainer" }
    target?.let { root.removeView(it) }
}

