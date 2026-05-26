package com.deepdots.sdk.models

import com.deepdots.sdk.storage.InMemoryStorage
import com.deepdots.sdk.storage.KeyValueStorage

enum class Mode { Client, Server }

/**
 * Backend environment the SDK should talk to.
 *
 * Defaults to [Production] (https://api.deepdots.com). Set [Development] to point
 * the SDK at https://api-dev.deepdots.com. This is independent of [InitOptions.debug],
 * which only controls SDK log output.
 */
enum class Environment { Production, Development }

data class InitOptions(
    val debug: Boolean? = false, // controls SDK log verbosity only
    val environment: Environment? = Environment.Production, // controls backend base URL
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
