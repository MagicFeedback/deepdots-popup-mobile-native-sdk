package com.deepdots.sdk.models

import com.deepdots.sdk.util.currentTimeMillis

enum class Event {
    PopupShown,
    PopupClicked,
    SurveyCompleted
}

fun Event.code(): String = when (this) {
    Event.PopupShown -> "popup_shown"
    Event.PopupClicked -> "popup_clicked"
    Event.SurveyCompleted -> "survey_completed"
}

data class EventData(
    val popupId: String,
    val surveyId: String,
    val productId: String,
    val extra: Map<String, Any?> = emptyMap(),
    val timestamp: Long = currentTimeMillis()
)
