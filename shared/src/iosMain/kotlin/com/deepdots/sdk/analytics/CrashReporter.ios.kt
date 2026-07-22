package com.deepdots.sdk.analytics

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.staticCFunction
import platform.Foundation.NSException
import platform.Foundation.NSSetUncaughtExceptionHandler

// staticCFunction no puede capturar estado local: el reporter y el flag de consentimiento
// viven en globals que el handler lee.
//
// LIMITACIÓN CONOCIDA — no encadena al handler previo: la versión actual de Kotlin/Native no
// permite invocar de forma fiable el CPointer<CFunction<…>> que devuelve
// NSGetUncaughtExceptionHandler(), así que no reinvocamos un handler previo (p. ej. Crashlytics).
// Si el host usa otro crash reporter de NSException en iOS, debe instalar el SDK de Deepdots
// PRIMERO (o gestionar la coexistencia manualmente). La captura nativa por señal (PLCrashReporter)
// del plan posterior cubre esto de forma robusta. NOTA: Android SÍ encadena al handler previo.
//
// Solo captura NSException (Obj-C/Swift "managed"); los crashes por señal (SIGSEGV/SIGABRT)
// no pasan por aquí — los cubre el plan nativo.
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
