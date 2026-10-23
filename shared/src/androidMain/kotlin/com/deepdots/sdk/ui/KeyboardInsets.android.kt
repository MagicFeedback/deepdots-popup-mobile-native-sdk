package com.deepdots.sdk.ui

import androidx.compose.foundation.layout.imePadding
import androidx.compose.ui.Modifier

internal actual fun Modifier.avoidKeyboard(): Modifier = imePadding()
