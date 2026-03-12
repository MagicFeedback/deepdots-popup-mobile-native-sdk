@file:Suppress("unused")

package com.deepdots.sdk

import com.deepdots.sdk.models.Action
import com.deepdots.sdk.models.Actions
import com.deepdots.sdk.models.CooldownCondition
import com.deepdots.sdk.models.Event
import com.deepdots.sdk.models.EventData
import com.deepdots.sdk.models.InitOptions
import com.deepdots.sdk.models.LegacyCondition
import com.deepdots.sdk.models.Mode
import com.deepdots.sdk.models.PopupDefinition
import com.deepdots.sdk.models.Position
import com.deepdots.sdk.models.Segments
import com.deepdots.sdk.models.ShowOptions
import com.deepdots.sdk.models.Style
import com.deepdots.sdk.models.SurveyProgressState
import com.deepdots.sdk.models.Theme
import com.deepdots.sdk.models.Trigger
import com.deepdots.sdk.models.TriggerConditionStatus
import com.deepdots.sdk.platform.PlatformContext
import com.deepdots.sdk.platform.dismissPopup
import com.deepdots.sdk.renderer.PopupRenderer
import com.deepdots.sdk.service.DefaultPopupsService
import com.deepdots.sdk.service.PopupsService
import com.deepdots.sdk.util.currentTimeMillis
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.longOrNull
import kotlin.math.roundToInt

class DeepdotsPopups {

    @Serializable
    private data class DeferredExitPopup(
        val id: String,
        val surveyId: String,
        val dueAt: Long,
        val sourcePath: String,
    )

    @Serializable
    private data class ServerPopupDto(
        val id: String,
        val title: String? = "",
        val message: String? = "",
        val trigger: JsonElement? = null,
        val triggers: JsonElement? = null,
        val conditions: JsonElement? = null,
        val cooldown: JsonElement? = null,
        val actions: ServerActionsDto? = null,
        val style: JsonElement? = null,
        val segments: ServerSegmentsDto? = null,
        val surveyId: String,
        val productId: String? = null,
    )

    @Serializable
    private data class ServerActionsDto(
        val accept: ServerActionAcceptDto? = null,
        val decline: ServerActionDeclineDto? = null,
        val start: ServerActionLabelDto? = null,
        val complete: ServerActionLabelDto? = null,
        val back: ServerActionLabelDto? = null,
    )

    @Serializable
    private data class ServerActionAcceptDto(
        val label: String? = null,
        val surveyId: String? = null,
    )

    @Serializable
    private data class ServerActionDeclineDto(
        val label: String? = null,
        val cooldownDays: Int? = null,
    )

    @Serializable
    private data class ServerActionLabelDto(
        val label: String? = null,
    )

    @Serializable
    private data class ServerSegmentsDto(
        val lang: List<String> = emptyList(),
        val path: List<String> = emptyList(),
    )

    @Serializable
    private data class ServerStyleDto(
        val theme: String? = null,
        val position: String? = null,
        @SerialName("imageUrl") val imageUrl: String? = null,
    )

    private var initOptions: InitOptions? = null
    private var initialized = false
    private var popupsLoaded = false
    private var pendingAutoLaunch = false
    private val eventBus = EventBus()
    private val popupDefinitions = linkedMapOf<String, PopupDefinition>()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val answeredSurveys = mutableSetOf<String>()
    private val surveyProgress = mutableMapOf<String, SurveyProgressState>()
    private val surveyToPopupId = mutableMapOf<String, String>()
    private val lastShown = mutableMapOf<String, Long>()
    private var currentPath: String? = null
    private var initOptionsContextCache: PlatformContext? = null
    private val triggerJobs = mutableListOf<Job>()
    private val popupQueue = ArrayDeque<String>()
    private var processingQueue = false
    private val scrollTriggeredPopupIds = mutableSetOf<String>()
    private val deferredExitJobs = mutableMapOf<String, Job>()
    private var popupsService: PopupsService = DefaultPopupsService()
    private val json = Json { ignoreUnknownKeys = true; isLenient = true }

    private val lastShownStoragePrefix = "popup_last_shown_"
    private val exitQueueStorageKey = "__deepdots_exit_popup_queue__"
    private val dayInMs = 24L * 60L * 60L * 1000L

