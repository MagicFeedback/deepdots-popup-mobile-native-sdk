package com.deepdots.sdk.ui

import androidx.compose.ui.graphics.Color
import com.deepdots.sdk.models.Theme
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/** DD-246: el texto del chrome sale del fondo final, no de `style.theme`. */
class PopupContrastTest {

    @Test
    fun darkStyleWithLightThemeGetsWhiteText() {
        val c = PopupContrast.colorsFor(Color(0xFF111827), Theme.Light)
        assertEquals(Color(0xFF111827), c.background)
        assertEquals(PopupContrast.DARK_TEXT, c.text)
        assertEquals(PopupContrast.DARK_TRACK, c.progressTrack)
        assertTrue(c.isDark)
    }

    @Test
    fun lightStyleWithDarkThemeGetsBlackText() {
        val c = PopupContrast.colorsFor(Color(0xFFFFF7ED), Theme.Dark)
        assertEquals(PopupContrast.LIGHT_TEXT, c.text)
        assertEquals(PopupContrast.LIGHT_TRACK, c.progressTrack)
        assertFalse(c.isDark)
    }

    @Test
    fun withoutStyleBackgroundTheThemeGivesTheDefault() {
        val light = PopupContrast.colorsFor(null, Theme.Light)
        assertEquals(Color.White, light.background)
        assertEquals(Color.Black, light.text)
        val dark = PopupContrast.colorsFor(null, Theme.Dark)
        assertEquals(Color(0xFF2B2B2B), dark.background)
        assertEquals(Color.White, dark.text)
    }

    @Test
    fun midTonesPickTheTextWithMoreContrast() {
        // Brand colours seen in client Styles.
        assertTrue(PopupContrast.isDark(Color(0xFF1E293B)))  // slate 800
        assertTrue(PopupContrast.isDark(Color(0xFF0B3D2E)))  // dark green
        assertTrue(PopupContrast.isDark(Color(0xFF6D28D9)))  // violet 700
        assertFalse(PopupContrast.isDark(Color(0xFFFACC15))) // yellow
        assertFalse(PopupContrast.isDark(Color(0xFFE5E7EB))) // light grey
        assertFalse(PopupContrast.isDark(Color(0xFF22C55E))) // green 500
        // Same 0.2 cut as the survey (DD-238): #767676 (~0.18) is dark, mid grey #808080 (~0.22) is not.
        assertTrue(PopupContrast.isDark(Color(0xFF767676)))
        assertFalse(PopupContrast.isDark(Color(0xFF808080)))
    }

    @Test
    fun alphaIsIgnored() {
        assertTrue(PopupContrast.isDark(Color(0x80000000)))
        assertFalse(PopupContrast.isDark(Color(0x10FFFFFF)))
    }
}
