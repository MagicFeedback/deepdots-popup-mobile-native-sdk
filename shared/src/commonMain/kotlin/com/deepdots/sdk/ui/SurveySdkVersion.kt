package com.deepdots.sdk.ui

/**
 * Qué versión de `@magicfeedback/native` (el "survey SDK") pinta cada survey dentro de un popup
 * (DD-70). Espejo de `src/ui/surveySdkVersion.ts` del SDK web: mismas constantes, mismas razones.
 *
 * La API devuelve en cada popup `surveyFrontend.sdkVersion`: el survey SDK con el que se
 * construyó la versión de MagicSurvey asignada al survey en el Admin (latest o fijada). Si está
 * en [SUPPORTED_SURVEY_SDK_VERSIONS] y no es la vendorizada, el WebView carga su hoja y su JS del
 * CDN antes que la copia local; si cualquiera falla, la hoja y la JS de siempre.
 */

/**
 * Versiones de survey SDK que este SDK acepta cuando la API asigna otra: las que tienen su hoja
 * vendorizada en el popup-sdk fijado en [POPUP_SDK_CSS_VERSION]
 * (`dist/assets/assets/survey-css/<v>.css`, desde popup-sdk 1.19.0). JS y CSS van juntos, así que
 * una versión sin hoja no se acepta. Espejo de `SUPPORTED_SURVEY_SDK_VERSIONS` del SDK web; con
 * un popup-sdk anterior a 1.19.0 la hoja no existe y todo cae en la vendorizada.
 */
internal val SUPPORTED_SURVEY_SDK_VERSIONS: List<String> = listOf("2.2.38", "2.2.39")

internal enum class SurveySdkSource { ASSIGNED, BUNDLED }

internal enum class BundledReason(val wire: String) {
    NOT_ASSIGNED("not_assigned"),
    INVALID("invalid"),
    UNSUPPORTED("unsupported"),
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

    /**
     * Versión inválida: se avisa siempre, no solo con debug. No soportada NO: es lo normal para
     * surveys en versiones antiguas (prod latest 3.2.0 → 2.2.36) y saldría en cada apertura.
     */
    val isProblem: Boolean
        get() = source == SurveySdkSource.BUNDLED && reason == BundledReason.INVALID
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

internal fun isSupportedSurveySdk(version: String): Boolean = version in SUPPORTED_SURVEY_SDK_VERSIONS

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
    if (!isSupportedSurveySdk(requested)) return bundled(BundledReason.UNSUPPORTED)
    if (requested == MAGICFEEDBACK_VERSION) return bundled(BundledReason.SAME_AS_BUNDLED)
    return SurveySdkChoice(requested, SurveySdkSource.ASSIGNED, requested = requested)
}

/** URLs del bundle de navegador de una versión, en orden (jsDelivr, unpkg). */
internal fun surveySdkUrls(version: String): List<String> = listOf(
    "https://cdn.jsdelivr.net/npm/@magicfeedback/native@$version/dist/magicfeedback-sdk.browser.js",
    "https://unpkg.com/@magicfeedback/native@$version/dist/magicfeedback-sdk.browser.js",
)

/** Hoja vendorizada de una versión, del popup-sdk fijado en [POPUP_SDK_CSS_VERSION]. */
internal fun surveySdkCssUrls(version: String): List<String> = listOf(
    "https://cdn.jsdelivr.net/npm/@magicfeedback/popup-sdk@$POPUP_SDK_CSS_VERSION/dist/assets/assets/survey-css/$version.css",
    "https://unpkg.com/@magicfeedback/popup-sdk@$POPUP_SDK_CSS_VERSION/dist/assets/assets/survey-css/$version.css",
)

/**
 * Aviso del WebView con la versión que pinta el survey (`{version: "2.2.38 assigned"}`). El SDK lo
 * escribe en el log nativo ([surveySdkLogLine]); el gate de release (test-apps/scripts/run-maestro.sh)
 * lo busca ahí. En iOS los console.log del WebView no llegan al log del sistema; el println sí.
 */
internal const val SURVEY_SDK_EVENT: String = "survey_sdk"

internal fun surveySdkLogLine(versionAndSource: String): String = "[MagicFeedback] survey-sdk $versionAndSource"

/**
 * Escribe [surveySdkLogLine] donde lo lee el gate de release: logcat en Android (println) y el log
 * unificado del sistema en iOS (NSLog; el println de Kotlin/Native solo va a stdout).
 */
internal expect fun logSurveySdk(line: String)
