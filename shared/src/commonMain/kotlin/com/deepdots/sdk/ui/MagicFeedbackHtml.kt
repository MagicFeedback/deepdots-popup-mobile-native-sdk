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
    // Different base style between platforms if desired
    val fontFamily = if (isIOS) "-apple-system" else "system-ui"
    // Bridge wrapper function to unify usage in script
    // For iOS we cannot safely use optional chaining inside a string for older WKWebView JS engine, so keep simple call
    val emitWrapper = if (bridgeEmitCall.contains("(")) {
        // developer provided full call style e.g. window.webkit.messageHandlers.DeepdotsBridge.postMessage(event)
        "function emit(e){ try { ${
            bridgeEmitCall.replace(
                "(event)",
                "(e)"
            )
        } } catch(err){ console.error('[MagicFeedback] emit error', err); } }"
    } else {
        // call style like DeepdotsBridge.emit
        "function emit(e){ try { ${bridgeEmitCall}(e); } catch(err){ console.error('[MagicFeedback] emit error', err); } }"
    }
    // Build CDN URLs using the centralized version
    val cdnBase = "https://cdn.jsdelivr.net/npm/@magicfeedback/native@${MAGICFEEDBACK_VERSION}/dist"
    val unpkgBase = "https://unpkg.com/@magicfeedback/native@${MAGICFEEDBACK_VERSION}/dist"
    val urlBrowserJsDelivr = "$cdnBase/magicfeedback-sdk.browser.js"
    val urlBrowserUnpkg = "$unpkgBase/magicfeedback-sdk.browser.js"
    val urlUmdJsDelivr = "$cdnBase/index.umd.js" // kept for fetch+eval fallback if ever needed
    val urlEsmModule = "$cdnBase/index.js"
    val urlStyleDefault = "$cdnBase/styles/magicfeedback-default.css"

    return """
        <html><head>
          <meta name='viewport' content='width=device-width, initial-scale=1.0'/>
          <style>body{margin:0;background:transparent;font-family:$fontFamily;} #mf-status{color:#666;font-size:12px;padding:4px;} </style>
          <link rel="stylesheet" href="$urlStyleDefault" />
        </head>
        <body>
          <div id='mf-form'></div>
          <div id='mf-status'>Initializing survey...</div>
          <script>
            (function(){
              var LOCAL_SRC = $localSrcLiteral;
              var ASSET_SIZE = $assetSizeLiteral; // -1 if unknown
              $emitWrapper
              var initialized = false;
              var mfReady = false; // becomes true when form onLoadedEvent fires
              function initMF(){
                try {
                  if (window.magicfeedback && !initialized) {
                    initialized = true;
                    window.magicfeedback.init({debug:true, env:'prod'});
                    var form = window.magicfeedback.form('$surveyId', '$productId');
                    // Expose form and actions for host popup buttons
                    window.DeepdotsForm = form;
                    window.DeepdotsActions = {
                      send: function(){ try { form.send(); } catch(e){ console.error('[DeepdotsActions] send error', e); } },
                      back: function(){ try { form.back(); } catch(e){ console.error('[DeepdotsActions] back error', e); } },
                      close: function(){ try { emit('popup_close'); } catch(e){ console.error('[DeepdotsActions] close emit error', e); } }
                    };
                    form.generate('mf-form', {
                      addButton:false,
                      getMetaData:true,
                      onLoadedEvent: function(){ console.log('[MagicFeedback] loaded'); mfReady = true; var s=document.getElementById('mf-status'); if(s) s.textContent=''; emit('popup_clicked'); },
                      afterSubmitEvent: function(payload){
                        // payload: { loading:boolean, progress:number, total:number, response:string, error:string }
                        try {
                          if (payload.completed) { emit('survey_completed'); }
                          else if (payload.error) { console.warn('[MagicFeedback] afterSubmit error', payload.error); }
                        } catch(e){ console.error('[MagicFeedback] afterSubmit exception', e); }
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
                console.warn('[MagicFeedback] trying ESM module fallback');
                addScript('$urlEsmModule','module',function(){
                  console.log('[MagicFeedback] module loaded');
                  setTimeout(function(){ if(!initMF()){ console.warn('[MagicFeedback] ESM module fallback did not provide magicfeedback'); emit('error:module'); } },100);
                },function(){ console.error('[MagicFeedback] module fallback error'); emit('error:module-load'); });
              }
              function fetchAndEval(src){
                console.log('[MagicFeedback] fetch+eval', src);
                fetch(src).then(r=>r.text()).then(code=>{ try { eval(code); console.log('[MagicFeedback] eval done'); if(!initMF()){ console.warn('[MagicFeedback] not available after eval'); addModuleFallback(); } } catch(e){ console.error('[MagicFeedback] eval error', e); addModuleFallback(); } });
              }
              var triedUnpkg = false;
              function tryCdn(){
                console.log('[MagicFeedback] trying jsDelivr browser');
                addScript('$urlBrowserJsDelivr', null, function(){
                  console.log('[MagicFeedback] jsDelivr loaded');
                  if(!initMF() && !triedUnpkg){ triedUnpkg = true; console.log('[MagicFeedback] trying unpkg browser'); addScript('$urlBrowserUnpkg', null, function(){
                    console.log('[MagicFeedback] unpkg loaded');
                    if(!initMF()){ console.warn('[MagicFeedback] still not available after unpkg'); fetchAndEval('$urlBrowserUnpkg'); }
                  }, function(){ console.error('[MagicFeedback] unpkg load error'); fetchAndEval('$urlBrowserUnpkg'); }); }
                }, function(){
                  console.error('[MagicFeedback] jsDelivr load error');
                  if(!triedUnpkg){ triedUnpkg = true; addScript('$urlBrowserUnpkg', null, function(){
                    console.log('[MagicFeedback] unpkg loaded');
                    if(!initMF()){ console.warn('[MagicFeedback] still not available after unpkg'); fetchAndEval('$urlBrowserUnpkg'); }
                  }, function(){ console.error('[MagicFeedback] unpkg load error'); fetchAndEval('$urlBrowserJsDelivr'); }); }
                  else { fetchAndEval('$urlBrowserJsDelivr'); }
                });
              }
              function tryLocalThenCdn(){
                if(LOCAL_SRC){
                  console.log('[MagicFeedback] trying local asset');
                  if(ASSET_SIZE > 0 && ASSET_SIZE < 10000){ console.warn('[MagicFeedback] asset seems too small, likely a placeholder'); }
                  addScript(LOCAL_SRC, null, function(){
                    console.log('[MagicFeedback] local asset loaded');
                    if(!initMF()){ console.warn('[MagicFeedback] not available after local (typeof='+typeof window.magicfeedback+')'); tryCdn(); }
                  }, function(){ console.warn('[MagicFeedback] local asset not found'); tryCdn(); });
                } else { tryCdn(); }
              }
              tryLocalThenCdn();
              var t0 = Date.now();
              var poll = setInterval(function(){
                if(mfReady || initMF()){ clearInterval(poll); }
                else if(Date.now() - t0 > $timeoutMs){ clearInterval(poll); console.warn('[MagicFeedback] not available (timeout)'); var s=document.getElementById('mf-status'); if(s) s.textContent='Could not load the survey'; emit('error:timeout'); }
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
