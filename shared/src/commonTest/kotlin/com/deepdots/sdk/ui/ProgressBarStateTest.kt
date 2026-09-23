package com.deepdots.sdk.ui

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Paridad con la barra de progreso del SDK Web (`surveyHtml.ts` / `renderPopup.ts`), que a su
 * vez espeja `LineProgressQuestion` de MagicSurvey.
 */
class ProgressBarStateTest {

    @Test
    fun oculta_cuando_el_host_la_desactiva() {
        val s = progressBarState(enabled = false, progress = 1.0, total = 3)
        assertFalse(s.visible)
    }

    @Test
    fun oculta_con_una_sola_pagina() {
        // Mismo corte que MagicSurvey: sin más de una página no hay nada que medir.
        assertFalse(progressBarState(enabled = true, progress = 0.0, total = 1).visible)
        assertFalse(progressBarState(enabled = true, progress = 0.0, total = 0).visible)
    }

    @Test
    fun oculta_en_la_pantalla_de_inicio_y_al_completar() {
        assertFalse(progressBarState(enabled = true, progress = 0.0, total = 3, onStartPage = true).visible)
        assertFalse(progressBarState(enabled = true, progress = 2.0, total = 3, completed = true).visible)
    }

    @Test
    fun primera_pagina_de_tres() {
        val s = progressBarState(enabled = true, progress = 0.0, total = 3)
        assertTrue(s.visible)
        assertEquals("Question 1 of 3", s.label)
        assertEquals(1f / 3f, s.fraction)
    }

    @Test
    fun segunda_pagina_de_tres() {
        val s = progressBarState(enabled = true, progress = 1.0, total = 3)
        assertEquals("Question 2 of 3", s.label)
        assertEquals(2f / 3f, s.fraction)
    }

    @Test
    fun una_follow_up_avanza_media_casilla_pero_la_etiqueta_no_sube() {
        // Las follow-up dinámicas suman +0.5 al progress y no tocan el total: son un paso
        // DENTRO de la misma pregunta, así que la etiqueta redondea hacia abajo.
        val s = progressBarState(enabled = true, progress = 1.5, total = 3)
        assertEquals("Question 2 of 3", s.label)
        assertEquals(2.5f / 3f, s.fraction)
    }

    @Test
    fun nunca_pasa_del_total() {
        val s = progressBarState(enabled = true, progress = 9.0, total = 3)
        assertEquals("Question 3 of 3", s.label)
        assertEquals(1f, s.fraction)
    }

    @Test
    fun unidad_porcentaje() {
        val s = progressBarState(enabled = true, progress = 1.0, total = 3, unit = ProgressUnit.Percentage)
        assertEquals("67%", s.label)
    }

    @Test
    fun sin_unidad_la_barra_sigue_visible_pero_sin_etiqueta() {
        val s = progressBarState(enabled = true, progress = 1.0, total = 3, showUnit = false)
        assertTrue(s.visible)
        assertEquals("", s.label)
    }
}
