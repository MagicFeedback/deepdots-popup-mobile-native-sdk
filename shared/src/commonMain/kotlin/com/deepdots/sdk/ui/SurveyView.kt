package com.deepdots.sdk.ui

import androidx.compose.runtime.Composable

interface SurveyController {
    fun send()
    fun back()
    fun close()
}

/**
 * Cross-platform survey rendering surface that should embed MagicFeedback Native form.
 * Android actual implementation uses a WebView bridge. iOS currently shows a placeholder.
 * onEvent receives MagicFeedback lifecycle events like: popup_clicked, survey_completed
 */
@Composable
expect fun SurveyView(
    surveyId: String,
    productId: String,
    onEvent: (String) -> Unit,
    onController: (SurveyController) -> Unit = {}
)
