package com.deepdots.sdk.renderer

/** Qué hay presentado encima del controlador del contexto (iOS), de abajo arriba. */
internal enum class PresentedKind {
    /** Una pantalla del host (pantalla completa, navegación modal). */
    Screen,

    /** Un sheet, popover o similar del host. */
    Sheet,

    /** Una alerta (`UIAlertController`). */
    Alert,

    /** Un controlador en plena animación de cierre. */
    Dismissing,
}

/** Dónde presentar el popup en iOS. */
internal sealed class PresentationPlan {
    /** Desde el controlador en [index] de la cadena; -1 = el del contexto. */
    data class PresentFrom(val index: Int) : PresentationPlan()

    /** Ahora no se puede: reintentar cuando lo de arriba haya desaparecido. */
    data object Wait : PresentationPlan()
}

/**
 * Lógica pura de la presentación en iOS (testeable fuera de UIKit).
 *
 * UIKit solo deja presentar desde un controlador que no esté presentando ya otro. El popup se
 * presentaba siempre desde el del contexto y, con un sheet o una pantalla completa del host
 * encima, la presentación fallaba en silencio: el popup contaba como mostrado y nadie lo veía.
 * Ahora se presenta desde lo más alto de la cadena. Excepciones: una alerta (no se tapa una alerta
 * del sistema; paridad con Android, que espera a que el host cierre sus modales) y un controlador
 * cerrándose (UIKit rechaza la presentación hasta que termina).
 */
internal fun presentationPlan(chain: List<PresentedKind>): PresentationPlan = when (chain.lastOrNull()) {
    null -> PresentationPlan.PresentFrom(-1)
    PresentedKind.Alert, PresentedKind.Dismissing -> PresentationPlan.Wait
    PresentedKind.Screen, PresentedKind.Sheet -> PresentationPlan.PresentFrom(chain.lastIndex)
}
