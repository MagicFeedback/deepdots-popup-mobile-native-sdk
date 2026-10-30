package com.deepdots.sdk.renderer

import kotlin.test.Test
import kotlin.test.assertEquals

/** Popup abierto cuando su Activity se destruye (MF-3246: rotar lo perdía). */
class DestroyFollowUpTest {

    @Test
    fun rotation_with_the_popup_mounted_shows_it_on_the_recreated_activity() {
        assertEquals(DestroyFollowUp.ShowOnRecreated, popupDestroyFollowUp(stillMounted = true, changingConfigurations = true))
    }

    @Test
    fun activity_closing_for_good_forgets_the_popup() {
        assertEquals(DestroyFollowUp.Forget, popupDestroyFollowUp(stillMounted = true, changingConfigurations = false))
    }

    @Test
    fun popup_already_closed_is_not_shown_again_after_rotation() {
        assertEquals(DestroyFollowUp.Forget, popupDestroyFollowUp(stillMounted = false, changingConfigurations = true))
    }
}
