@file:Suppress("unused")
package com.deepdots.sdk

import com.deepdots.sdk.models.Event
import com.deepdots.sdk.models.EventData
import com.deepdots.sdk.models.InitOptions
import com.deepdots.sdk.models.PopupDefinition
import com.deepdots.sdk.models.ShowOptions
import com.deepdots.sdk.models.Action
import com.deepdots.sdk.models.Trigger
import com.deepdots.sdk.models.Condition
import com.deepdots.sdk.platform.PlatformContext
import com.deepdots.sdk.platform.dismissPopup
import com.deepdots.sdk.renderer.PopupRenderer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.Deferred
import com.deepdots.sdk.util.currentTimeMillis
import com.deepdots.sdk.storage.KeyValueStorage

class DeepdotsPopups {

    private var initOptions: InitOptions? = null
    private val eventBus = EventBus()
    private val activePopups = mutableMapOf<String, PopupDefinition>()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val answeredSurveys = mutableSetOf<String>()

    // Caché de último contexto usado en show para permitir dismiss desde acciones
    private var initOptionsContextCache: PlatformContext? = null

    private val triggerJobs = mutableListOf<Deferred<Unit>>()
    private val popupQueue = ArrayDeque<PopupDefinition>()
    private var processingQueue = false
    private val lastShown = mutableMapOf<String, Long>() // popupId -> timestamp (cache en memoria)
    private val storagePrefix = "popup_last_shown_"

    private fun isDebug(): Boolean = initOptions?.debug == true
    private fun log(msg: String, detail: Any? = null) {
        if (isDebug()) {
            println("[DeepdotsPopups] $msg" + (detail?.let { ": $detail" } ?: ""))
        }
    }

    /**
     * Inicializar el SDK
     */
    fun init(options: InitOptions) {
        if (initOptions != null) {
            log("SDK already initialized")
            return
        }
        initOptions = options

        options.popups.forEach { popup ->
            activePopups[popup.surveyId] = popup
        }

        log("Initialized", activePopups.keys)
        if (options.autoLaunch) {
            startAutoLaunch()
        }
    }

    private fun startAutoLaunch() {
        // Configurar triggers derivados de definiciones
        activePopups.values.forEach { def ->
            val trigger = def.trigger
            when (trigger) {
                is Trigger.TimeOnPage -> scheduleTimeOnPage(def, trigger)
                is Trigger.Scroll -> { /* Scroll se implementará cuando haya integración de scroll */ }
                is Trigger.Exit -> { /* Exit intent pendiente */ }
            }
        }
    }

    private fun scheduleTimeOnPage(def: PopupDefinition, t: Trigger.TimeOnPage) {
        val job = scope.async {
            delay(t.value * 1000L)
            if (shouldEnqueue(def, t.condition)) {
                enqueue(def)
            }
        }
        triggerJobs += job
    }

    private fun shouldEnqueue(def: PopupDefinition, conditions: List<Condition>): Boolean {
        // Segmentación (segments ahora nullable)
        val langOk = def.segments?.lang?.let { seg ->
            val current = initOptions?.provideLang?.invoke()
            current != null && seg.contains(current)
        } ?: true
        val pathOk = def.segments?.path?.let { seg ->
            val current = initOptions?.providePath?.invoke()
            current != null && seg.contains(current)
        } ?: true
        if (!langOk || !pathOk) return false
        // Condiciones trigger
        return evaluateConditions(def, conditions)
    }

    private fun evaluateConditions(def: PopupDefinition, conditions: List<Condition>): Boolean {
        if (conditions.isEmpty()) return true
        return conditions.all { c ->
            if (!c.answered && answeredSurveys.contains(def.surveyId)) return@all false
            if (c.cooldownDays > 0) {
                val key = storagePrefix + def.id
                val last = initOptions?.storage?.getLong(key)
                if (last != null) {
                    val elapsed = currentTimeMillis() - last
                    val required = c.cooldownDays * 24L * 60L * 60L * 1000L
                    if (elapsed < required) return@all false
                }
            }
            true
        }
    }

    private fun enqueue(def: PopupDefinition) {
        popupQueue.addLast(def)
        processQueue()
    }

