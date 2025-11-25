package com.deepdots.sdk.renderer

import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.activity.ComponentActivity
import androidx.compose.ui.platform.ComposeView
import com.deepdots.sdk.models.Action
import com.deepdots.sdk.models.PopupDefinition
import com.deepdots.sdk.platform.PlatformContext
import com.deepdots.sdk.ui.PopupView

actual object PopupRenderer {
    actual fun show(
        popup: PopupDefinition,
        context: PlatformContext,
        onAction: (Action) -> Unit
    ) {
        val activity = context.activity as? ComponentActivity ?: return
        val root = activity.window?.decorView as? ViewGroup ?: return
        val container = FrameLayout(activity).apply {
            layoutParams = FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            tag = "DeepdotsPopupContainer"
        }
        val composeView = ComposeView(activity).apply {
            setContent { PopupView(popup) { action ->
                onAction(action)
                root.removeView(container)
            } }
        }
        container.addView(composeView)
        root.addView(container)
    }
}
