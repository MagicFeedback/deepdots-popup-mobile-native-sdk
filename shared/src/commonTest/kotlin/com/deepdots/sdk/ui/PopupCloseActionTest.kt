package com.deepdots.sdk.ui

import com.deepdots.sdk.models.Action
import com.deepdots.sdk.models.Actions
import com.deepdots.sdk.models.PopupDefinition
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * La X de la cabecera y el Back del sistema (Android) cierran con la MISMA acción: si
 * divergieran, el Back reportaría, guardaría el cooldown o emitiría eventos distintos que la X.
 */
class PopupCloseActionTest {

    private fun popup(actions: Actions = Actions()) =
        PopupDefinition(id = "p1", surveyId = "s1", productId = "prod", actions = actions)

    @Test
    fun usesTheDeclineConfiguredByTheApi() {
        val decline = Action.Decline(label = "No, gracias", cooldownDays = 7)
        assertEquals(decline, popupCloseAction(popup(Actions(decline = decline)), declineLabel = "Cancel"))
    }

    @Test
    fun withoutApiDeclineFallsBackToADeclineWithoutCooldown() {
        assertEquals(
            Action.Decline(label = "Annuller", cooldownDays = 0),
            popupCloseAction(popup(), declineLabel = "Annuller"),
        )
    }
}
