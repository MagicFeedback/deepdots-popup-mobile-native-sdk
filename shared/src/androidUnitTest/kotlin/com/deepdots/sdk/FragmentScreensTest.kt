package com.deepdots.sdk

import com.deepdots.sdk.platform.FragmentScreens
import kotlin.test.Test
import kotlin.test.assertFalse

class FragmentScreensTest {
    @Test
    fun containers_dialogs_and_fragments_without_view_are_not_screens() {
        assertFalse(FragmentScreens.isScreen("NavHostFragment", isDialog = false, view = null))
        assertFalse(FragmentScreens.isScreen("CartSheet", isDialog = true, view = null))
        assertFalse(FragmentScreens.isScreen("HeadlessFragment", isDialog = false, view = null))
    }
}