    fun init(options: InitOptions) {
        if (initialized) {
            log("SDK already initialized")
            return
        }

        initialized = true
        initOptions = options
        SdkRuntime.env = if (options.debug == true) "dev" else "prod"
        SdkRuntime.publicKey = options.popupOptions.publicKey
        SdkRuntime.metadata = options.metadata
        SdkRuntime.userId = when (val userIdMeta = options.metadata?.get("userId")) {
            is String -> userIdMeta
            is Number -> userIdMeta.toString()
            else -> null
        }

        if (options.mode == Mode.Server) {
            val publicKey = options.popupOptions.publicKey
            if (publicKey.isNullOrBlank()) {
                log("Server mode requires popupOptions.publicKey")
                return
            }

            scope.launch {
                try {
                    val responseText = popupsService.fetchPopups(publicKey, buildFilterParam())
                    log("Server response (truncated)", responseText.take(512))
                    val remoteDefinitions = parseServerPopups(responseText)
                    loadPopupDefinitions(remoteDefinitions)
                    if (options.autoLaunch == true || pendingAutoLaunch) {
                        startAutoLaunch()
                        pendingAutoLaunch = false
                    }
                } catch (t: Throwable) {
                    log("Error fetching server popups", t.message ?: "unknown")
                }
            }
            return
        }

        loadPopupDefinitions(options.popupOptions.popups ?: emptyList())
        if (options.autoLaunch == true) {
            autoLaunch()
        }
    }

    fun initialize(options: InitOptions) {
        init(options)
    }

    fun autoLaunch() {
        if (!ensureInitialized()) return
        if (!popupsLoaded) {
            pendingAutoLaunch = true
            log("Auto-launch deferred until popups are loaded")
            return
        }
        startAutoLaunch()
    }

    fun attachContext(context: PlatformContext) {
        initOptionsContextCache = context
        log("Context attached")
        processQueue()
    }

    fun on(event: Event, listener: (EventData) -> Unit) {
        scope.launch { eventBus.on(event, listener) }
    }

    fun off(event: Event, listener: (EventData) -> Unit) {
        scope.launch { eventBus.off(event, listener) }
    }

    fun show(options: ShowOptions, context: PlatformContext) {
        if (!ensureInitialized()) return
        attachContext(context)
        val popup = findPopupDefinition(options.surveyId)
        if (popup == null) {
            log("Popup not found for surveyId", options.surveyId)
            return
        }
        showDefinition(popup, context, options.data ?: emptyMap())
    }

    fun showByPopupId(popupId: String, context: PlatformContext) {
        if (!ensureInitialized()) return
        attachContext(context)
        val popup = popupDefinitions[popupId]
        if (popup == null) {
            log("Popup definition not found", popupId)
            return
        }
        showDefinition(popup, context)
    }

    fun triggerSurvey(surveyId: String, context: PlatformContext, popupId: String? = null) {
        if (!ensureInitialized()) return
        attachContext(context)
        val popup = findPopupDefinition(surveyId, popupId)
        if (popup == null) {
            log("No popup definition for trigger", "surveyId=$surveyId popupId=$popupId")
            return
        }
        if (!shouldShow(popup)) {
            log("Conditions prevented showing popup", popup.id)
            return
        }
        showDefinition(popup, context)
    }

    fun triggerEvent(eventName: String) {
        if (!ensureInitialized()) return
        val normalized = eventName.trim()
        if (normalized.isEmpty()) {
            log("Ignoring empty event trigger name")
            return
        }

        val matched = popupDefinitions.values.firstOrNull { popup ->
            popup.triggers.any { trigger ->
                trigger is Trigger.Event && trigger.name.trim() == normalized
            } && shouldShow(popup)
        }

        if (matched == null) {
            log("No eligible event popup", normalized)
            return
        }
        enqueuePopup(matched.id)
    }

    fun triggerClick(targetId: String) {
        if (!ensureInitialized()) return
        val normalized = targetId.trim()
        if (normalized.isEmpty()) {
            log("Ignoring empty click trigger id")
            return
        }

        val matched = popupDefinitions.values.firstOrNull { popup ->
            popup.triggers.any { trigger ->
                trigger is Trigger.Click && trigger.targetId.trim() == normalized
            } && shouldShow(popup)
        }

        if (matched == null) {
            log("No eligible click popup", normalized)
            return
        }
        enqueuePopup(matched.id)
    }

    fun markSurveyAnswered(surveyId: String) {
        answeredSurveys += surveyId
        markSurveyProgress(surveyId, TriggerConditionStatus.COMPLETED)
        log("Marked survey answered", surveyId)
    }

    fun close(context: PlatformContext) {
        dismissPopup(context)
    }

