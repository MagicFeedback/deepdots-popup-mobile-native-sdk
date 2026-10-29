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

    /**
     * Cuánto ignora toques un survey que llega con el popup ya a la vista.
     *
     * Si el survey tarda más que el techo de la revelación (WebView en frío, red lenta), el popup
     * se abre con el spinner y una tarjeta pequeña; cuando el survey llega, la tarjeta crece y se
     * vuelve a centrar, y lo que había bajo el dedo cambia. Un toque que iba a la X caía en una
     * opción del rating, que auto-avanza: se enviaba una respuesta que nadie había dado (visto en
     * el iPad del CI, run 40918433857). Cubre la animación del alto
     * ([PageTransition.HEIGHT_ANIMATION_MS]) más el tiempo de reaccionar al cambio.
     */
    const val LATE_SURVEY_GUARD_MS: Int = 600

    /**
     * Bloquea [el] durante `ms` y lo suelta, salvo que para entonces se esté enviando una página
     * (`isSubmitting()`): ese bloqueo lo suelta `afterSubmitEvent`/`onBackEvent`.
     */
    val LATE_SURVEY_GUARD_JS: String = """
        function ddGuardLateSurvey(el, ms, isSubmitting){
          ddSetSurveyBusy(el, true);
          setTimeout(function(){ if(!isSubmitting()){ ddSetSurveyBusy(el, false); } }, ms);
        }
    """.trimIndent()
}
