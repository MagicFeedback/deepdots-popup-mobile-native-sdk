package com.deepdots.sdk.analytics

import java.util.Locale

actual fun platformLanguage(): String? =
    runCatching { Locale.getDefault().toLanguageTag().takeIf { it.isNotBlank() && it != "und" } }.getOrNull()