    fun setPath(path: String?) {
        val normalizedPath = normalizeUrl(path ?: "")
        if (normalizedPath == currentPath) return

        val previousPath = currentPath
        if (!previousPath.isNullOrBlank()) {
            queueExitPopups(previousPath)
        }

        currentPath = normalizedPath.ifBlank { null }
        scrollTriggeredPopupIds.clear()
        log("Path updated", currentPath ?: "null")
        processDeferredExitQueue()

        if (initOptions?.autoLaunch == true) {
            startAutoLaunch()
        }
    }

    fun onScroll(percentage: Int) {
        if (!ensureInitialized()) return
        val pct = percentage.coerceIn(0, 100)
        popupDefinitions.values.forEach { popup ->
            popup.triggers.forEach { trigger ->
                if (trigger is Trigger.Scroll) {
                    if (scrollTriggeredPopupIds.contains(popup.id)) return@forEach
                    if (pct >= trigger.percentage && shouldShow(popup)) {
                        scrollTriggeredPopupIds += popup.id
                        enqueuePopup(popup.id)
                    }
                }
            }
        }
    }

    fun onExit() {
        if (!ensureInitialized()) return
        val sourcePath = currentPath ?: return
        queueExitPopups(sourcePath)
    }

    fun surveyCompletedFromJs(surveyId: String) {
        val popup = findPopupDefinition(surveyId) ?: return
        completeSurvey(popup, dismissAfter = false)
    }

    fun debugListPopups(): List<PopupDefinition> = popupDefinitions.values.toList()

    internal fun debugQueuedPopupIds(): List<String> = popupQueue.toList()

    internal fun debugDeferredExitQueue(): List<String> = getDeferredExitQueue().map { "${it.id}@${it.sourcePath}" }

    internal fun debugProgressStatus(surveyId: String): TriggerConditionStatus? = surveyProgress[surveyId]?.status

    internal fun debugLastShownAt(popupId: String): Long? = getLastShown(popupId)

    internal fun debugShouldShowPopup(
        popupId: String,
        pathOverride: String? = null,
        skipPathCheck: Boolean = false,
    ): Boolean {
        val popup = popupDefinitions[popupId] ?: return false
        return shouldShow(popup, pathOverride = pathOverride, skipPathCheck = skipPathCheck)
    }

    internal fun debugHandleSurveyRuntimeEvent(popupId: String, name: String, payload: String? = null) {
        val popup = popupDefinitions[popupId] ?: return
        handleSurveyRuntimeEvent(popup, name, payload)
    }

    internal fun debugParseServerPayload(payload: String): List<PopupDefinition> = parseServerPopups(payload)

    private fun startAutoLaunch() {
        triggerJobs.forEach { it.cancel() }
        triggerJobs.clear()

        popupDefinitions.values.forEach { popup ->
            popup.triggers.forEach { trigger ->
                when (trigger) {
                    is Trigger.TimeOnPage -> scheduleTimeOnPage(popup, trigger)
                    is Trigger.Scroll -> log("Registered Scroll trigger", popup.id)
                    is Trigger.Exit -> log("Registered Exit trigger", popup.id)
                    is Trigger.Event -> log("Registered Event trigger", "${popup.id}:${trigger.name}")
                    is Trigger.Click -> log("Registered Click trigger", "${popup.id}:${trigger.targetId}")
                }
            }
        }
    }

    private fun scheduleTimeOnPage(popup: PopupDefinition, trigger: Trigger.TimeOnPage) {
        val delayMs = (trigger.seconds * 1000.0).toLong().coerceAtLeast(0L)
        val job = scope.launch {
            delay(delayMs)
            if (shouldShow(popup)) {
                enqueuePopup(popup.id)
            }
        }
        triggerJobs += job
    }

    private fun loadPopupDefinitions(definitions: List<PopupDefinition>) {
        popupDefinitions.clear()
        definitions.mapNotNull(::normalizePopupDefinition).forEach { popup ->
            popupDefinitions[popup.id] = popup
        }
        popupsLoaded = true
        processDeferredExitQueue()
        log("Loaded popups", popupDefinitions.keys.joinToString())
    }

    private fun normalizePopupDefinition(definition: PopupDefinition): PopupDefinition? {
        val normalizedTriggers = normalizeTriggers(definition)
        if (normalizedTriggers.isEmpty()) {
            log("Ignoring popup without valid triggers", definition.id)
            return null
        }

        val normalizedLegacyConditions = (definition.legacyConditions + definition.conditions)
            .filter { it.cooldownDays >= 0 }
        val normalizedCooldown = definition.cooldown.filter { it.cooldownDays >= 0 }

        return definition.copy(
            trigger = normalizedTriggers.firstOrNull(),
            triggers = normalizedTriggers,
            conditions = normalizedLegacyConditions,
            legacyConditions = normalizedLegacyConditions,
            cooldown = normalizedCooldown,
        )
    }

