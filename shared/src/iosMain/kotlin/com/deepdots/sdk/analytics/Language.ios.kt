package com.deepdots.sdk.analytics

import platform.Foundation.NSLocale
import platform.Foundation.preferredLanguages

actual fun platformLanguage(): String? = runCatching {
    // preferredLanguages ya viene en BCP-47 ("es-ES"), igual que navigator.language en Web.
    (NSLocale.preferredLanguages.firstOrNull() as? String)?.takeIf { it.isNotBlank() }
}.getOrNull()
