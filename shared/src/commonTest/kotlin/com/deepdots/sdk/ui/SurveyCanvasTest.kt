package com.deepdots.sdk.ui

import androidx.compose.ui.graphics.Color
import kotlin.test.Test
import kotlin.test.assertEquals

class SurveyCanvasTest {
    @Test
    fun hex_is_uppercase_rrggbb() {
        assertEquals("#0F2238", SurveyCanvas.hex(Color(0xFF0F2238)))
        assertEquals("#FFFFFF", SurveyCanvas.hex(Color.White))
        assertEquals("#000000", SurveyCanvas.hex(Color.Black))
    }

    @Test
    fun js_paints_the_root_with_the_card_colour() {
        assertEquals(
            "document.documentElement.style.background='#16181D';",
            SurveyCanvas.backgroundJs(Color(0xFF16181D)),
        )
    }
}
