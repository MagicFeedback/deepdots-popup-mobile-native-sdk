@file:Suppress("unused")

package com.deepdots.sdk

import com.deepdots.sdk.models.Action
import com.deepdots.sdk.models.Actions
import com.deepdots.sdk.models.CooldownCondition
import com.deepdots.sdk.models.Event
import com.deepdots.sdk.models.Environment
import com.deepdots.sdk.models.EventData
import com.deepdots.sdk.models.InitOptions
import com.deepdots.sdk.models.LegacyCondition
import com.deepdots.sdk.models.PopupDefinition
import com.deepdots.sdk.models.PopupFont
import com.deepdots.sdk.models.Position
import com.deepdots.sdk.models.Segments
import com.deepdots.sdk.models.ShowOptions
import com.deepdots.sdk.models.Style
import com.deepdots.sdk.models.SurveyProgressState
import com.deepdots.sdk.models.Theme
import com.deepdots.sdk.tracking.TrackingManager
import com.deepdots.sdk.storage.KeyValueStorage
import com.deepdots.sdk.storage.createDefaultStorage
import com.deepdots.sdk.analytics.AnalyticsManager
import com.deepdots.sdk.analytics.AnalyticsEnvelope
import com.deepdots.sdk.analytics.AnalyticsContext
import com.deepdots.sdk.analytics.AnalyticsIdentity
import com.deepdots.sdk.analytics.collectGeoInfo
import com.deepdots.sdk.analytics.CrashReporter
import com.deepdots.sdk.analytics.DeviceSnapshot
import com.deepdots.sdk.analytics.crashRecordToParams
import com.deepdots.sdk.analytics.installCrashHandlers
import com.deepdots.sdk.tracking.NavigationObserver
import com.deepdots.sdk.models.Trigger
import com.deepdots.sdk.models.TriggerConditionStatus
import com.deepdots.sdk.i18n.DefaultLabels
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
import kotlinx.coroutines.isActive
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

