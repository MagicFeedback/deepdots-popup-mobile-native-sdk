package com.deepdots.sdk.ui

import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

/**
 * El HTML del survey carga PRIMERO el bundle vendorizado de `@magicfeedback/native` y solo va al
 * CDN ([MAGICFEEDBACK_VERSION]) si el local falla. En Android el local carga siempre, así que es
 * el que manda: llegó a quedarse en la 2.2.4 mientras el CDN apuntaba a la 2.2.31, y el rating
 * numérico salía con el texto de la escala metido en los chips ("1 = Meget dårlig").
 *
 * Al subir [MAGICFEEDBACK_VERSION]: `scripts/vendor_magicfeedback.sh <versión>`.
 */
class VendoredSurveyBundleTest {

    @Test
    fun androidAssetMatchesCdnVersion() {
        assertEquals(MAGICFEEDBACK_VERSION, bundleVersion("src/androidMain/assets/magicfeedback/magicfeedback-sdk.browser.js"))
    }

    @Test
    fun iosResourceMatchesCdnVersion() {
        assertEquals(MAGICFEEDBACK_VERSION, bundleVersion("src/iosMain/resources/magicfeedback/magicfeedback-sdk.browser.js"))
    }

    /** El bundle lleva su `package.json` dentro: `"version":"X.Y.Z"`. */
    private fun bundleVersion(path: String): String? {
        val file = File(path)
        assertNotNull(file.takeIf { it.exists() }, "No existe $path (cwd=${File(".").absolutePath})")
        return Regex("\"name\":\"@magicfeedback/native\",\"version\":\"([^\"]+)\"").find(file.readText())?.groupValues?.get(1)
            ?: Regex("\"version\":\"([^\"]+)\"").find(file.readText())?.groupValues?.get(1)
    }
}
