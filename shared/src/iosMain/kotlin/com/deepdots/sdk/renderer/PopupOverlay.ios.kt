@file:OptIn(ExperimentalForeignApi::class)

package com.deepdots.sdk.renderer

import com.deepdots.sdk.ui.CardBounds
import com.deepdots.sdk.ui.cardKeepsTouch
import kotlinx.cinterop.CValue
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.useContents
import platform.CoreGraphics.CGPoint
import platform.CoreGraphics.CGRect
import platform.UIKit.UIColor
import platform.UIKit.UIEvent
import platform.UIKit.UIView
import platform.UIKit.UIViewAutoresizingFlexibleHeight
import platform.UIKit.UIViewAutoresizingFlexibleWidth
import platform.UIKit.UIViewController
import platform.UIKit.addChildViewController
import platform.UIKit.didMoveToParentViewController
import platform.UIKit.removeFromParentViewController
import platform.UIKit.willMoveToParentViewController

/**
 * Vista del popup que no bloquea (DD-334): se queda con los toques que caen en la tarjeta y deja
 * pasar el resto a la app (`hitTest` devuelve `nil`, UIKit sigue con la vista de debajo). La
 * propia vista nunca se queda un toque: con el popup aún invisible (interacción desactivada en la
 * vista de Compose) todo pasa a la app, igual que con el modal.
 */
internal class PassThroughView(frame: CValue<CGRect>) : UIView(frame) {
    var card: CardBounds? = null

    override fun hitTest(point: CValue<CGPoint>, withEvent: UIEvent?): UIView? {
        val hit = super.hitTest(point, withEvent) ?: return null
        if (hit === this) return null
        val keeps = point.useContents { cardKeepsTouch(card, x, y) }
        return if (keeps) hit else null
    }
}

/**
 * El popup que no bloquea no se puede presentar como modal: con `OverFullScreen` UIKit mete la
 * vista en un contenedor de transición a pantalla completa que se queda con TODOS los toques,
 * aunque la vista del popup los deje pasar. Así que va como hijo del controlador de arriba del
 * todo, en una [PassThroughView] encima de su vista. Un solo popup a la vez, como con el modal.
 */
internal object PopupOverlay {
    private var popup: UIViewController? = null
    private var container: PassThroughView? = null

    /** Monta [vc] encima de [parent], dentro de [overlay] (la vista que decide qué toques se queda). */
    fun attach(parent: UIViewController, vc: UIViewController, overlay: PassThroughView) {
        detachCurrent()
        val host = parent.view
        overlay.setFrame(host.bounds)
        overlay.autoresizingMask = UIViewAutoresizingFlexibleWidth or UIViewAutoresizingFlexibleHeight
        overlay.backgroundColor = UIColor.clearColor
        parent.addChildViewController(vc)
        vc.view.setFrame(overlay.bounds)
        vc.view.autoresizingMask = UIViewAutoresizingFlexibleWidth or UIViewAutoresizingFlexibleHeight
        overlay.addSubview(vc.view)
        host.addSubview(overlay)
        vc.didMoveToParentViewController(parent)
        popup = vc
        container = overlay
    }

    /** Quita [vc] si es el popup montado. */
    fun detach(vc: UIViewController) {
        if (popup === vc) detachCurrent()
    }

    /** Quita el popup montado, si hay. `true` si había uno. */
    fun detachCurrent(): Boolean {
        val vc = popup ?: return false
        vc.willMoveToParentViewController(null)
        container?.removeFromSuperview()
        vc.view.removeFromSuperview()
        vc.removeFromParentViewController()
        popup = null
        container = null
        return true
    }

    fun isShowing(): Boolean = popup != null
}