    private fun processQueue() {
        if (processingQueue) return
        processingQueue = true
        scope.launch {
            while (popupQueue.isNotEmpty()) {
                val def = popupQueue.removeFirst()
                try {
                    initOptionsContextCache?.let { ctx ->
                        show(ShowOptions(def.surveyId, def.productId), ctx)
                        val now = currentTimeMillis()
                        lastShown[def.id] = now
                        initOptions?.storage?.putLong(storagePrefix + def.id, now)
                    } ?: run {
                        log("No context cached; cannot auto-show popup", def.id)
                    }
                } catch (t: Throwable) {
                    log("Error processing popup", "${def.id}: ${t.message}")
                }
                // Pausa mínima entre popups para evitar stacking
                delay(300L)
            }
            processingQueue = false
        }
    }

    private fun ensureInitialized(): Boolean {
        if (initOptions == null) {
            log("SDK not initialized")
            return false
        }
        return true
    }

    /**
     * Mostrar un popup desde la app anfitriona
     */
    fun show(
        options: ShowOptions,
        context: PlatformContext
    ) {
        if (!ensureInitialized()) return
        val popup = activePopups[options.surveyId]
        if (popup == null) {
            log("Popup not found for surveyId", options.surveyId)
            return
        }
        initOptionsContextCache = context
        scope.launch {
            eventBus.emit(
                Event.PopupShown,
                EventData(
                    popupId = popup.id,
                    surveyId = popup.surveyId,
                    productId = popup.productId,
                    extra = options.data ?: emptyMap()
                )
            )
        }

        renderPopup(popup, context)
    }

    fun showByPopupId(popupId: String, context: PlatformContext) {
        if (!ensureInitialized()) return
        val def = activePopups.values.find { it.id == popupId }
        if (def == null) {
            log("Popup definition not found", popupId)
            return
        }
        show(ShowOptions(def.surveyId, def.productId), context)
    }

    fun markSurveyAnswered(surveyId: String) {
        answeredSurveys += surveyId
        log("Marked survey answered", surveyId)
    }

    /**
     * Stub que se completará en Tareas de triggers automáticos.
     */
    fun triggerSurvey(surveyId: String, context: PlatformContext) {
        if (!ensureInitialized()) return
        val def = activePopups[surveyId]
        if (def == null) {
            log("No popup definition for surveyId", surveyId)
            return
        }
        // Futuras condiciones cooldown / answered irán aquí.
        show(ShowOptions(def.surveyId, def.productId), context)
    }

    /** Permite cierre manual desde host (si hay popup activo). */
    fun close(context: PlatformContext) {
        dismissPopup(context)
    }

    /**
     * Manejar acciones (accept / decline)
     */
    private fun handleAction(popup: PopupDefinition, action: Action) {
        val baseExtra = mutableMapOf<String, Any?>("popupId" to popup.id)
        val ctx = initOptionsContextCache
        when (action) {
            is Action.Accept -> {
                scope.launch {
                    eventBus.emit(
                        Event.SurveyCompleted,
                        EventData(
                            popupId = popup.id,
                            surveyId = popup.surveyId,
                            productId = popup.productId,
                            extra = baseExtra.apply { this["action"] = "accept"; this["surveyId"] = action.surveyId }
                        )
                    )
                    initOptions?.storage?.putLong(storagePrefix + popup.id, currentTimeMillis())
                    ctx?.let { dismissPopup(it) }
                }
            }
            is Action.Decline -> {
                scope.launch {
                    eventBus.emit(
                        Event.PopupClicked,
                        EventData(
                            popupId = popup.id,
                            surveyId = popup.surveyId,
                            productId = popup.productId,
                            extra = baseExtra.apply { this["action"] = "decline"; this["cooldownDays"] = action.cooldownDays }
                        )
                    )
                    initOptions?.storage?.putLong(storagePrefix + popup.id, currentTimeMillis())
                    ctx?.let { dismissPopup(it) }
                }
            }
        }
    }

    /**
     * Render multiplataforma
     */
    private fun renderPopup(
        popup: PopupDefinition,
        context: PlatformContext
    ) {
        scope.launch(Dispatchers.Main) {
            PopupRenderer.show(popup, context) { action ->
                handleAction(popup, action)
            }
        }
    }

    /**
     * Registrar listeners
     */
    fun on(event: Event, listener: (EventData) -> Unit) {
        scope.launch { eventBus.on(event, listener) }
    }

    fun attachContext(context: PlatformContext) {
        initOptionsContextCache = context
        log("Context attached")
        // If there are queued popups and we lacked context earlier, kick processing again
        processQueue()
    }
}
