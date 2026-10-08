package com.deepdots.sdk.ui

import com.deepdots.sdk.SdkRuntime
import com.deepdots.sdk.models.PopupFont
import com.deepdots.sdk.tracking.buildSurveyIdentity

// Centralized MagicFeedback package version used for all CDN URLs
private const val MAGICFEEDBACK_VERSION: String = "2.2.22"

/**
 * Version de `@magicfeedback/popup-sdk` de la que se sirve la hoja de estilos del survey.
 *
 * ⚠️ Va PINEADA a proposito. Sin version, jsDelivr sirve la ultima publicada, de modo que una
 * app ya distribuida -- con su [MAGICFEEDBACK_VERSION] compilado dentro -- empezaria a
 * combinar su JS con el CSS de una release posterior en cuanto alguien publique el popup-sdk.
 * El salto 2.2.8 -> 2.2.22 lo deja claro: la hoja cambia a propiedades logicas y anade RTL.
 *
 * Esta hoja es la copia vendorizada de `magicfeedback-default.css` (no la del paquete de
 * surveys), con los 5 deltas locales del SDK, asi que el pin es a la version del popup-sdk y
 * NO a [MAGICFEEDBACK_VERSION]. Al subir [MAGICFEEDBACK_VERSION] hay que publicar el popup-sdk
 * con su CSS re-vendorizado y apuntar aqui a esa version; mientras ese npm no este publicado,
 * el WebView pedira una URL que no existe y el survey saldra sin estilos.
 */
internal const val POPUP_SDK_CSS_VERSION: String = "1.9.0"

/**
 * Common HTML builder for MagicFeedback survey popup used by Android/iOS WebViews.
 * This generates a self-contained HTML document that attempts to load a local asset first
 * and then falls back to CDN strategies. Bridge emission is abstracted so each platform
 * can map events appropriately.
 */
