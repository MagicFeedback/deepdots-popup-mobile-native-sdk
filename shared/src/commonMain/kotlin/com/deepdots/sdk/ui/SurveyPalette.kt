package com.deepdots.sdk.ui

/**
 * Color de marca dentro del survey.
 *
 * El survey pinta sus controles (chips del rating, bordes, foco, selección) con `--mf-primary`,
 * que `@magicfeedback/native` rellena desde `formData.style.primaryColor`. Las integraciones
 * configuran `buttonPrimaryColor` para el botón, pero muchas dejan `primaryColor` vacío, así que
 * el popup mezclaba un botón con el color de la marca y unos controles con el `#5d7bad` por
 * defecto del paquete.
 *
 * Espejo de `src/ui/surveyPalette.ts` en el SDK Web. El chrome de KMP es Compose y ya usa
 * `buttonPrimaryColor`; lo que faltaba era el CSS de dentro del WebView, de ahí que esto vaya
 * como texto JS.
 */
object SurveyPalette {

    /**
     * Solo hex: el estilo viene de la API y se interpola en CSS, así que cualquier otra cosa
     * (`red; background: url(...)`, `var(--x)`, …) se descarta en vez de colarse en la hoja.
     *
     * Redeclara las cinco variables, no solo `--mf-primary`: un `color-mix` escrito en `:root` ya
     * se resolvió allí contra el color por defecto y heredaría ese valor ya calculado.
     */
    val PRIMARY_COLOR_JS: String = """
        function ddResolveSurveyPrimaryColor(style){
          var hex=/^#(?:[0-9a-fA-F]{3,4}|[0-9a-fA-F]{6}|[0-9a-fA-F]{8})${'$'}/;
          var candidates=[style&&style.primaryColor, style&&style.buttonPrimaryColor];
          for(var i=0;i<candidates.length;i++){
            var value=(candidates[i]||'').trim();
            if(hex.test(value)){ return value; }
          }
          return null;
        }
        function ddApplySurveyPrimaryColor(el, style){
          var color=ddResolveSurveyPrimaryColor(style);
          if(!color||!el){ return; }
          el.style.setProperty('--mf-primary', color);
          el.style.setProperty('--mf-primary-hover', 'color-mix(in srgb, '+color+' 85%, black)');
          el.style.setProperty('--mf-primary-light', 'color-mix(in srgb, '+color+' 15%, white)');
          el.style.setProperty('--mf-primary-border', 'color-mix(in srgb, '+color+' 35%, transparent)');
          el.style.setProperty('--mf-border-focus', color);
        }
    """.trimIndent()
}
