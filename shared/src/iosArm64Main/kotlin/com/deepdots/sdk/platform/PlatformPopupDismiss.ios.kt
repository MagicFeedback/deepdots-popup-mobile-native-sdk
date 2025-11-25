package com.deepdots.sdk.platform

import platform.UIKit.UIViewController

actual fun dismissPopup(context: PlatformContext) {
    // Intenta hacer dismiss si hay un VC presentado
    context.viewController.presentedViewController?.let { presented ->
        presented.dismissViewControllerAnimated(true, null)
    }
}

