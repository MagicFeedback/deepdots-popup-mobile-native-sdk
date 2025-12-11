package com.deepdots.sdk.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp

// Expect/Actual composable to load network image per platform.
// Android actual will use Coil; iOS actual will render a simple placeholder for now.
@Composable
expect fun PlatformImage(
    url: String,
    modifier: Modifier,
    maxHeight: Dp,
    alignment: Alignment,
    contentDescription: String?
)