    private fun normalizeTriggers(definition: PopupDefinition): List<Trigger> {
        val rawTriggers = buildList {
            definition.trigger?.let(::add)
            addAll(definition.triggers)
        }

        return rawTriggers.mapNotNull { trigger ->
            when (trigger) {
                is Trigger.TimeOnPage -> Trigger.TimeOnPage(seconds = trigger.seconds.coerceAtLeast(0.0))
                is Trigger.Scroll -> Trigger.Scroll(percentage = trigger.percentage.coerceIn(0, 100))
                is Trigger.Exit -> Trigger.Exit(delaySeconds = trigger.delaySeconds.coerceAtLeast(0.0))
                is Trigger.Event -> trigger.name.trim().takeIf { it.isNotEmpty() }?.let(Trigger::Event)
                is Trigger.Click -> trigger.targetId.trim().takeIf { it.isNotEmpty() }?.let(Trigger::Click)
            }
        }
    }

    private fun enqueuePopup(popupId: String) {
        if (popupQueue.contains(popupId)) return
        popupQueue.addLast(popupId)
        processQueue()
    }

    private fun processQueue() {
        val context = initOptionsContextCache ?: return
        if (processingQueue) return
        processingQueue = true

        scope.launch {
            while (popupQueue.isNotEmpty()) {
                val popupId = popupQueue.removeFirst()
                val popup = popupDefinitions[popupId] ?: continue
                showDefinition(popup, context)
                delay(300L)
            }
            processingQueue = false
        }
    }

    private fun showDefinition(
        popup: PopupDefinition,
        context: PlatformContext,
        extra: Map<String, Any?> = emptyMap(),
    ) {
        initOptionsContextCache = context
        surveyToPopupId[popup.surveyId] = popup.id
        setLastShown(popup.id, currentTimeMillis())

        scope.launch {
            eventBus.emit(
                Event.PopupShown,
                EventData(
                    popupId = popup.id,
                    surveyId = popup.surveyId,
                    productId = popup.productId,
                    extra = extra,
                ),
            )

            val publicKey = initOptions?.popupOptions?.publicKey
            if (!publicKey.isNullOrBlank()) {
                try {
                    popupsService.postPopupEvent(
                        publicKey = publicKey,
                        status = "opened",
                        popupId = popup.id,
                        userId = SdkRuntime.userId,
                    )
                } catch (t: Throwable) {
                    log("Error posting opened event", t.message ?: "unknown")
                }
            }
        }

        renderPopup(popup, context)
    }

    private fun renderPopup(popup: PopupDefinition, context: PlatformContext) {
        scope.launch(Dispatchers.Main) {
            PopupRenderer.show(
                popup = popup,
                context = context,
                onAction = { action -> handleAction(popup, action) },
                onSurveyEvent = { name, payload -> handleSurveyRuntimeEvent(popup, name, payload) },
                onDismiss = { },
            )
        }
    }

    private fun handleAction(popup: PopupDefinition, action: Action) {
        val extra = mutableMapOf<String, Any?>()
        val context = initOptionsContextCache

        when (action) {
            is Action.Accept -> {
                extra["action"] = "accept"
                extra["surveyId"] = action.surveyId
                emitPopupClicked(popup, extra)
            }

            is Action.Decline -> {
                extra["action"] = "decline"
                extra["cooldownDays"] = action.cooldownDays
                setLastShown(popup.id, currentTimeMillis())
                emitPopupClicked(popup, extra)
                if (context != null) {
                    scope.launch(Dispatchers.Main) { dismissPopup(context) }
                }
            }

            is Action.Start -> {
                extra["action"] = "start"
                emitPopupClicked(popup, extra)
            }

            is Action.Complete -> {
                extra["action"] = "complete"
                emitPopupClicked(popup, extra)
                completeSurvey(popup, dismissAfter = true)
            }

            is Action.Back -> {
                extra["action"] = "back"
                emitPopupClicked(popup, extra)
            }
        }
    }

    private fun handleSurveyRuntimeEvent(popup: PopupDefinition, name: String, payload: String?) {
        when (name) {
            "popup_clicked" -> {
                val extra = parseEventPayload(payload)
                emitPopupClicked(popup, extra)
                val action = extra["action"] as? String
                if (action == "partial") {
                    markSurveyProgress(popup.surveyId, TriggerConditionStatus.PARTIAL)
                }
            }

            "survey_completed" -> {
                completeSurvey(popup, dismissAfter = false)
            }
        }
    }

