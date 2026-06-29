package com.deepdots.sdk.analytics

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.staticCFunction
import platform.Foundation.NSException
import platform.Foundation.NSSetUncaughtExceptionHandler

// staticCFunction no puede capturar estado local: el reporter y el flag de consentimiento
// viven en globals que el handler lee. (Las excepciones Obj-C/NSException son "managed";
// los crashes por señal (SIGSEGV/SIGABRT) requieren PLCrashReporter — plan posterior.)
private var iosCrashReporter: CrashReporter? = null
private var iosCrashEnabled: (() -> Boolean)? = null

@OptIn(ExperimentalForeignApi::class)
actual fun installCrashHandlers(reporter: CrashReporter, enabled: () -> Boolean) {
    iosCrashReporter = reporter
    iosCrashEnabled = enabled
    NSSetUncaughtExceptionHandler(
        staticCFunction { exception: NSException? ->
            val r = iosCrashReporter ?: return@staticCFunction
            if (iosCrashEnabled?.invoke() != true) return@staticCFunction
            val name = exception?.name ?: "NSException"
            val reason = exception?.reason ?: ""
            val stack = exception?.callStackSymbols?.joinToString("\n") { it.toString() } ?: ""
            r.captureUnhandled(crashType = name, message = reason, stack = stack)
        },
    )
}
