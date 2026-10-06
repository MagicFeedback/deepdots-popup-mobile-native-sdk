package com.deepdots.sdk

import com.deepdots.sdk.ui.PopupReveal
import com.deepdots.sdk.ui.SurveyPalette
import com.deepdots.sdk.ui.buildMagicFeedbackHtml
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Apertura diferida: el popup se monta invisible y se enseña cuando el survey está pintado, en
 * vez de enseñar el spinner girando. Paridad con el SDK Web (`src/ui/reveal.ts`).
 *
 * En móvil la espera es mayor que en web, porque al JSON del survey se le suman el arranque del
 * WebView y la descarga del bundle de `@magicfeedback/native` desde el CDN.
 *
 * El chrome de KMP es Compose, así que el aviso de "ya se puede enseñar" tiene que cruzar el
 * puente desde el WebView; aquí se comprueba el lado del HTML, que es el que lo manda.
 */
class PopupRevealTest {

    private fun html(): String = buildMagicFeedbackHtml(
        surveyId = "survey-abc",
        productId = "product-xyz",
        localAssetUrl = null,
        assetSize = null,
        bridgeEmitCall = "DeepdotsBridge.emit",
        isIOS = false,
    )

    @Test
    fun techo_igual_al_del_sdk_web() {
        // Si los dos SDK abren con techos distintos, el mismo survey se comporta distinto en web
        // y en móvil sin que nadie lo haya decidido.
        assertEquals(1200L, PopupReveal.REVEAL_TIMEOUT_MS)
    }

    @Test
    fun el_html_avisa_cuando_el_survey_esta_pintado() {
        val h = html()
        assertTrue(h.contains("ddCreateReveal(document"), "debe instalar la revelación")
        assertTrue(h.contains("emit('${PopupReveal.READY_EVENT}')"), "debe avisar con el evento ready")
        assertTrue(h.contains("ddReveal.whenPainted();"), "debe avisar al cargar el survey")
    }

    @Test
    fun el_html_espera_a_las_imagenes_del_survey() {
        // Los emojis del rating son SVG y llegan DESPUÉS del onLoadedEvent: sin esta espera el
        // popup aparece y se rellena delante del usuario.
        val h = html()
        assertTrue(h.contains("querySelectorAll('img')"), "debe mirar las imágenes pendientes")
        assertTrue(h.contains("addEventListener('load'"), "debe esperar a que carguen")
        assertTrue(h.contains("addEventListener('error'"), "una imagen rota no puede bloquear la apertura")
    }

    @Test
    fun el_html_lleva_su_propio_techo() {
        // El survey puede no cargar nunca (CDN caído): el aviso tiene que salir igualmente, y el
        // popup abrirse con el spinner, que es el comportamiento de siempre.
        val h = html()
        assertTrue(
            h.contains("setTimeout(reveal, timeoutMs)"),
            "la revelación debe armar su techo",
        )
        assertTrue(
            h.contains("}, ${PopupReveal.REVEAL_TIMEOUT_MS});"),
            "el techo del HTML debe ser el mismo valor compartido",
        )
    }

    @Test
    fun el_survey_se_alinea_con_el_chrome_nativo() {
        // El margen lateral lo pone la tarjeta de Compose: con el padding del paquete el
        // enunciado y las opciones quedaban ~38px más adentro que el logo y la barra.
        val h = html()
        for (sel in listOf("magicfeedback-container", "magicfeedback-form", "magicfeedback-div")) {
            assertTrue(
                Regex("\\.$sel\\{padding-left:0;padding-right:0;\\}").containsMatchIn(h),
                "debe anular el padding lateral de .$sel",
            )
        }
        // El vertical se conserva: es el que separa las preguntas del borde y entre sí.
        assertTrue(!h.contains(".magicfeedback-form{padding:0"), "no debe anular el padding vertical")
    }

    @Test
    fun el_html_reporta_lo_que_ocupa_el_survey() {
        // El WebView no tiene tamaño propio: sin este aviso la capa nativa lo estira al máximo y
        // una sola pregunta deja un hueco enorme hasta el footer.
        val h = html()
        assertTrue(h.contains("emitJSON('${PopupReveal.CONTENT_HEIGHT_EVENT}'"), "debe reportar la altura")
        // `dd-content`, no `mf-form`: el Surveys SDK renombra ese div al cargar (ver MagicFeedbackHtmlTest).
        assertTrue(h.contains("getElementById('dd-content')"), "debe medir el contenido del survey, no el body")
        assertTrue(h.contains("ResizeObserver"), "debe reaccionar a los cambios de página y follow-ups")
        assertTrue(
            Regex("ddReportHeight\\(\\);\\s*emit\\('${PopupReveal.READY_EVENT}'\\)").containsMatchIn(h),
            "la altura debe llegar ANTES de revelar, o el popup se abriría y daría un salto",
        )
    }

