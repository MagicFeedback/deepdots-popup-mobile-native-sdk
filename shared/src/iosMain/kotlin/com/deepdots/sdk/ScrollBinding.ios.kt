package com.deepdots.sdk

/**
 * Vincula un UIScrollView y envía el progreso de scroll (0..100) al SDK.
 * Devuelve una lambda para desuscribir el delegate.
 */
expect fun DeepdotsPopupsSdk.bindScrollView(scrollView: Any): () -> Unit
