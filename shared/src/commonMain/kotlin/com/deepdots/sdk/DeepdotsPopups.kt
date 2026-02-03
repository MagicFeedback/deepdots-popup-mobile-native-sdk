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
import kotlinx.coroutines.withContext
import kotlinx.coroutines.cancel
import kotlinx.serialization.json.Json
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import com.deepdots.sdk.service.PopupsService
import com.deepdots.sdk.service.DefaultPopupsService

class DeepdotsPopups {

    private var initOptions: InitOptions? = null
    private val eventBus = EventBus()
    private val activePopups = mutableMapOf<String, PopupDefinition>()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val answeredSurveys = mutableSetOf<String>()

    // Track current app path internally (set by host when navigation changes)
    private var currentPath: String? = null

    // Caché de último contexto usado en show para permitir dismiss desde acciones
    private var initOptionsContextCache: PlatformContext? = null

    private val triggerJobs = mutableListOf<Deferred<Unit>>()
    private val popupQueue = ArrayDeque<PopupDefinition>()
    private var processingQueue = false
    private val lastShown = mutableMapOf<String, Long>() // popupId -> timestamp (cache en memoria)
    private val storagePrefix = "popup_last_shown_"
    // Track per-page trigger hits to avoid repeated enqueue spam
    private val scrollTriggeredSurveys = mutableSetOf<String>()
    private val exitTriggeredSurveys = mutableSetOf<String>()

    // Service to fetch popups from server
    private var popupsService: PopupsService = DefaultPopupsService()

    // DTOs para parseo de respuesta del servidor
    @Serializable
    private data class ServerPopupDto(
        val id: String,
        val title: String? = "",
        val message: String? = "",
        val triggers: ServerTriggerDto? = null,
        val conditions: List<ServerConditionDto> = emptyList(),
        val actions: ServerActionsDto? = null,
        val style: JsonElement? = null, // puede venir como objeto o como string JSON
        val segments: ServerSegmentsDto? = null,
        val surveyId: String,
        val productId: String? = null,
    )
    @Serializable
    private data class ServerTriggerDto(val type: String? = null, val value: Int? = null)
    @Serializable
    private data class ServerConditionDto(val answered: Boolean? = null, val cooldownDays: Int? = null)
    @Serializable
    private data class ServerActionsDto(
        val accept: ServerActionAcceptDto? = null,
        val decline: ServerActionDeclineDto? = null,
        val start: ServerActionLabelDto? = null,
        val complete: ServerActionLabelDto? = null,
        val back: ServerActionLabelDto? = null,
    )
    @Serializable private data class ServerActionAcceptDto(val label: String? = null, val surveyId: String? = null)
    @Serializable private data class ServerActionDeclineDto(val label: String? = null, val cooldownDays: Int? = null)
    @Serializable private data class ServerActionLabelDto(val label: String? = null)
    @Serializable
    private data class ServerSegmentsDto(val lang: List<String> = emptyList(), val path: List<String> = emptyList())
    @Serializable
    private data class ServerStyleDto(val theme: String? = null, val position: String? = null, val imageUrl: String? = null)

    private fun mapStyle(style: ServerStyleDto?): com.deepdots.sdk.models.Style {
        val theme = when (style?.theme?.lowercase()) { "dark" -> com.deepdots.sdk.models.Theme.Dark; else -> com.deepdots.sdk.models.Theme.Light }
        val position = when (style?.position?.lowercase()) {
            "top-left" -> com.deepdots.sdk.models.Position.TopLeft
            "top-right" -> com.deepdots.sdk.models.Position.TopRight
            "bottom-left" -> com.deepdots.sdk.models.Position.BottomLeft
            "bottom-right" -> com.deepdots.sdk.models.Position.BottomRight
            else -> com.deepdots.sdk.models.Position.Center
        }
        return com.deepdots.sdk.models.Style(theme = theme, position = position, imageUrl = style?.imageUrl)
    }

