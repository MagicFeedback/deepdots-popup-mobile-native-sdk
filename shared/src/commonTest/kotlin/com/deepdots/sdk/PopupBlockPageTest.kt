package com.deepdots.sdk

import com.deepdots.sdk.models.Position
import com.deepdots.sdk.models.Style
import com.deepdots.sdk.ui.popupBlocksHost
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * DD-334: `style.blockPage`. Con `false` y una posición que no es `center`, el popup no pone velo
 * y los toques fuera de la tarjeta llegan a la app. `center` bloquea siempre (es un modal) y sin
 * el campo todo sigue como hasta ahora en el SDK nativo: bloqueando.
 */
class PopupBlockPageTest {

    private fun payload(style: String) = """
        [{"id":"popup-place","title":"","message":"","triggers":[{"type":"time_on_page","value":5}],"actions":{},
          "style":$style,"surveyId":"survey-place","productId":"product-1"}]
    """.trimIndent()

    private fun parsedStyle(style: String): Style =
        DeepdotsPopups().debugParseServerPayload(payload(style)).single().style

    @Test
    fun reads_block_page_from_the_api() {
        assertEquals(false, parsedStyle("""{"position":"top-right","blockPage":false}""").blockPage)
        assertEquals(true, parsedStyle("""{"position":"top-right","blockPage":true}""").blockPage)
        assertNull(parsedStyle("""{"position":"top-right"}""").blockPage)
    }

    @Test
    fun a_string_block_page_is_read_too() {
        assertEquals(false, parsedStyle("""{"position":"bottom-left","blockPage":"false"}""").blockPage)
        assertEquals(true, parsedStyle("""{"position":"bottom-left","blockPage":"true"}""").blockPage)
    }

    @Test
    fun a_bad_block_page_does_not_drop_the_rest_of_the_style() {
        val style = parsedStyle("""{"position":"bottom-right","size":"small","blockPage":"maybe"}""")
        assertNull(style.blockPage)
        assertEquals(Position.BottomRight, style.position)
    }

    @Test
    fun reads_top_and_bottom_positions() {
        assertEquals(Position.Top, parsedStyle("""{"position":"top"}""").position)
        assertEquals(Position.Bottom, parsedStyle("""{"position":"bottom"}""").position)
        assertEquals(Position.Center, parsedStyle("""{"position":"middle"}""").position)
    }

    @Test
    fun only_an_explicit_false_off_center_lets_touches_through() {
        for (position in Position.entries.filter { it != Position.Center }) {
            assertFalse(popupBlocksHost(Style(position = position, blockPage = false)), "$position + false")
            assertTrue(popupBlocksHost(Style(position = position, blockPage = true)), "$position + true")
            assertTrue(popupBlocksHost(Style(position = position, blockPage = null)), "$position + absent")
        }
    }

    @Test
    fun center_always_blocks() {
        assertTrue(popupBlocksHost(Style(position = Position.Center, blockPage = false)))
        assertTrue(popupBlocksHost(Style(position = Position.Center, blockPage = null)))
    }
}

/** iOS: qué toques se queda el popup que no bloquea (`PassThroughView.hitTest`). */
class PopupCardHitTest {
    private val card = com.deepdots.sdk.ui.CardBounds(left = 200.0, top = 60.0, right = 380.0, bottom = 300.0)

    @Test
    fun touches_on_the_card_stay_in_the_popup() {
        assertTrue(com.deepdots.sdk.ui.cardKeepsTouch(card, 200.0, 60.0))
        assertTrue(com.deepdots.sdk.ui.cardKeepsTouch(card, 290.0, 180.0))
        assertTrue(com.deepdots.sdk.ui.cardKeepsTouch(card, 380.0, 300.0))
    }

    @Test
    fun touches_outside_the_card_go_to_the_app() {
        assertFalse(com.deepdots.sdk.ui.cardKeepsTouch(card, 199.0, 180.0))
        assertFalse(com.deepdots.sdk.ui.cardKeepsTouch(card, 290.0, 301.0))
        assertFalse(com.deepdots.sdk.ui.cardKeepsTouch(card, 10.0, 700.0))
    }

    @Test
    fun before_the_card_is_measured_nothing_is_kept() {
        assertFalse(com.deepdots.sdk.ui.cardKeepsTouch(null, 290.0, 180.0))
    }
}
