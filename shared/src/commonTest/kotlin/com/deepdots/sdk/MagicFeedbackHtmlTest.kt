package com.deepdots.sdk

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
        // Check for local asset attempt marker
        assertTrue(html.contains("trying local asset"), "Should contain local asset attempt log string")
        // Check for CDN fallback markers
        assertTrue(html.contains("jsDelivr"), "Should reference jsDelivr fallback")
        assertTrue(html.contains("unpkg"), "Should reference unpkg fallback")
        // Check for timeout emission
        assertTrue(html.contains("error:timeout"), "Should emit timeout error event")
    }
}

