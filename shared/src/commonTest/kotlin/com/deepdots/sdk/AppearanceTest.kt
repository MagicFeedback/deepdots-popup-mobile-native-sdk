package com.deepdots.sdk

import com.deepdots.sdk.models.Appearance
import com.deepdots.sdk.models.InitOptions
import com.deepdots.sdk.models.PopupOptions
import com.deepdots.sdk.models.Theme
import com.deepdots.sdk.storage.InMemoryStorage
import com.deepdots.sdk.ui.resolveForcedMode
import com.deepdots.sdk.ui.surveyModeOption
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/**
 * DD-240: light/dark forced by the app (`appearance` in init or `setAppearance`). Without it the
 * survey Style colours decide (DD-246); System follows the device.
 */
class AppearanceTest {

    @AfterTest
    fun reset() {
        SdkRuntime.appearance = null
    }

    private fun popupWithTheme(theme: String?): Theme {
        val style = if (theme == null) "{}" else """{"theme":"$theme"}"""
        val popups = DeepdotsPopups().debugParseServerPayload(
            """
            [{"id":"p1","title":"","message":"","triggers":[{"type":"exit","value":0}],
              "conditions":[],"actions":{},"style":$style,"surveyId":"s1","productId":"pr1"}]
            """.trimIndent(),
        )
        return popups.single().style.theme
    }

    @Test
    fun admin_theme_is_light_or_dark_only() {
        assertEquals(Theme.Light, popupWithTheme("light"))
        assertEquals(Theme.Dark, popupWithTheme("dark"))
        // "auto" was never written by the Admin (selector dropped with DD-237): plain light.
        assertEquals(Theme.Light, popupWithTheme("auto"))
        assertEquals(Theme.Light, popupWithTheme(null))
        assertEquals(Theme.Light, popupWithTheme("purple"))
    }

    @Test
    fun without_app_value_nothing_is_forced() {
        for (systemDark in listOf(false, true)) assertNull(resolveForcedMode(null, systemDark))
    }

    @Test
    fun app_value_forces_the_mode() {
        for (systemDark in listOf(false, true)) {
            assertEquals(Theme.Light, resolveForcedMode(Appearance.Light, systemDark))
            assertEquals(Theme.Dark, resolveForcedMode(Appearance.Dark, systemDark))
        }
        assertEquals(Theme.Dark, resolveForcedMode(Appearance.System, systemDark = true))
        assertEquals(Theme.Light, resolveForcedMode(Appearance.System, systemDark = false))
    }

    @Test
    fun survey_mode_option_matches_the_survey_sdk() {
        assertEquals("dark", surveyModeOption(Theme.Dark))
        assertEquals("light", surveyModeOption(Theme.Light))
        assertNull(surveyModeOption(null))
    }

    @Test
    fun init_sets_the_app_appearance() {
        val sdk = DeepdotsPopups()
        sdk.init(
            InitOptions(
                popupOptions = PopupOptions(publicKey = "pk-ap"),
                storage = InMemoryStorage(),
                appearance = Appearance.System,
            ),
        )
        assertEquals(Appearance.System, SdkRuntime.appearance)
        assertEquals(Appearance.System, sdk.getAppearance())
    }

    @Test
    fun init_without_appearance_leaves_the_admin_theme_in_charge() {
        SdkRuntime.appearance = Appearance.Dark
        DeepdotsPopups().init(
            InitOptions(popupOptions = PopupOptions(publicKey = "pk-ap"), storage = InMemoryStorage()),
        )
        assertNull(SdkRuntime.appearance)
    }

    @Test
    fun setAppearance_changes_it_at_runtime_and_null_goes_back_to_admin() {
        val sdk = DeepdotsPopups()
        sdk.setAppearance(Appearance.Dark)
        assertEquals(Appearance.Dark, SdkRuntime.appearance)
        sdk.setAppearance(Appearance.Light)
        assertEquals(Appearance.Light, sdk.getAppearance())
        sdk.setAppearance(null)
        assertNull(sdk.getAppearance())
    }

    @Test
    fun legacy_full_constructor_still_works_and_leaves_appearance_null() {
        // Swift apps call InitOptions with every parameter: the 0.7.12 list must still compile.
        val options = InitOptions(
            false, null, null, PopupOptions(publicKey = "pk-legacy"), { null }, false, null,
            true, null, null, true, null, true, null, null,
        )
        assertNull(options.appearance)
        assertEquals("pk-legacy", options.popupOptions.publicKey)
    }
}