    @Test
    fun altura_del_survey_a_partir_de_lo_reportado() {
        // Caso normal: se respeta lo que ocupa el contenido.
        assertEquals(340, PopupReveal.resolveSurveyHeightDp(340, floorDp = 280, ceilingDp = 560))
        // Survey largo: se acota al espacio de la tarjeta y el WebView scrollea por dentro.
        assertEquals(560, PopupReveal.resolveSurveyHeightDp(2000, floorDp = 280, ceilingDp = 560))
        // Survey muy corto: no se colapsa por debajo del mínimo.
        assertEquals(
            PopupReveal.MIN_SURVEY_HEIGHT_DP,
            PopupReveal.resolveSurveyHeightDp(10, floorDp = 280, ceilingDp = 560),
        )
    }

    @Test
    fun sin_altura_reportada_se_mantiene_el_comportamiento_anterior() {
        // Mientras el WebView no diga nada (o diga algo absurdo) se usa el suelo de siempre.
        assertEquals(280, PopupReveal.resolveSurveyHeightDp(null, floorDp = 280, ceilingDp = 560))
        assertEquals(280, PopupReveal.resolveSurveyHeightDp(0, floorDp = 280, ceilingDp = 560))
        assertEquals(280, PopupReveal.resolveSurveyHeightDp(-5, floorDp = 280, ceilingDp = 560))
        // Pantalla pequeña: el suelo nunca puede pasarse del techo disponible.
        assertEquals(200, PopupReveal.resolveSurveyHeightDp(null, floorDp = 280, ceilingDp = 200))
    }

    @Test
    fun el_survey_hereda_el_color_de_marca_del_boton() {
        // Las integraciones configuran buttonPrimaryColor pero muchas dejan primaryColor vacio,
        // y el survey pinta sus controles con --mf-primary: sin esto, el popup mezcla el color de
        // la marca (que el chrome de Compose si usa) con el gris azulado por defecto del paquete.
        val h = html()
        assertTrue(h.contains("ddApplySurveyPrimaryColor(document.documentElement, style)"), "debe aplicar el color al cargar")
        assertTrue(h.contains("--mf-primary"), "debe escribir la variable del survey")
        assertTrue(h.contains("--mf-border-focus"), "y las derivadas, que se resuelven en :root")
    }

    @Test
    fun solo_acepta_colores_hex() {
        // El estilo viene de la API y se interpola en CSS.
        assertTrue(
            SurveyPalette.PRIMARY_COLOR_JS.contains("hex.test(value)"),
            "debe filtrar por hex antes de tocar el CSS",
        )
    }

    @Test
    fun el_survey_no_acepta_toques_mientras_se_envia() {
        // El spinner lo pinta Compose FUERA del WebView, y el WebView es una vista de interop que
        // recibe sus propios toques: el velo no lo protege. Sin esto el usuario sigue cambiando de
        // opcion mientras se envia la pagina, y ese cambio ya no viaja con ella.
        val h = html()
        assertTrue(h.contains("function ddSetSurveyBusy(el, busy)"), "debe llevar el helper")
        assertTrue(
            Regex("beforeSubmitEvent[\\s\\S]*?ddSetSurveyBusy\\(document\\.body, true\\)").containsMatchIn(h),
            "debe bloquear al empezar el envio",
        )
        for (evento in listOf("afterSubmitEvent", "onBackEvent")) {
            assertTrue(
                Regex("$evento[\\s\\S]{0,200}?ddSetSurveyBusy\\(document\\.body, false\\)").containsMatchIn(h),
                "debe soltar en $evento, o la pantalla se queda muerta",
            )
        }
    }

    @Test
    fun la_revelacion_ocurre_una_sola_vez() {
        // `whenPainted` se puede llamar en cada página del survey; solo la primera abre el popup.
        assertTrue(
            PopupReveal.REVEAL_JS.contains("if(revealed){ return; }"),
            "reveal debe ser idempotente",
        )
    }
}
