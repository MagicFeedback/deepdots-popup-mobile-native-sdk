package com.deepdots.sdk.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import org.jetbrains.skia.Image as SkiaImage
import androidx.compose.ui.graphics.toComposeImageBitmap
import platform.Foundation.*
import platform.darwin.dispatch_async
import platform.darwin.dispatch_get_main_queue

@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun PlatformImage(
    url: String,
    modifier: Modifier,
    maxHeight: Dp,
    alignment: Alignment,
    contentDescription: String?
) {
    var bitmap by remember(url) { mutableStateOf<androidx.compose.ui.graphics.ImageBitmap?>(null) }

    LaunchedEffect(url) {
        val nsUrl = NSURL(string = url)
        if (nsUrl == null) {
            NSLog("[Deepdots] Invalid URL: %s", url)
            bitmap = null
            return@LaunchedEffect
        }
        val session = NSURLSession.sharedSession
        session.dataTaskWithURL(nsUrl) { data, response, error ->
            if (error != null) {
                NSLog("[Deepdots] Image load error: %s", error.localizedDescription)
                bitmap = null
                return@dataTaskWithURL
            }
            val http = response as? NSHTTPURLResponse
            val status = http?.statusCode?.toInt() ?: -1
            if (status in 200..299 && data != null) {
                // Convert NSData to Skia Image, then to Compose ImageBitmap
                val bytes = data.toByteArray()
                val skia = runCatching { SkiaImage.makeFromEncoded(bytes) }.getOrNull()
                val img = skia?.toComposeImageBitmap()
                dispatch_async(dispatch_get_main_queue()) { bitmap = img }
            } else {
                NSLog("[Deepdots] HTTP status %d for url=%s", status, url)
                dispatch_async(dispatch_get_main_queue()) { bitmap = null }
            }
        }.resume()
    }

    if (bitmap != null) {
        Image(
            bitmap!!,
            contentDescription = contentDescription,
            modifier = modifier
                .fillMaxWidth()
                .heightIn(max = maxHeight),
            contentScale = ContentScale.Fit,
            alignment = alignment
        )
    }
}

@OptIn(ExperimentalForeignApi::class)
private fun NSData.toByteArray(): ByteArray {
    val length = this.length.toInt()
    val bytes = ByteArray(length)
    bytes.usePinned { pinned ->
        // Call getBytes on NSData directly
        this.getBytes(pinned.addressOf(0), length.toULong())
    }
    return bytes
}
