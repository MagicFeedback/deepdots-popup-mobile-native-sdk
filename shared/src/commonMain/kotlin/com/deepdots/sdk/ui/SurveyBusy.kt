package com.deepdots.sdk.ui

/**
 * Bloqueo del survey mientras se envía una página.
 *
 * El spinner de KMP lo pinta Compose sobre el popup, pero el survey vive en un WebView metido
 * con interop, que se dibuja por encima del canvas y recibe sus propios toques: el velo no lo
 * protege. Sin esto, el usuario puede seguir cambiando de opción mientras se envía la página, y
 * ese cambio ya no viaja con ella, porque la respuesta se mandó al pulsar.
 *
 * Espejo de `src/ui/busy.ts` en el SDK Web. Se bloquea el `<body>` entero porque aquí el chrome
 * (cabecera, progreso, footer) es nativo y vive FUERA del WebView: dentro solo está el survey.
 */
object SurveyBusy {

    val BUSY_JS: String = """
        function ddSetSurveyBusy(el, busy){
          if(!el){ return; }
          el.style.pointerEvents = busy ? 'none' : '';
          el.setAttribute('aria-busy', busy ? 'true' : 'false');
          if('inert' in el){ el.inert = busy; }
        }
    """.trimIndent()
}