    private fun mapActions(a: ServerActionsDto?): com.deepdots.sdk.models.Actions {
        val accept = a?.accept?.let { acc ->
            val label = acc.label ?: "Send"
            val sid = acc.surveyId ?: ""
            com.deepdots.sdk.models.Action.Accept(label = label, surveyId = sid)
        }
        val decline = a?.decline?.let { dec ->
            com.deepdots.sdk.models.Action.Decline(label = dec.label ?: "Cancel", cooldownDays = dec.cooldownDays ?: 0)
        }
        val start = a?.start?.let { com.deepdots.sdk.models.Action.Start(label = it.label ?: "Start") }
        val complete = a?.complete?.let { com.deepdots.sdk.models.Action.Complete(label = it.label ?: "Complete") }
        val back = a?.back?.let { com.deepdots.sdk.models.Action.Back(label = it.label ?: "Back") }
        return com.deepdots.sdk.models.Actions(accept = accept, decline = decline, start = start, complete = complete, back = back)
    }

    private fun mapSegments(s: ServerSegmentsDto?): com.deepdots.sdk.models.Segments? {
        if (s == null) return null
        return com.deepdots.sdk.models.Segments(lang = s.lang, path = s.path)
    }

    private fun mapConditions(conds: List<ServerConditionDto>): List<com.deepdots.sdk.models.Condition> {
        return conds.map { c -> com.deepdots.sdk.models.Condition(answered = c.answered ?: false, cooldownDays = c.cooldownDays ?: 0) }
    }

    private fun mapTrigger(t: ServerTriggerDto?, mappedConds: List<com.deepdots.sdk.models.Condition>): com.deepdots.sdk.models.Trigger {
        return when (t?.type) {
            "time_on_page" -> com.deepdots.sdk.models.Trigger.TimeOnPage(value = (t.value ?: 0).coerceAtLeast(0), condition = mappedConds)
            "scroll" -> com.deepdots.sdk.models.Trigger.Scroll(percentage = (t.value ?: 0).coerceIn(0, 100), condition = mappedConds)
            "exit" -> com.deepdots.sdk.models.Trigger.Exit(condition = mappedConds)
            null -> com.deepdots.sdk.models.Trigger.TimeOnPage(value = 2, condition = mappedConds) // fallback if triggers is {}
            else -> com.deepdots.sdk.models.Trigger.TimeOnPage(value = 2, condition = mappedConds)
        }
    }

    private fun parseServerStyle(styleField: JsonElement?): ServerStyleDto? {
        if (styleField == null) return null
        val json = Json { ignoreUnknownKeys = true; isLenient = true }
        return try {
            when (styleField) {
                is JsonObject -> json.decodeFromJsonElement(ServerStyleDto.serializer(), styleField)
                is JsonPrimitive -> {
                    val content = styleField.content
                    val trimmed = content.trim()
                    val inner = if (trimmed.startsWith('"') && trimmed.endsWith('"')) {
                        json.decodeFromString<String>(trimmed)
                    } else trimmed
                    json.decodeFromString<ServerStyleDto>(inner)
                }
                else -> null
            }
        } catch (_: Throwable) { null }
    }

    private fun isDebug(): Boolean = initOptions?.debug == true
    private fun log(msg: String, detail: Any? = null) {
        if (isDebug()) {
            println("[DeepdotsPopups] $msg" + (detail?.let { ": $it" } ?: ""))
        }
    }

