package com.deepdots.sdk.ui

import com.deepdots.sdk.i18n.DefaultLabels

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
    fun la_etiqueta_usa_el_idioma_del_survey() {
        // Paridad con Web: "Question X of Y" sale de la tabla de i18n, no de un literal inglés.
        val da = progressBarState(enabled = true, progress = 0.0, total = 3, labels = DefaultLabels.labels("da"))
        assertEquals("Spørgsmål 1 af 3", da.label)
        val de = progressBarState(enabled = true, progress = 1.0, total = 3, labels = DefaultLabels.labels("de"))
        assertEquals("Frage 2 von 3", de.label)
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

    // DD-210: style.progressMode = "completed" (espejo de progressView.test.ts del SDK Web).

    @Test
    fun modo_por_defecto_es_la_barra_de_siempre() {
        assertEquals(ProgressMode.Current, ProgressMode.fromStyle(null))
        assertEquals(ProgressMode.Current, ProgressMode.fromStyle("otro"))
        assertEquals(ProgressMode.Completed, ProgressMode.fromStyle("completed"))
    }

    @Test
    fun respondidas_empieza_en_cero() {
        val s = progressBarState(enabled = true, progress = 0.0, total = 5, mode = ProgressMode.Completed)
        assertTrue(s.visible)
        assertEquals(0f, s.fraction)
        assertEquals("0 of 5 answered", s.label)
    }

    @Test
    fun respondidas_en_la_pregunta_3_son_2_de_5() {
        val s = progressBarState(enabled = true, progress = 2.0, total = 5, mode = ProgressMode.Completed)
        assertEquals(0.4f, s.fraction)
        assertEquals("2 of 5 answered", s.label)
    }

    @Test
    fun respondidas_la_follow_up_abierta_no_mueve_la_barra() {
        val open = progressBarState(enabled = true, progress = 2.5, total = 5, mode = ProgressMode.Completed)
        assertEquals(0.4f, open.fraction)
        assertEquals("2 of 5 answered", open.label)
    }

    @Test
    fun respondidas_la_pantalla_final_sale_al_100() {
        val s = progressBarState(enabled = true, progress = 4.0, total = 5, completed = true, mode = ProgressMode.Completed)
        assertTrue(s.visible)
        assertEquals(1f, s.fraction)
        assertEquals("5 of 5 answered", s.label)
        // Una rama saltó páginas: se respondieron 3.
        val branch = progressBarState(enabled = true, progress = 2.0, total = 5, completed = true, mode = ProgressMode.Completed)
        assertEquals("3 of 3 answered", branch.label)
    }

    @Test
    fun respondidas_con_porcentaje_y_traducido() {
        val pct = progressBarState(
            enabled = true, progress = 2.0, total = 5, unit = ProgressUnit.Percentage, mode = ProgressMode.Completed,
        )
        assertEquals("40%", pct.label)
        val da = progressBarState(
            enabled = true, progress = 2.0, total = 5, labels = DefaultLabels.labels("da"), mode = ProgressMode.Completed,
        )
        assertEquals("2 af 5 besvaret", da.label)
    }

    @Test
    fun modo_actual_sigue_oculta_en_la_pantalla_final() {
        assertFalse(progressBarState(enabled = true, progress = 4.0, total = 5, completed = true).visible)
    }

    @Test
    fun todo_idioma_tiene_answeredOf_con_los_dos_huecos() {
        for (lang in listOf("en", "es", "da", "no", "sv", "fi", "de", "fr", "pt", "ar", "bn", "zh-CN", "bg", "cs",
            "el", "et", "ga", "hr", "hu", "it", "lt", "lv", "mt", "nl", "pl", "ro", "sk", "sl", "ca")) {
            val l = DefaultLabels.labels(lang).answeredOf
            assertTrue(l.contains("{answered}") && l.contains("{total}"), lang)
        }
    }
}
