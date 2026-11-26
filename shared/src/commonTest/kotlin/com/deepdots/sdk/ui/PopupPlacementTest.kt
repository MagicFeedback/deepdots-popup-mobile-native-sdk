package com.deepdots.sdk.ui

import androidx.compose.ui.unit.dp
import com.deepdots.sdk.models.PopupSize
import kotlin.test.Test
import kotlin.test.assertEquals

/** DD-334: tamaño de la tarjeta. Sin `style.size` todo sigue como siempre (420dp). */
class PopupPlacementTest {
    @Test
    fun parses_size_from_the_api() {
        assertEquals(PopupSize.Small, parsePopupSize("small"))
        assertEquals(PopupSize.Small, parsePopupSize(" Small "))
        assertEquals(PopupSize.Regular, parsePopupSize("regular"))
        assertEquals(PopupSize.Regular, parsePopupSize(null))
        assertEquals(PopupSize.Regular, parsePopupSize("huge"))
    }

    @Test
    fun card_width_per_size() {
        assertEquals(420.dp, cardMaxWidthFor(PopupSize.Regular))
        assertEquals(300.dp, cardMaxWidthFor(PopupSize.Small))
    }

    @Test
    fun survey_style_width_cannot_widen_a_small_card() {
        assertEquals(300.dp, resolveCardMaxWidth(PopupSize.Small, 600.dp))
        assertEquals(280.dp, resolveCardMaxWidth(PopupSize.Small, 280.dp))
        assertEquals(600.dp, resolveCardMaxWidth(PopupSize.Regular, 600.dp))
        assertEquals(420.dp, resolveCardMaxWidth(PopupSize.Regular, null))
    }
}
