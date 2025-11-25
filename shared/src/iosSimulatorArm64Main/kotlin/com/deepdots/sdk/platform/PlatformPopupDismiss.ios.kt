package com.deepdots.sdk.platform

actual fun dismissPopup(context: PlatformContext) {
    context.viewController.presentedViewController?.dismissViewControllerAnimated(true, null)
}

