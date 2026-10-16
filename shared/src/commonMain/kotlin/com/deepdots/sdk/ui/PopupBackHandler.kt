package com.deepdots.sdk.ui

import androidx.compose.runtime.Composable
import com.deepdots.sdk.models.Action
import com.deepdots.sdk.models.PopupDefinition

/**
 * Acción con la que se cierra el popup desde el chrome: la X de la cabecera y el Back del
 * sistema en Android. Es UNA sola para que los dos cierres pasen por el mismo `onAction` y, por
 * tanto, reporten, guarden el cooldown y emitan los eventos exactamente igual.
 *
 * El `decline` de la API manda; si la plataforma no lo configuró, un `Decline` sin cooldown.
 */
internal fun popupCloseAction(popup: PopupDefinition, declineLabel: String): Action =
    popup.actions.decline ?: Action.Decline(label = declineLabel, cooldownDays = 0)

/**
 * Engancha el Back del sistema al cierre del popup mientras [enabled] (el popup está a la
 * vista). Android: botón, gesto y predictive back (API 33+) llaman a [onBack] en vez de llegar
 * al host. iOS: no hay Back de sistema, no hace nada.
 *
 * Al salir de la composición (o con [enabled] a `false`) se desengancha: el Back vuelve al host.
 */
@Composable
internal expect fun PopupBackHandler(enabled: Boolean, onBack: () -> Unit)