    private fun completeSurvey(popup: PopupDefinition, dismissAfter: Boolean) {
        val alreadyCompleted = surveyProgress[popup.surveyId]?.status == TriggerConditionStatus.COMPLETED
        if (alreadyCompleted) {
            if (dismissAfter) {
                initOptionsContextCache?.let { context ->
                    scope.launch(Dispatchers.Main) { dismissPopup(context) }
                }
            }
            return
        }

        markSurveyAnswered(popup.surveyId)
        setLastShown(popup.id, currentTimeMillis())

        scope.launch {
            eventBus.emit(
                Event.SurveyCompleted,
                EventData(
                    popupId = popup.id,
                    surveyId = popup.surveyId,
                    productId = popup.productId,
                    extra = mapOf("action" to "completed"),
                ),
            )

            val publicKey = initOptions?.popupOptions?.publicKey
            if (!publicKey.isNullOrBlank()) {
                try {
                    popupsService.postPopupEvent(
                        publicKey = publicKey,
                        status = "completed",
                        popupId = popup.id,
                        userId = SdkRuntime.userId,
                    )
                } catch (t: Throwable) {
                    log("Error posting completed event", t.message ?: "unknown")
                }
            }

            if (dismissAfter) {
                initOptionsContextCache?.let { context ->
                    withContext(Dispatchers.Main) { dismissPopup(context) }
                }
            }
        }
    }

    private fun emitPopupClicked(popup: PopupDefinition, extra: Map<String, Any?>) {
        scope.launch {
            eventBus.emit(
                Event.PopupClicked,
                EventData(
                    popupId = popup.id,
                    surveyId = popup.surveyId,
                    productId = popup.productId,
                    extra = extra,
                ),
            )
        }
    }

    private fun queueExitPopups(sourcePath: String) {
        val normalizedSource = normalizeUrl(sourcePath)
        popupDefinitions.values.forEach { popup ->
            popup.triggers.forEach { trigger ->
                if (trigger is Trigger.Exit) {
                    queueExitPopup(popup, trigger.delaySeconds, normalizedSource)
                }
            }
        }
    }

    private fun queueExitPopup(popup: PopupDefinition, delaySeconds: Double, sourcePath: String) {
        if (!shouldShow(popup, pathOverride = sourcePath)) return

        val dueAt = currentTimeMillis() + (delaySeconds * 1000.0).toLong().coerceAtLeast(0L)
        val item = DeferredExitPopup(
            id = popup.id,
            surveyId = popup.surveyId,
            dueAt = dueAt,
            sourcePath = sourcePath,
        )

        val updatedQueue = getDeferredExitQueue()
            .filterNot { it.id == item.id && it.sourcePath == item.sourcePath } + item
        setDeferredExitQueue(updatedQueue)
        scheduleDeferredExit(item)
    }

    private fun processDeferredExitQueue() {
        val queue = getDeferredExitQueue()
        if (queue.isEmpty()) return

        val now = currentTimeMillis()
        queue.forEach { item ->
            if (item.dueAt <= now) {
                tryShowDeferredExit(item)
            } else {
                scheduleDeferredExit(item)
            }
        }
    }

    private fun scheduleDeferredExit(item: DeferredExitPopup) {
        val key = deferredExitKey(item)
        if (deferredExitJobs[key]?.isActive == true) return

        deferredExitJobs[key] = scope.launch {
            val delayMs = (item.dueAt - currentTimeMillis()).coerceAtLeast(0L)
            delay(delayMs)
            tryShowDeferredExit(item)
        }
    }

    private fun tryShowDeferredExit(item: DeferredExitPopup) {
        val popup = popupDefinitions[item.id]
        if (popup == null) {
            removeDeferredExit(item)
            return
        }

        val destinationPath = currentPath
        if (destinationPath.isNullOrBlank() || normalizeUrl(destinationPath) == normalizeUrl(item.sourcePath)) {
            removeDeferredExit(item)
            log("Exit popup dropped because route did not change", item.id)
            return
        }

        if (!shouldShow(popup, skipPathCheck = true)) {
            removeDeferredExit(item)
            return
        }

        removeDeferredExit(item)
        enqueuePopup(popup.id)
    }

    private fun removeDeferredExit(item: DeferredExitPopup) {
        deferredExitJobs.remove(deferredExitKey(item))?.cancel()
        val updatedQueue = getDeferredExitQueue()
            .filterNot { it.id == item.id && it.sourcePath == item.sourcePath }
        setDeferredExitQueue(updatedQueue)
    }

