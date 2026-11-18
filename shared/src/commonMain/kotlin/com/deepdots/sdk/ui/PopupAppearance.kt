package com.deepdots.sdk.ui

import com.deepdots.sdk.models.Appearance
import com.deepdots.sdk.models.Theme

/**
 * Light or dark forced by the app ([Appearance]), or `null` when the app sets nothing: then the
 * survey Style colours decide, as before (DD-246 for the frame, DD-238 for the survey).
 * [Appearance.System] follows the device ([systemDark]). Mirror of the web SDK (DD-241).
 */
internal fun resolveForcedMode(appearance: Appearance?, systemDark: Boolean): Theme? =
    when (appearance) {
        Appearance.Light -> Theme.Light
        Appearance.Dark -> Theme.Dark
        Appearance.System -> if (systemDark) Theme.Dark else Theme.Light
        null -> null
    }

/** Value of the survey SDK `mode` option (DD-238) for a forced mode; `null` = let the Style decide. */
internal fun surveyModeOption(mode: Theme?): String? = when (mode) {
    Theme.Light -> "light"
    Theme.Dark -> "dark"
    null -> null
}
