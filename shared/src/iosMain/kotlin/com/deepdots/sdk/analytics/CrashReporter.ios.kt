package com.deepdots.sdk.analytics

import kotlinx.cinterop.CFunction
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.reinterpret
import kotlinx.cinterop.staticCFunction
import platform.Foundation.NSException
import platform.Foundation.NSGetUncaughtExceptionHandler
import platform.Foundation.NSSetUncaughtExceptionHandler

// staticCFunction no puede capturar estado local: el reporter, el flag de consentimiento y el
// handler previo viven en globals que el handler lee. (NSException = "managed"; los crashes por
// señal (SIGSEGV/SIGABRT) requieren PLCrashReporter — plan posterior.)
private var iosCrashReporter: CrashReporter? = null
private var iosCrashEnabled: (() -> Boolean)? = null

@OptIn(ExperimentalForeignApi::class)
private var iosPreviousHandler: CPointer<CFunction<(NSException?) -> Unit>>? = null

@OptIn(ExperimentalForeignApi::class)
actual fun installCrashHandlers(reporter: CrashReporter, enabled: () -> Boolean) {
    // Captura el handler previo ANTES de instalar el nuestro, reinterpretando el tipo Unit?→Unit.
    iosPreviousHandler = NSGetUncaughtExceptionHandler()?.reinterpret()
    iosCrashReporter = reporter
    iosCrashEnabled = enabled
    NSSetUncaughtExceptionHandler(
        staticCFunction { exception: NSException? ->
            val r = iosCrashReporter
            if (r != null && iosCrashEnabled?.invoke() == true) {
                val name = exception?.name ?: "NSException"
                val reason = exception?.reason ?: ""
                val stack = exception?.callStackSymbols?.joinToString("\n") { it.toString() } ?: ""
                r.captureUnhandled(crashType = name, message = reason, stack = stack)
            }
            // Encadena al handler previo (no romper un Crashlytics/handler del host).
            val prev: CPointer<CFunction<(NSException?) -> Unit>>? = iosPreviousHandler
            if (prev != null) {
                NSSetUncaughtExceptionHandler(prev)
            }
        },
    )
}
