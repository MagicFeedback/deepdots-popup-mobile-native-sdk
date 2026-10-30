package com.deepdots.sdk.renderer

import platform.Foundation.NSLog
import platform.UIKit.UIAlertController
import platform.UIKit.UIModalPresentationFullScreen
import platform.UIKit.UIModalPresentationOverFullScreen
import platform.UIKit.UIViewController
import platform.darwin.DISPATCH_TIME_NOW
import platform.darwin.dispatch_after
import platform.darwin.dispatch_get_main_queue
import platform.darwin.dispatch_time

/**
 * Presenta el popup desde lo más alto de la cadena de controladores presentados (ver
 * [presentationPlan]). Si ahora no se puede (alerta del host, controlador cerrándose), reintenta
 * cada [RETRY_MS] hasta que se pueda o hasta que [cancelPendingPresentation] lo cancele.
 */
internal object PopupPresenter {
    private const val RETRY_MS = 300L

    /** Presentación en espera: `close()` / `Deepdots.dismiss` la anulan poniéndola a null. */
    private var pending: Any? = null

    fun present(base: UIViewController, popup: UIViewController) {
        val token = Any()
        pending = token
        attempt(base, popup, token, logged = false)
    }

    /** ¿Hay un popup esperando a que el host cierre su alerta? */
    fun hasPendingPresentation(): Boolean = pending != null

    fun cancelPendingPresentation() {
        pending = null
    }

    private fun attempt(base: UIViewController, popup: UIViewController, token: Any, logged: Boolean) {
        if (pending !== token) return
        val chain = presentedChain(base)
        when (val plan = presentationPlan(chain.map { it.kind() })) {
            is PresentationPlan.PresentFrom -> {
                pending = null
                val presenter = if (plan.index < 0) base else chain[plan.index]
                presenter.presentViewController(popup, true, null)
            }
            PresentationPlan.Wait -> {
                if (!logged) NSLog("[DeepdotsPopups] Host alert on top: popup waits until it closes")
                val delay = dispatch_time(DISPATCH_TIME_NOW, RETRY_MS * 1_000_000)
                dispatch_after(delay, dispatch_get_main_queue()) { attempt(base, popup, token, logged = true) }
            }
        }
    }

    private fun presentedChain(base: UIViewController): List<UIViewController> {
        val chain = mutableListOf<UIViewController>()
        var current = base.presentedViewController
        while (current != null) {
            chain += current
            current = current.presentedViewController
        }
        return chain
    }

    private fun UIViewController.kind(): PresentedKind = when {
        isBeingDismissed() -> PresentedKind.Dismissing
        this is UIAlertController -> PresentedKind.Alert
        modalPresentationStyle == UIModalPresentationFullScreen ||
            modalPresentationStyle == UIModalPresentationOverFullScreen -> PresentedKind.Screen
        else -> PresentedKind.Sheet
    }
}
