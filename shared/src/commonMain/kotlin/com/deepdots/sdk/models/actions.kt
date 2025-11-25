package com.deepdots.sdk.models

import com.deepdots.sdk.models.Trigger

data class PopupDefinition(
    val id: String,
    val title: String,
    val message: String,
    val trigger: Trigger,
    val actions: Actions,
    val surveyId: String,
    val productId: String,
    val style: Style,
    val segments: Segments? = null // ahora opcional
)

data class Actions(
    val accept: Action.Accept,
    val decline: Action.Decline
)

sealed class Action {
    data class Accept(val label: String, val surveyId: String) : Action()
    data class Decline(val label: String, val cooldownDays: Int) : Action()
}

data class Style(
    val theme: Theme,
    val position: Position,
    val imageUrl: String? = null
)

enum class Theme {
    Light, Dark
}

enum class Position {
    TopLeft, TopRight, BottomLeft, BottomRight, Center
}

data class Segments(
    val lang: List<String> = emptyList(),
    val path: List<String> = emptyList()
)
