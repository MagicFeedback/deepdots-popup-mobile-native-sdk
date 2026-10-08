package com.deepdots.sdk.ui

/**
 * Foco automático en la primera pregunta de la página cuando es de escribir.
 *
 * `@magicfeedback/native` vacía el formulario y lo repinta en cada página, con un bloque
 * `.magicfeedback-div` por pregunta. Solo cuenta la PRIMERA pregunta: si es de opciones y la de
 * texto va después, no se enfoca nada.
 *
 * - `open` (el popup acaba de aparecer): solo con puntero fino. En un móvil el teclado taparía el
 *   popup sin que el usuario haya tocado nada.
 * - `navigation` (Start, Siguiente, Atrás): siempre, porque el usuario acaba de pulsar.
 *
 * Espejo literal de `AUTOFOCUS_JS` en `src/ui/autofocus.ts` del SDK Web, que lo prueba contra
 * su versión TypeScript. El chrome de KMP es nativo, pero el survey vive en el WebView, así que
 * el foco se pone desde el HTML.
 *
 * ⚠️ En iOS, `WKWebView` no abre el teclado para un foco puesto desde JavaScript sin toque
 * directo dentro de la web (y aquí el toque es sobre un botón nativo), así que allí el campo
 * queda enfocado pero el teclado no sale solo. No hay API pública para cambiarlo.
 */
object SurveyAutofocus {

    /** Tipos de `<input>` que cuentan como "de escribir". Fuera `date` (abre selector) y `password`. */
    val INPUT_TYPES: List<String> = listOf("text", "email", "number", "tel", "url", "search")

    val AUTOFOCUS_JS: String = """
        function ddAutofocusFirstTextQuestion(root, trigger){
          if(!root){ return false; }
          var fine = typeof window.matchMedia==='function' && window.matchMedia('(pointer: fine)').matches;
          if(trigger==='open' && !fine){ return false; }
          var block = root.querySelector('.magicfeedback-div');
          if(!block){ return false; }
          var control = block.querySelector('input:not([type="hidden"]), textarea, select, button');
          if(!control || control.disabled || control.readOnly){ return false; }
          var types = ${INPUT_TYPES.joinToString(",", "[", "]") { "\"$it\"" }};
          var ok = control.tagName==='TEXTAREA' || (control.tagName==='INPUT' && types.indexOf(control.type)!==-1);
          if(!ok){ return false; }
          var active = document.activeElement;
          if(active && !root.contains(active)){
            var tag = active.tagName;
            if(tag==='INPUT' || tag==='TEXTAREA' || tag==='SELECT' || active.isContentEditable===true){ return false; }
          }
          try { control.focus({ preventScroll: true }); } catch(e){ control.focus(); }
          return document.activeElement===control;
        }
    """.trimIndent()
}
