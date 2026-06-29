package com.deepdots.sdk.analytics

/**
 * Captura de crashes GESTIONADOS en Android: uncaught exceptions de la JVM.
 * Encadena SIEMPRE al handler previo (no romper Crashlytics/Sentry del host).
 * Los crashes nativos (NDK/señales) se cubren en un plan posterior (xCrash).
 */
actual fun installCrashHandlers(reporter: CrashReporter, enabled: () -> Boolean) {
    val previous = Thread.getDefaultUncaughtExceptionHandler()
    Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
        try {
            if (enabled()) {
                reporter.captureUnhandled(
                    crashType = throwable::class.simpleName ?: "Error",
                    message = throwable.message ?: "",
                    stack = throwable.stackTraceToString(),
                )
            }
        } catch (_: Throwable) {
            /* nunca interferir con la terminación del proceso */
        }
        previous?.uncaughtException(thread, throwable)
    }
}
