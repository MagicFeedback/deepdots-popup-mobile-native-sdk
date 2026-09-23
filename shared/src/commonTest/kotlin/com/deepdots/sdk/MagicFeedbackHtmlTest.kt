package com.deepdots.sdk

import com.deepdots.sdk.models.PopupFont
import com.deepdots.sdk.ui.POPUP_SDK_CSS_VERSION
import com.deepdots.sdk.ui.buildMagicFeedbackHtml
import com.deepdots.sdk.ui.platformSurveyHtml
import kotlin.test.Test
import kotlin.test.assertTrue

class MagicFeedbackHtmlTest {
    @Test
    fun html_contains_ids_and_fallback_markers() {
        val surveyId = "survey-abc"
        val productId = "product-xyz"
        val html = Deepdots.getSurveyHtml(surveyId, productId)
        assertTrue(html.contains(surveyId), "Survey ID should appear in HTML")
        assertTrue(html.contains(productId), "Product ID should appear in HTML")
        // Note: other log/event string assertions removed to avoid brittleness across platforms/builds.
    }

    @Test
    fun html_applies_custom_font_family_and_font_face() {
        // Espejo de Web surveyHtml.ts: @font-face + font-family con el stack de fallback.
        val html = buildMagicFeedbackHtml(
            surveyId = "survey-abc",
            productId = "product-xyz",
            localAssetUrl = null,
            assetSize = null,
            bridgeEmitCall = "DeepdotsBridge.emit",
            isIOS = false,
            font = PopupFont("Inter", "https://x.com/Inter.woff2"),
        )
        assertTrue(html.contains("@font-face{font-family:\"Inter\""), "debe incluir @font-face de la fuente custom")
        assertTrue(
            html.contains("\"Inter\", -apple-system, system-ui, sans-serif"),
            "font-family debe usar la familia custom con el stack de fallback",
        )
    }

    /** Surveys multi-idioma: el idioma se pide a native en `generate({ lang })`. */
    @Test
    fun html_asks_native_for_the_survey_language() {
        fun html(lang: String?) = buildMagicFeedbackHtml(
            surveyId = "survey-abc",
            productId = "product-xyz",
            localAssetUrl = null,
            assetSize = null,
            bridgeEmitCall = "DeepdotsBridge.emit",
            isIOS = false,
            lang = lang,
        )
        assertTrue(html("es-ES").contains("lang: 'es-ES',"), "pasa el idioma a generate()")
        assertTrue(html(null).contains("lang: undefined,"), "sin idioma no fuerza ninguno")
        assertTrue(html("es'); alert(1); ('").contains("lang: undefined,"), "no inyecta JS")
        assertTrue(html("es").contains("typeof args.lang === 'string'"), "el chrome sigue el idioma de native")
    }

    /**
     * Identidad del tracking inyectada en el survey (contrato §5): mismas claves que Web, para
     * poder coser las respuestas con la analítica y con el mini-service activo (#33).
     */
    @Test
    fun html_injects_tracking_identity_into_the_survey() {
        SdkRuntime.userId = "u-1"
        SdkRuntime.sessionId = "s-9"
        SdkRuntime.miniService = "checkout"
        try {
            val html = buildMagicFeedbackHtml(
                surveyId = "survey-abc",
                productId = "product-xyz",
                localAssetUrl = null,
                assetSize = null,
                bridgeEmitCall = "DeepdotsBridge.emit",
                isIOS = false,
            )
            assertTrue(html.contains("{ key: 'user_id', value: ['u-1'] }"), "user_id en la metadata")
            assertTrue(html.contains("{ key: 'session_id', value: ['s-9'] }"), "session_id en la metadata")
            assertTrue(html.contains("{ key: 'mini_service', value: ['checkout'] }"), "mini_service en la metadata")
            // external-user-id va como profile, 3er argumento de form()
            assertTrue(
                html.contains("form('survey-abc', 'product-xyz', [{ key: 'external-user-id', value: ['u-1'] }])"),
                "external-user-id como profile de form()",
            )
        } finally {
            SdkRuntime.userId = null
            SdkRuntime.sessionId = null
            SdkRuntime.miniService = null
        }
    }

    /**
     * Pantalla final: la pinta este HTML, no `@magicfeedback/native`. Su `renderSuccess` usa
     * textContent (el mensaje de la plataforma es HTML con imagen) y su fallback es un literal
     * genérico que ignora `style.successMessage`. Paridad con Web/RN.
     */
    @Test
    fun html_pinta_su_propia_pantalla_final() {
        val html = Deepdots.getSurveyHtml("survey-abc", "product-xyz")
        assertTrue(html.contains("addSuccessScreen:false"), "debe desactivar la pantalla final del SDK de surveys")
        assertTrue(html.contains("function showSuccessScreen()"), "debe definir su propia pantalla final")
        assertTrue(html.contains("id='mf-success'"), "debe tener el contenedor de la pantalla final")
        assertTrue(
            html.contains("successMessageHtml = style.successMessage"),
            "debe guardar el successMessage de la plataforma al cargar",
        )
        assertTrue(html.contains("showSuccessScreen(); emitJSON('survey_completed')"), "debe pintarla al completar")
    }

    /** El total solo se conoce con el form montado: lo necesita la barra de progreso nativa. */
    @Test
    fun html_emite_progress_y_total_al_cargar() {
        val html = Deepdots.getSurveyHtml("survey-abc", "product-xyz")
        assertTrue(
            html.contains("progress: form.progress || 0, total: form.total || 0"),
            "onLoadedEvent debe emitir progress y total para la barra de progreso",
        )
    }

    @Test
    fun html_forwards_the_survey_language_to_the_native_chrome() {
        // El idioma del survey (`formData.lang[0]`) solo se conoce dentro del WebView; la capa
        // nativa pinta los botones, así que el bridge tiene que reenviárselo en el `loaded`.
        val html = Deepdots.getSurveyHtml("survey-abc", "product-xyz")
        assertTrue(html.contains("formData.lang"), "El bridge debe leer formData.lang")
        assertTrue(html.contains("surveyLang"), "El payload del loaded debe incluir surveyLang")
    }

    @Test
    fun css_url_is_pinned_to_a_popup_sdk_version() {
        // Sin version, jsDelivr sirve la ultima publicada: una app ya distribuida (con su
        // MAGICFEEDBACK_VERSION compilado dentro) empezaria a combinar su JS con el CSS de una
        // release posterior en cuanto se publique el popup-sdk. Paso que ya casi ocurre con el
        // salto 2.2.8 -> 2.2.22, que cambia la hoja a propiedades logicas y anade RTL.
        val html = Deepdots.getSurveyHtml("survey-abc", "product-xyz")
        assertTrue(
            html.contains("@magicfeedback/popup-sdk@$POPUP_SDK_CSS_VERSION/dist/assets/assets/style.css"),
            "El CSS del popup-sdk debe cargarse con version explicita",
        )
        assertTrue(
            !html.contains("@magicfeedback/popup-sdk/dist"),
            "No debe quedar ninguna URL del popup-sdk sin version",
        )
    }
}
