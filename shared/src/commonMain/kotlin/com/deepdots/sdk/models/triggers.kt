package com.deepdots.sdk.models

enum class TriggerType {
    TimeOnPage,
    Scroll,
    Exit,
    Event,
    Click,
}

enum class TriggerConditionStatus {
    SHOWED,
    PARTIAL,
    COMPLETED,
}

data class CooldownCondition(
    val answered: TriggerConditionStatus,
    val cooldownDays: Int = 0,
)

data class LegacyCondition(
    val answered: Boolean? = null,
    val cooldownDays: Int = 0,
)

sealed class Trigger {
    abstract val type: TriggerType
    abstract val rawValue: String?

    data class TimeOnPage(
        val seconds: Double,
    ) : Trigger() {
        override val type: TriggerType = TriggerType.TimeOnPage
        override val rawValue: String = seconds.toString()
    }

    data class Scroll(
        val percentage: Int,
    ) : Trigger() {
        override val type: TriggerType = TriggerType.Scroll
        override val rawValue: String = percentage.toString()
    }

    data class Exit(
        val delaySeconds: Double = 0.0,
    ) : Trigger() {
        override val type: TriggerType = TriggerType.Exit
        override val rawValue: String = delaySeconds.toString()
    }

    data class Event(
        val name: String,
    ) : Trigger() {
        override val type: TriggerType = TriggerType.Event
        override val rawValue: String = name
    }

    data class Click(
        val targetId: String,
    ) : Trigger() {
        override val type: TriggerType = TriggerType.Click
        override val rawValue: String = targetId
    }
}

data class SurveyProgressState(
    val status: TriggerConditionStatus,
    val timestamp: Long,
)

typealias Condition = LegacyCondition