    /**
     * Inicializar el SDK
     */
    fun init(options: InitOptions) {
        /*
        if (initOptions != null) {
            log("SDK already initialized")
            return
        }
        */
        initOptions = options

        if (options.mode === com.deepdots.sdk.models.Mode.Server) {
            log("Initialized in Server mode;")
            val publicKey = options.popupOptions.publicKey
            if (publicKey.isNullOrBlank()) {
                log("Server mode requires popupOptions.publicKey")
                return
            }
            // Expose runtime values for HTML builder
            SdkRuntime.publicKey = publicKey
            SdkRuntime.env = if (options.debug == true) "dev" else "prod"
            // Populate optional userId from metadata
            val userIdMeta = options.metadata?.get("userId")
            SdkRuntime.userId = when (userIdMeta) {
                is String -> userIdMeta
                is Number -> userIdMeta.toString()
                else -> null
            }
            SdkRuntime.metadata = options.metadata
            scope.launch {
                try {
                    // Build optional filter parameter
                    val filterParam: String? = run {
                        val uid = SdkRuntime.userId
                        if (!uid.isNullOrBlank()) {
                            "{" + "\"where\":{" + "\"userId\":\"$uid\"" + "}}"
                        } else null
                    }
                    // val endpointPreview = (if (SdkRuntime.env == "dev") "https://api-dev.deepdots.com" else "https://api.deepdots.com") + "/sdk/$publicKey/popups"
                    // log("Fetching popups from server", endpointPreview)
                    val responseText = popupsService.fetchPopups(publicKey, filterParam)
                    log("Server response (truncated)", responseText.take(512))
                    // Parse como array de popups
                    val list: List<ServerPopupDto> = Json { ignoreUnknownKeys = true; isLenient = true }.decodeFromString(responseText)
                    // Mapear cada item a PopupDefinition
                    list.forEach { dto ->
                        val styleDto = parseServerStyle(dto.style)
                        val conds = mapConditions(dto.conditions)
                        val trigger = mapTrigger(dto.triggers, conds)
                        val def = com.deepdots.sdk.models.PopupDefinition(
                            id = dto.id,
                            title = dto.title ?: "",
                            message = dto.message ?: "",
                            trigger = trigger,
                            actions = mapActions(dto.actions),
                            surveyId = dto.surveyId,
                            productId = dto.productId ?: "",
                            style = mapStyle(styleDto),
                            segments = mapSegments(dto.segments)
                        )
                        activePopups[def.surveyId] = def
                    }
                    if (options.autoLaunch == true) startAutoLaunch()
                } catch (t: Throwable) {
                    log("Error fetching server popups", t.message ?: "unknown")
                }
            }
            return
        } else {
            // Also populate userId and metadata in client mode
            val userIdMeta = options.metadata?.get("userId")
            SdkRuntime.userId = when (userIdMeta) {
                is String -> userIdMeta
                is Number -> userIdMeta.toString()
                else -> null
            }
            SdkRuntime.metadata = options.metadata
            // Prefer new popupOptions.popups, fallback to legacy options.popups for backward compatibility
            val defs: List<PopupDefinition> = options.popupOptions.popups ?: emptyList()
            defs.forEach { popup ->
                activePopups[popup.surveyId] = popup
            }

            log("Initialized", activePopups.keys)
            if (options.autoLaunch == true) {
                startAutoLaunch()
            }
        }
    }

    /** Public initializer alias to avoid Swift bridging conflict with `init` constructor */
    fun initialize(options: InitOptions) {
        init(options)
    }

    private fun startAutoLaunch() {
        // Cancel any previous jobs to avoid duplicate scheduling
        triggerJobs.forEach { it.cancel() }
        triggerJobs.clear()
        // Configurar triggers derivados de definiciones
        activePopups.values.forEach { def ->
            val trigger = def.trigger
            when (trigger) {
                is Trigger.TimeOnPage -> {
                    log("Scheduling TimeOnPage", "id=${def.id} sec=${trigger.value}")
                    scheduleTimeOnPage(def, trigger)
                }
                is Trigger.Scroll -> { log("Scheduling Scroll trigger", def.id) }
                is Trigger.Exit -> { log("Scheduling Exit trigger", def.id) }
            }
        }
    }

    private fun scheduleTimeOnPage(def: PopupDefinition, t: Trigger.TimeOnPage) {
        val job = scope.async {
            delay(t.value * 1000L)
            val seg = def.segments
            val langCur = initOptions?.provideLang?.invoke()
            val pathCur = currentPath
            val langOk = seg?.lang?.let { langCur != null && it.contains(langCur) } ?: true
            val pathOk = seg?.path?.let { pathCur != null && it.contains(pathCur) } ?: true
            log("TimeOnPage hit", "id=${def.id} pathCur=${'$'}pathCur requires=${'$'}{seg?.path} pathOk=${'$'}pathOk langCur=${'$'}langCur requires=${'$'}{seg?.lang} langOk=${'$'}langOk")
            if (shouldEnqueue(def, t.condition)) {
                enqueue(def)
            } else {
                log("Not enqueuing (seg/cond failed)", def.id)
            }
        }
        triggerJobs += job
    }

    private fun shouldEnqueue(def: PopupDefinition, conditions: List<Condition>): Boolean {
        // Si el survey ya fue respondido, nunca lo mostramos de nuevo
        if (answeredSurveys.contains(def.surveyId)) {
            log("Popup blocked: already answered", def.surveyId)
            return false
        }
        // Segmentación (segments ahora nullable)
        val langOk = def.segments?.lang?.let { seg ->
            val current = initOptions?.provideLang?.invoke()
            current != null && seg.contains(current)
        } ?: true
        val pathOk = def.segments?.path?.let { seg ->
            val path = currentPath
            path != null && seg.contains(path)
        } ?: true
        if (!langOk || !pathOk) {
            log("Segmentation blocked", "id=${def.id} pathOk=${'$'}pathOk langOk=${'$'}langOk currentPath=${'$'}currentPath")
            return false
        }
        // Condiciones trigger
        val ok = evaluateConditions(def, conditions)
        if (!ok) log("Conditions blocked", def.id)
        return ok
    }

