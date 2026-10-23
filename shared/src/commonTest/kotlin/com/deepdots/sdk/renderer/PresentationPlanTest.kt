package com.deepdots.sdk.renderer

import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * iOS: desde qué controlador se presenta el popup. Se presentaba siempre desde el del contexto
 * (normalmente el `rootViewController`); si ese ya estaba presentando algo (un sheet, una pantalla
 * a pantalla completa, una alerta), UIKit rechazaba la presentación en silencio y el popup se
 * perdía: contaba como mostrado (evento, reporte, cooldown) sin que nadie lo viera.
 */
class PresentationPlanTest {

    @Test
    fun sinNadaPresentadoSePresentaDesdeLaRaiz() {
        assertEquals(PresentationPlan.PresentFrom(-1), presentationPlan(emptyList()))
    }

    @Test
    fun conUnaPantallaOUnSheetSePresentaEncimaDelUltimo() {
        assertEquals(PresentationPlan.PresentFrom(0), presentationPlan(listOf(PresentedKind.Screen)))
        assertEquals(PresentationPlan.PresentFrom(0), presentationPlan(listOf(PresentedKind.Sheet)))
        assertEquals(
            PresentationPlan.PresentFrom(1),
            presentationPlan(listOf(PresentedKind.Screen, PresentedKind.Sheet)),
        )
    }

    @Test
    fun conUnaAlertaDelHostEspera() {
        // Las alertas son del sistema: no se tapa una alerta con un popup.
        assertEquals(PresentationPlan.Wait, presentationPlan(listOf(PresentedKind.Alert)))
        assertEquals(PresentationPlan.Wait, presentationPlan(listOf(PresentedKind.Screen, PresentedKind.Alert)))
    }

    @Test
    fun siLoDeArribaSeEstaCerrandoEspera() {
        // Durante la animación de cierre UIKit también rechaza presentar desde él o desde abajo.
        assertEquals(PresentationPlan.Wait, presentationPlan(listOf(PresentedKind.Dismissing)))
        assertEquals(PresentationPlan.Wait, presentationPlan(listOf(PresentedKind.Screen, PresentedKind.Dismissing)))
    }
}
