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

/**
 * Trigger de un popup. `delaySeconds` es el "Delay (seconds)" del Admin: lo que se espera desde
 * que el trigger salta hasta mostrar el popup (0 = al momento). Cada tipo conserva su constructor
 * sin `delaySeconds` para no romper a quien ya los construye (Swift no ve los valores por defecto
 * de Kotlin).
 */
sealed class Trigger {
    abstract val type: TriggerType
    abstract val rawValue: String?
    abstract val delaySeconds: Double

    data class TimeOnPage(
        val seconds: Double,
        override val delaySeconds: Double,
    ) : Trigger() {
        constructor(seconds: Double) : this(seconds, 0.0)

        override val type: TriggerType = TriggerType.TimeOnPage
        override val rawValue: String = seconds.toString()
    }

    data class Scroll(
        val percentage: Int,
        override val delaySeconds: Double,
    ) : Trigger() {
        constructor(percentage: Int) : this(percentage, 0.0)

        override val type: TriggerType = TriggerType.Scroll
        override val rawValue: String = percentage.toString()
    }

    data class Exit(
        override val delaySeconds: Double = 0.0,
    ) : Trigger() {
        override val type: TriggerType = TriggerType.Exit
        override val rawValue: String = delaySeconds.toString()
    }

    data class Event(
        val name: String,
        override val delaySeconds: Double,
    ) : Trigger() {
        constructor(name: String) : this(name, 0.0)

        override val type: TriggerType = TriggerType.Event
        override val rawValue: String = name
    }

    data class Click(
        val targetId: String,
        override val delaySeconds: Double,
    ) : Trigger() {
        constructor(targetId: String) : this(targetId, 0.0)

        override val type: TriggerType = TriggerType.Click
        override val rawValue: String = targetId
    }
}

data class SurveyProgressState(
    val status: TriggerConditionStatus,
    val timestamp: Long,
)

typealias Condition = LegacyCondition
