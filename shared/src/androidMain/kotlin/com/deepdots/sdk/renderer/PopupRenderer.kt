package com.deepdots.sdk.renderer

import android.view.ViewGroup
import android.webkit.WebView
import android.widget.FrameLayout
import androidx.activity.ComponentActivity
import androidx.compose.ui.platform.ComposeView
import com.deepdots.sdk.models.Action
import com.deepdots.sdk.models.PopupDefinition
import com.deepdots.sdk.platform.PlatformContext
import com.deepdots.sdk.ui.PopupView
import java.util.concurrent.atomic.AtomicBoolean

actual object PopupRenderer {
    actual fun show(
        popup: PopupDefinition,
        context: PlatformContext,
        onAction: (Action) -> Unit,
        onDismiss: () -> Unit
    ) {
        val activity = context.activity as? ComponentActivity ?: return
        val root = activity.window?.decorView as? ViewGroup ?: return
        val dismissed = AtomicBoolean(false)
        val container = FrameLayout(activity).apply {
            layoutParams = FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            tag = "DeepdotsPopupContainer"
        }
        val composeView = ComposeView(activity).apply {
            setContent { PopupView(popup) { action ->
                if (dismissed.getAndSet(true)) return@PopupView
                onAction(action)
                // Defer teardown and removal to avoid detaching AndroidView during pointer event
                container.post {
                    try {
                        (0 until container.childCount)
                            .asSequence()
                            .map { container.getChildAt(it) }
                            .forEach { child ->
                                if (child is WebView) {
                                    try {
                                        child.stopLoading()
                                    } catch (_: Throwable) {}
                                    child.onPause()
                                    child.loadUrl("about:blank")
                                    child.clearHistory()
                                    child.removeAllViews()
                                    child.destroy()
                                }
                            }
                    } catch (_: Throwable) { }
                    // Remove from root after teardown
                    try { root.removeView(container) } catch (_: Throwable) { }
                    onDismiss()
                }
            } }
        }
        container.addView(composeView)
        root.addView(container)
    }
}
