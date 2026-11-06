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
 * Locales soportados: los 11 que la plataforma ofrecía para un survey (en, es, da, fi, no, sv,
 * ar, bn, de, pt, fr), zh-CN y el resto de los 24 idiomas oficiales de la UE (DD-67: bg, cs,
 * el, et, ga, hr, hu, it, lt, lv, mt, nl, pl, ro, sk, sl, traducidos a máquina y pendientes de
 * revisión nativa). Reglas de resolución:
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
        /** Aviso al intentar avanzar con un email que no es una dirección válida (`INVALID_EMAIL`). */
        val errorInvalidEmail: String,
        /** Aviso cuando el envío falla. */
        val errorSubmit: String,
        /** Aviso cuando el survey no ha podido cargar (el popup solo ofrece cerrarlo). */
        val errorLoad: String,
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
        errorInvalidEmail = "Please enter a valid email address.",
        errorSubmit = "An error occurred while submitting. Please try again or close the popup.",
        errorLoad = "We couldn't load this survey.",
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
        errorInvalidEmail = "Introduce una dirección de correo electrónico válida.",
        errorSubmit = "Se produjo un error al enviar. Inténtalo de nuevo o cierra la ventana.",
        errorLoad = "No se ha podido cargar la encuesta.",
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
        errorInvalidEmail = "Indtast venligst en gyldig e-mailadresse.",
        errorSubmit = "Der opstod en fejl under afsendelsen. Prøv igen, eller luk pop op-vinduet.",
        errorLoad = "Undersøgelsen kunne ikke indlæses.",
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
        errorInvalidEmail = "Skriv inn en gyldig e-postadresse.",
        errorSubmit = "Det oppstod en feil under innsendingen. Prøv igjen, eller lukk popupen.",
        errorLoad = "Undersøkelsen kunne ikke lastes inn.",
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
        errorInvalidEmail = "Ange en giltig e-postadress.",
        errorSubmit = "Ett fel uppstod vid inskickningen. Försök igen eller stäng popupen.",
        errorLoad = "Enkäten kunde inte laddas.",
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
        errorInvalidEmail = "Anna kelvollinen sähköpostiosoite.",
        errorSubmit = "Lähetyksessä tapahtui virhe. Yritä uudelleen tai sulje ponnahdusikkuna.",
        errorLoad = "Kyselyä ei voitu ladata.",
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
        errorInvalidEmail = "Bitte gib eine gültige E-Mail-Adresse ein.",
        errorSubmit = "Beim Senden ist ein Fehler aufgetreten. Versuche es erneut oder schließe das Popup.",
        errorLoad = "Die Umfrage konnte nicht geladen werden.",
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
        errorInvalidEmail = "Veuillez saisir une adresse e-mail valide.",
        errorSubmit = "Une erreur s'est produite lors de l'envoi. Réessayez ou fermez la fenêtre.",
        errorLoad = "Impossible de charger l'enquête.",
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
        errorInvalidEmail = "Insira um endereço de e-mail válido.",
        errorSubmit = "Ocorreu um erro ao enviar. Tente novamente ou feche a janela.",
        errorLoad = "Não foi possível carregar o inquérito.",
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
        errorInvalidEmail = "يرجى إدخال عنوان بريد إلكتروني صالح.",
        errorSubmit = "حدث خطأ أثناء الإرسال. يرجى المحاولة مرة أخرى أو إغلاق النافذة.",
        errorLoad = "تعذّر تحميل الاستبيان.",
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
        errorInvalidEmail = "একটি বৈধ ইমেল ঠিকানা লিখুন।",
        errorSubmit = "জমা দেওয়ার সময় একটি ত্রুটি ঘটেছে। আবার চেষ্টা করুন বা পপআপ বন্ধ করুন।",
        errorLoad = "সমীক্ষাটি লোড করা যায়নি।",
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
        errorInvalidEmail = "请输入有效的电子邮件地址。",
        errorSubmit = "提交时出错。请重试或关闭弹窗。",
        errorLoad = "无法加载问卷。",
    )

    private val BG = Labels(
        accept = "Изпращане",
        decline = "Отказ",
        start = "Започване на анкетата",
        complete = "Завършване на анкетата",
        back = "Назад",
        question = "Въпрос",
        of = "от",
        followUp = "Допълнителен въпрос",
        closeAria = "Затваряне на изскачащия прозорец",
        loadingAria = "Анкетата се зарежда",
        errorRequired = "Моля, отговорете на задължителния въпрос, за да продължите.",
        errorInvalidEmail = "Моля, въведете валиден имейл адрес.",
        errorSubmit = "Възникна грешка при изпращането. Моля, опитайте отново или затворете прозореца.",
        errorLoad = "Не успяхме да заредим тази анкета.",
    )

    private val CS = Labels(
        accept = "Odeslat",
        decline = "Zrušit",
        start = "Zahájit průzkum",
        complete = "Dokončit průzkum",
        back = "Zpět",
        question = "Otázka",
        of = "z",
        followUp = "Doplňující otázka",
        closeAria = "Zavřít vyskakovací okno",
        loadingAria = "Načítání průzkumu",
        errorRequired = "Před pokračováním prosím odpovězte na povinnou otázku.",
        errorInvalidEmail = "Zadejte prosím platnou e-mailovou adresu.",
        errorSubmit = "Při odesílání došlo k chybě. Zkuste to prosím znovu nebo okno zavřete.",
        errorLoad = "Tento průzkum se nepodařilo načíst.",
    )

    private val EL = Labels(
        accept = "Αποστολή",
        decline = "Ακύρωση",
        start = "Έναρξη έρευνας",
        complete = "Ολοκλήρωση έρευνας",
        back = "Πίσω",
        question = "Ερώτηση",
        of = "από",
        followUp = "Συμπληρωματική ερώτηση",
        closeAria = "Κλείσιμο αναδυόμενου παραθύρου",
        loadingAria = "Φόρτωση έρευνας",
        errorRequired = "Απαντήστε στην υποχρεωτική ερώτηση για να συνεχίσετε.",
        errorInvalidEmail = "Εισαγάγετε μια έγκυρη διεύθυνση email.",
        errorSubmit = "Παρουσιάστηκε σφάλμα κατά την αποστολή. Δοκιμάστε ξανά ή κλείστε το παράθυρο.",
        errorLoad = "Δεν ήταν δυνατή η φόρτωση αυτής της έρευνας.",
    )

    private val ET = Labels(
        accept = "Saada",
        decline = "Tühista",
        start = "Alusta küsitlust",
        complete = "Lõpeta küsitlus",
        back = "Tagasi",
        question = "Küsimus",
        of = "/",
        followUp = "Täpsustav küsimus",
        closeAria = "Sulge hüpikaken",
        loadingAria = "Küsitluse laadimine",
        errorRequired = "Jätkamiseks vasta palun kohustuslikule küsimusele.",
        errorInvalidEmail = "Palun sisesta kehtiv e-posti aadress.",
        errorSubmit = "Saatmisel tekkis viga. Proovi uuesti või sulge hüpikaken.",
        errorLoad = "Seda küsitlust ei õnnestunud laadida.",
    )

    private val GA = Labels(
        accept = "Seol",
        decline = "Cealaigh",
        start = "Tosaigh an suirbhé",
        complete = "Críochnaigh an suirbhé",
        back = "Siar",
        question = "Ceist",
        of = "as",
        followUp = "Ceist leantach",
        closeAria = "Dún an phreabfhuinneog",
        loadingAria = "Suirbhé á lódáil",
        errorRequired = "Freagair an cheist éigeantach chun leanúint ar aghaidh, le do thoil.",
        errorInvalidEmail = "Cuir isteach seoladh ríomhphoist bailí, le do thoil.",
        errorSubmit = "Tharla earráid agus é á sheoladh. Triail arís nó dún an phreabfhuinneog.",
        errorLoad = "Níorbh fhéidir linn an suirbhé seo a lódáil.",
    )

    private val HR = Labels(
        accept = "Pošalji",
        decline = "Odustani",
        start = "Započni anketu",
        complete = "Završi anketu",
        back = "Natrag",
        question = "Pitanje",
        of = "od",
        followUp = "Dodatno pitanje",
        closeAria = "Zatvori skočni prozor",
        loadingAria = "Učitavanje ankete",
        errorRequired = "Molimo odgovorite na obavezno pitanje kako biste nastavili.",
        errorInvalidEmail = "Unesite valjanu e-adresu.",
        errorSubmit = "Došlo je do pogreške pri slanju. Pokušajte ponovno ili zatvorite prozor.",
        errorLoad = "Nismo uspjeli učitati ovu anketu.",
    )

    private val HU = Labels(
        accept = "Küldés",
        decline = "Mégse",
        start = "Kérdőív indítása",
        complete = "Kérdőív befejezése",
        back = "Vissza",
        question = "Kérdés",
        of = "/",
        followUp = "Kiegészítő kérdés",
        closeAria = "Felugró ablak bezárása",
        loadingAria = "Kérdőív betöltése",
        errorRequired = "Kérjük, válaszoljon a kötelező kérdésre a továbblépéshez.",
        errorInvalidEmail = "Kérjük, adjon meg érvényes e-mail-címet.",
        errorSubmit = "Hiba történt a küldés során. Próbálja újra, vagy zárja be az ablakot.",
        errorLoad = "Nem sikerült betölteni ezt a kérdőívet.",
    )

    private val IT = Labels(
        accept = "Invia",
        decline = "Annulla",
        start = "Inizia il sondaggio",
        complete = "Completa il sondaggio",
        back = "Indietro",
        question = "Domanda",
        of = "di",
        followUp = "Approfondimento",
        closeAria = "Chiudi popup",
        loadingAria = "Caricamento del sondaggio",
        errorRequired = "Rispondi alla domanda obbligatoria per continuare.",
        errorInvalidEmail = "Inserisci un indirizzo email valido.",
        errorSubmit = "Si è verificato un errore durante l'invio. Riprova o chiudi il popup.",
        errorLoad = "Non è stato possibile caricare questo sondaggio.",
    )

    private val LT = Labels(
        accept = "Siųsti",
        decline = "Atšaukti",
        start = "Pradėti apklausą",
        complete = "Baigti apklausą",
        back = "Atgal",
        question = "Klausimas",
        of = "iš",
        followUp = "Papildomas klausimas",
        closeAria = "Uždaryti iššokantį langą",
        loadingAria = "Įkeliama apklausa",
        errorRequired = "Norėdami tęsti, atsakykite į privalomą klausimą.",
        errorInvalidEmail = "Įveskite galiojantį el. pašto adresą.",
        errorSubmit = "Siunčiant įvyko klaida. Bandykite dar kartą arba uždarykite langą.",
        errorLoad = "Nepavyko įkelti šios apklausos.",
    )

    private val LV = Labels(
        accept = "Nosūtīt",
        decline = "Atcelt",
        start = "Sākt aptauju",
        complete = "Pabeigt aptauju",
        back = "Atpakaļ",
        question = "Jautājums",
        of = "no",
        followUp = "Papildjautājums",
        closeAria = "Aizvērt uznirstošo logu",
        loadingAria = "Notiek aptaujas ielāde",
        errorRequired = "Lūdzu, atbildiet uz obligāto jautājumu, lai turpinātu.",
        errorInvalidEmail = "Lūdzu, ievadiet derīgu e-pasta adresi.",
        errorSubmit = "Nosūtot radās kļūda. Lūdzu, mēģiniet vēlreiz vai aizveriet logu.",
        errorLoad = "Neizdevās ielādēt šo aptauju.",
    )

    private val MT = Labels(
        accept = "Ibgħat",
        decline = "Ikkanċella",
        start = "Ibda l-istħarriġ",
        complete = "Lesti l-istħarriġ",
        back = "Lura",
        question = "Mistoqsija",
        of = "minn",
        followUp = "Mistoqsija ta' segwitu",
        closeAria = "Agħlaq il-popup",
        loadingAria = "Qed jitgħabba l-istħarriġ",
        errorRequired = "Jekk jogħġbok wieġeb il-mistoqsija obbligatorja biex tkompli.",
        errorInvalidEmail = "Jekk jogħġbok daħħal indirizz tal-email validu.",
        errorSubmit = "Seħħ żball waqt il-bgħit. Erġa' pprova jew agħlaq il-popup.",
        errorLoad = "Ma stajniex intellgħu dan l-istħarriġ.",
    )

    private val NL = Labels(
        accept = "Verzenden",
        decline = "Annuleren",
        start = "Enquête starten",
        complete = "Enquête afronden",
        back = "Terug",
        question = "Vraag",
        of = "van",
        followUp = "Vervolgvraag",
        closeAria = "Pop-up sluiten",
        loadingAria = "Enquête wordt geladen",
        errorRequired = "Beantwoord de verplichte vraag om verder te gaan.",
        errorInvalidEmail = "Voer een geldig e-mailadres in.",
        errorSubmit = "Er is iets misgegaan bij het verzenden. Probeer het opnieuw of sluit de pop-up.",
        errorLoad = "We konden deze enquête niet laden.",
    )

    private val PL = Labels(
        accept = "Wyślij",
        decline = "Anuluj",
        start = "Rozpocznij ankietę",
        complete = "Zakończ ankietę",
        back = "Wstecz",
        question = "Pytanie",
        of = "z",
        followUp = "Pytanie uzupełniające",
        closeAria = "Zamknij okno",
        loadingAria = "Ładowanie ankiety",
        errorRequired = "Odpowiedz na wymagane pytanie, aby przejść dalej.",
        errorInvalidEmail = "Wpisz prawidłowy adres e-mail.",
        errorSubmit = "Podczas wysyłania wystąpił błąd. Spróbuj ponownie lub zamknij okno.",
        errorLoad = "Nie udało się wczytać tej ankiety.",
    )

    private val RO = Labels(
        accept = "Trimiteți",
        decline = "Anulați",
        start = "Începeți sondajul",
        complete = "Finalizați sondajul",
        back = "Înapoi",
        question = "Întrebarea",
        of = "din",
        followUp = "Întrebare suplimentară",
        closeAria = "Închideți fereastra pop-up",
        loadingAria = "Se încarcă sondajul",
        errorRequired = "Vă rugăm să răspundeți la întrebarea obligatorie pentru a continua.",
        errorInvalidEmail = "Vă rugăm să introduceți o adresă de e-mail validă.",
        errorSubmit = "A apărut o eroare la trimitere. Încercați din nou sau închideți fereastra.",
        errorLoad = "Nu am putut încărca acest sondaj.",
    )

    private val SK = Labels(
        accept = "Odoslať",
        decline = "Zrušiť",
        start = "Začať prieskum",
        complete = "Dokončiť prieskum",
        back = "Späť",
        question = "Otázka",
        of = "z",
        followUp = "Doplňujúca otázka",
        closeAria = "Zavrieť vyskakovacie okno",
        loadingAria = "Načítava sa prieskum",
        errorRequired = "Ak chcete pokračovať, odpovedzte, prosím, na povinnú otázku.",
        errorInvalidEmail = "Zadajte, prosím, platnú e-mailovú adresu.",
        errorSubmit = "Pri odosielaní sa vyskytla chyba. Skúste to znova alebo okno zatvorte.",
        errorLoad = "Tento prieskum sa nepodarilo načítať.",
    )

    // Catalán: no es oficial de la UE, se pidió en la review v2.2.29. Espejo de `CA` en web.
    private val CA = Labels(
        accept = "Envia",
        decline = "Cancel·la",
        start = "Comença l'enquesta",
        complete = "Completa l'enquesta",
        back = "Enrere",
        question = "Pregunta",
        of = "de",
        followUp = "Seguiment",
        closeAria = "Tanca la finestra emergent",
        loadingAria = "S'està carregant l'enquesta",
        errorRequired = "Respon la pregunta obligatòria per continuar.",
        errorInvalidEmail = "Introdueix una adreça de correu electrònic vàlida.",
        errorSubmit = "S'ha produït un error en enviar. Torna-ho a provar o tanca la finestra.",
        errorLoad = "No s'ha pogut carregar l'enquesta.",
    )

    private val SL = Labels(
        accept = "Pošlji",
        decline = "Prekliči",
        start = "Začni anketo",
        complete = "Zaključi anketo",
        back = "Nazaj",
        question = "Vprašanje",
        of = "od",
        followUp = "Dodatno vprašanje",
        closeAria = "Zapri pojavno okno",
        loadingAria = "Nalaganje ankete",
        errorRequired = "Za nadaljevanje odgovorite na obvezno vprašanje.",
        errorInvalidEmail = "Vnesite veljaven e-poštni naslov.",
        errorSubmit = "Pri pošiljanju je prišlo do napake. Poskusite znova ali zaprite okno.",
        errorLoad = "Ankete ni bilo mogoče naložiti.",
    )

    private val TABLE: Map<String, Labels> = mapOf(
        "en" to EN, "es" to ES, "da" to DA, "no" to NO, "sv" to SV, "fi" to FI,
        "de" to DE, "fr" to FR, "pt" to PT, "ar" to AR, "bn" to BN, "zh-CN" to ZH_CN,
        // El resto de idiomas oficiales de la UE (DD-67).
        "bg" to BG, "cs" to CS, "el" to EL, "et" to ET, "ga" to GA, "hr" to HR, "hu" to HU, "it" to IT, "lt" to LT, "lv" to LV, "mt" to MT, "nl" to NL, "pl" to PL, "ro" to RO, "sk" to SK, "sl" to SL,
        "ca" to CA,
    )

    /**
     * Todos los prefijos BCP-47 con traducción propia. Si la plataforma añade un idioma nuevo
     * al selector del survey, hay que añadirlo aquí o el chrome saldrá en inglés debajo de un
     * survey traducido.
     */
    val supportedLanguages: List<String> =
        listOf(
            "en", "es", "da", "no", "sv", "fi", "de", "fr", "pt", "ar", "bn", "zh-CN",
            // El resto de idiomas oficiales de la UE (DD-67), traducidos a máquina: pendientes
            // de revisión nativa.
            "bg", "cs", "el", "et", "ga", "hr", "hu", "it", "lt", "lv", "mt", "nl", "pl", "ro", "sk", "sl",
            // Catalán (no oficial de la UE).
            "ca",
        )

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
            "bg" -> "bg"
            "cs" -> "cs"
            "el" -> "el"
            "et" -> "et"
            "ga" -> "ga"
            "hr" -> "hr"
            "hu" -> "hu"
            "it" -> "it"
            "lt" -> "lt"
            "lv" -> "lv"
            "mt" -> "mt"
            "nl" -> "nl"
            "pl" -> "pl"
            "ro" -> "ro"
            "sk" -> "sk"
            "sl" -> "sl"
            "ca" -> "ca"
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
