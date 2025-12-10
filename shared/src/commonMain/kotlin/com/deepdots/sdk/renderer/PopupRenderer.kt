package com.deepdots.sdk.renderer

import com.deepdots.sdk.models.Action
import com.deepdots.sdk.models.PopupDefinition
import com.deepdots.sdk.platform.PlatformContext

expect object PopupRenderer {
    fun show(
        popup: PopupDefinition,
        context: PlatformContext,
        onAction: (Action) -> Unit,
        onDismiss: () -> Unit
    )
}
