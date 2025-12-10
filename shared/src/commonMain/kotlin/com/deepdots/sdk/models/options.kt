package com.deepdots.sdk.models

import com.deepdots.sdk.storage.InMemoryStorage
import com.deepdots.sdk.storage.KeyValueStorage

enum class Mode { Client, Server }

data class InitOptions(
    val debug: Boolean? = false,
    val mode: Mode? = Mode.Client,
    val popupOptions: PopupOptions = PopupOptions(),
    val provideLang: () -> String? = { null }, // lambda para resolver idioma actual
    val autoLaunch: Boolean? = false, // si true inicia triggers automáticos tras init
    val storage: KeyValueStorage? = InMemoryStorage(), // nuevo para cooldowns
    val metadata: Map<String, Any>? = null // datos adicionales para el SDK
)

data class ShowOptions(
    val surveyId: String,
    val productId: String,
    val data: Map<String, Any>? = null
)
