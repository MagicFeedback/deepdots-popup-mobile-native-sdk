package com.deepdots.sdk.renderer

import androidx.compose.ui.window.ComposeUIViewController
import com.deepdots.sdk.models.Action
import com.deepdots.sdk.models.PopupDefinition
import com.deepdots.sdk.platform.PlatformContext
import com.deepdots.sdk.ui.PopupView
import platform.UIKit.UIViewController

actual object PopupRenderer {
    actual fun show(
        popup: PopupDefinition,
        context: PlatformContext,
        onAction: (Action) -> Unit,
        onDismiss: () -> Unit
    ) {
        var controllerRef: UIViewController? = null
        val vc = ComposeUIViewController { PopupView(popup) { action ->
            onAction(action)
            controllerRef?.dismissViewControllerAnimated(true, null)
            onDismiss()
        } }
        controllerRef = vc
        context.viewController.presentViewController(vc, true, null)
    }
}
