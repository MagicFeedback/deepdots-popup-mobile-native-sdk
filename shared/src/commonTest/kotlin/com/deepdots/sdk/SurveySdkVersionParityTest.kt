package com.deepdots.sdk

import com.deepdots.sdk.ui.BundledReason
import com.deepdots.sdk.ui.MAGICFEEDBACK_VERSION
import com.deepdots.sdk.ui.POPUP_SDK_CSS_VERSION
import com.deepdots.sdk.ui.SUPPORTED_SURVEY_SDK_VERSIONS
import com.deepdots.sdk.ui.SurveySdkSource
import com.deepdots.sdk.ui.buildMagicFeedbackHtml
import com.deepdots.sdk.ui.chooseSurveySdkVersion
import com.deepdots.sdk.ui.isSupportedSurveySdk
import com.deepdots.sdk.ui.surveySdkCssUrls
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
    fun supported_list_is_the_same_as_web_and_contains_the_vendored_version() {
        assertEquals(listOf("2.2.38", "2.2.39"), SUPPORTED_SURVEY_SDK_VERSIONS)
        assertTrue(isSupportedSurveySdk(MAGICFEEDBACK_VERSION))
    }

    @Test
    fun only_listed_versions_are_supported() {
        assertFalse(isSupportedSurveySdk("2.2.36"))
        assertFalse(isSupportedSurveySdk("2.2.37"))
        assertTrue(isSupportedSurveySdk("2.2.38"))
        assertFalse(isSupportedSurveySdk("2.2.40"))
        assertFalse(isSupportedSurveySdk("2.3.0"))
    }

    @Test
    fun css_of_a_version_comes_from_the_pinned_popup_sdk() {
        assertEquals(
            "https://cdn.jsdelivr.net/npm/@magicfeedback/popup-sdk@$POPUP_SDK_CSS_VERSION/dist/assets/assets/survey-css/2.2.38.css",
            surveySdkCssUrls("2.2.38")[0],
        )
    }

    @Test
    fun assigned_and_supported() {
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
            "2.2.36" to BundledReason.UNSUPPORTED,
            "2.3.0" to BundledReason.UNSUPPORTED,
            MAGICFEEDBACK_VERSION to BundledReason.SAME_AS_BUNDLED,
        )
        for ((requested, reason) in cases) {
            val c = chooseSurveySdkVersion(requested)
            assertEquals(SurveySdkSource.BUNDLED, c.source, "$requested")
            assertEquals(MAGICFEEDBACK_VERSION, c.version, "$requested")
            assertEquals(reason, c.reason, "$requested")
        }
        assertEquals(
            "survey SDK $MAGICFEEDBACK_VERSION (bundled: unsupported, requested 2.3.1)",
            chooseSurveySdkVersion("2.3.1").describe(),
        )
        assertFalse(chooseSurveySdkVersion("2.3.1").isProblem)
        assertFalse(chooseSurveySdkVersion("2.2.36").isProblem)
        assertTrue(chooseSurveySdkVersion("latest").isProblem)
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
        val css = "var ASSIGNED_CSS = ['${surveySdkCssUrls("2.2.38")[0]}','${surveySdkCssUrls("2.2.38")[1]}'];"
        assertTrue(h.contains(css), "assigned css")
        // La hoja primero; la JS asignada solo después de ponerla.
        assertTrue(h.contains("if(ASSIGNED_SRCS.length){ fetchAssignedCss(0); } else { useBundled(''); }"))
        assertTrue(h.contains("<link id=\"dd-survey-css\""))
        // La cadena de siempre (copia local + CDN de la vendorizada) sigue de respaldo.
        assertTrue(h.contains("var LOCAL_SRC = 'file:///android_asset/magicfeedback/magicfeedback-sdk.browser.js';"))
        assertTrue(h.contains(surveySdkUrls(MAGICFEEDBACK_VERSION)[0]))
        assertTrue(h.contains("markSurveySdk('2.2.38 assigned')"))
        assertTrue(h.contains("tryAssigned(0);"))
    }

    @Test
    fun html_without_assignment_or_unsupported_keeps_the_vendored_chain() {
        for (v in listOf(null, "2.3.0", MAGICFEEDBACK_VERSION)) {
            val h = html(v)
            assertTrue(h.contains("var ASSIGNED_SRCS = [];"), "$v")
            assertTrue(h.contains("var ASSIGNED_CSS = [];"), "$v")
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