    private fun getDeferredExitQueue(): List<DeferredExitPopup> {
        val raw = initOptions?.storage?.getString(exitQueueStorageKey) ?: return emptyList()
        return try {
            json.decodeFromString<List<DeferredExitPopup>>(raw)
        } catch (_: Throwable) {
            emptyList()
        }
    }

    private fun setDeferredExitQueue(queue: List<DeferredExitPopup>) {
        val storage = initOptions?.storage ?: return
        if (queue.isEmpty()) {
            storage.remove(exitQueueStorageKey)
            return
        }
        storage.putString(exitQueueStorageKey, json.encodeToString(queue))
    }

    private fun shouldShow(
        popup: PopupDefinition,
        pathOverride: String? = null,
        skipPathCheck: Boolean = false,
    ): Boolean {
        if (!skipPathCheck && !matchesSegmentsPath(popup, pathOverride ?: currentPath)) {
            return false
        }

        if (popup.cooldown.isNotEmpty() && !popup.cooldown.all { evaluateCooldown(popup, it) }) {
            return false
        }

        if (popup.legacyConditions.isNotEmpty() && !popup.legacyConditions.all { evaluateLegacyCondition(popup, it) }) {
            return false
        }

        return true
    }

    private fun matchesSegmentsPath(popup: PopupDefinition, pathValue: String?): Boolean {
        val paths = popup.segments?.path.orEmpty()
        if (paths.isEmpty()) return true

        val normalizedHref = normalizeUrl(pathValue ?: "")
        if (normalizedHref.isEmpty()) return false
        val normalizedPath = normalizePathName(normalizedHref)

        return paths.any { rawCandidate ->
            val candidate = normalizeUrl(rawCandidate)
            when {
                candidate.startsWith("http://") || candidate.startsWith("https://") -> normalizedHref == candidate
                candidate.startsWith("/") -> normalizedHref.contains(candidate)
                else -> normalizedPath == candidate
            }
        }
    }

    private fun evaluateCooldown(popup: PopupDefinition, condition: CooldownCondition): Boolean {
        return when (condition.answered) {
            TriggerConditionStatus.SHOWED -> hasCooldownElapsed(getLastShown(popup.id), condition.cooldownDays)
            TriggerConditionStatus.PARTIAL,
            TriggerConditionStatus.COMPLETED -> {
                val progress = surveyProgress[popup.surveyId]
                if (progress?.status != condition.answered) {
                    true
                } else {
                    hasCooldownElapsed(progress.timestamp, condition.cooldownDays)
                }
            }
        }
    }

    private fun evaluateLegacyCondition(popup: PopupDefinition, condition: LegacyCondition): Boolean {
        if (condition.answered == false && answeredSurveys.contains(popup.surveyId)) {
            return false
        }
        return hasCooldownElapsed(getLastShown(popup.id), condition.cooldownDays)
    }

    private fun hasCooldownElapsed(timestamp: Long?, cooldownDays: Int): Boolean {
        if (timestamp == null || cooldownDays <= 0) return true
        return currentTimeMillis() - timestamp >= cooldownDays * dayInMs
    }

    private fun markSurveyProgress(surveyId: String, status: TriggerConditionStatus) {
        val current = surveyProgress[surveyId]
        if (current?.status == TriggerConditionStatus.COMPLETED && status != TriggerConditionStatus.COMPLETED) {
            return
        }
        surveyProgress[surveyId] = SurveyProgressState(status = status, timestamp = currentTimeMillis())
    }

    private fun findPopupDefinition(surveyId: String, popupId: String? = null): PopupDefinition? {
        if (!popupId.isNullOrBlank()) {
            popupDefinitions[popupId]?.let { return it }
        }
        return popupDefinitions.values.firstOrNull { it.surveyId == surveyId }
    }

    private fun getLastShown(popupId: String): Long? {
        return lastShown[popupId] ?: initOptions?.storage?.getLong(lastShownStoragePrefix + popupId)
    }

    private fun setLastShown(popupId: String, timestamp: Long) {
        lastShown[popupId] = timestamp
        initOptions?.storage?.putLong(lastShownStoragePrefix + popupId, timestamp)
    }

    private fun buildFilterParam(): String? {
        val userId = SdkRuntime.userId
        return if (!userId.isNullOrBlank()) {
            "{" + "\"where\":{" + "\"userId\":\"$userId\"" + "}}"
        } else {
            null
        }
    }

