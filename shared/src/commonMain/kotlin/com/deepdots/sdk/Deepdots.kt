@file:Suppress("unused")
package com.deepdots.sdk

import com.deepdots.sdk.util.currentTimeMillis
import com.deepdots.sdk.platform.PlatformContext
import com.deepdots.sdk.platform.dismissPopup
import com.deepdots.sdk.util.parsePopupHtml
import com.deepdots.sdk.models.*
import com.deepdots.sdk.ui.platformSurveyHtml

typealias DeepdotsPopupsSdk = DeepdotsPopups

/**
 * Convenience entry point for creating and configuring an SDK instance.
 */
object Deepdots {
    /** Creates an empty instance (you must call `init` separately). */
    fun create(): DeepdotsPopupsSdk = DeepdotsPopupsSdk()

    /** Creates and initializes an instance in a single step. */
    fun createInitialized(options: InitOptions): DeepdotsPopupsSdk = DeepdotsPopupsSdk().apply { init(options) }

    /** Current epoch millis (re-export). */
    fun now(): Long = currentTimeMillis()

    /** Basic popup HTML parser (re-export). */
    fun parseHtml(html: String) = parsePopupHtml(html)

    /** Manually dismisses the active popup (re-export). */
    fun dismiss(context: PlatformContext) = dismissPopup(context)

    /**
     * Returns the full HTML used to render a MagicFeedback survey (loader + CDN fallback included).
     * Useful for hosts that want to load it directly inside an Android WebView or iOS WKWebView.
     */
    fun getSurveyHtml(surveyId: String, productId: String): String = platformSurveyHtml(surveyId, productId)

    /**
     * Simple helper for Swift/ObjC interop: create and init SDK with a single popup without referencing Kotlin data classes from Swift.
     */
    fun createInitializedSimple(
        id: String,
        title: String,
        messageHtml: String,
        surveyId: String,
        productId: String,
        triggerSeconds: Int = 3,
        acceptLabel: String = "Send",
        declineLabel: String = "Cancel",
        declineCooldownDays: Int = 1,
        debug: Boolean = true,
        autoLaunch: Boolean = true,
        lang: String? = "en",
        path: String? = "/home"
    ): DeepdotsPopupsSdk {
        val def = PopupDefinition(
            id = id,
            title = title,
            message = messageHtml,
            trigger = Trigger.TimeOnPage(seconds = triggerSeconds.toDouble()),
            triggers = listOf(Trigger.TimeOnPage(seconds = triggerSeconds.toDouble())),
            conditions = listOf(LegacyCondition(answered = false, cooldownDays = declineCooldownDays)),
            actions = Actions(
                accept = Action.Accept(label = acceptLabel, surveyId = surveyId),
                decline = Action.Decline(label = declineLabel, cooldownDays = declineCooldownDays)
            ),
            surveyId = surveyId,
            productId = productId,
            style = Style(theme = Theme.Light, position = Position.Center),
            segments = null
        )
        val opts = InitOptions(
            debug = debug,
            popupOptions = PopupOptions(popups = listOf(def)),
            autoLaunch = autoLaunch,
            provideLang = { lang }
        )
        return DeepdotsPopupsSdk().apply { init(opts); setPath(path) }
    }
}

object Events {
    val popupShown: Event = Event.PopupShown
    val popupClicked: Event = Event.PopupClicked
    val surveyCompleted: Event = Event.SurveyCompleted
}
