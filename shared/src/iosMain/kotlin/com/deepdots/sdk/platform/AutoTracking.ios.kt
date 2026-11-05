@file:OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)

package com.deepdots.sdk.platform

import com.deepdots.sdk.tracking.AutoSignals
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.CFunction
import kotlinx.cinterop.COpaquePointer
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.interpretObjCPointerOrNull
import kotlinx.cinterop.invoke
import kotlinx.cinterop.reinterpret
import kotlinx.cinterop.staticCFunction
import platform.Foundation.NSBundle
import platform.Foundation.NSLog
import platform.Foundation.NSNotificationCenter
import platform.Foundation.NSOperationQueue
import platform.Foundation.NSStringFromClass
import platform.Foundation.NSThread
import platform.UIKit.UIApplication
import platform.UIKit.UIApplicationDidBecomeActiveNotification
import platform.UIKit.UIApplicationState
import platform.UIKit.UIApplicationDidEnterBackgroundNotification
import platform.UIKit.UIApplicationWillEnterForegroundNotification
import platform.UIKit.UIApplicationWillResignActiveNotification
import platform.UIKit.UINavigationController
import platform.UIKit.UIPageViewController
import platform.UIKit.UISplitViewController
import platform.UIKit.UITabBarController
import platform.UIKit.UIViewController
import platform.UIKit.UIWindow
import platform.UIKit.UIWindowScene
import platform.UIKit.restorationIdentifier
import platform.darwin.dispatch_async
import platform.darwin.dispatch_get_main_queue
import platform.objc.class_getInstanceMethod
import platform.objc.method_setImplementation
import platform.objc.sel_registerName
import com.deepdots.sdk.tracking.screenNameForClass

/** Mismo valor que `DEEPDOTS_POPUP_VC_ID` (renderer): el popup del SDK no es una pantalla. */
private const val SDK_POPUP_VC_ID = "DeepdotsPopupContainer"

private var contextInstalled = false
private var lifecycleInstalled = false
private var screensInstalled = false

/**
 * App activa (hay ventana en la que pintar): se lee del estado de la app al instalar y luego sigue
 * a `didBecomeActive` / `didEnterBackground`. Fuera de una app (binario de tests) siempre false.
 */
private var appActive = false

private fun isRunningInApp(): Boolean = NSBundle.mainBundle.bundlePath.endsWith(".app")

/** Observadores retenidos (el centro de notificaciones solo guarda referencias débiles al token). */
private val observers = mutableListOf<Any>()

private fun observe(name: String?, block: () -> Unit) {
    val token = NSNotificationCenter.defaultCenter.addObserverForName(
        name = name,
        `object` = null,
        queue = NSOperationQueue.mainQueue,
    ) { _ ->
        try {
            block()
        } catch (_: Throwable) {
        }
    }
    observers += token
}

internal actual fun installPlatformAutoTracking(lifecycle: Boolean, screens: Boolean) {
    if (!NSThread.isMainThread) {
        dispatch_async(dispatch_get_main_queue()) { installPlatformAutoTracking(lifecycle, screens) }
        return
    }
    try {
        if (!contextInstalled) {
            contextInstalled = true
            appActive = isRunningInApp() &&
                UIApplication.sharedApplication.applicationState == UIApplicationState.UIApplicationStateActive
            observe(UIApplicationDidBecomeActiveNotification) {
                appActive = true
                AutoSignals.contextAvailable()
            }
            observe(UIApplicationDidEnterBackgroundNotification) { appActive = false }
        }
        if (lifecycle && !lifecycleInstalled) {
            lifecycleInstalled = true
            observe(UIApplicationWillEnterForegroundNotification) { AutoSignals.foreground() }
            observe(UIApplicationDidEnterBackgroundNotification) { AutoSignals.background() }
            // `inactive` es transitorio (llamada, centro de control, selector de apps): solo flush.
            observe(UIApplicationWillResignActiveNotification) { AutoSignals.inactive() }
        }
        if (screens && !screensInstalled) {
            screensInstalled = true
            swizzleViewDidAppear()
        }
    } catch (t: Throwable) {
        NSLog("[DeepdotsPopups] Automatic detection unavailable: ${t.message}")
    }
}

internal actual fun hasDetectedPlatformContext(): Boolean = appActive

/**
 * Root view controller de la key window (el presentador sube desde ahí por la cadena de
 * presentados). Solo en el hilo principal.
 */
internal fun detectedRootViewController(): UIViewController? {
    if (!NSThread.isMainThread || !isRunningInApp()) return null
    val app = UIApplication.sharedApplication
    val sceneWindows = app.connectedScenes
        .mapNotNull { it as? UIWindowScene }
        .flatMap { scene -> scene.windows.mapNotNull { it as? UIWindow } }
    val window = sceneWindows.firstOrNull { it.isKeyWindow() }
        ?: @Suppress("DEPRECATION") app.keyWindow
        ?: sceneWindows.firstOrNull()
    return window?.rootViewController
}

// ───────── pantallas: viewDidAppear ─────────

private typealias ViewDidAppearImp = CFunction<(COpaquePointer?, COpaquePointer?, Boolean) -> Unit>

/** IMP original de `-[UIViewController viewDidAppear:]`, al que se llama siempre primero. */
private var originalViewDidAppear: CPointer<ViewDidAppearImp>? = null

/**
 * Sustituye `-[UIViewController viewDidAppear:]` por uno que llama al original y luego avisa de
 * la pantalla. Los controladores del host que lo sobrescriben llaman a `super`, así que también
 * pasan por aquí. Una vez por proceso.
 */
private fun swizzleViewDidAppear() {
    val cls = UIViewController.`class`() ?: return
    val method = class_getInstanceMethod(cls, sel_registerName("viewDidAppear:")) ?: return
    val replacement = staticCFunction { self: COpaquePointer?, cmd: COpaquePointer?, animated: Boolean ->
        originalViewDidAppear?.invoke(self, cmd, animated)
        try {
            val vc = self?.let { interpretObjCPointerOrNull<UIViewController>(it.rawValue) }
            if (vc != null) screenName(vc)?.let { AutoSignals.screen(it) }
        } catch (_: Throwable) {
        }
    }
    val previous = method_setImplementation(method, replacement.reinterpret())
    originalViewDidAppear = previous?.reinterpret()
}

private fun isContainer(vc: UIViewController?): Boolean =
    vc is UINavigationController || vc is UITabBarController ||
        vc is UIPageViewController || vc is UISplitViewController

/**
 * Nombre de pantalla de [vc], o null si no es una pantalla: contenedores (navegación, tabs…),
 * hijos de un contenedor propio, el popup del SDK y clases del sistema / genéricas (UIKit,
 * SwiftUI `UIHostingController`, el host de Compose: esas apps usan el helper).
 */
internal fun screenName(vc: UIViewController): String? {
    if (isContainer(vc)) return null
    val parent = vc.parentViewController
    if (parent != null && !isContainer(parent)) return null
    if (vc.restorationIdentifier == SDK_POPUP_VC_ID) return null
    return screenNameForClass(NSStringFromClass(vc.`class`() ?: return null))
}
