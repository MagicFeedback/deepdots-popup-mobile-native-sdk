package com.deepdots.sdk.models

// Modelos de triggers del SDK
sealed class Trigger {
    data class TimeOnPage(
        val value: Int, // segundos
        val condition: List<Condition> = emptyList()
    ) : Trigger()
    data class Scroll(
        val percentage: Int,
        val condition: List<Condition> = emptyList()
    ) : Trigger()
    data class Exit(
        val condition: List<Condition> = emptyList()
    ) : Trigger()
}

// Condición asociada a un trigger
// answered: si el usuario ya contestó la encuesta
// cooldownDays: tiempo en días antes de volver a mostrar
data class Condition(
    val answered: Boolean = false,
    val cooldownDays: Int = 0
)
