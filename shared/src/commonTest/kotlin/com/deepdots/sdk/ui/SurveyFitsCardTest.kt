package com.deepdots.sdk.ui

import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Con logo, el survey no puede quedarse con el 72 % de la tarjeta: la parte central se desbordaba
 * y el final del survey quedaba bajo el footer (Lemvigh-Müller, 23-nov-2026).
 */
class SurveyFitsCardTest {

    @Test
    fun withoutMuchChromeTheUsualCeilingStays() {
        // Tarjeta de 600 dp, cabecera + footer + huecos = 120 dp: sobran 480, techo 72 % = 432.
        assertEquals(432, PopupReveal.fitSurveyCeilingDp(maxCeilingDp = 432, columnMaxDp = 600, chromeDp = 120))
    }

    @Test
    fun aLogoAndProgressTakeTheirSpaceFromTheSurvey() {
        // S25 (≈628 dp de tarjeta): cabecera 32 + footer 48 + huecos 16 + logo 88 + barra 38 +
        // mensaje 0 + huecos del centro 24 = 246 → al survey le quedan 382, no 452.
        val ceiling = PopupReveal.fitSurveyCeilingDp(maxCeilingDp = 452, columnMaxDp = 628, chromeDp = 246)
        assertEquals(382, ceiling)
        assertEquals(382, PopupReveal.resolveSurveyHeightDp(reportedDp = 700, floorDp = 280, ceilingDp = ceiling))
    }

    @Test
    fun aTinyCardKeepsTheMinimumSurveyHeight() {
        // Teclado abierto en un móvil pequeño: casi no queda sitio. El survey no desaparece.
        val ceiling = PopupReveal.fitSurveyCeilingDp(maxCeilingDp = 200, columnMaxDp = 280, chromeDp = 260)
        assertEquals(PopupReveal.MIN_SURVEY_HEIGHT_DP, PopupReveal.resolveSurveyHeightDp(400, 280, ceiling))
    }
}