private const val ANALYTICS_MAX_BATCH_SIZE = 20
private const val ANALYTICS_FLUSH_INTERVAL_MS = 30_000L

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
    private data class ServerFontDto(
        val family: String? = null,
        val url: String? = null,
    )

    @Serializable
    private data class ServerStyleDto(
        val theme: String? = null,
        val position: String? = null,
        @SerialName("imageUrl") val imageUrl: String? = null,
        val font: ServerFontDto? = null,
    )

    private var initOptions: InitOptions? = null
    private var initialized = false
    private var popupsLoaded = false

    /**
     * Backend environment the SDK is talking to.
     *
     * Reflects the [Environment] passed in [InitOptions.environment]. Defaults to
     * [Environment.Production] before [init] is called. Useful for host apps that
     * want to assert at runtime which backend their public key is hitting and
     * surface it in their own diagnostics or logs.
     */
    var environment: Environment = Environment.Production
        private set
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
    /** Identidad + sesión (Fase 1 tracking). Null hasta init(). */
    private var tracking: TrackingManager? = null
    /** Capa de analytics (canal separado del feedback). Null hasta init(). */
    private var analytics: AnalyticsManager? = null
    /** feedbackSessionId cacheado del canal de analytics (devuelto por POST /sdk/feedback). */
    private var analyticsFeedbackSessionId: String? = null
    /** Observador de navegación (Fase 2): emite page_view por el canal de analytics. */
    private var navObserver: NavigationObserver? = null
    private var navStarted = false
    /** Tiempo activo (engagement time, #8). */
    private var engagement: com.deepdots.sdk.analytics.EngagementTracker? = null
    /** Storage resuelto internamente: el del host si lo pasa, si no el persistente por defecto. */
    private var resolvedStorage: KeyValueStorage? = null
    /** Crash & error reporting (#14–17). Null hasta init(). */
    private var crashReporter: CrashReporter? = null
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
        environment = options.environment ?: Environment.Production
        SdkRuntime.env = when (environment) {
            Environment.Development -> "dev"
            else -> "prod"
        }
        SdkRuntime.publicKey = options.popupOptions.publicKey
        SdkRuntime.metadata = options.metadata
        SdkRuntime.provideLang = options.provideLang
        SdkRuntime.userId = when (val userIdMeta = options.metadata?.get("userId")) {
            is String -> userIdMeta
            is Number -> userIdMeta.toString()
            else -> null
        }

        // Storage interno: host > persistente por defecto (SharedPreferences/NSUserDefaults).
        val storage = options.storage ?: createDefaultStorage()
        resolvedStorage = storage
        // user_id lo gestiona el SDK (persistente); el session_id lo provee el backend
        // (respuesta de POST /sdk/popups) y se cachea — el SDK no genera ni expira sesiones.
        val tm = TrackingManager(storage = storage, clientUserId = SdkRuntime.userId, enabled = options.trackingEnabled ?: true)
        tracking = tm
        // Expone el user_id resuelto para el builder del HTML del WebView (inyección §5).
        SdkRuntime.userId = tm.getUserId()

        // Analytics: se envía como Feedback a la integración (POST /sdk/feedback) si se
        // pasan claves en options.analytics; si no, queda en dry-run (solo println).
        val analyticsKeys = options.analytics
        val analyticsSink: com.deepdots.sdk.analytics.AnalyticsSink? = if (analyticsKeys != null) {
            { envelope ->
                val body = com.deepdots.sdk.analytics.buildAnalyticsFeedbackBody(envelope, analyticsKeys, analyticsFeedbackSessionId)
                scope.launch {
                    try {
                        val returnedSessionId = popupsService.postFeedback(body)
                        if (returnedSessionId != null && returnedSessionId != analyticsFeedbackSessionId) {
                            analyticsFeedbackSessionId = returnedSessionId
                            SdkRuntime.analyticsFeedbackSessionId = returnedSessionId
                            log("analytics · feedbackSessionId cacheado: $analyticsFeedbackSessionId")
                        }
                    } catch (t: Throwable) {
                        log("analytics · error enviando feedback", t.message)
                    }
                }
            }
        } else {
            null
        }
        val device = com.deepdots.sdk.analytics.collectDeviceInfo()
        analytics = AnalyticsManager(
            sink = analyticsSink ?: com.deepdots.sdk.analytics.dryRunSink,
            publicKey = analyticsKeys?.publicKey ?: options.popupOptions.publicKey,
            platform = if (getPlatform().name.startsWith("iOS", ignoreCase = true)) "ios" else "android",
            language = options.provideLang.invoke(),
            device = device,
            maxBatchSize = ANALYTICS_MAX_BATCH_SIZE,
            onFlushNeeded = { flushAnalytics() },
        )
        // Crash & error reporting (#14–17): captura uncaught (a disco, replay en el siguiente
        // arranque) y expone reportError() para el host (emite ya).
        val crash = CrashReporter(
            storage = storage,
            emit = { params -> track("deepdots_app_crash", params) },
            device = { DeviceSnapshot(appVersion = device.appVersion, osVersion = device.osVersion, deviceModel = device.deviceModel) },
            sessionId = { tracking?.getSessionId() },
            now = { currentTimeMillis() },
            enabled = { tracking?.isTrackingEnabled() == true },
        )
        crashReporter = crash
        if (tracking?.isTrackingEnabled() == true) {
            installCrashHandlers(crash) { tracking?.isTrackingEnabled() == true }
        }
        // Marca de inicio de sesión (base para Crash-Free Users #14).
        track("deepdots_session_start", emptyMap())
        // Drena SIEMPRE la cola (descarta pendientes si tracking off); solo reenvía si activo.
        val pendingCrashes = crash.drainPendingCrashes()
        if (tracking?.isTrackingEnabled() == true) {
            for (rec in pendingCrashes) track("deepdots_app_crash", crashRecordToParams(rec))
        }
        // Flush periódico: envía el lote cada 30 s mientras la app está activa.
        scope.launch {
            while (isActive) {
                delay(ANALYTICS_FLUSH_INTERVAL_MS)
                flushAnalytics()
            }
        }

        // Geo info async: country/city via ipapi.co (fire-and-forget, igual que Web)
        scope.launch {
            try {
                val geo = collectGeoInfo()
                if (geo != null) analytics?.updateDevice(geo)
            } catch (_: Throwable) {}
        }

        // Fase 2: navegación → eventos page_view por el canal de analytics.
        // En KMP la navegación entra por setPath() (manual); ahí se alimenta el observador.
        navObserver = NavigationObserver().also { obs ->
            obs.onVisit { v -> track("deepdots_page_view", mapOf("screen" to v.screen, "duration_seconds" to v.durationSeconds)) }
        }
        // Engagement time (#8): cuenta tiempo activo en primer plano (resume al arrancar).
        engagement = com.deepdots.sdk.analytics.EngagementTracker().also { it.resume() }

        // Los popups se reciben SIEMPRE de la API (no se definen en init).
        val publicKey = options.popupOptions.publicKey
        if (publicKey.isNullOrBlank()) {
            log("Missing publicKey; cannot fetch popups from API")
            return
        }
        scope.launch {
            try {
                val responseText = popupsService.fetchPopups(publicKey, buildFilterParam())
                log("API response (truncated)", responseText.take(512))
                val remoteDefinitions = parseServerPopups(responseText)
                loadPopupDefinitions(remoteDefinitions)
                if (options.autoLaunch == true || pendingAutoLaunch) {
                    startAutoLaunch()
                    pendingAutoLaunch = false
                }
            } catch (t: Throwable) {
                log("Error fetching popups", t.message ?: "unknown")
            }
        }
    }

    /** User id actual (generado por el SDK o provisto por el cliente). Null si tracking off. */
    fun getUserId(): String? = tracking?.getUserId()

    /** Session id de navegación actual. Null si tracking off. */
    fun getSessionId(): String? = tracking?.getSessionId()

    /** Activa/desactiva el tracking (identidad + sesión). Kill-switch del contrato §7bis. */
    fun setTrackingEnabled(enabled: Boolean) { tracking?.setTrackingEnabled(enabled) }

    // ───────── Analytics (canal separado del feedback, vinculado por user_id) ─────────

    /** Registra un evento de analítica (modelo GA: nombre + params). No-op si tracking off. */
    fun track(name: String, params: Map<String, Any?>? = null) {
        if (tracking?.isTrackingEnabled() != true) return
        analytics?.track(name, params)
    }

    /** User attributes para breakdowns (registration_status, pass_type, sector, pass_status…). */
    fun setUserAttributes(attributes: Map<String, Any?>) {
        if (tracking?.isTrackingEnabled() != true) return
        analytics?.setUserAttributes(attributes)
    }

    /** Reporta un error del host (manejado o no) → evento `deepdots_app_crash`. No-op si tracking off. */
    fun reportError(
        error: Throwable,
        severity: String = "error",
        handled: Boolean = true,
        context: Map<String, Any?>? = null,
    ) {
        if (tracking?.isTrackingEnabled() != true) return
        crashReporter?.reportError(error, severity, handled, context)
    }

    /** Marca el inicio de un mini-service; etiqueta los eventos siguientes. No-op si tracking off. */
    fun enterMiniService(name: String, entryPointType: String? = null) {
        if (tracking?.isTrackingEnabled() != true) return
        analytics?.enterMiniService(name, entryPointType)
        SdkRuntime.miniService = analytics?.getMiniService() // se inyecta en la metadata del survey (#33)
    }

    /** Cierra el mini-service `name` (emite `mini_service_exit` con duración, #27). No-op si tracking off o si ese no está activo. */
    fun exitMiniService(name: String) {
        if (tracking?.isTrackingEnabled() != true) return
        analytics?.exitMiniService(name)
        SdkRuntime.miniService = analytics?.getMiniService() // el actual pasa a ser el siguiente más reciente (o null)
    }

    /** Findability (#31/#35): registra una búsqueda. `has_results` se deriva de `resultsCount`. */
    fun trackSearch(query: String, resultsCount: Int, params: Map<String, Any?>? = null) {
        track("deepdots_search", buildMap<String, Any?> {
            put("query", query)
            put("results_count", resultsCount)
            put("has_results", resultsCount > 0)
            params?.let { putAll(it) }
        })
    }

    /** Findability friction (#34/#35): señal de fricción con su `friction_topic`. */
    fun trackFindabilityFriction(frictionTopic: String, params: Map<String, Any?>? = null) {
        track("deepdots_findability_friction", buildMap<String, Any?> {
            put("friction_topic", frictionTopic)
            params?.let { putAll(it) }
        })
    }

    /** Funnel: un paso del embudo, correlacionado por `taskId`. El backend reconstruye conversión/drop-off/tiempo. */
    fun trackFunnelStep(funnel: String, step: String, taskId: String, params: Map<String, Any?>? = null) {
        track("deepdots_funnel_step", buildMap<String, Any?> {
            put("funnel", funnel)
            put("step", step)
            put("task_id", taskId)
            params?.let { putAll(it) }
        })
    }

    /** Messaging (#18–22): registra una etapa del funnel de una notificación (push/in-app). No-op si tracking off. */
    fun trackMessage(
        stage: String,
        id: String,
        title: String,
        channel: String,
        campaign: String? = null,
        value: Double? = null,
        currency: String? = null,
        params: Map<String, Any?>? = null,
    ) {
        track("deepdots_message", com.deepdots.sdk.analytics.buildMessageParams(stage, id, title, channel, campaign, value, currency, params))
    }

    /**
     * Señal de "app a background" (el host la llama desde Activity.onStop /
     * applicationDidEnterBackground): cierra la pantalla actual (page_view), cierra el
     * mini-service activo (mini_service_exit) y hace flush del lote de analytics.
     */
    fun onBackground() {
        navObserver?.stop()
        navStarted = false
        if (tracking?.isTrackingEnabled() == true) analytics?.exitAllMiniServices()
        SdkRuntime.miniService = analytics?.getMiniService()
        flushEngagement()
        engagement?.pause()
        flushAnalytics()
    }

    /** Señal de "app a foreground" (Activity.onStart / applicationWillEnterForeground): reanuda el engagement time. */
    fun onForeground() {
        engagement?.resume()
    }

    /** Emite `user_engagement` con el tiempo activo acumulado (#8). */
    private fun flushEngagement() {
        if (tracking?.isTrackingEnabled() != true) return
        val ms = engagement?.consume() ?: 0L
        if (ms > 0) track("deepdots_user_engagement", mapOf("engagement_time_msec" to ms))
    }

    /** Payload que se ENVIARÍA al endpoint de analytics (no envía ni vacía el buffer). */
    fun previewAnalytics(): AnalyticsEnvelope =
        analytics?.buildPayload(analyticsIdentity())
            ?: AnalyticsEnvelope(context = AnalyticsContext(platform = "unknown"), events = emptyList())

    /** Envía (hoy dry-run → println) el lote acumulado de analytics y vacía el buffer. */
    fun flushAnalytics() {
        if (tracking?.isTrackingEnabled() != true) return
        analytics?.flush(analyticsIdentity())
    }

    private fun analyticsIdentity(): AnalyticsIdentity =
        AnalyticsIdentity(userId = tracking?.getUserId(), sessionId = tracking?.getSessionId())

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

    /**
     * Records that a survey has been completed for the current user.
     *
     * You normally do **not** need to call this from host code: the SDK invokes it
     * automatically when the underlying survey emits `survey_completed` or when the
     * user taps the "Complete" action. It is exposed publicly only for unusual
     * integrations that bypass the normal completion flow.
     */
    fun markSurveyAnswered(surveyId: String) {
        answeredSurveys += surveyId
        markSurveyProgress(surveyId, TriggerConditionStatus.COMPLETED)
        log("Marked survey answered", surveyId)
    }

    fun close(context: PlatformContext) {
        dismissPopup(context)
    }

    fun setPath(path: String?) {
        // Fase 2: alimentar el observador de navegación (su propia dedup/normalización).
        path?.let { feedNavigation(it) }
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

    private fun feedNavigation(path: String) {
        val obs = navObserver ?: return
        if (!navStarted) {
            obs.begin(path)
            navStarted = true
        } else {
            obs.visit(path)
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

    /** Solo test: carga definiciones directamente (los popups vienen de la API en producción). */
    internal fun debugLoadPopups(defs: List<PopupDefinition>) = loadPopupDefinitions(defs)

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
                    val returnedSessionId = popupsService.postPopupEvent(
                        publicKey = publicKey,
                        status = "SHOWED",
                        popupId = popup.id,
                        userId = tracking?.getUserId() ?: SdkRuntime.userId,
                    )
                    // El backend devuelve el sessionId (lo cose por user_id); lo cacheamos.
                    if (returnedSessionId != null) {
                        tracking?.setSessionId(returnedSessionId)
                        SdkRuntime.sessionId = tracking?.getSessionId()
                    }
                } catch (t: Throwable) {
                    log("Error posting showed event", t.message ?: "unknown")
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
                    recordSurveyPartial(popup)
                }
            }

            // A successful, non-final step submission means the user answered a
            // question and advanced. Treat the first such event as PARTIAL progress.
            // (after_submit is also emitted on validation errors with a non-empty
            // `error`; those must not count as a partial answer.)
            "after_submit" -> {
                val data = parseEventPayload(payload)
                val err = data["error"] as? String
                if (err.isNullOrBlank()) {
                    recordSurveyPartial(popup)
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
                    val returnedSessionId = popupsService.postPopupEvent(
                        publicKey = publicKey,
                        status = "COMPLETED",
                        popupId = popup.id,
                        userId = tracking?.getUserId() ?: SdkRuntime.userId,
                    )
                    // El backend devuelve el sessionId (lo cose por user_id); lo cacheamos.
                    if (returnedSessionId != null) {
                        tracking?.setSessionId(returnedSessionId)
                        SdkRuntime.sessionId = tracking?.getSessionId()
                    }
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

    /**
     * Records the first partial progress for a survey and reports it to the API
     * (status `PARTIAL`). Only the first partial per survey is sent: once the local
     * progress is PARTIAL (or already COMPLETED) this is a no-op, so repeated
     * `after_submit` events do not generate duplicate calls.
     */
    private fun recordSurveyPartial(popup: PopupDefinition) {
        val current = surveyProgress[popup.surveyId]?.status
        if (current == TriggerConditionStatus.PARTIAL || current == TriggerConditionStatus.COMPLETED) {
            return
        }
        markSurveyProgress(popup.surveyId, TriggerConditionStatus.PARTIAL)

        scope.launch {
            val publicKey = initOptions?.popupOptions?.publicKey
            if (!publicKey.isNullOrBlank()) {
                try {
                    val returnedSessionId = popupsService.postPopupEvent(
                        publicKey = publicKey,
                        status = "PARTIAL",
                        popupId = popup.id,
                        userId = tracking?.getUserId() ?: SdkRuntime.userId,
                    )
                    // El backend devuelve el sessionId (lo cose por user_id); lo cacheamos.
                    if (returnedSessionId != null) {
                        tracking?.setSessionId(returnedSessionId)
                        SdkRuntime.sessionId = tracking?.getSessionId()
                    }
                } catch (t: Throwable) {
                    log("Error posting partial event", t.message ?: "unknown")
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
        val raw = resolvedStorage?.getString(exitQueueStorageKey) ?: return emptyList()
        return try {
            json.decodeFromString<List<DeferredExitPopup>>(raw)
        } catch (_: Throwable) {
            emptyList()
        }
    }

    private fun setDeferredExitQueue(queue: List<DeferredExitPopup>) {
        val storage = resolvedStorage ?: return
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
        return lastShown[popupId] ?: resolvedStorage?.getLong(lastShownStoragePrefix + popupId)
    }

    private fun setLastShown(popupId: String, timestamp: Long) {
        lastShown[popupId] = timestamp
        resolvedStorage?.putLong(lastShownStoragePrefix + popupId, timestamp)
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
        val lang = initOptions?.provideLang?.invoke()
        val accept = actions?.accept?.let {
            Action.Accept(
                label = it.label.orFallback(DefaultLabels.Slot.ACCEPT, lang),
                surveyId = it.surveyId ?: "",
            )
        }
        val decline = actions?.decline?.let {
            Action.Decline(
                label = it.label.orFallback(DefaultLabels.Slot.DECLINE, lang),
                cooldownDays = it.cooldownDays ?: 0,
            )
        }
        val start = actions?.start?.let {
            Action.Start(label = it.label.orFallback(DefaultLabels.Slot.START, lang))
        }
        val complete = actions?.complete?.let {
            Action.Complete(label = it.label.orFallback(DefaultLabels.Slot.COMPLETE, lang))
        }
        val back = actions?.back?.let {
            Action.Back(label = it.label.orFallback(DefaultLabels.Slot.BACK, lang))
        }
        return Actions(accept = accept, decline = decline, start = start, complete = complete, back = back)
    }

    private fun String?.orFallback(slot: DefaultLabels.Slot, lang: String?): String =
        this?.takeIf { it.isNotBlank() } ?: DefaultLabels.resolve(slot, lang)

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
        val font = style?.font?.family?.takeIf { it.isNotBlank() }?.let { family ->
            PopupFont(family = family, url = style.font.url)
        }
        return Style(theme = theme, position = position, imageUrl = style?.imageUrl, font = font)
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
