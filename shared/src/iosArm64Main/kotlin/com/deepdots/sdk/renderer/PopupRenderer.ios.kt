// `opaque` (fondo transparente del controlador de Compose) sigue siendo API experimental de
// Compose Multiplatform, y el opt-in tiene que cubrir la lambda `configure`, no solo la función.
@file:OptIn(ExperimentalComposeApi::class, ExperimentalComposeUiApi::class)

package com.deepdots.sdk.renderer

import androidx.compose.runtime.ExperimentalComposeApi
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeUIViewController
import com.deepdots.sdk.models.Action
import com.deepdots.sdk.models.PopupDefinition
import com.deepdots.sdk.platform.DEEPDOTS_POPUP_VC_ID
import com.deepdots.sdk.platform.PlatformContext
import com.deepdots.sdk.ui.PopupView
import platform.UIKit.UIColor
import platform.UIKit.UIModalPresentationOverFullScreen
import platform.UIKit.UIViewController
import platform.UIKit.restorationIdentifier

actual object PopupRenderer {
    actual fun show(
        popup: PopupDefinition,
        context: PlatformContext,
        onAction: (Action) -> Unit,
        onSurveyEvent: (name: String, payload: String?) -> Unit,
        onDismiss: () -> Unit
    ) {
        var controllerRef: UIViewController? = null
        // `opaque = false`: el canvas de Compose no pinta fondo propio, así que el scrim del
        // popup (semitransparente) deja ver la app de debajo, como en Android y en web.
        // `enforceStrictPlistSanityCheck = false`: Compose exige por defecto
        // `CADisableMinimumFrameDurationOnPhone = true` en el Info.plist y, si falta, lanza una
        // excepción que nadie captura y CIERRA LA APP DEL HOST al abrir el popup (en cualquier
        // iPhone, no solo en los de 120 Hz). Un SDK no puede tumbar la app por una clave del
        // plist: sin ella el popup solo se anima a 60 Hz en pantallas ProMotion.
        val vc = ComposeUIViewController(configure = {
            opaque = false
            enforceStrictPlistSanityCheck = false
        }) {
            PopupView(
                popup = popup,
                // Apertura diferida: la vista del controlador se presenta con alpha 0 y se
                // enseña aquí. La ocultación tiene que ser nativa, no solo el `alpha` de Compose
                // de `PopupView`: el survey es un WKWebView metido con UIKitView, y los
                // modificadores de dibujo de Compose no se aplican de forma fiable a las vistas
                // de interop, así que el WebView podría quedar a la vista con la tarjeta todavía
                // invisible. El alpha de UIView sí baja por toda la jerarquía.
                // Mientras espera, la vista va con la interacción desactivada: un toque a ciegas
                // no puede activar un botón que no se ve, y el hit test lo deja pasar a la vista
                // de debajo (equivalente al `View.INVISIBLE` de Android).
                onReady = {
                    controllerRef?.view?.let { view ->
                        view.setUserInteractionEnabled(true)
                        view.setAlpha(1.0)
                    }
                },
                onAction = { action ->
                    onAction(action)
                    controllerRef?.dismissViewControllerAnimated(true, null)
                    onDismiss()
                },
                onSurveyEvent = onSurveyEvent,
            )
        }
        controllerRef = vc
        // Marca para que `dismissPopup` cierre solo este VC y nunca uno del host.
        vc.restorationIdentifier = DEEPDOTS_POPUP_VC_ID
        // Overlay a pantalla completa, no la "sheet" del sistema (el estilo por defecto en
        // iOS 13+). Con la sheet, el chrome gris que dibuja UIKit se ve SIEMPRE, aunque la vista
        // esté a alpha 0: durante la espera el usuario vería un panel gris vacío, que es peor que
        // el spinner que se quería quitar. Además el popup pasa a verse como en Android y en web
        // (scrim + tarjeta) en vez de como una hoja que no cubre la pantalla.
        vc.modalPresentationStyle = UIModalPresentationOverFullScreen
        vc.view.backgroundColor = UIColor.clearColor
        vc.view.setUserInteractionEnabled(false)
        vc.view.setAlpha(0.0)
        // Desde lo más alto de la cadena, no desde el del contexto: si este ya presenta algo (un
        // sheet o una pantalla completa del host), UIKit rechazaba la presentación en silencio y
        // el popup se perdía aunque contase como mostrado. Ver PopupPresenter.
        PopupPresenter.present(context.viewController, vc)
    }
}
