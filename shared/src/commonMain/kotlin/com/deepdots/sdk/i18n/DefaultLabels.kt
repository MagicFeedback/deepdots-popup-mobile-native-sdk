package com.deepdots.sdk.i18n

/**
 * Built-in localized labels used as fallbacks when a [com.deepdots.sdk.models.PopupDefinition]
 * does not supply an explicit `actions.*.label`.
 *
 * Supported locales: en, es, da, no (incl. nb/nn), sv, fi, zh-CN (incl. plain zh).
 * Resolution rules:
 *  - The first two characters of the language tag determine the locale (case-insensitive),
 *    so values like `da`, `da-DK`, `da_DK` all map to Danish.
 *  - Norwegian variants (`nb`, `nn`) collapse to `no`.
 *  - Chinese variants (`zh-Hans`, `zh-CN`, `zh`) collapse to `zh-CN`.
 *  - Any unsupported locale falls back to English.
 */
object DefaultLabels {

    /** The five built-in actions the SDK can render. */
    enum class Slot { ACCEPT, DECLINE, START, COMPLETE, BACK }

    private data class LabelSet(
        val accept: String,
        val decline: String,
        val start: String,
        val complete: String,
        val back: String,
    ) {
        fun get(slot: Slot): String = when (slot) {
            Slot.ACCEPT -> accept
            Slot.DECLINE -> decline
            Slot.START -> start
            Slot.COMPLETE -> complete
            Slot.BACK -> back
        }
    }

    private val EN = LabelSet(
        accept = "Send",
        decline = "Cancel",
        start = "Start survey",
        complete = "Complete survey",
        back = "Back",
    )

    private val ES = LabelSet(
        accept = "Enviar",
        decline = "Cancelar",
        start = "Empezar encuesta",
        complete = "Completar encuesta",
        back = "Atrás",
    )

    private val DA = LabelSet(
        accept = "Send",
        decline = "Annuller",
        start = "Start undersøgelse",
        complete = "Afslut undersøgelse",
        back = "Tilbage",
    )

    private val NO = LabelSet(
        accept = "Send",
        decline = "Avbryt",
        start = "Start undersøkelse",
        complete = "Fullfør undersøkelse",
        back = "Tilbake",
    )

    private val SV = LabelSet(
        accept = "Skicka",
        decline = "Avbryt",
        start = "Starta undersökning",
        complete = "Slutför undersökning",
        back = "Tillbaka",
    )

    private val FI = LabelSet(
        accept = "Lähetä",
        decline = "Peruuta",
        start = "Aloita kysely",
        complete = "Viimeistele kysely",
        back = "Takaisin",
    )

    private val ZH_CN = LabelSet(
        accept = "发送",
        decline = "取消",
        start = "开始问卷",
        complete = "完成问卷",
        back = "返回",
    )

    /**
     * Resolves the default label for [slot] in [lang]. Falls back to English when
     * [lang] is null/blank or the language is not supported.
     */
    fun resolve(slot: Slot, lang: String?): String = labelsFor(lang).get(slot)

    /** All BCP-47 prefixes supported natively by the SDK. */
    val supportedLanguages: List<String> = listOf("en", "es", "da", "no", "sv", "fi", "zh-CN")

    private fun labelsFor(lang: String?): LabelSet {
        if (lang.isNullOrBlank()) return EN
        val normalized = lang.trim().lowercase().replace('_', '-')
        // Chinese: collapse zh, zh-hans, zh-cn, zh-sg to zh-CN
        if (normalized == "zh" || normalized.startsWith("zh-")) return ZH_CN
        val primary = normalized.substringBefore('-')
        return when (primary) {
            "en" -> EN
            "es" -> ES
            "da" -> DA
            "no", "nb", "nn" -> NO
            "sv" -> SV
            "fi" -> FI
            else -> EN
        }
    }
}
