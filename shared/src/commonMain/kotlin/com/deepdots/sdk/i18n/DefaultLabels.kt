package com.deepdots.sdk.i18n

/**
 * Textos que pinta el SDK alrededor del survey: los botones del footer, el contador de
 * progreso, el distintivo de follow-up, las descripciones de accesibilidad y los avisos de
 * error. NO son textos del survey: las preguntas, los placeholders y las opciones las localiza
 * `@magicfeedback/native` dentro del WebView con `formData.lang[0]`, el idioma configurado en
 * la integración.
 *
 * Espejo exacto de `src/i18n/labels.ts` del SDK Web. Cualquier cambio aquí va también allí.
 *
 * Locales soportados: los 11 que la plataforma ofrece para un survey (en, es, da, fi, no, sv,
 * ar, bn, de, pt, fr) más zh-CN. Reglas de resolución:
 *  - los dos primeros caracteres del tag deciden el locale, sin importar caja ni región,
 *    así que `da`, `da-DK` y `da_DK` son todos danés (y `pt-BR`/`pt-PT` comparten juego);
 *  - las variantes noruegas (`nb`, `nn`) colapsan en `no`;
 *  - las variantes chinas (`zh-Hans`, `zh-CN`, `zh`) colapsan en `zh-CN`;
 *  - cualquier idioma no soportado cae a inglés.
 */
object DefaultLabels {

    /** Las cinco acciones que puede pintar el SDK. */
    enum class Slot { ACCEPT, DECLINE, START, COMPLETE, BACK }

    /** Juego completo de textos del chrome para un idioma. Espejo de `PopupLabels` en TS. */
    data class Labels(
        /** Acción principal: enviar la respuesta. */
        val accept: String,
        /** Acción de descarte. */
        val decline: String,
        /** Botón de la pantalla de bienvenida. */
        val start: String,
        /** Botón de la pantalla final. */
        val complete: String,
        /** Volver a la pregunta anterior. */
        val back: String,
        /** Sustantivo del contador de progreso ("Question" en `Question 2 of 5`). */
        val question: String,
        /** Separador del contador de progreso ("of" en `Question 2 of 5`). */
        val of: String,
        /** Distintivo de las preguntas de seguimiento dinámicas. */
        val followUp: String,
        /** Descripción accesible del botón de cerrar. */
        val closeAria: String,
        /** Descripción accesible del indicador de carga. */
        val loadingAria: String,
        /** Aviso al intentar avanzar con una pregunta obligatoria sin responder. */
        val errorRequired: String,
        /** Aviso cuando el envío falla. */
        val errorSubmit: String,
    ) {
        fun get(slot: Slot): String = when (slot) {
            Slot.ACCEPT -> accept
            Slot.DECLINE -> decline
            Slot.START -> start
            Slot.COMPLETE -> complete
            Slot.BACK -> back
        }
    }

    private val EN = Labels(
        accept = "Send",
        decline = "Cancel",
        start = "Start survey",
        complete = "Complete survey",
        back = "Back",
        question = "Question",
        of = "of",
        followUp = "Follow-up",
        closeAria = "Close popup",
        loadingAria = "Loading survey",
        errorRequired = "Please answer the required question to continue.",
        errorSubmit = "An error occurred while submitting. Please try again or close the popup.",
    )

    private val ES = Labels(
        accept = "Enviar",
        decline = "Cancelar",
        start = "Empezar encuesta",
        complete = "Completar encuesta",
        back = "Atrás",
        question = "Pregunta",
        of = "de",
        followUp = "Seguimiento",
        closeAria = "Cerrar ventana emergente",
        loadingAria = "Cargando encuesta",
        errorRequired = "Responde la pregunta obligatoria para continuar.",
        errorSubmit = "Se produjo un error al enviar. Inténtalo de nuevo o cierra la ventana.",
    )

    private val DA = Labels(
        accept = "Send",
        decline = "Annuller",
        start = "Start undersøgelse",
        complete = "Afslut undersøgelse",
        back = "Tilbage",
        question = "Spørgsmål",
        of = "af",
        followUp = "Opfølgning",
        closeAria = "Luk pop op",
        loadingAria = "Indlæser undersøgelse",
        errorRequired = "Besvar venligst det obligatoriske spørgsmål for at fortsætte.",
        errorSubmit = "Der opstod en fejl under afsendelsen. Prøv igen, eller luk pop op-vinduet.",
    )

