package com.deepdots.sdk

import com.deepdots.sdk.models.Theme
import com.deepdots.sdk.ui.buildMagicFeedbackHtml
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/** DD-240: the app's forced mode reaches the survey SDK (`mode`, DD-238) and the page scheme. */
class DarkModeHtmlTest {
    private fun html(mode: Theme?) = buildMagicFeedbackHtml(
        surveyId = "survey-abc",
        productId = "product-xyz",
        localAssetUrl = null,
        assetSize = null,
        bridgeEmitCall = "DeepdotsBridge.emit",
        isIOS = false,
        colorMode = mode,
    )

    @Test
    fun no_forced_mode_keeps_todays_html() {
        val h = html(null)
        assertFalse(h.contains("mode: '"), "sin modo forzado no se pasa `mode`: decide el Style")
        assertTrue(h.contains("<meta name='color-scheme' content='light'/>"))
        assertTrue(h.contains(":root{color-scheme:light;}"))
    }

    @Test
    fun dark_is_passed_to_the_survey_and_the_page() {
        val h = html(Theme.Dark)
        assertTrue(h.contains("mode: 'dark',"))
        assertTrue(h.contains("<meta name='color-scheme' content='dark'/>"))
        assertTrue(h.contains(":root{color-scheme:dark;}"))
    }

    @Test
    fun light_is_passed_too() {
        val h = html(Theme.Light)
        assertTrue(h.contains("mode: 'light',"))
        assertTrue(h.contains(":root{color-scheme:light;}"))
    }
}
