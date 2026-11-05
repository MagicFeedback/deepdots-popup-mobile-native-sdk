package com.deepdots.sdk.ui

/**
 * Qué versión de `@magicfeedback/native` (el "survey SDK") pinta cada survey dentro de un popup
 * (DD-70). Espejo de `src/ui/surveySdkVersion.ts` del SDK web: mismas constantes, mismas razones.
 *
 * La API devuelve en cada popup `surveyFrontend.sdkVersion`: el survey SDK con el que se
 * construyó la versión de MagicSurvey asignada al survey en el Admin (latest o fijada). Si está
 * en [SUPPORTED_SURVEY_SDK_MIN]..<[SUPPORTED_SURVEY_SDK_BELOW] y no es la vendorizada, el WebView
 * la carga del CDN (jsDelivr, unpkg) antes que la copia local; si no, la de siempre.
 */

/**
 * Mínimo soportado (incluido): la versión más antigua con la que pasan las test apps de este SDK.
 * 2.2.37 es la que llevaba el popup web 1.18.0; los eventos y códigos de error que lee el HTML
 * (`onLoadedEvent` con `error`, `SubmitError`, `onBackEvent`) ya están en ella.
 */
internal const val SUPPORTED_SURVEY_SDK_MIN: String = "2.2.37"

/**
 * Tope (excluido): la minor siguiente. El CSS del survey no se descarga con la versión asignada,
 * va pineado al del popup-sdk ([POPUP_SDK_CSS_VERSION]), así que solo se aceptan parches de la
 * minor de [MAGICFEEDBACK_VERSION]. Una 2.3.0 no llega a este SDK hasta un release que la declare.
 */
internal const val SUPPORTED_SURVEY_SDK_BELOW: String = "2.3.0"

internal enum class SurveySdkSource { ASSIGNED, BUNDLED }

internal enum class BundledReason(val wire: String) {
    NOT_ASSIGNED("not_assigned"),
    INVALID("invalid"),
    OUT_OF_RANGE("out_of_range"),
    SAME_AS_BUNDLED("same_as_bundled"),
}

internal data class SurveySdkChoice(
    val version: String,
    val source: SurveySdkSource,
    val reason: BundledReason? = null,
    val requested: String? = null,
) {
    /** Texto de log común con Web, p. ej. `survey SDK 2.2.38 (assigned)`. */
    fun describe(): String = when (source) {
        SurveySdkSource.ASSIGNED -> "survey SDK $version (assigned)"
        SurveySdkSource.BUNDLED -> "survey SDK $version (bundled: ${reason?.wire}" +
            (requested?.let { ", requested $it" } ?: "") + ")"
    }

    /** Pidió una versión y no se puede usar: se avisa siempre, no solo con debug. */
    val isProblem: Boolean
        get() = source == SurveySdkSource.BUNDLED &&
            (reason == BundledReason.INVALID || reason == BundledReason.OUT_OF_RANGE)
}

private val SEMVER = Regex("^(0|[1-9]\\d*)\\.(0|[1-9]\\d*)\\.(0|[1-9]\\d*)$")

internal fun isSemver(value: String?): Boolean = value != null && SEMVER.matches(value)

/** Negativo si [a] es anterior a [b]. Ambas SemVer. */
internal fun compareSemver(a: String, b: String): Int {
    val pa = a.split('.').map { it.toInt() }
    val pb = b.split('.').map { it.toInt() }
    for (i in 0..2) if (pa[i] != pb[i]) return pa[i] - pb[i]
    return 0
}

internal fun isSupportedSurveySdk(version: String): Boolean =
    isSemver(version) &&
        compareSemver(version, SUPPORTED_SURVEY_SDK_MIN) >= 0 &&
        compareSemver(version, SUPPORTED_SURVEY_SDK_BELOW) < 0

/** Decide la versión a cargar a partir de `surveyFrontend.sdkVersion` de la API. */
internal fun chooseSurveySdkVersion(requested: String?): SurveySdkChoice {
    fun bundled(reason: BundledReason) = SurveySdkChoice(
        version = MAGICFEEDBACK_VERSION,
        source = SurveySdkSource.BUNDLED,
        reason = reason,
        requested = requested?.takeIf { it.isNotEmpty() },
    )
    if (requested.isNullOrEmpty()) return bundled(BundledReason.NOT_ASSIGNED)
    if (!isSemver(requested)) return bundled(BundledReason.INVALID)
    if (!isSupportedSurveySdk(requested)) return bundled(BundledReason.OUT_OF_RANGE)
    if (requested == MAGICFEEDBACK_VERSION) return bundled(BundledReason.SAME_AS_BUNDLED)
    return SurveySdkChoice(requested, SurveySdkSource.ASSIGNED, requested = requested)
}

/** URLs del bundle de navegador de una versión, en orden (jsDelivr, unpkg). */
internal fun surveySdkUrls(version: String): List<String> = listOf(
    "https://cdn.jsdelivr.net/npm/@magicfeedback/native@$version/dist/magicfeedback-sdk.browser.js",
    "https://unpkg.com/@magicfeedback/native@$version/dist/magicfeedback-sdk.browser.js",
)
