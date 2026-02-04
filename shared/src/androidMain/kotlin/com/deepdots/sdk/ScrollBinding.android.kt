package com.deepdots.sdk

import android.view.View
import android.widget.ScrollView
import androidx.recyclerview.widget.RecyclerView
import kotlin.math.roundToInt

/**
 * Helpers para vincular scroll views nativos y notificar progreso al SDK sin que la app calcule porcentaje.
 * Devuelve una lambda para desuscribir el listener cuando la vista se destruya.
 */
fun DeepdotsPopupsSdk.bindRecyclerView(recyclerView: RecyclerView): () -> Unit {
    var lastSent = -1
    val listener = object : RecyclerView.OnScrollListener() {
        override fun onScrolled(rv: RecyclerView, dx: Int, dy: Int) {
            val extent = rv.computeVerticalScrollExtent()
            val range = rv.computeVerticalScrollRange()
            val offset = rv.computeVerticalScrollOffset()
            val denom = (range - extent).coerceAtLeast(1)
            val pct = ((offset.toFloat() / denom.toFloat()) * 100f).roundToInt().coerceIn(0, 100)
            if (pct != lastSent) {
                lastSent = pct
                onScroll(pct)
            }
        }
    }
    recyclerView.addOnScrollListener(listener)
    // Emit initial state
    listener.onScrolled(recyclerView, 0, 0)
    return { recyclerView.removeOnScrollListener(listener) }
}

fun DeepdotsPopupsSdk.bindScrollView(scrollView: ScrollView): () -> Unit {
    var lastSent = -1
    val listener = View.OnScrollChangeListener { _, _, scrollY, _, _ ->
        val child = scrollView.getChildAt(0) ?: return@OnScrollChangeListener
        val range = (child.measuredHeight - scrollView.height).coerceAtLeast(1)
        val pct = ((scrollY.toFloat() / range.toFloat()) * 100f).roundToInt().coerceIn(0, 100)
        if (pct != lastSent) {
            lastSent = pct
            onScroll(pct)
        }
    }
    scrollView.setOnScrollChangeListener(listener)
    // Emit initial state
    listener.onScrollChange(scrollView, 0, scrollView.scrollY, 0, 0)
    return { scrollView.setOnScrollChangeListener(null as View.OnScrollChangeListener?) }
}
