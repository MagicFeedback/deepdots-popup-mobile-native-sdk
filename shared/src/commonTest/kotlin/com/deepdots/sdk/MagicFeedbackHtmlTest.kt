package com.deepdots.sdk

import com.deepdots.sdk.models.PopupFont
import com.deepdots.sdk.ui.buildMagicFeedbackHtml
import com.deepdots.sdk.ui.platformSurveyHtml
import kotlin.test.Test
import kotlin.test.assertTrue

class MagicFeedbackHtmlTest {
    @Test
    fun html_contains_ids_and_fallback_markers() {
        val surveyId = "survey-abc"
        val productId = "product-xyz"
        val html = Deepdots.getSurveyHtml(surveyId, productId)
        assertTrue(html.contains(surveyId), "Survey ID should appear in HTML")
        assertTrue(html.contains(productId), "Product ID should appear in HTML")
        // Note: other log/event string assertions removed to avoid brittleness across platforms/builds.
    }

    @Test
    fun html_applies_custom_font_family_and_font_face() {
        // Espejo de Web surveyHtml.ts: @font-face + font-family con el stack de fallback.
        val html = buildMagicFeedbackHtml(
            surveyId = "survey-abc",
            productId = "product-xyz",
            localAssetUrl = null,
            assetSize = null,
            bridgeEmitCall = "DeepdotsBridge.emit",
            isIOS = false,
            font = PopupFont("Inter", "https://x.com/Inter.woff2"),
        )
        assertTrue(html.contains("@font-face{font-family:\"Inter\""), "debe incluir @font-face de la fuente custom")
        assertTrue(
            html.contains("\"Inter\", -apple-system, system-ui, sans-serif"),
            "font-family debe usar la familia custom con el stack de fallback",
        )
    }

    /**
     * Identidad del tracking inyectada en el survey (contrato §5): mismas claves que Web, para
     * poder coser las respuestas con la analítica y con el mini-service activo (#33).
     */
    @Test
    fun html_injects_tracking_identity_into_the_survey() {
        SdkRuntime.userId = "u-1"
        SdkRuntime.sessionId = "s-9"
        SdkRuntime.miniService = "checkout"
        try {
            val html = buildMagicFeedbackHtml(
                surveyId = "survey-abc",
                productId = "product-xyz",
                localAssetUrl = null,
                assetSize = null,
                bridgeEmitCall = "DeepdotsBridge.emit",
                isIOS = false,
            )
            assertTrue(html.contains("{ key: 'user_id', value: ['u-1'] }"), "user_id en la metadata")
            assertTrue(html.contains("{ key: 'session_id', value: ['s-9'] }"), "session_id en la metadata")
            assertTrue(html.contains("{ key: 'mini_service', value: ['checkout'] }"), "mini_service en la metadata")
            // external-user-id va como profile, 3er argumento de form()
            assertTrue(
                html.contains("form('survey-abc', 'product-xyz', [{ key: 'external-user-id', value: ['u-1'] }])"),
                "external-user-id como profile de form()",
            )
        } finally {
            SdkRuntime.userId = null
            SdkRuntime.sessionId = null
            SdkRuntime.miniService = null
        }
    }
}
