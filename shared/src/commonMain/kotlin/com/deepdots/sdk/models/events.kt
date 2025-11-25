package com.deepdots.sdk.models

import com.deepdots.sdk.util.currentTimeMillis

enum class Event {
    PopupShown, // "popup_shown"
    PopupClicked, // "popup_clicked"
    SurveyCompleted // "survey_completed"
}

// Map enum to TypeScript-style string value
fun Event.code(): String = when (this) {
    Event.PopupShown -> "popup_shown"
    Event.PopupClicked -> "popup_clicked"
    Event.SurveyCompleted -> "survey_completed"
}

// Nuevo modelo según TAREA 2
// EventData(popupId, surveyId, productId, extra)
// Se agrega timestamp interno para facilitar depuración aunque no fue explícito (puede removerse luego)
data class EventData(
    val popupId: String,
    val surveyId: String,
    val productId: String,
    val extra: Map<String, Any?> = emptyMap(),
    val timestamp: Long = currentTimeMillis()
)
