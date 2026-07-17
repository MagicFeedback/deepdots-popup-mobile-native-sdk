package com.deepdots.sdk.ui

import androidx.compose.ui.text.font.FontFamily

/**
 * Convierte los bytes de una fuente descargada en un [FontFamily] de Compose.
 * Devuelve null si el formato no es válido o la construcción falla (el caller
 * cae entonces a la fuente por defecto). El paso equivalente en Web es que el
 * navegador cargue el @font-face; aquí hay que materializar los bytes por plataforma.
 */
expect fun fontFamilyFromBytes(family: String, bytes: ByteArray): FontFamily?
