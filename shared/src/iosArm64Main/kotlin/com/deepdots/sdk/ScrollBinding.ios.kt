package com.deepdots.sdk

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.useContents
import kotlin.math.max
import kotlin.math.roundToInt
import platform.CoreGraphics.*
import platform.darwin.NSObject
import platform.UIKit.UIScrollView
import platform.UIKit.UIScrollViewDelegateProtocol

@OptIn(ExperimentalForeignApi::class)
actual fun DeepdotsPopupsSdk.bindScrollView(scrollView: Any): () -> Unit {
    val uiScroll = scrollView as? UIScrollView ?: return { }
    var lastSent = -1
    val delegate = object : NSObject(), UIScrollViewDelegateProtocol {
        override fun scrollViewDidScroll(scrollView: UIScrollView) {
            val contentHeight = scrollView.contentSize.useContents { height }
            val visibleHeight = scrollView.bounds.useContents { size.height }
            val offsetY = scrollView.contentOffset.useContents { y }.coerceAtLeast(0.0)
            val range = max(contentHeight - visibleHeight, 1.0)
            val pct = ((offsetY / range) * 100.0).roundToInt().coerceIn(0, 100)
            if (pct != lastSent) {
                lastSent = pct
                onScroll(pct)
            }
        }
    }
    uiScroll.delegate = delegate
    delegate.scrollViewDidScroll(uiScroll)
    return {
        if (uiScroll.delegate === delegate) {
            uiScroll.delegate = null
        }
    }
}
