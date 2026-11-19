package com.deepdots.sdk.ui

import androidx.compose.ui.graphics.Color
import com.deepdots.sdk.models.Theme
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

/** DD-240: chrome colours when the app forces light or dark, and the Style's `dark` block (DD-239). */
class DarkModeColorsTest {
    private val brandLight = Color(0xFFFFF7ED) // light brand card
    private val brandDark = Color(0xFF1C1F26)  // dark Style card
    private val darkBlock = Color(0xFF14161B)  // Style `dark.boxBackgroundColor`

    @Test
    fun no_forced_mode_is_step_1() {
        assertEquals(brandLight, PopupContrast.colorsFor(brandLight, Theme.Light).background)
        val dark = PopupContrast.colorsFor(brandDark, Theme.Light)
        assertEquals(brandDark, dark.background)
        assertTrue(dark.isDark)
        assertEquals(PopupContrast.DARK_DEFAULT_BG, PopupContrast.colorsFor(null, Theme.Dark).background)
    }

    @Test
    fun forced_dark_uses_the_dark_block_first() {
        val c = PopupContrast.colorsFor(brandLight, Theme.Light, Theme.Dark, darkBlock)
        assertEquals(darkBlock, c.background)
        assertEquals(PopupContrast.DARK_TEXT, c.text)
    }

    @Test
    fun forced_dark_keeps_a_style_colour_that_is_already_dark() {
        assertEquals(brandDark, PopupContrast.colorsFor(brandDark, Theme.Light, Theme.Dark).background)
    }

    @Test
    fun forced_dark_never_uses_a_light_style_colour() {
        val c = PopupContrast.colorsFor(brandLight, Theme.Light, Theme.Dark)
        assertEquals(PopupContrast.DARK_DEFAULT_BG, c.background)
        assertTrue(c.isDark)
    }

    @Test
    fun forced_light_ignores_the_dark_theme_default() {
        assertEquals(PopupContrast.LIGHT_DEFAULT_BG, PopupContrast.colorsFor(null, Theme.Dark, Theme.Light).background)
        assertEquals(brandLight, PopupContrast.colorsFor(brandLight, Theme.Dark, Theme.Light, darkBlock).background)
    }

    @Test
    fun primary_button_on_light_and_dark_cards() {
        val brand = Color(0xFFE11D48)
        val darkBrand = Color(0xFFFDA4AF)
        // Light card: brand or the usual slate, white text (unchanged).
        assertEquals(brand, PopupContrast.primaryFor(brand, darkBrand, darkCard = false))
        assertEquals(PopupContrast.DEFAULT_PRIMARY, PopupContrast.primaryFor(null, null, darkCard = false))
        assertEquals(Color.White, PopupContrast.onPrimary(PopupContrast.DEFAULT_PRIMARY, darkCard = false))
        assertEquals(Color.White, PopupContrast.onPrimary(Color(0xFFFACC15), darkCard = false))
        // Dark card: dark block, then brand, then a light default with dark text.
        assertEquals(darkBrand, PopupContrast.primaryFor(brand, darkBrand, darkCard = true))
        assertEquals(brand, PopupContrast.primaryFor(brand, null, darkCard = true))
        val def = PopupContrast.primaryFor(null, null, darkCard = true)
        assertEquals(PopupContrast.DARK_DEFAULT_PRIMARY, def)
        assertEquals(PopupContrast.ON_LIGHT_PRIMARY, PopupContrast.onPrimary(def, darkCard = true))
        assertEquals(Color.White, PopupContrast.onPrimary(Color(0xFF1D4ED8), darkCard = true))
    }

    @Test
    fun style_colors_split_the_dark_block() {
        val payload = """{"name":"loaded","payload":{"style":{"dark":{"boxBackgroundColor":"#14161B","buttonPrimaryColor":"#8EAAF0"},"boxBackgroundColor":"#FFFFFF","buttonPrimaryColor":"#3D5FA8","loadingBarColor":"#22C55E"}}}"""
        val c = StyleColors.from(payload)
        assertEquals("#FFFFFF", c.light("boxBackgroundColor"))
        assertEquals("#3D5FA8", c.light("buttonPrimaryColor"))
        assertEquals("#22C55E", c.light("loadingBarColor"))
        assertEquals("#14161B", c.dark("boxBackgroundColor"))
        assertEquals("#8EAAF0", c.dark("buttonPrimaryColor"))
        assertNull(c.dark("loadingBarColor"))
    }

    @Test
    fun style_colors_without_dark_block() {
        val c = StyleColors.from("""{"style":{"boxBackgroundColor":"#1C1F26"}}""")
        assertEquals("#1C1F26", c.light("boxBackgroundColor"))
        assertNull(c.dark("boxBackgroundColor"))
        assertNull(StyleColors.from(null).light("boxBackgroundColor"))
        assertFalse(StyleColors.from("{}").light("x") != null)
    }
}
