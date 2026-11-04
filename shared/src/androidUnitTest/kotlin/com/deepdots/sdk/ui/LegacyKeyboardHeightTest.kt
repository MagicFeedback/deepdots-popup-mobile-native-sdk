package com.deepdots.sdk.ui

import kotlin.test.Test
import kotlin.test.assertEquals

class LegacyKeyboardHeightTest {
    @Test
    fun keyboardOpenReturnsTheHiddenHeight() {
        // Pixel 6 (2400 px): teclado + barra de navegación tapan 1050 px.
        assertEquals(1050, legacyKeyboardHeight(rootHeight = 2400, visibleBottom = 1350))
    }

    @Test
    fun navigationBarAloneIsNotAKeyboard() {
        assertEquals(0, legacyKeyboardHeight(rootHeight = 2400, visibleBottom = 2274))
    }

    @Test
    fun nothingHiddenIsZero() {
        assertEquals(0, legacyKeyboardHeight(rootHeight = 2400, visibleBottom = 2400))
    }
}
