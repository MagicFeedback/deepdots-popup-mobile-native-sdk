package com.deepdots.sdk.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

interface SurveyController {
    fun send()
    fun back()
    fun close()
    fun startForm()
}

/**
 * Cross-platform survey rendering surface that should embed MagicFeedback Native form.
 * Android actual implementation uses a WebView bridge. iOS currently shows a placeholder.
 * onEvent receives MagicFeedback lifecycle events like: popup_clicked, survey_completed
 *
 * [backgroundColor] is the popup's themed background. The survey WebView must paint this
 * as an opaque background: on iOS the Compose interop layer punches a transparent hole
 * where the WebView sits, so a transparent WebView would reveal the host view controller's
 * background (black in system dark mode) instead of the popup card. Painting the themed
 * color keeps the survey area consistent with the rest of the popup.
 */
@Composable
expect fun SurveyView(
    surveyId: String,
    productId: String,
    backgroundColor: Color,
    onEvent: (String) -> Unit,
    onController: (SurveyController) -> Unit = {}
)
