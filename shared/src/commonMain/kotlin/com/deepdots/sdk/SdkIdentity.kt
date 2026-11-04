package com.deepdots.sdk

/**
 * Identidad del SDK que acompaña a los datos que produce, para que el dashboard de Collection
 * sepa por qué SDK y versión llegó cada feedback (paridad con Web `src/version.ts`):
 *  - respuestas de popup: `deepdots_channel=popup` + `deepdots_sdk` (+ `deepdots_sdk_core`)
 *  - cada lote de analítica: `deepdots_sdk` (+ `deepdots_sdk_core`)
 *
 * La versión sale de `PUBLISHING_VERSION` (gradle.properties) vía el `SdkBuildInfo` que genera
 * el build; nunca se mantiene a mano. Un wrapper (el plugin de Flutter) se declara con
 * [setWrapper] antes de `init`: `deepdots_sdk` pasa a ser el suyo y el nativo va en
 * `deepdots_sdk_core`.
 */
object SdkIdentity {
    /** Valor de `deepdots_channel` en las respuestas que llegan desde un popup. */
    const val POPUP_CHANNEL: String = "popup"

    /** Versión publicada del SDK nativo (Maven / SPM). */
    val version: String = SdkBuildInfo.VERSION

    private var wrapper: String? = null

    /**
     * Declara el SDK que envuelve al nativo (p. ej. `setWrapper("flutter", "0.1.0")`). Un
     * nombre o versión en blanco quita el wrapper.
     */
    fun setWrapper(name: String, version: String) {
        wrapper = if (name.isBlank() || version.isBlank()) null else "${name.trim()}@${version.trim()}"
    }

    /** Quita el wrapper: `deepdots_sdk` vuelve a ser el nativo. */
    fun clearWrapper() {
        wrapper = null
    }

    /** `android@<versión>` o `ios@<versión>`: el núcleo nativo, con o sin wrapper. */
    val core: String get() = "${platformName()}@$version"

    /** Valor de `deepdots_sdk`: el wrapper si lo hay, si no el núcleo nativo. */
    val sdk: String get() = wrapper ?: core

    /** Valor de `deepdots_sdk_core`: solo con wrapper (sin él coincidiría con `deepdots_sdk`). */
    val sdkCore: String? get() = if (wrapper != null) core else null

    internal fun platformName(): String =
        if (getPlatform().name.startsWith("iOS", ignoreCase = true)) "ios" else "android"
}
