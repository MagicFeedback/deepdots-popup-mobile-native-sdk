package com.deepdots.sdk.ui

import com.deepdots.sdk.SdkRuntime
import com.deepdots.sdk.models.PopupFont
import com.deepdots.sdk.tracking.buildSurveyIdentity

// Centralized MagicFeedback package version used for all CDN URLs
private const val MAGICFEEDBACK_VERSION: String = "2.2.4"

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
        "https://cdn.jsdelivr.net/npm/@magicfeedback/popup-sdk/dist/assets/assets/style.css"

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
          </style>
          <link rel="stylesheet" href="$urlStyleDefault" />
        </head>
        <body class="deepdots-popup">
          <div id='mf-form'></div>
          <script>
            (function(){
              var LOCAL_SRC = $localSrcLiteral;
              var ASSET_SIZE = $assetSizeLiteral; // -1 if unknown
              $emitWrapper
              var initialized = false;
              var mfReady = false; // becomes true when form onLoadedEvent fires
              var PUBLIC_KEY = ${if (pubKeyJs.isNotEmpty()) "'${pubKeyJs}'" else "null"};
              var ENV = ${if (envJs.isNotEmpty()) "'${envJs}'" else "'prod'"};
            
              function emitJSON(name, payload){
                try { emit(JSON.stringify({ name: name, payload: payload || {} })); } catch(err){ console.error('[MagicFeedback] emitJSON error', err); }
              }
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
                      onLoadedEvent: function(args){
                        mfReady = true; var s=document.getElementById('mf-status'); if(s) s.textContent='';
                        try {
                          var style = (args && args.formData && args.formData.style) ? args.formData.style : null;
                          emitJSON('popup_clicked', { style: style });
                          emit('loaded'); // explicit loaded for Kotlin UI state
                        } catch(e){ console.error('[MagicFeedback] onLoadedEvent emit error', e); }
                      },
                      beforeSubmitEvent: function(){ try { emitJSON('before_submit'); } catch(e){ console.error('[MagicFeedback] before_submit emit error', e); } },
                      afterSubmitEvent: function(payload){
                        try {
                          var err = payload && payload.error ? String(payload.error) : '';
                          var completed = !!(payload && payload.completed);
                          var progress = (payload && payload.progress) || 0;
                          var total = (payload && payload.total) || 0;
                          if (err) {
                             var lower = err.toLowerCase();
                             if (lower.indexOf('no response') !== -1) { emitJSON('validation_error_required'); }
                             else { emitJSON('submit_error', { error: err }); }
                          }
                          if (completed) { emitJSON('survey_completed'); }
                          else { emitJSON('after_submit', { error: err, completed: completed, progress: progress, total: total }); }
                        } catch(e){ console.error('[MagicFeedback] afterSubmit exception', e); }
                      },
                      onBackEvent: function(args){
                        try {
                          var progress = (args && args.progress) || 0;
                          var total = (args && args.total) || 0;
                          emitJSON('back', { progress: progress, total: total });
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
