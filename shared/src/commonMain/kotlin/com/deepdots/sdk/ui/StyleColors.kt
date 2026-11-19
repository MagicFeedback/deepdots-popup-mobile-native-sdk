package com.deepdots.sdk.ui

/**
 * Colores hex del Style del survey en el payload del `loaded` (JSON de `formData.style`).
 * El bloque opcional `dark` (DD-239, mismas claves que el Style) se separa del resto: [light] lee
 * fuera de él y [dark] solo dentro, así la primera coincidencia nunca es la del modo equivocado.
 * Solo acepta `#rgb`, `#rrggbb` y `#aarrggbb`, como el parseo de siempre.
 */
internal class StyleColors private constructor(private val lightJson: String, private val darkJson: String?) {

    fun light(key: String): String? = find(lightJson, key)

    fun dark(key: String): String? = darkJson?.let { find(it, key) }

    companion object {
        private val DARK_BLOCK = Regex("\"dark\"\\s*:\\s*\\{([^{}]*)\\}")

        fun from(payload: String?): StyleColors {
            val json = payload ?: ""
            val block = DARK_BLOCK.find(json)
            return if (block == null) StyleColors(json, null)
            else StyleColors(json.removeRange(block.range), block.groupValues[1])
        }

        private fun find(json: String, key: String): String? =
            Regex("\"${Regex.escape(key)}\"\\s*:\\s*\"(#[0-9A-Fa-f]{3,8})\"").find(json)?.groupValues?.get(1)
    }
}
