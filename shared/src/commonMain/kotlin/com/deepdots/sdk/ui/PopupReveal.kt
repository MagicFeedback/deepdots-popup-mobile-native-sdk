package com.deepdots.sdk.ui

/**
 * Apertura diferida del popup: el chrome se monta invisible y se enseña cuando el survey ya está
 * pintado, en vez de enseñar el spinner girando mientras se espera. Espejo del SDK Web
 * (`src/ui/reveal.ts` y `src/ui/surveyHtml.ts`).
 *
 * En móvil la espera es mayor que en web: al `GET .../info` del survey se le suma el arranque del
 * WebView y la descarga del bundle de `@magicfeedback/native` desde el CDN.
 */
object PopupReveal {

    /**
     * Techo de la espera. Pasado este tiempo el popup se abre igualmente (con spinner, el
     * comportamiento de siempre), para que una red mala retrase la apertura pero nunca la impida.
     *
     * 1200 ms es el mismo valor que en Web, medido allí contra producción: el JSON del survey
     * tarda unos 250 ms de mediana desde Europa, con picos de 775 ms cuando el servicio está
     * frío. El techo tiene que quedar por encima de esa cola: si cayera justo encima, el popup se
     * abriría con el spinner para quitarlo 30 ms después, que es peor que las dos opciones.
     */
    const val REVEAL_TIMEOUT_MS: Long = 1200L

    /** Nombre del mensaje con el que el WebView avisa de que el survey ya se puede enseñar. */
    const val READY_EVENT: String = "ready"

    /** Mensaje con el que el WebView informa de lo que mide su contenido, en px CSS = dp. */
    const val CONTENT_HEIGHT_EVENT: String = "content_height"

    /**
     * Altura mínima del área del survey mientras no se sepa lo que ocupa. Evita que la tarjeta
     * nazca colapsada si el WebView tarda en reportar.
     */
    const val MIN_SURVEY_HEIGHT_DP: Int = 120

    /**
     * Altura del área del survey a partir de lo que el WebView dice que ocupa su contenido.
     *
     * Sin dato (o con uno absurdo) se mantiene el suelo de siempre, que es el comportamiento
     * anterior; con dato se usa tal cual, acotado al espacio disponible en la tarjeta — por
     * encima de ese techo el WebView hace su propio scroll. Es lo que evita que una sola pregunta
     * deje un hueco enorme entre la última opción y el footer.
     *
     * Función pura para poder probar la decisión sin infraestructura de Compose UI.
     */
    fun resolveSurveyHeightDp(reportedDp: Int?, floorDp: Int, ceilingDp: Int): Int {
        val ceiling = maxOf(ceilingDp, MIN_SURVEY_HEIGHT_DP)
        val reported = reportedDp?.takeIf { it > 0 } ?: return minOf(floorDp, ceiling)
        return reported.coerceIn(MIN_SURVEY_HEIGHT_DP, ceiling)
    }

    /**
     * Espera a que lleguen las imágenes del survey antes de avisar. Los emojis del rating son SVG
     * y el logo es una imagen: llegan DESPUÉS del `onLoadedEvent`, así que sin esto el popup
     * aparecería y se rellenaría delante del usuario.
     *
     * Va como texto porque corre dentro del WebView. Es el mismo algoritmo que `REVEAL_JS` en
     * `surveyHtml.ts` (ruta React Native) y que `revealWhenPainted` en `reveal.ts` (DOM web).
     */
    val REVEAL_JS: String = """
        function ddCreateReveal(root, onReveal, timeoutMs){
          var revealed=false, timer=null;
          function reveal(){
            if(revealed){ return; }
            revealed=true;
            if(timer){ clearTimeout(timer); timer=null; }
            onReveal();
          }
          function revealWhenPainted(){
            if(revealed){ return; }
            var imgs=root.querySelectorAll('img'), pending=[];
            for(var i=0;i<imgs.length;i++){ if(!imgs[i].complete){ pending.push(imgs[i]); } }
            if(!pending.length){ reveal(); return; }
            var left=pending.length;
            function onSettled(){ left--; if(left<=0){ reveal(); } }
            for(var j=0;j<pending.length;j++){
              pending[j].addEventListener('load', onSettled);
              pending[j].addEventListener('error', onSettled);
            }
          }
          timer=setTimeout(reveal, timeoutMs);
          return { reveal: reveal, whenPainted: revealWhenPainted };
        }
    """.trimIndent()
}
