package com.deepdots.sdk.ui

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage

@Composable
actual fun PlatformImage(
    url: String,
    modifier: Modifier,
    maxHeight: Dp,
    alignment: Alignment,
    contentDescription: String?
) {
    AsyncImage(
        model = url,
        contentDescription = contentDescription,
        contentScale = ContentScale.Fit,
        modifier = modifier
            .fillMaxWidth(0.9f)
            .heightIn(max = maxHeight)
            .clip(RoundedCornerShape(6.dp))
    )
}

