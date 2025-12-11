package com.deepdots.sdk.ui

// Centralized MagicFeedback package version used for all CDN URLs
private const val MAGICFEEDBACK_VERSION: String = "2.1.2-beta.7"

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
    timeoutMs: Int = 4000,
    isIOS: Boolean
): String {
    val localSrcLiteral = localAssetUrl?.let { "'${it}'" } ?: "null"
    val assetSizeLiteral = assetSize?.toString() ?: "-1"
    val fontFamily = if (isIOS) "-apple-system" else "system-ui"
    val emitWrapper = if (bridgeEmitCall.contains("(")) {
        "function emit(e){ try { ${bridgeEmitCall.replace("(event)", "(e)")} } catch(err){ console.error('[MagicFeedback] emit error', err); } }"
    } else {
        "function emit(e){ try { ${bridgeEmitCall}(e); } catch(err){ console.error('[MagicFeedback] emit error', err); } }"
    }
    val cdnBase = "https://cdn.jsdelivr.net/npm/@magicfeedback/native@${MAGICFEEDBACK_VERSION}/dist"
    val unpkgBase = "https://unpkg.com/@magicfeedback/native@${MAGICFEEDBACK_VERSION}/dist"
    val urlBrowserJsDelivr = "$cdnBase/magicfeedback-sdk.browser.js"
    val urlBrowserUnpkg = "$unpkgBase/magicfeedback-sdk.browser.js"
    val urlEsmModule = "$cdnBase/index.js"
    val urlStyleDefault = "https://cdn.jsdelivr.net/npm/@magicfeedback/popup-sdk/dist/assets/assets/style.css"

    return """
        <html><head>
          <meta name='viewport' content='width=device-width, initial-scale=1.0'/>
          <style>body{margin:0;background:transparent;font-family:$fontFamily;} #mf-status{color:#666;font-size:12px;padding:4px;} </style>
          <link rel="stylesheet" href="$urlStyleDefault" />
        </head>
        <body>
          <div id='mf-form'></div>
          <script>
            (function(){
              var LOCAL_SRC = $localSrcLiteral;
              var ASSET_SIZE = $assetSizeLiteral; // -1 if unknown
              $emitWrapper
              var initialized = false;
              var mfReady = false; // becomes true when form onLoadedEvent fires
              function emitJSON(name, payload){
                try { emit(JSON.stringify({ name: name, payload: payload || {} })); } catch(err){ console.error('[MagicFeedback] emitJSON error', err); }
              }
              function initMF(){
                try {
                  if (window.magicfeedback && !initialized) {
                    initialized = true;
                    window.magicfeedback.init({debug:true, env:'prod'});
                    var form = window.magicfeedback.form('$surveyId', '$productId');
                    window.DeepdotsForm = form;
                    window.DeepdotsActions = {
                      send: function(){ try { form.send(); } catch(e){ console.error('[DeepdotsActions] send error', e); } },
                      back: function(){ try { form.back(); } catch(e){ console.error('[DeepdotsActions] back error', e); } },
                      close: function(){ try { emit('popup_close'); } catch(e){ console.error('[DeepdotsActions] close emit error', e); } },
                      startForm: function(){ try { if (typeof form.startForm === 'function') { form.startForm(); } else { console.warn('[DeepdotsActions] startForm not available'); } } catch(e){ console.error('[DeepdotsActions] startForm error', e); } }
                    };
                    form.generate('mf-form', {
                      addButton:false,
                      getMetaData:true,
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
                      }
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
expect fun platformSurveyHtml(surveyId: String, productId: String): String
