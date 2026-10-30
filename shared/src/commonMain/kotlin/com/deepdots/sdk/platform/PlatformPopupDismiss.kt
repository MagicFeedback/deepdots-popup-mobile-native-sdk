package com.deepdots.sdk.platform

expect fun dismissPopup(context: PlatformContext)


/**
 * ¿Hay un popup del SDK en pantalla (o esperando a que el host cierre su modal) en la ventana de
 * [context]? Solo desde el hilo principal. Mira la ventana, no un flag: un popup que se fue sin
 * pasar por sus acciones (Activity destruida, `close()`) no puede dejar el SDK bloqueado.
 */
expect fun isPopupShowing(context: PlatformContext): Boolean
