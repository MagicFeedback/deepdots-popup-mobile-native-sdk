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
 * Objeto de conveniencia para crear y configurar una instancia del SDK.
 */
object Deepdots {
    /** Crea una instancia vacía (requiere llamar a init) */
    fun create(): DeepdotsPopupsSdk = DeepdotsPopupsSdk()

    /** Crea e inicializa una instancia en un paso. */
    fun createInitialized(options: InitOptions): DeepdotsPopupsSdk = DeepdotsPopupsSdk().apply { init(options) }

    /** Timestamp utilitario (reexport). */
    fun now(): Long = currentTimeMillis()

    /** Parseo HTML básico (reexport). */
    fun parseHtml(html: String) = parsePopupHtml(html)

    /** Dismiss manual (reexport). */
    fun dismiss(context: PlatformContext) = dismissPopup(context)

    /**
     * Devuelve el HTML completo para renderizar la encuesta MagicFeedback (incluye loader y fallback CDN).
     * Útil para que la app iOS/Android lo cargue directamente en su WebView/WKWebView.
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
            trigger = Trigger.TimeOnPage(value = triggerSeconds, condition = listOf(Condition(answered = false, cooldownDays = declineCooldownDays))),
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