    private val NO = Labels(
        accept = "Send",
        decline = "Avbryt",
        start = "Start undersøkelse",
        complete = "Fullfør undersøkelse",
        back = "Tilbake",
        question = "Spørsmål",
        of = "av",
        followUp = "Oppfølging",
        closeAria = "Lukk popup",
        loadingAria = "Laster undersøkelse",
        errorRequired = "Svar på det obligatoriske spørsmålet for å fortsette.",
        errorSubmit = "Det oppstod en feil under innsendingen. Prøv igjen, eller lukk popupen.",
    )

    private val SV = Labels(
        accept = "Skicka",
        decline = "Avbryt",
        start = "Starta undersökning",
        complete = "Slutför undersökning",
        back = "Tillbaka",
        question = "Fråga",
        of = "av",
        followUp = "Uppföljning",
        closeAria = "Stäng popup",
        loadingAria = "Laddar undersökning",
        errorRequired = "Svara på den obligatoriska frågan för att fortsätta.",
        errorSubmit = "Ett fel uppstod vid inskickningen. Försök igen eller stäng popupen.",
    )

    private val FI = Labels(
        accept = "Lähetä",
        decline = "Peruuta",
        start = "Aloita kysely",
        complete = "Viimeistele kysely",
        back = "Takaisin",
        question = "Kysymys",
        of = "/",
        followUp = "Jatkokysymys",
        closeAria = "Sulje ponnahdusikkuna",
        loadingAria = "Ladataan kyselyä",
        errorRequired = "Vastaa pakolliseen kysymykseen jatkaaksesi.",
        errorSubmit = "Lähetyksessä tapahtui virhe. Yritä uudelleen tai sulje ponnahdusikkuna.",
    )

    private val DE = Labels(
        accept = "Senden",
        decline = "Abbrechen",
        start = "Umfrage starten",
        complete = "Umfrage abschließen",
        back = "Zurück",
        question = "Frage",
        of = "von",
        followUp = "Folgefrage",
        closeAria = "Popup schließen",
        loadingAria = "Umfrage wird geladen",
        errorRequired = "Bitte beantworte die Pflichtfrage, um fortzufahren.",
        errorSubmit = "Beim Senden ist ein Fehler aufgetreten. Versuche es erneut oder schließe das Popup.",
    )

    private val FR = Labels(
        accept = "Envoyer",
        decline = "Annuler",
        start = "Commencer l'enquête",
        complete = "Terminer l'enquête",
        back = "Retour",
        question = "Question",
        of = "sur",
        followUp = "Question complémentaire",
        closeAria = "Fermer la fenêtre",
        loadingAria = "Chargement de l'enquête",
        errorRequired = "Veuillez répondre à la question obligatoire pour continuer.",
        errorSubmit = "Une erreur s'est produite lors de l'envoi. Réessayez ou fermez la fenêtre.",
    )

    private val PT = Labels(
        accept = "Enviar",
        decline = "Cancelar",
        start = "Iniciar questionário",
        complete = "Concluir questionário",
        back = "Voltar",
        question = "Pergunta",
        of = "de",
        followUp = "Seguimento",
        closeAria = "Fechar janela",
        loadingAria = "A carregar questionário",
        errorRequired = "Responda à pergunta obrigatória para continuar.",
        errorSubmit = "Ocorreu um erro ao enviar. Tente novamente ou feche a janela.",
    )

    // ⚠️ Árabe: el texto es RTL pero el chrome sigue maquetado LTR, igual que en Web. Traducir
    // ya es mejor que dejarlo en inglés; la dirección es una decisión aparte (afectaría también
    // al contenido del survey, que trae su propio `order: ltr|rtl` por pregunta).
    private val AR = Labels(
        accept = "إرسال",
        decline = "إلغاء",
        start = "ابدأ الاستبيان",
        complete = "إنهاء الاستبيان",
        back = "رجوع",
        question = "سؤال",
        of = "من",
        followUp = "سؤال متابعة",
        closeAria = "إغلاق النافذة المنبثقة",
        loadingAria = "جارٍ تحميل الاستبيان",
        errorRequired = "يرجى الإجابة على السؤال المطلوب للمتابعة.",
        errorSubmit = "حدث خطأ أثناء الإرسال. يرجى المحاولة مرة أخرى أو إغلاق النافذة.",
    )

