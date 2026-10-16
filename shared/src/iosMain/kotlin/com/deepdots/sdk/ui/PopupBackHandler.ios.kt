package com.deepdots.sdk.ui

import androidx.compose.runtime.Composable

/**
 * iOS no tiene botón Back de sistema y el popup se presenta como modal a pantalla completa
 * (sin gesto de "swipe back" de navegación), así que no hay nada que interceptar: se cierra con
 * la X, igual que hasta ahora.
 */
@Composable
internal actual fun PopupBackHandler(enabled: Boolean, onBack: () -> Unit) = Unit
