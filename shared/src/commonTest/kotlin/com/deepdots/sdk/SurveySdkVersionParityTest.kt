package com.deepdots.sdk

import com.deepdots.sdk.ui.BundledReason
import com.deepdots.sdk.ui.MAGICFEEDBACK_VERSION
import com.deepdots.sdk.ui.SUPPORTED_SURVEY_SDK_BELOW
import com.deepdots.sdk.ui.SUPPORTED_SURVEY_SDK_MIN
import com.deepdots.sdk.ui.SurveySdkSource
import com.deepdots.sdk.ui.buildMagicFeedbackHtml
import com.deepdots.sdk.ui.chooseSurveySdkVersion
import com.deepdots.sdk.ui.isSupportedSurveySdk
import com.deepdots.sdk.ui.surveySdkUrls
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * DD-70: qué survey SDK pinta el survey del popup. Espejo de `src/ui/surveySdkVersion.test.ts`
 * del SDK web: mismo rango, mismas razones, mismo texto de log.
 */
class SurveySdkVersionParityTest {

    @Test
    fun range_is_the_same_as_web_and_contains_the_vendored_version() {
        assertEquals("2.2.37", SUPPORTED_SURVEY_SDK_MIN)
        assertEquals("2.3.0", SUPPORTED_SURVEY_SDK_BELOW)
        assertTrue(isSupportedSurveySdk(MAGICFEEDBACK_VERSION))
    }

    @Test
    fun range_min_included_below_excluded() {
        assertFalse(isSupportedSurveySdk("2.2.36"))
        assertTrue(isSupportedSurveySdk("2.2.37"))
        assertTrue(isSupportedSurveySdk("2.2.99"))
        assertFalse(isSupportedSurveySdk("2.3.0"))
        assertFalse(isSupportedSurveySdk("3.0.0"))
    }

    @Test
    fun assigned_in_range() {
        val c = chooseSurveySdkVersion("2.2.38")
        assertEquals("2.2.38", c.version)
        assertEquals(SurveySdkSource.ASSIGNED, c.source)
        assertEquals("survey SDK 2.2.38 (assigned)", c.describe())
    }

    @Test
    fun bundled_reasons() {
        val cases = listOf(
            null to BundledReason.NOT_ASSIGNED,
            "" to BundledReason.NOT_ASSIGNED,
            "latest" to BundledReason.INVALID,
            "2.2" to BundledReason.INVALID,
            "2.2.36" to BundledReason.OUT_OF_RANGE,
            "2.3.0" to BundledReason.OUT_OF_RANGE,
            MAGICFEEDBACK_VERSION to BundledReason.SAME_AS_BUNDLED,
        )
        for ((requested, reason) in cases) {
            val c = chooseSurveySdkVersion(requested)
            assertEquals(SurveySdkSource.BUNDLED, c.source, "$requested")
            assertEquals(MAGICFEEDBACK_VERSION, c.version, "$requested")
            assertEquals(reason, c.reason, "$requested")
        }
        assertEquals(
            "survey SDK $MAGICFEEDBACK_VERSION (bundled: out_of_range, requested 2.3.1)",
            chooseSurveySdkVersion("2.3.1").describe(),
        )
        assertTrue(chooseSurveySdkVersion("2.3.1").isProblem)
        assertFalse(chooseSurveySdkVersion(null).isProblem)
    }

    private fun html(surveySdkVersion: String?, local: String? = "file:///android_asset/magicfeedback/magicfeedback-sdk.browser.js") =
        buildMagicFeedbackHtml(
            surveyId = "s",
            productId = "p",
            localAssetUrl = local,
            assetSize = null,
            bridgeEmitCall = "DeepdotsBridge.emit",
            isIOS = false,
            surveySdkVersion = surveySdkVersion,
        )

    @Test
    fun html_tries_the_assigned_version_first_then_the_usual_chain() {
        val h = html("2.2.38")
        val assigned = "var ASSIGNED_SRCS = ['${surveySdkUrls("2.2.38")[0]}','${surveySdkUrls("2.2.38")[1]}'];"
        assertTrue(h.contains(assigned), "assigned sources")
        // La cadena de siempre (copia local + CDN de la vendorizada) sigue de respaldo.
        assertTrue(h.contains("var LOCAL_SRC = 'file:///android_asset/magicfeedback/magicfeedback-sdk.browser.js';"))
        assertTrue(h.contains(surveySdkUrls(MAGICFEEDBACK_VERSION)[0]))
        assertTrue(h.contains("markSurveySdk('2.2.38 assigned')"))
        assertTrue(h.contains("tryAssigned(0);"))
    }

    @Test
    fun html_without_assignment_or_out_of_range_keeps_the_vendored_chain() {
        for (v in listOf(null, "2.3.0", MAGICFEEDBACK_VERSION)) {
            val h = html(v)
            assertTrue(h.contains("var ASSIGNED_SRCS = [];"), "$v")
            assertFalse(h.contains("@magicfeedback/native@2.3.0"), "$v")
        }
    }

    @Test
    fun api_surveyFrontend_sdkVersion_reaches_the_popup_definition() {
        val sdk = DeepdotsPopups()
        val payload = """
            [
              {"id":"p1","title":"t","message":"m","triggers":[{"type":"event","value":"E"}],
               "surveyId":"s1","productId":"pr",
               "surveyFrontend":{"version":"3.3.0","preference":"3.3.0","status":"stable","sdkVersion":"2.2.38"}},
              {"id":"p2","title":"t","message":"m","triggers":[{"type":"event","value":"E"}],
               "surveyId":"s2","productId":"pr","surveyFrontend":null},
              {"id":"p3","title":"t","message":"m","triggers":[{"type":"event","value":"E"}],
               "surveyId":"s3","productId":"pr"}
            ]
        """.trimIndent()
        val defs = sdk.debugParseServerPayload(payload).associateBy { it.id }
        assertEquals(3, defs.size)
        assertEquals("2.2.38", defs.getValue("p1").surveySdkVersion)
        assertNull(defs.getValue("p2").surveySdkVersion)
        assertNull(defs.getValue("p3").surveySdkVersion)
    }
}