    private fun parseServerPopups(payload: String): List<PopupDefinition> {
        val root = json.parseToJsonElement(payload)
        val items = root as? JsonArray ?: return emptyList()
        return items.mapNotNull { item ->
            val obj = item as? JsonObject ?: return@mapNotNull null
            mapServerPopup(obj)
        }
    }

    private fun mapServerPopup(source: JsonObject): PopupDefinition? {
        val dto = runCatching {
            json.decodeFromJsonElement(ServerPopupDto.serializer(), source)
        }.getOrElse { error ->
            log("Skipping malformed popup payload", error.message ?: "unknown")
            return null
        }
        return mapServerPopup(dto, source)
    }

    private fun mapServerPopup(dto: ServerPopupDto, source: JsonObject): PopupDefinition? {
        val rawTriggerItems = rawTriggerItems(source["trigger"] ?: dto.trigger, source["triggers"] ?: dto.triggers)
        val triggers = rawTriggerItems.mapNotNull(::mapTrigger)
        if (triggers.isEmpty()) return null

        val legacyConditions = buildList {
            addAll(parseLegacyConditions(source["conditions"] ?: dto.conditions))
            rawTriggerItems.forEach { triggerItem ->
                if (triggerItem is JsonObject) {
                    addAll(parseLegacyConditions(triggerItem["condition"]))
                }
            }
        }

        return normalizePopupDefinition(
            PopupDefinition(
                id = dto.id,
                title = dto.title ?: "",
                message = dto.message ?: "",
                trigger = null,
                triggers = triggers,
                conditions = legacyConditions,
                legacyConditions = legacyConditions,
                cooldown = parseCooldown(source["cooldown"] ?: dto.cooldown),
                actions = mapActions(dto.actions),
                surveyId = dto.surveyId,
                productId = dto.productId ?: "",
                style = mapStyle(parseServerStyle(source["style"] ?: dto.style)),
                segments = mapSegments(dto.segments),
            ),
        )
    }

    private fun rawTriggerItems(trigger: JsonElement?, triggers: JsonElement?): List<JsonElement> {
        val raw = triggers ?: trigger ?: return emptyList()
        return when (raw) {
            is JsonArray -> raw.toList()
            else -> listOf(raw)
        }
    }

    private fun mapTrigger(element: JsonElement): Trigger? {
        val obj = element as? JsonObject ?: return null
        val type = obj["type"]?.jsonPrimitive?.content?.trim()?.lowercase() ?: return null
        return when (type) {
            "time_on_page" -> parseDouble(obj["value"])?.let { Trigger.TimeOnPage(it.coerceAtLeast(0.0)) }
            "scroll" -> parseDouble(obj["value"])?.let { Trigger.Scroll(it.roundToInt().coerceIn(0, 100)) }
            "exit" -> Trigger.Exit(parseDouble(obj["value"])?.coerceAtLeast(0.0) ?: 0.0)
            "event" -> parseString(obj["value"])?.trim()?.takeIf { it.isNotEmpty() }?.let(Trigger::Event)
            "click" -> parseString(obj["value"])?.trim()?.takeIf { it.isNotEmpty() }?.let(Trigger::Click)
            else -> null
        }
    }

    private fun parseCooldown(element: JsonElement?): List<CooldownCondition> {
        val array = element as? JsonArray ?: return emptyList()
        return array.mapNotNull { item ->
            val obj = item as? JsonObject ?: return@mapNotNull null
            val answered = when (obj["answered"]?.jsonPrimitive?.content?.uppercase()) {
                "SHOWED" -> TriggerConditionStatus.SHOWED
                "PARTIAL" -> TriggerConditionStatus.PARTIAL
                "COMPLETED" -> TriggerConditionStatus.COMPLETED
                else -> null
            } ?: return@mapNotNull null

            CooldownCondition(
                answered = answered,
                cooldownDays = parseDouble(obj["cooldownDays"])?.roundToInt()?.coerceAtLeast(0) ?: 0,
            )
        }
    }

    private fun parseLegacyConditions(element: JsonElement?): List<LegacyCondition> {
        val array = element as? JsonArray ?: return emptyList()
        return array.mapNotNull { item ->
            val obj = item as? JsonObject ?: return@mapNotNull null
            val answered = obj["answered"]?.let { primitive ->
                (primitive as? JsonPrimitive)?.booleanOrNull
            }
            LegacyCondition(
                answered = answered,
                cooldownDays = parseDouble(obj["cooldownDays"])?.roundToInt()?.coerceAtLeast(0) ?: 0,
            )
        }
    }