    private fun evaluateConditions(def: PopupDefinition, conditions: List<Condition>): Boolean {
        if (conditions.isEmpty()) return true
        return conditions.all { c ->
            val answeredFlag = (c.answered == true)
            if (answeredFlag && answeredSurveys.contains(def.surveyId)) {
                false
            } else {
                val cd = c.cooldownDays
                if (cd > 0) {
                    val key = storagePrefix + def.id
                    val last = initOptions?.storage?.getLong(key)
                    if (last != null) {
                        val elapsed = currentTimeMillis() - last
                        val required = cd * 24L * 60L * 60L * 1000L
                        elapsed >= required
                    } else {
                        true
                    }
                } else {
                    true
                }
            }
        }
    }

    private fun enqueue(def: PopupDefinition) {
        log("Enqueue popup", def.id)
        popupQueue.addLast(def)
        processQueue()
    }

    private fun processQueue() {
        if (processingQueue) return
        processingQueue = true
        log("Start processing queue")
        scope.launch {
            while (popupQueue.isNotEmpty()) {
                val def = popupQueue.removeFirst()
                log("Process popup", "id=${def.id}")
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
                    log("Error processing popup", "${'$'}{def.id}: ${'$'}{t.message}")
                }
                // Pausa mínima entre popups para evitar stacking
                delay(300L)
            }
            processingQueue = false
            log("Queue empty; stop processing")
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
            // Post SHOWED event to API
            val publicKey = initOptions?.popupOptions?.publicKey
            if (!publicKey.isNullOrBlank()) {
                val userId = SdkRuntime.userId
                try {
                    popupsService.postPopupEvent(publicKey = publicKey, status = "SHOWED", popupId = popup.id, userId = userId)
                } catch (t: Throwable) {
                    log("Error posting SHOWED event", t.message ?: "unknown")
                }
            } else {
                log("Missing publicKey; cannot post SHOWED event")
            }
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
    private fun completeSurvey(popup: PopupDefinition) {
        scope.launch {
            eventBus.emit(
                Event.SurveyCompleted,
                EventData(
                    popupId = popup.id,
                    surveyId = popup.surveyId,
                    productId = popup.productId,
                    extra = mapOf("action" to "accept")
                )
            )
            // Post COMPLETED event to API
            val publicKey = initOptions?.popupOptions?.publicKey
            if (!publicKey.isNullOrBlank()) {
                val userId = SdkRuntime.userId
                try {
                    popupsService.postPopupEvent(publicKey = publicKey, status = "COMPLETED", popupId = popup.id, userId = userId)
                } catch (t: Throwable) {
                    log("Error posting COMPLETED event", t.message ?: "unknown")
                }
            } else {
                log("Missing publicKey; cannot post COMPLETED event")
            }

            markSurveyAnswered(popup.surveyId)
            initOptions?.storage?.putLong(storagePrefix + popup.id, currentTimeMillis())
            // Refuerza localmente que el popup no vuelva a mostrarse
            // Solo si el trigger es TimeOnPage, actualiza la condición answered
            val updatedTrigger = when (val trig = popup.trigger) {
                is Trigger.TimeOnPage -> {
                    val newConds = trig.condition.map { it.copy(answered = true) }
                    trig.copy(condition = newConds)
                }
                else -> trig
            }
            activePopups[popup.surveyId] = popup.copy(trigger = updatedTrigger)
            initOptionsContextCache?.let { dismissPopup(it) }
        }
    }

    private fun handleAction(popup: PopupDefinition, action: Action) {
        val baseExtra = mutableMapOf<String, Any?>("popupId" to popup.id)
        val ctx = initOptionsContextCache
        when (action) {
            is Action.Accept -> {
                scope.launch {
                    eventBus.emit(
                        Event.PopupClicked,
                        EventData(
                            popupId = popup.id,
                            surveyId = popup.surveyId,
                            productId = popup.productId,
                            extra = baseExtra.apply {
                                this["action"] = "accept"; this["surveyId"] = action.surveyId
                            }
                        )
                    )
                    // Do not dismiss or mark answered yet (wait for JS survey_completed)
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
                            extra = baseExtra.apply {
                                this["action"] = "decline"; this["cooldownDays"] =
                                action.cooldownDays
                            }
                        )
                    )
                    initOptions?.storage?.putLong(storagePrefix + popup.id, currentTimeMillis())
                    val ctxLocal = ctx
                    if (ctxLocal != null) {
                        withContext(Dispatchers.Main) { dismissPopup(ctxLocal) }
                    }
                }
            }

            is Action.Start -> {
                scope.launch {
                    eventBus.emit(
                        Event.PopupClicked,
                        EventData(
                            popupId = popup.id,
                            surveyId = popup.surveyId,
                            productId = popup.productId,
                            extra = baseExtra.apply { this["action"] = "start" }
                        )
                    )
                }
            }

            is Action.Complete -> {
                scope.launch {
                    eventBus.emit(
                        Event.PopupClicked,
                        EventData(
                            popupId = popup.id,
                            surveyId = popup.surveyId,
                            productId = popup.productId,
                            extra = baseExtra.apply { this["action"] = "complete" }
                        )
                    )
                    // Marcar como completado localmente para cooldown
                    initOptions?.storage?.putLong(storagePrefix + popup.id, currentTimeMillis())
                    // Llamar a completeSurvey para marcar como completado en la API
                    log("Action.Complete received; completing survey", popup.surveyId)
                    completeSurvey(popup)
                    val ctxLocal = ctx
                    if (ctxLocal != null) {
                        withContext(Dispatchers.Main) { dismissPopup(ctxLocal) }
                    }
                }
            }

            is Action.Back -> {
                scope.launch {
                    eventBus.emit(
                        Event.PopupClicked,
                        EventData(
                            popupId = popup.id,
                            surveyId = popup.surveyId,
                            productId = popup.productId,
                            extra = baseExtra.apply { this["action"] = "back" }
                        )
                    )
                }
            }

            else -> { /* no-op for future actions */
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
            PopupRenderer.show(popup, context, onAction = { action ->
                handleAction(popup, action)
            }, onDismiss = {
                // no-op; dismissal already handled per action
            })
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

    // Allow host app to update current path/page on navigation changes
    fun setPath(path: String?) {
        currentPath = path
        // Reset per-page trigger flags on navigation change
        scrollTriggeredSurveys.clear()
        exitTriggeredSurveys.clear()
        log("Path updated", path ?: "null")
        // Re-evaluate queued popups if any
        processQueue()
        // Reschedule auto-launch triggers so segmentation by path can take effect on the new page
        if (initOptions?.autoLaunch == true) {
            startAutoLaunch()
        }
    }

    /** Report scroll progress (0..100) from host page to evaluate scroll triggers */
    fun onScroll(percentage: Int) {
        val pct = percentage.coerceIn(0, 100)
        log("onScroll", pct)
        activePopups.values.forEach { def ->
            val trig = def.trigger
            if (trig is Trigger.Scroll) {
                // Already triggered for this survey on current page? skip
                if (scrollTriggeredSurveys.contains(def.surveyId)) return@forEach
                // Popup already in queue? skip
                val alreadyQueued = popupQueue.any { it.id == def.id }
                if (alreadyQueued) return@forEach
                if (pct >= trig.percentage && shouldEnqueue(def, trig.condition)) {
                    scrollTriggeredSurveys += def.surveyId
                    enqueue(def)
                }
            }
        }
    }

    /** Report exit intent from host page to evaluate exit triggers */
    fun onExit() {
        log("onExit")
        activePopups.values.forEach { def ->
            val trig = def.trigger
            if (trig is Trigger.Exit) {
                if (exitTriggeredSurveys.contains(def.surveyId)) return@forEach
                val alreadyQueued = popupQueue.any { it.id == def.id }
                if (alreadyQueued) return@forEach
                if (shouldEnqueue(def, trig.condition)) {
                    exitTriggeredSurveys += def.surveyId
                    enqueue(def)
                }
            }
        }
    }

    fun surveyCompletedFromJs(surveyId: String) {
        val popup = activePopups[surveyId] ?: return
        completeSurvey(popup)
    }

    /** Expose loaded popups for debugging/demo purposes */
    fun debugListPopups(): List<PopupDefinition> = activePopups.values.toList()
}