internal fun buildMagicFeedbackHtml(
    surveyId: String,
    productId: String,
    localAssetUrl: String?,
    assetSize: Int?,
    bridgeEmitCall: String, // JS snippet to emit an event string (e.g. DeepdotsBridge.emit or window.webkit?.messageHandlers?.DeepdotsBridge?.postMessage)
    timeoutMs: Int = 6000,
    isIOS: Boolean,
    font: PopupFont? = null
): String {
    val localSrcLiteral = localAssetUrl?.let { "'${it}'" } ?: "null"
    val assetSizeLiteral = assetSize?.toString() ?: "-1"
    // Default per-platform stack kept as-is when no custom font is set (existing behaviour).
    val defaultFontFamily = if (isIOS) "-apple-system" else "system-ui"
    // Espejo de Web src/ui/surveyHtml.ts: family/url son del API -> se sanean antes de ir al <style>.
    val fontFaceCss = if (font != null) buildFontFaceCss(font.family, font.url) else ""
    val fontFamilyValue = if (font != null) buildFontFamilyValue(font.family) else defaultFontFamily
    val emitWrapper = if (bridgeEmitCall.contains("(")) {
        "function emit(e){ try { ${
            bridgeEmitCall.replace(
                "(event)",
                "(e)"
            )
        } } catch(err){ console.error('[MagicFeedback] emit error', err); } }"
    } else {
        "function emit(e){ try { ${bridgeEmitCall}(e); } catch(err){ console.error('[MagicFeedback] emit error', err); } }"
    }
    val cdnBase = "https://cdn.jsdelivr.net/npm/@magicfeedback/native@${MAGICFEEDBACK_VERSION}/dist"
    val unpkgBase = "https://unpkg.com/@magicfeedback/native@${MAGICFEEDBACK_VERSION}/dist"
    val urlBrowserJsDelivr = "$cdnBase/magicfeedback-sdk.browser.js"
    val urlBrowserUnpkg = "$unpkgBase/magicfeedback-sdk.browser.js"
    val urlEsmModule = "$cdnBase/index.js"
    val urlStyleDefault =
        "https://cdn.jsdelivr.net/npm/@magicfeedback/popup-sdk@$POPUP_SDK_CSS_VERSION/dist/assets/assets/style.css"

    val pubKeyJs = (SdkRuntime.publicKey ?: "")
    val envJs = (SdkRuntime.env.ifBlank { "prod" })
    val hasProduct = productId.isNotBlank()

    // Build custom meta merging device_system, userId, and InitOptions.metadata entries.
    val meta = mutableMapOf<String, MutableList<String>>()
    // Always include device_system placeholder (resolved client-side via navigator.platform)
    meta["device_system"] = mutableListOf("__device_system__")
    // Include arbitrary metadata entries passed at init

    SdkRuntime.metadata?.forEach { (k, v) ->
        if (k.isBlank()) return@forEach
        when (v) {
            is String -> meta.getOrPut(k) { mutableListOf() }.add(v)
            is Number, is Boolean -> meta.getOrPut(k) { mutableListOf() }.add(v.toString())
            is Iterable<*> -> {
                val list = meta.getOrPut(k) { mutableListOf() }
                v.forEach { item ->
                    when (item) {
                        is String -> list.add(item)
                        is Number, is Boolean -> list.add(item.toString())
                    }
                }
            }

            is Array<*> -> {
                val list = meta.getOrPut(k) { mutableListOf() }
                v.forEach { item ->
                    when (item) {
                        is String -> list.add(item)
                        is Number, is Boolean -> list.add(item.toString())
                    }
                }
            }

            else -> { /* ignore complex values */
            }
        }
    }
    // Identidad del tracking (contrato §5): mismas claves que Web (buildSurveyIdentity) para que
    // las respuestas del survey se puedan coser con la analítica: session_id, user_id y el
    // mini_service activo (#33, CSAT por mini-service).
    val identity = buildSurveyIdentity(
        userId = SdkRuntime.userId,
        sessionId = SdkRuntime.sessionId,
        miniService = SdkRuntime.miniService,
        analyticsFeedbackSessionId = SdkRuntime.analyticsFeedbackSessionId,
    )
    identity.metadata.forEach { answer ->
        meta[answer.key] = answer.value.toMutableList()
    }

    // Now serialize into JS array of objects { key, value: [...] }
    val customMetaJsArray = buildString {
        append("[")
        var first = true
        meta.forEach { (key, values) ->
            if (!first) append(",") else first = false
            if (key == "device_system") {
                append("{ key: 'device_system', value: [(navigator.platform || 'unknown')] }")
            } else {
                val escapedVals = values.filter { it.isNotBlank() }
                    .joinToString(separator = ",") { v -> "'" + v.replace("'", "\\'") + "'" }
                append("{ key: '")
                append(key.replace("'", "\\'"))
                append("', value: [")
                append(escapedVals)
                append("] }")
            }
        }
        append("]")
    }

    // `profile` del survey: el external-user-id, 3er argumento de form() (igual que Web).
    val profileJsArray = buildString {
        append("[")
        identity.profile.forEachIndexed { index, answer ->
            if (index > 0) append(",")
            val values = answer.value.filter { it.isNotBlank() }
                .joinToString(",") { "'" + it.replace("'", "\\'") + "'" }
            append("{ key: '").append(answer.key.replace("'", "\\'")).append("', value: [").append(values).append("] }")
        }
        append("]")
    }

    // Add a log to verify custom meta serialization on the Kotlin side
    println("[MagicFeedback] CUSTOM_META (Kotlin) $MAGICFEEDBACK_VERSION: $customMetaJsArray")

    return """
        <html><head>
          <meta name='viewport' content='width=device-width, initial-scale=1.0'/>
          <!-- Render the survey in light mode only. The host app may be in system dark mode,
               which otherwise leaks into the WebView (prefers-color-scheme: dark) and turns the
               survey background/text dark and illegible. Native side also forces a light UI style. -->
          <meta name='color-scheme' content='light'/>
          <style>
            $fontFaceCss
            :root{color-scheme:light;}
            html,body{margin:0;padding:0;height:100%;background:transparent;font-family:$fontFamilyValue;}
            /* Allow the survey to scroll vertically inside the WebView, never horizontally. */
            body{overflow-x:hidden;overflow-y:auto;-webkit-overflow-scrolling:touch;font-size:15px;line-height:1.4;}
            #mf-form{width:100%;max-width:100%;box-sizing:border-box;}
            #mf-form *{max-width:100%;box-sizing:border-box;}
            /* Typography hierarchy: question slightly larger, with comfortable line-height. */
            #mf-form .magicfeedback-title,#mf-form h1,#mf-form h2,#mf-form h3,#mf-form legend{font-size:16px;line-height:1.35;margin:0 0 8px 0;}
            #mf-form label,#mf-form .magicfeedback-label{line-height:1.35;}
            #mf-status{color:#666;font-size:12px;padding:4px;}
            /* Pantalla final: HTML del editor de la plataforma (imagen + texto), centrado. */
            .deepdots-success{display:none;width:100%;text-align:center;padding:24px 0;}
            .deepdots-success img{max-width:100%;height:auto;margin:0 auto 16px auto;display:block;}
            .deepdots-success p{margin:0;font-size:16px;font-weight:600;line-height:1.4;}
          </style>
          <link rel="stylesheet" href="$urlStyleDefault" />
          <style>
            /* El margen lateral del popup lo pone el chrome nativo (la tarjeta de Compose), así
               que el survey no debe añadir el suyo: con el padding del paquete (12px del
               container + 14px del form en el breakpoint móvil) el enunciado y las opciones
               quedaban ~26px más adentro que el logo y la barra de progreso, que sí se alinean
               con el borde de la tarjeta. El padding vertical se conserva: es el que separa las
               preguntas del borde. Va después del <link> porque el CSS del paquete se carga
               ahí y, a igualdad de especificidad, gana el último. */
            .magicfeedback-container{padding-left:0;padding-right:0;}
            .magicfeedback-form{padding-left:0;padding-right:0;}
            /* Cada pregunta va en un bloque con 12px más de padding y fondo blanco sobre la
               tarjeta (también blanca), así que ese sangrado no se ve: solo desalinea. */
            .magicfeedback-div{padding-left:0;padding-right:0;}
          </style>
        </head>
        <body class="deepdots-popup">
          <!-- `generate('mf-form')` de @magicfeedback/native RENOMBRA ese div a
               `magicfeedback-container-<id>`, así que tras cargar no se puede volver a buscar
               por 'mf-form'. El JS se apoya en estos dos wrappers, que el Surveys SDK no toca:
               dd-form-wrapper se oculta al completar y dd-content (formulario o mensaje final)
               es lo que se mide para dimensionar el WebView. Mismo patrón que el HTML de RN. -->
          <div id='dd-content'><div id='dd-form-wrapper'><div id='mf-form'></div></div><div id='mf-success' class='deepdots-success'></div></div>
          <script>
            (function(){
              var LOCAL_SRC = $localSrcLiteral;
              var ASSET_SIZE = $assetSizeLiteral; // -1 if unknown
              $emitWrapper
              var initialized = false;
              var mfReady = false; // becomes true when form onLoadedEvent fires
${SurveyPalette.PRIMARY_COLOR_JS}
${SurveyBusy.BUSY_JS}
${SurveyAutofocus.AUTOFOCUS_JS}
              // Foco en la primera pregunta si es de escribir (ver SurveyAutofocus). Se mira el
              // wrapper porque `generate('mf-form')` renombra el div del survey.
              var ddSurveyLoaded = false;
              function ddAutofocusPage(trigger){ ddAutofocusFirstTextQuestion(document.getElementById('dd-form-wrapper'), trigger); }
${PopupReveal.REVEAL_JS}
              // Apertura diferida: la capa nativa mantiene el popup invisible hasta este aviso,
              // para que el usuario vea la tarjeta ya pintada en vez del spinner. Se manda
              // cuando el survey está montado y sus imágenes han llegado, o al vencer el techo
              // (survey que no carga: el popup se abre igual, con el spinner de siempre).
              var ddReveal = ddCreateReveal(document, function(){
                ddReportHeight();
                emit('${PopupReveal.READY_EVENT}');
              }, ${PopupReveal.REVEAL_TIMEOUT_MS});
              // Mensaje final configurado en la plataforma (style.successMessage). Va por
              // innerHTML porque es HTML del editor (imagen + texto), igual que hace
              // renderStartMessage de @magicfeedback/native con el mensaje de inicio.
              var successMessageHtml = '';
              function showSuccessScreen(){
                try {
                  var form = document.getElementById('dd-form-wrapper'); if (form) { form.style.display = 'none'; }
                  var done = document.getElementById('mf-success'); if (!done) return;
                  done.innerHTML = successMessageHtml || '<p>Thank you for your feedback!</p>';
                  done.style.display = 'block';
                  // Si el usuario había hecho scroll en la última pregunta, el mensaje quedaría
                  // fuera de la vista: el WebView encoge a su alto y el scroll se queda abajo.
                  window.scrollTo(0, 0);
                  ddReportHeight();
                } catch(e){ console.error('[MagicFeedback] success screen error', e); }
              }
              var PUBLIC_KEY = ${if (pubKeyJs.isNotEmpty()) "'${pubKeyJs}'" else "null"};
              var ENV = ${if (envJs.isNotEmpty()) "'${envJs}'" else "'prod'"};
            
              function emitJSON(name, payload){
                try { emit(JSON.stringify({ name: name, payload: payload || {} })); } catch(err){ console.error('[MagicFeedback] emitJSON error', err); }
              }
              // Altura real del survey. El WebView no tiene tamaño propio, así que sin esto la
              // capa nativa lo estira hasta el máximo y una sola pregunta deja un hueco enorme
              // entre la última opción y el footer. Se mide `#dd-content` (no el body, que va a
              // height:100% y siempre devuelve el alto del WebView), que contiene tanto el
              // formulario como el mensaje final.
              var ddLastHeight = -1;
              function ddReportHeight(){
                try {
                  var host = document.getElementById('dd-content');
                  if(!host) { return; }
                  var h = Math.ceil(host.getBoundingClientRect().height);
                  if(h > 0 && Math.abs(h - ddLastHeight) > 1){
                    ddLastHeight = h;
                    emitJSON('${PopupReveal.CONTENT_HEIGHT_EVENT}', { height: h });
                  }
                } catch(e){ console.error('[MagicFeedback] height report error', e); }
              }
              try {
                if (window.ResizeObserver) {
                  // Cubre la carga, el cambio de página, las follow-up y los avisos de validación
                  // sin tener que acordarse de llamarlo en cada evento.
                  new ResizeObserver(function(){ ddReportHeight(); }).observe(document.getElementById('dd-content'));
                }
              } catch(e){ console.error('[MagicFeedback] ResizeObserver error', e); }
              function initMF(){
                try {
                  if (window.magicfeedback && !initialized) {
                    initialized = true;
                    window.magicfeedback.init({debug:true, env: ENV, publicKey: PUBLIC_KEY});
                    var form = ${
        if (hasProduct) {
            // 3er argumento = profile (external-user-id), como en Web.
            if (identity.profile.isNotEmpty()) {
                "window.magicfeedback.form('$surveyId', '$productId', $profileJsArray)"
            } else {
                "window.magicfeedback.form('$surveyId', '$productId')"
            }
        } else {
            "window.magicfeedback.form('$surveyId')"
        }
    };
                    window.DeepdotsForm = form;
                    window.DeepdotsActions = {
                      send: function(){ try { form.send(); } catch(e){ console.error('[DeepdotsActions] send error', e); } },
                      back: function(){ try { form.back(); } catch(e){ console.error('[DeepdotsActions] back error', e); } },
                      close: function(){ try { emit('popup_close'); } catch(e){ console.error('[DeepdotsActions] close emit error', e); } },
                      startForm: function(){ try { if (typeof form.startForm === 'function') { form.startForm(); } else { console.warn('[DeepdotsActions] startForm not available'); } } catch(e){ console.error('[DeepdotsActions] startForm error', e); } }
                    };
                 
                    form.generate('mf-form', {
                      addButton:false,
                      // La pantalla final la pinta este HTML: renderSuccess de
                      // @magicfeedback/native usa textContent, así que el mensaje de la
                      // plataforma (HTML con imagen) no se vería, y su fallback es un literal
                      // genérico que ignora style.successMessage. Paridad con Web/RN.
                      addSuccessScreen:false,
                      onLoadedEvent: function(args){
                        mfReady = true; var s=document.getElementById('mf-status'); if(s) s.textContent='';
                        try {
                          var style = (args && args.formData && args.formData.style) ? args.formData.style : null;
                          // El survey pinta sus controles con --mf-primary, que sale de
                          // primaryColor; si la integracion solo configura el del boton, el popup
                          // mezclaria el color de la marca (que el chrome nativo si usa) con el
                          // gris azulado por defecto del paquete.
                          ddApplySurveyPrimaryColor(document.documentElement, style);
                          if (style && style.successMessage) { successMessageHtml = style.successMessage; }
                          // Idioma del survey: lo configura la plataforma en la integración y solo
                          // se conoce aquí dentro, pero los botones los pinta la capa nativa, así
                          // que se lo reenviamos para que traduzca su chrome (paridad con Web,
                          // donde `renderPopup` lee el mismo `formData.lang` al cargar).
                          var langs = (args && args.formData && args.formData.lang) ? args.formData.lang : null;
                          var surveyLang = '';
                          if (langs && langs.length) {
                            for (var li = 0; li < langs.length; li++) {
                              if (typeof langs[li] === 'string' && langs[li].trim()) { surveyLang = langs[li]; break; }
                            }
                          }
                          // El total solo se conoce con el form ya montado: lo necesita la barra
                          // de progreso que pinta la capa nativa.
                          emitJSON('popup_clicked', { style: style, surveyLang: surveyLang, progress: form.progress || 0, total: form.total || 0 });
                          emit('loaded'); // explicit loaded for Kotlin UI state
                          ddReportHeight(); // por si el WebView no trae ResizeObserver
                          ddReveal.whenPainted();
                          // La primera carga es la apertura; las siguientes llegan tras Start.
                          ddAutofocusPage(ddSurveyLoaded ? 'navigation' : 'open'); ddSurveyLoaded = true;
                        } catch(e){ console.error('[MagicFeedback] onLoadedEvent emit error', e); }
                      },
                      beforeSubmitEvent: function(){
                        try {
                          // El spinner lo pinta Compose FUERA del WebView, asi que no protege al
                          // survey: sin esto el usuario sigue cambiando de opcion mientras se
                          // envia la pagina, y ese cambio ya no viaja con ella.
                          ddSetSurveyBusy(document.body, true);
                          emitJSON('before_submit');
                        } catch(e){ console.error('[MagicFeedback] before_submit emit error', e); }
                      },
                      afterSubmitEvent: function(payload){
                        try {
                          ddSetSurveyBusy(document.body, false);
                          var err = payload && payload.error ? String(payload.error) : '';
                          var completed = !!(payload && payload.completed);
                          var progress = (payload && payload.progress) || 0;
                          var total = (payload && payload.total) || 0;
                          if (err) {
                             var lower = err.toLowerCase();
                             if (lower.indexOf('no response') !== -1) { emitJSON('validation_error_required'); }
                             else { emitJSON('submit_error', { error: err }); }
                          }
                          if (completed) { showSuccessScreen(); emitJSON('survey_completed'); }
                          else { emitJSON('after_submit', { error: err, completed: completed, progress: progress, total: total }); if (!err) { ddAutofocusPage('navigation'); } }
                        } catch(e){ console.error('[MagicFeedback] afterSubmit exception', e); }
                      },
                      onBackEvent: function(args){
                        try {
                          ddSetSurveyBusy(document.body, false);
                          var progress = (args && args.progress) || 0;
                          var total = (args && args.total) || 0;
                          emitJSON('back', { progress: progress, total: total });
                          if (!(args && args.error)) { ddAutofocusPage('navigation'); }
                        } catch(e){ console.error('[MagicFeedback] onBackEvent emit error', e); }
                      },
                      getMetaData: true,
                      customMetaData: $customMetaJsArray
                    }).catch(function(e){ console.error(e); emit('error:init'); });
                    return true;
                  }
                } catch(e){ console.error('[MagicFeedback] exception', e); }
                return false;
              }
              function addScript(src, type, onload, onerror){
                var s = document.createElement('script'); s.src = src; if(type) s.type = type; s.async = true; s.defer = true; s.onload = onload; s.onerror = onerror; document.head.appendChild(s);
              }
              function addModuleFallback(){
                if (initialized) return;
                addScript('$urlEsmModule','module',function(){ setTimeout(function(){ if(!initMF()){ emit('error:module'); } },100); },function(){ emit('error:module-load'); });
              }
              function fetchAndEval(src){
                fetch(src).then(r=>r.text()).then(code=>{ try { eval(code); if(!initMF()){ addModuleFallback(); } } catch(e){ addModuleFallback(); } });
              }
              var triedUnpkg = false;
              function tryCdn(){
                addScript('$urlBrowserJsDelivr', null, function(){ if(!initMF() && !triedUnpkg){ triedUnpkg = true; addScript('$urlBrowserUnpkg', null, function(){ if(!initMF()){ fetchAndEval('$urlBrowserUnpkg'); } }, function(){ fetchAndEval('$urlBrowserUnpkg'); }); } }, function(){ if(!triedUnpkg){ triedUnpkg = true; addScript('$urlBrowserUnpkg', null, function(){ if(!initMF()){ fetchAndEval('$urlBrowserUnpkg'); } }, function(){ fetchAndEval('$urlBrowserJsDelivr'); }); } else { fetchAndEval('$urlBrowserJsDelivr'); } });
              }
              function tryLocalThenCdn(){
                if(LOCAL_SRC){ addScript(LOCAL_SRC, null, function(){ if(!initMF()){ tryCdn(); } }, function(){ tryCdn(); }); } else { tryCdn(); }
              }
              tryLocalThenCdn();
              var t0 = Date.now();
              var poll = setInterval(function(){
                if(mfReady || initMF()){ clearInterval(poll); }
                else if(Date.now() - t0 > $timeoutMs){ clearInterval(poll); var s=document.getElementById('mf-status'); if(s) s.textContent='Could not load the survey'; emit('error:timeout'); }
              }, 250);
            })();
          </script>
        </body></html>
    """.trimIndent()
}

/**
 * Expect platform implementation to expose pre-built HTML string so iOS native app can obtain it directly.
 */
expect fun platformSurveyHtml(surveyId: String, productId: String, font: PopupFont? = null): String
