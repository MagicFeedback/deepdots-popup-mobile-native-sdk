package com.deepdots.sdk.ui

import androidx.compose.ui.unit.LayoutDirection
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Dirección de escritura del chrome del popup. En Web/RN esto es un atributo `dir` en el
 * contenedor; aquí el chrome es Compose, así que se traduce a `LayoutDirection` y se inyecta
 * con `LocalLayoutDirection`. La regla de decisión es la misma en las tres rutas.
 */
class PopupDirectionTest {

    @Test
    fun arabicMapsToRtl() {
        assertEquals(LayoutDirection.Rtl, popupLayoutDirection("ar"))
        assertEquals(LayoutDirection.Rtl, popupLayoutDirection("ar-EG"))
    }

    @Test
    fun everyOtherLanguageMapsToLtr() {
        assertEquals(LayoutDirection.Ltr, popupLayoutDirection("da"))
        assertEquals(LayoutDirection.Ltr, popupLayoutDirection("en-GB"))
        assertEquals(LayoutDirection.Ltr, popupLayoutDirection("zh-CN"))
        assertEquals(LayoutDirection.Ltr, popupLayoutDirection(null))
        assertEquals(LayoutDirection.Ltr, popupLayoutDirection("is-IS"))
    }
}