    private fun mapActions(actions: ServerActionsDto?): Actions {
        val accept = actions?.accept?.let {
            Action.Accept(label = it.label ?: "Send", surveyId = it.surveyId ?: "")
        }
        val decline = actions?.decline?.let {
            Action.Decline(label = it.label ?: "Cancel", cooldownDays = it.cooldownDays ?: 0)
        }
        val start = actions?.start?.let { Action.Start(label = it.label ?: "Start") }
        val complete = actions?.complete?.let { Action.Complete(label = it.label ?: "Complete") }
        val back = actions?.back?.let { Action.Back(label = it.label ?: "Back") }
        return Actions(accept = accept, decline = decline, start = start, complete = complete, back = back)
    }

    private fun mapSegments(segments: ServerSegmentsDto?): Segments? {
        if (segments == null) return null
        return Segments(lang = segments.lang, path = segments.path)
    }

    private fun parseServerStyle(styleField: JsonElement?): ServerStyleDto? {
        if (styleField == null || styleField is JsonNull) return null
        return try {
            when (styleField) {
                is JsonObject -> json.decodeFromJsonElement(ServerStyleDto.serializer(), styleField)
                is JsonPrimitive -> {
                    val content = styleField.content.trim()
                    val inner = if (content.startsWith("\"") && content.endsWith("\"")) {
                        json.decodeFromString<String>(content)
                    } else {
                        content
                    }
                    json.decodeFromString<ServerStyleDto>(inner)
                }
                else -> null
            }
        } catch (_: Throwable) {
            null
        }
    }

    private fun mapStyle(style: ServerStyleDto?): Style {
        val theme = when (style?.theme?.lowercase()) {
            "dark" -> Theme.Dark
            else -> Theme.Light
        }
        val position = when (style?.position?.lowercase()) {
            "top-left" -> Position.TopLeft
            "top-right" -> Position.TopRight
            "bottom-left" -> Position.BottomLeft
            "bottom-right" -> Position.BottomRight
            else -> Position.Center
        }
        return Style(theme = theme, position = position, imageUrl = style?.imageUrl)
    }

    private fun parseEventPayload(payload: String?): Map<String, Any?> {
        if (payload.isNullOrBlank()) return emptyMap()
        return try {
            val root = json.parseToJsonElement(payload)
            val obj = root as? JsonObject ?: return emptyMap()
            val payloadElement = obj["payload"] ?: root
            val normalized = when (payloadElement) {
                is JsonObject -> payloadElement
                else -> obj
            }
            normalized.mapValues { (_, value) -> jsonValueToAny(value) }
        } catch (_: Throwable) {
            emptyMap()
        }
    }

    private fun jsonValueToAny(element: JsonElement): Any? {
        return when (element) {
            is JsonNull -> null
            is JsonPrimitive -> {
                when {
                    element.isString -> element.content
                    element.booleanOrNull != null -> element.booleanOrNull
                    element.longOrNull != null -> element.longOrNull
                    element.doubleOrNull != null -> element.doubleOrNull
                    else -> element.content
                }
            }
            is JsonObject -> element.mapValues { (_, value) -> jsonValueToAny(value) }
            is JsonArray -> element.map { jsonValueToAny(it) }
        }
    }

    private fun parseString(element: JsonElement?): String? {
        return (element as? JsonPrimitive)?.content
    }

    private fun parseDouble(element: JsonElement?): Double? {
        val primitive = element as? JsonPrimitive ?: return null
        return primitive.doubleOrNull ?: primitive.content.toDoubleOrNull()
    }

    private fun normalizeUrl(value: String): String {
        if (value.isBlank()) return ""
        val withoutIndex = value.replace(Regex("/index\\.html(?=($|[?#]))", RegexOption.IGNORE_CASE), "")
        return if (withoutIndex.length > 1 && withoutIndex.endsWith("/")) {
            withoutIndex.dropLast(1)
        } else {
            withoutIndex
        }
    }

    private fun normalizePathName(value: String): String {
        val withoutOrigin = value
            .replace(Regex("^https?://[^/]+"), "")
            .substringBefore("?")
            .substringBefore("#")
        return normalizeUrl(withoutOrigin.ifBlank { "/" })
    }

    private fun deferredExitKey(item: DeferredExitPopup): String = "${item.id}::${item.sourcePath}"

    private fun ensureInitialized(): Boolean {
        if (!initialized) {
            log("SDK not initialized")
            return false
        }
        return true
    }

    private fun isDebug(): Boolean = initOptions?.debug == true

    private fun log(msg: String, detail: Any? = null) {
        if (isDebug()) {
            println("[DeepdotsPopups] $msg" + (detail?.let { ": $it" } ?: ""))
        }
    }
}
