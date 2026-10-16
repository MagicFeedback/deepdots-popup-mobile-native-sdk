package com.deepdots.sdk.platform

import platform.UIKit.restorationIdentifier
import platform.UIKit.UIViewController

/**
 * Marca del view controller del popup (`restorationIdentifier`), el equivalente al `tag`
 * "DeepdotsPopupContainer" del contenedor en Android. La pone el renderer al presentarlo.
 */
internal const val DEEPDOTS_POPUP_VC_ID = "DeepdotsPopupContainer"

actual fun dismissPopup(context: PlatformContext) {
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
