package com.deepdots.sdk.models

data class PopupOptions(
    val id: String? = null,
    val publicKey: String? = null,
    val popups: List<PopupDefinition>? = emptyList(),
    val companyId: String? = null,
)

data class PopupDefinition(
    val id: String,
    val title: String = "",
    val message: String = "",
    val trigger: Trigger,
    val conditions: List<Condition>? = emptyList(),
    val actions: Actions,
    val surveyId: String,
    val productId: String,
    val style: Style,
    val segments: Segments? = null // ahora opcional
)

data class Actions(
    val accept: Action.Accept? = null,
    val decline: Action.Decline? = null,
    val start: Action.Start? = null,
    val complete: Action.Complete? = null,
    val back: Action.Back? = null
)

sealed class Action {
    data class Accept(val label: String, val surveyId: String) : Action()
    data class Decline(val label: String, val cooldownDays: Int) : Action()

    data class Start(val label: String) : Action()

    data class Complete(val label: String) : Action()

    data class Back(val label: String) : Action()
}

data class Style(
    val theme: Theme = Theme.Light,
    val position: Position = Position.Center,
    val imageUrl: String? = null,
    val imageSize: ImageSize = ImageSize.Medium,
    val imageAlignment: ImageAlignment = ImageAlignment.Center
)

enum class Theme {
    Light, Dark
}

enum class Position {
    TopLeft, TopRight, BottomLeft, BottomRight, Center
}

enum class ImageSize {
    Small, Medium, Large
}

enum class ImageAlignment {
    Center, Left, Right
}

data class Segments(
    val lang: List<String> = emptyList(),
    val path: List<String> = emptyList()
)
