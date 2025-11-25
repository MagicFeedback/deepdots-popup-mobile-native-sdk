package com.deepdots.sdk.renderer

import androidx.compose.ui.window.ComposeUIViewController
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
        val vc = ComposeUIViewController { PopupView(popup, onAction) }
        context.viewController.presentViewController(vc, true, null)
    }
}