    private val BN = Labels(
        accept = "পাঠান",
        decline = "বাতিল",
        start = "জরিপ শুরু করুন",
        complete = "জরিপ সম্পূর্ণ করুন",
        back = "পিছনে",
        question = "প্রশ্ন",
        of = "/",
        followUp = "ফলো-আপ",
        closeAria = "পপআপ বন্ধ করুন",
        loadingAria = "জরিপ লোড হচ্ছে",
        errorRequired = "চালিয়ে যেতে আবশ্যক প্রশ্নের উত্তর দিন।",
        errorSubmit = "জমা দেওয়ার সময় একটি ত্রুটি ঘটেছে। আবার চেষ্টা করুন বা পপআপ বন্ধ করুন।",
    )

    private val ZH_CN = Labels(
        accept = "发送",
        decline = "取消",
        start = "开始问卷",
        complete = "完成问卷",
        back = "返回",
        question = "问题",
        of = "/",
        followUp = "追问",
        closeAria = "关闭弹窗",
        loadingAria = "正在加载问卷",
        errorRequired = "请回答必答题后继续。",
        errorSubmit = "提交时出错。请重试或关闭弹窗。",
    )

    private val TABLE: Map<String, Labels> = mapOf(
        "en" to EN, "es" to ES, "da" to DA, "no" to NO, "sv" to SV, "fi" to FI,
        "de" to DE, "fr" to FR, "pt" to PT, "ar" to AR, "bn" to BN, "zh-CN" to ZH_CN,
    )

    /**
     * Todos los prefijos BCP-47 con traducción propia. Si la plataforma añade un idioma nuevo
     * al selector del survey, hay que añadirlo aquí o el chrome saldrá en inglés debajo de un
     * survey traducido.
     */
    val supportedLanguages: List<String> =
        listOf("en", "es", "da", "no", "sv", "fi", "de", "fr", "pt", "ar", "bn", "zh-CN")

    /**
     * Locales que se escriben de derecha a izquierda. Espejo de `RTL_LANGUAGES` de
     * `@magicfeedback/native` (2.2.22), que estampa `dir="rtl"` en el contenedor del survey
     * para esos idiomas: el chrome tiene que voltear con él o media tarjeta queda mirando a
     * un lado y media al otro.
     */
    val rtlLocales: List<String> = listOf("ar")

    /** Si el chrome debe pintarse de derecha a izquierda para [lang]. */
    fun isRtlLanguage(lang: String?): Boolean = rtlLocales.contains(resolveLocale(lang))

    /** Normaliza un tag BCP-47 al locale con traducción. Espejo de `resolveLocale` en TS. */
    fun resolveLocale(lang: String?): String {
        if (lang.isNullOrBlank()) return "en"
        val normalized = lang.trim().lowercase().replace('_', '-')
        if (normalized == "zh" || normalized.startsWith("zh-")) return "zh-CN"
        return when (normalized.substringBefore('-')) {
            "en" -> "en"
            "es" -> "es"
            "da" -> "da"
            "no", "nb", "nn" -> "no"
            "sv" -> "sv"
            "fi" -> "fi"
            "de" -> "de"
            "fr" -> "fr"
            "pt" -> "pt"
            "ar" -> "ar"
            "bn" -> "bn"
            else -> "en"
        }
    }

    /** Juego completo de textos para [lang]. Cae a inglés si el idioma no está soportado. */
    fun labels(lang: String?): Labels = TABLE[resolveLocale(lang)] ?: EN

    /**
     * Resuelve el texto por defecto de [slot] en [lang]. Cae a inglés si [lang] es nulo/vacío
     * o el idioma no está soportado.
     */
    fun resolve(slot: Slot, lang: String?): String = labels(lang).get(slot)
}
