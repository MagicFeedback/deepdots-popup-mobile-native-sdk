package com.deepdots.sdk.platform

import com.deepdots.sdk.renderer.PopupPresenter
import platform.UIKit.restorationIdentifier
import platform.UIKit.UIViewController

/**
 * Marca del view controller del popup (`restorationIdentifier`), el equivalente al `tag`
 * "DeepdotsPopupContainer" del contenedor en Android. La pone el renderer al presentarlo.
 */
internal const val DEEPDOTS_POPUP_VC_ID = "DeepdotsPopupContainer"

actual fun dismissPopup(context: PlatformContext) {
    // Un popup que esperaba a que el host cerrase su alerta tampoco debe aparecer después.
    PopupPresenter.cancelPendingPresentation()
    // Solo el popup del SDK, nunca un VC del host: antes se cerraba cualquier
    // `presentedViewController`, así que un `close()` con un picker o un share sheet del host
    // abierto se lo llevaba por delante. Se recorre la cadena de presentados porque el popup no
    // tiene por qué ser el primero.
    var current: UIViewController? = context.viewController.presentedViewController
    while (current != null) {
        if (current.restorationIdentifier == DEEPDOTS_POPUP_VC_ID) {
            current.dismissViewControllerAnimated(true, null)
            return
        }
        current = current.presentedViewController
    }
}

actual fun isPopupShowing(context: PlatformContext): Boolean {
    if (PopupPresenter.hasPendingPresentation()) return true
    // El que se está cerrando ya no cuenta: el siguiente popup puede salir detrás de él.
    var current: UIViewController? = context.viewController.presentedViewController
    while (current != null) {
        if (current.restorationIdentifier == DEEPDOTS_POPUP_VC_ID && !current.isBeingDismissed()) return true
        current = current.presentedViewController
    }
    return false
}
