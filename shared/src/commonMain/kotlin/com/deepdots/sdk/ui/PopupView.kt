package com.deepdots.sdk.ui

import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeOut
import androidx.compose.animation.fadeIn
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.deepdots.sdk.SdkRuntime
import com.deepdots.sdk.i18n.DefaultLabels
import com.deepdots.sdk.models.*
import com.deepdots.sdk.util.HtmlParagraph
import com.deepdots.sdk.util.parsePopupHtml
import kotlinx.coroutines.delay

// Top-level enum to avoid local enum compile restriction
private enum class ViewState { Loading, Start, InProgressFirst, InProgressNext, Completed, Error }

@Composable
fun PopupView(
    popup: PopupDefinition,
    onAction: (Action) -> Unit,
    onSurveyEvent: (name: String, payload: String?) -> Unit = { _, _ -> },
    /**
     * Se llama una sola vez, cuando el popup ya se puede enseñar (survey pintado o techo de
     * espera vencido). La capa de plataforma lo usa para sacar de la pantalla su contenedor
     * mientras tanto, que es lo que evita además comerse los toques del usuario.
     */
    onReady: () -> Unit = {}
) {
    val primaryColorDefault = Color(0xFF1E293B)
    var primaryColor by remember { mutableStateOf(primaryColorDefault) }
    var bgColorOverride by remember { mutableStateOf<Color?>(null) }
    var didApplyLoadedOnce by remember { mutableStateOf(false) }

    val bgColor = bgColorOverride ?: when (popup.style.theme) {
        Theme.Light -> Color.White
        Theme.Dark -> Color(0xFF2B2B2B)
    }
    val textColor = if (popup.style.theme == Theme.Light) Color.Black else Color.White
    val paragraphs = remember(popup.message) { parsePopupHtml(popup.message) }

    // Apertura diferida (paridad con Web): el popup se monta invisible y se enseña cuando el
    // WebView avisa de que el survey está pintado, en vez de enseñar el spinner girando. El techo
    // lo abre igualmente si el survey tarda de más, así que una red mala retrasa la apertura pero
    // nunca la impide.
    var revealed by remember { mutableStateOf(false) }
    fun reveal() {
        if (revealed) return
        revealed = true
        onReady()
    }
    LaunchedEffect(Unit) {
        delay(PopupReveal.REVEAL_TIMEOUT_MS)
        reveal()
    }

    // Initialize in first-page state so spinner doesn’t cover content until survey explicitly signals loading
    var viewState by remember { mutableStateOf(ViewState.Loading) }
    var errorHint by remember { mutableStateOf<String?>(null) }

    // Idioma del chrome. Arranca con el del host (`InitOptions.provideLang`) y pasa al del
    // survey en cuanto el WebView lo reenvía en el `loaded`: un survey en danés debe traer
    // también sus botones en danés aunque el móvil esté en inglés. Espejo del SDK Web.
    var surveyLang by remember { mutableStateOf<String?>(null) }
    val chromeLang = surveyLang ?: SdkRuntime.provideLang?.invoke()
    val labels = DefaultLabels.labels(chromeLang)
    // Desde @magicfeedback/native 2.2.22 el survey del WebView se voltea solo para los idiomas
    // RTL; sin esto el chrome de Compose se quedaría mirando al otro lado.
    val layoutDirection = popupLayoutDirection(chromeLang)
    /** Etiqueta de la API si la plataforma la configuró; si no, la traducción del SDK. */
    fun actionLabel(apiLabel: String?, slot: DefaultLabels.Slot): String =
        apiLabel?.takeIf { it.isNotBlank() } ?: labels.get(slot)
    var surveyController: SurveyController? by remember { mutableStateOf(null) }

    // Lo que el WebView dice que ocupa el survey (px CSS = dp). El WebView no tiene tamaño
    // propio, así que sin este dato se estira hasta el máximo y una sola pregunta deja un hueco
    // enorme entre la última opción y el footer.
    var surveyContentHeightDp by remember { mutableStateOf<Int?>(null) }

    // Transicion entre paginas (ver PageTransition): alto congelado mientras se envia, ultimo
    // estado de navegacion para que el footer no desaparezca, y spinner con retraso.
    var frozenSurveyHeightDp by remember { mutableStateOf<Int?>(null) }
    var lastSettledState by remember { mutableStateOf<ViewState?>(null) }
    var showSpinner by remember { mutableStateOf(false) }
    LaunchedEffect(viewState) {
        if (viewState == ViewState.Loading) {
            delay(PageTransition.SPINNER_DELAY_MS)
            showSpinner = true
        } else {
            showSpinner = false
            frozenSurveyHeightDp = null
            lastSettledState = viewState
        }
    }

    // Profundidad de navegación DENTRO del survey: +1 por página avanzada, -1 al volver.
    // Sustituye a `total > 1 && progress in 1 until total`, que escondía el Back siempre que la
    // siguiente pantalla era una follow-up dinámica: las follow-up no entran en el grafo (suman
    // +0.5 al progress y no tocan el total), así que un survey de una pregunta con follow-up
    // tenía total=1 y nunca cumplía `total > 1`. Paridad con el fix de Web/RN.
    var pageDepth by remember { mutableStateOf(0) }

    // Estado de la barra de progreso. `enabled` lo decide el host (InitOptions.showProgressBar)
    // y, si no se pronuncia, la plataforma (style.showProgressBar del survey).
    var progressValue by remember { mutableStateOf(0.0) }
    var progressTotal by remember { mutableStateOf(0) }
    var platformShowProgressBar by remember { mutableStateOf(false) }
    var progressShowUnit by remember { mutableStateOf(true) }
    var progressUnit by remember { mutableStateOf(ProgressUnit.Fraction) }
    var progressBarColor by remember { mutableStateOf(Color(0xFF22C55E)) }

    var customFontFamily by remember { mutableStateOf<FontFamily?>(null) }
    LaunchedEffect(popup.style.font) {
        customFontFamily = SharedFontLoader.load(popup.style.font)
    }

    var imageUrlOverride by remember { mutableStateOf<String?>(null) }
    var imageMaxHeight by remember { mutableStateOf(80.dp) }
    var imageAlignment by remember { mutableStateOf(Alignment.Center) }
    var imageHorizontalPadding by remember { mutableStateOf(PaddingValues(0.dp)) }
    var popupMaxWidth by remember { mutableStateOf(420.dp) }
    var popupMaxHeightFraction by remember { mutableStateOf(0.9f) }

    // renderChrome=false (InitOptions): sin scrim ni tarjeta; el host controla el marco visual.
    // El survey (header cerrar + footer) sigue funcional. Paridad con Web/RN.
    val chrome = SdkRuntime.renderChrome

    Box(
        modifier = Modifier
            .fillMaxSize()
            // Invisible, no "no compuesto": el WebView tiene que estar montado para cargar el
            // survey. El velo del scrim también desaparece, si no se vería el fondo oscuro
            // varios cientos de ms antes que la tarjeta.
            .alpha(if (revealed) 1f else 0f)
            .background(if (chrome) Color(0x66000000) else Color.Transparent),
        contentAlignment = mapPosition(popup.style.position)
    ) {
        Surface(
            modifier = if (chrome) {
                Modifier
                    .padding(16.dp)
                    .widthIn(max = popupMaxWidth)
                    .wrapContentHeight()
            } else {
                Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
            },
            shape = if (chrome) androidx.compose.foundation.shape.RoundedCornerShape(16.dp) else androidx.compose.ui.graphics.RectangleShape,
            color = if (chrome) bgColor else Color.Transparent,
            tonalElevation = if (chrome) 6.dp else 0.dp,
            shadowElevation = if (chrome) 8.dp else 0.dp
        ) {
            MaterialTheme(typography = MaterialTheme.typography.withFontFamily(customFontFamily)) {
                CompositionLocalProvider(
                    LocalTextStyle provides LocalTextStyle.current.copy(fontFamily = customFontFamily),
                    LocalLayoutDirection provides layoutDirection,
                ) {
                    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                val maxPopupHeight = maxHeight * popupMaxHeightFraction
                val minSurveyHeight = (maxPopupHeight * 0.35f).coerceAtLeast(280.dp)
                // Espacio que le queda al survey dentro de la tarjeta: el resto se lo llevan
                // cabecera, logo, barra de progreso y footer. Por encima de esto el WebView hace
                // su propio scroll vertical.
                val maxSurveyHeight = maxPopupHeight * 0.72f
                val scrollState = rememberScrollState()
                Box(modifier = Modifier.fillMaxWidth()) {
                    // Wrap-content column with a hard max-height. If the total intrinsic size
                    // (header + image + survey area + footer) overflows maxPopupHeight, users
                    // can scroll the popup. The WebView itself also scrolls internally for very
                    // long surveys, so this is a defensive outer scroll.
                    Column(
                        modifier = Modifier
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                            .heightIn(max = maxPopupHeight)
                            .verticalScroll(scrollState),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Header row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = popup.title,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = textColor,
                                lineHeight = 24.sp
                            )
                            IconButton(
                                onClick = {
                                    val decline = popup.actions.decline
                                    if (decline != null) onAction(decline)
                                    else onAction(Action.Decline(label = labels.decline, cooldownDays = 0))
                                },
                                // La X no tiene texto: sin descripción el lector de pantalla solo
                                // anuncia "botón". Paridad con el aria-label del popup web.
                                modifier = Modifier.size(32.dp)
                                    .semantics { contentDescription = labels.closeAria },
                            ) { Text("✕", color = textColor, fontSize = 18.sp) }
                        }

                        // Optional image placeholder (supports runtime override via loaded style).
                        // Va ANTES de la barra de progreso: la marca abre la tarjeta y la barra
                        // queda pegada a la pregunta (paridad con el popup web, ui/logo.ts).
                        val finalImageUrl = imageUrlOverride ?: popup.style.imageUrl
                        if (finalImageUrl != null) {
                            Row(modifier = Modifier.fillMaxWidth()) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(imageMaxHeight)
                                        .padding(imageHorizontalPadding),
                                    contentAlignment = imageAlignment
                                ) {
                                    PlatformImage(
                                        url = finalImageUrl,
                                        modifier = Modifier.fillMaxWidth(),
                                        maxHeight = imageMaxHeight,
                                        alignment = imageAlignment,
                                        contentDescription = "Popup image"
                                    )
                                }
                            }
                        }

                        // Barra de progreso: "Question X of Y" + barra. El host manda si se
                        // pronunció en init; si no, la plataforma vía style.showProgressBar.
                        val progressBar = progressBarState(
                            enabled = SdkRuntime.showProgressBar ?: platformShowProgressBar,
                            progress = progressValue,
                            total = progressTotal,
                            completed = viewState == ViewState.Completed,
                            onStartPage = viewState == ViewState.Start,
                            showUnit = progressShowUnit,
                            unit = progressUnit,
                            labels = labels,
                        )
                        // Al llegar a la pantalla final la barra se oculta: plegandola con la
                        // misma duracion que el alto del survey, en vez de quitarla de golpe (era
                        // un salto de ~44 dp justo antes de que la tarjeta se ajustase).
                        AnimatedVisibility(
                            visible = progressBar.visible,
                            enter = fadeIn(tween(PageTransition.HEIGHT_ANIMATION_MS)) +
                                expandVertically(tween(PageTransition.HEIGHT_ANIMATION_MS)),
                            exit = fadeOut(tween(PageTransition.HEIGHT_ANIMATION_MS)) +
                                shrinkVertically(tween(PageTransition.HEIGHT_ANIMATION_MS)),
                        ) {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                if (progressBar.label.isNotEmpty()) {
                                    Text(
                                        text = progressBar.label,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = textColor
                                    )
                                }
                                LinearProgressIndicator(
                                    progress = { progressBar.fraction },
                                    modifier = Modifier.fillMaxWidth().height(4.dp),
                                    color = progressBarColor,
                                    trackColor = Color(0xFFE5E7EB),
                                    strokeCap = StrokeCap.Round,
                                    gapSize = 0.dp,
                                    drawStopIndicator = {}
                                )
                            }
                        }

                        // Message HTML
                        Row(modifier = Modifier.fillMaxWidth()) {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                paragraphs.forEach { p -> HtmlParagraphView(p, textColor) }
                            }
                        }

                        // Área del survey: se dimensiona con lo que el WebView dice que ocupa su
                        // contenido, no llenando el espacio disponible. Un WebView no tiene
                        // tamaño propio, así que antes se estiraba hasta el máximo y una sola
                        // pregunta dejaba un hueco enorme hasta el footer. Mientras no llega el
                        // dato se usa el suelo de siempre, y por encima del techo el WebView hace
                        // su propio scroll vertical (el horizontal lo corta el CSS).
                        Row(modifier = Modifier.fillMaxWidth()) {
                            val targetSurveyHeight = PopupReveal.resolveSurveyHeightDp(
                                reportedDp = PageTransition.heightSource(
                                    reported = surveyContentHeightDp,
                                    frozen = frozenSurveyHeightDp,
                                    loading = viewState == ViewState.Loading,
                                ),
                                floorDp = minSurveyHeight.value.toInt(),
                                ceilingDp = maxSurveyHeight.value.toInt(),
                            ).dp
                            // Antes de enseñarse el popup el alto se aplica sin animar, para que
                            // aparezca ya con su tamaño; despues, cada cambio de pagina se ajusta
                            // con una transicion corta en vez de saltar.
                            val surveyHeight by animateDpAsState(
                                targetValue = targetSurveyHeight,
                                animationSpec = if (revealed) tween(PageTransition.HEIGHT_ANIMATION_MS) else snap(),
                                label = "surveyHeight",
                            )
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(surveyHeight)
                            ) {
                            Column(
                                modifier = Modifier.matchParentSize()
                            ) {
                                SurveyView(
                                    popup.surveyId,
                                    popup.productId,
                                    backgroundColor = bgColor,
                                    font = popup.style.font,
                                    onEvent = { eventJson ->
                                        val name: String
                                        val payload: String?
                                        if (eventJson.trim().startsWith("{")) {
                                            val nameMatch = Regex("\"name\"\\s*:\\s*\"(.*?)\"").find(eventJson)
                                            name = nameMatch?.groupValues?.get(1) ?: eventJson
                                            payload = eventJson
                                        } else {
                                            name = eventJson
                                            payload = null
                                        }
                                        fun parseHexColor(hex: String?): Color? {
                                            val h = hex?.trim()?.removePrefix("#") ?: return null
                                            val v = h.uppercase()
                                            fun hex2intNullable(s: String): Int? = s.toIntOrNull(16)
                                            return when (v.length) {
                                                3 -> {
                                                    val r = hex2intNullable(v.substring(0,1).repeat(2)) ?: return null
                                                    val g = hex2intNullable(v.substring(1,2).repeat(2)) ?: return null
                                                    val b = hex2intNullable(v.substring(2,3).repeat(2)) ?: return null
                                                    Color(red = r/255f, green = g/255f, blue = b/255f)
                                                }
                                                6 -> {
                                                    val r = hex2intNullable(v.substring(0,2)) ?: return null
                                                    val g = hex2intNullable(v.substring(2,4)) ?: return null
                                                    val b = hex2intNullable(v.substring(4,6)) ?: return null
                                                    Color(red = r/255f, green = g/255f, blue = b/255f)
                                                }
                                                8 -> {
                                                    val a = hex2intNullable(v.substring(0,2)) ?: return null
                                                    val r = hex2intNullable(v.substring(2,4)) ?: return null
                                                    val g = hex2intNullable(v.substring(4,6)) ?: return null
                                                    val b = hex2intNullable(v.substring(6,8)) ?: return null
                                                    Color(red = r/255f, green = g/255f, blue = b/255f, alpha = a/255f)
                                                }
                                                else -> null
                                            }
                                        }
                                        fun payloadValue(key: String): String? {
                                            if (payload == null) return null
                                            val m = Regex("\"$key\"\\s*:\\s*\"(.*?)\"").find(payload)
                                            return m?.groupValues?.get(1)
                                        }
                                        // `payloadValue` solo lee strings entrecomillados; los flags y
                                        // los números del bridge llegan sin comillas.
                                        fun payloadBool(key: String): Boolean? =
                                            Regex("\"$key\"\\s*:\\s*(true|false)").find(payload ?: "")
                                                ?.groupValues?.get(1)?.toBooleanStrictOrNull()
                                        fun payloadNumber(key: String): Double? =
                                            Regex("\"$key\"\\s*:\\s*(-?\\d+(?:\\.\\d+)?)").find(payload ?: "")
                                                ?.groupValues?.get(1)?.toDoubleOrNull()
                                        /** Estado de navegación según la profundidad recorrida, no según `total`. */
                                        fun navState(): ViewState =
                                            if (pageDepth > 0) ViewState.InProgressNext else ViewState.InProgressFirst
                                        when (name) {
                                            PopupReveal.READY_EVENT -> reveal()
                                            PopupReveal.CONTENT_HEIGHT_EVENT ->
                                                payloadNumber("height")?.let { h ->
                                                    surveyContentHeightDp = h.toInt()
                                                }
                                            "popup_clicked", "loaded" -> {
                                                // Apply runtime style overrides if provided
                                                val primaryHex = Regex("\"buttonPrimaryColor\"\\s*:\\s*\"(#[0-9A-Fa-f]{3,8})\"").find(payload ?: "")?.groupValues?.get(1)
                                                val bgHex = Regex("\"boxBackgroundColor\"\\s*:\\s*\"(#[0-9A-Fa-f]{3,8})\"").find(payload ?: "")?.groupValues?.get(1)
                                                parseHexColor(primaryHex)?.let { primaryColor = it }
                                                parseHexColor(bgHex)?.let { bgColorOverride = it }
                                                val startMessage = payloadValue("startMessage")
                                                if (!didApplyLoadedOnce && !startMessage.isNullOrBlank()) {
                                                    viewState = ViewState.Start
                                                    didApplyLoadedOnce = true
                                                } else {
                                                    // default first page state
                                                    if (!didApplyLoadedOnce) {
                                                        viewState = ViewState.InProgressFirst
                                                        didApplyLoadedOnce = true
                                                    }
                                                }
                                                // Image overrides from style
                                                val imgUrl = payloadValue("image") ?: payloadValue("logo")
                                                if (!imgUrl.isNullOrBlank()) { imageUrlOverride = imgUrl }
                                                when (payloadValue("imageSize") ?: payloadValue("logoSize")) {
                                                    "small" -> imageMaxHeight = 60.dp
                                                    "medium" -> imageMaxHeight = 80.dp
                                                    "large" -> imageMaxHeight = 120.dp
                                                    else -> { /* keep default */ }
                                                }
                                                when (payloadValue("imagePosition") ?: payloadValue("logoPosition")) {
                                                    "left" -> { imageAlignment = Alignment.CenterStart; imageHorizontalPadding = PaddingValues(start = 0.dp, end = 16.dp, top = 0.dp, bottom = 8.dp) }
                                                    "right" -> { imageAlignment = Alignment.CenterEnd; imageHorizontalPadding = PaddingValues(start = 16.dp, end = 0.dp, top = 0.dp, bottom = 8.dp) }
                                                    "center" -> { imageAlignment = Alignment.Center; imageHorizontalPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 0.dp, bottom = 8.dp) }
                                                    else -> { /* center by default */ }
                                                }
                                                // Optional popup sizing overrides
                                                payloadValue("popupMaxWidth")?.toFloatOrNull()?.let { w -> popupMaxWidth = w.dp }
                                                payloadValue("popupMaxHeightFraction")?.toFloatOrNull()?.let { f -> popupMaxHeightFraction = f.coerceIn(0.5f, 0.98f) }
                                                // Barra de progreso: el total solo se conoce con el form montado.
                                                payloadBool("showProgressBar")?.let { platformShowProgressBar = it }
                                                payloadBool("showProgressUnit")?.let { progressShowUnit = it }
                                                if (payloadValue("progressUnit") == "percentage") progressUnit = ProgressUnit.Percentage
                                                parseHexColor(Regex("\"loadingBarColor\"\\s*:\\s*\"(#[0-9A-Fa-f]{3,8})\"").find(payload ?: "")?.groupValues?.get(1))
                                                    ?.let { progressBarColor = it }
                                                payloadNumber("total")?.let { progressTotal = it.toInt() }
                                                payloadNumber("progress")?.let { progressValue = it }
                                                // Idioma del survey (`formData.lang[0]`), que el
                                                // WebView reenvía al cargar: manda sobre el del host.
                                                payloadValue("surveyLang")?.takeIf { it.isNotBlank() }
                                                    ?.let { surveyLang = it }
                                            }
                                            "before_submit" -> {
                                                // Se conserva el alto de la pagina que se envia hasta que llega la siguiente.
                                                frozenSurveyHeightDp = surveyContentHeightDp
                                                viewState = ViewState.Loading
                                            }
                                            // Broaden validation match
                                            "validation_error_required" -> {
                                                // La página no ha cambiado: el estado de navegación se queda como estaba.
                                                errorHint = labels.errorRequired
                                                viewState = navState()
                                            }
                                            else -> {
                                                if (name.startsWith("validation_error")) {
                                                    // Rama defensiva: hoy el WebView solo emite
                                                    // `validation_error_required`. Sin mensaje del
                                                    // bridge se usa el mismo aviso traducido, en
                                                    // vez de un literal inglés suelto.
                                                    errorHint = payloadValue("message") ?: labels.errorRequired
                                                    viewState = navState()
                                                } else if (name == "submit_error") {
                                                    // Treat submit error as inline banner so user can correct and retry
                                                    errorHint = payloadValue("message") ?: labels.errorSubmit
                                                    viewState = navState()
                                                } else if (name == "survey_completed") {
                                                    // Move to completed state and show final message; don't auto-close
                                                    viewState = ViewState.Completed
                                                    // Clear any stale validation banner so the completion screen isn't
                                                    // shown next to a leftover error hint from a previous step.
                                                    errorHint = null
                                                    // Previously we called onAction(complete/decline) here, which closed the popup before user could read the message.
                                                } else if (name == "after_submit") {
                                                    pageDepth += 1
                                                    payloadNumber("progress")?.let { progressValue = it }
                                                    payloadNumber("total")?.let { if (it > 0) progressTotal = it.toInt() }
                                                    viewState = navState()
                                                    errorHint = null
                                                } else if (name == "back") {
                                                    if (pageDepth > 0) pageDepth -= 1
                                                    payloadNumber("progress")?.let { progressValue = it }
                                                    viewState = navState()
                                                    errorHint = null
                                                } else if (name == "popup_close") {
                                                    popup.actions.decline?.let { onAction(it) }
                                                }
                                            }
                                        }

                                        onSurveyEvent(name, payload)
                                    },
                                    onController = { controller -> surveyController = controller }
                                )
                            }
                            // Spinner solo sobre el survey y solo si la pagina tarda: antes era un
                            // velo sobre toda la tarjeta que salia en cada cambio de pagina.
                            if (showSpinner) {
                                Box(
                                    modifier = Modifier
                                        .matchParentSize()
                                        .background(bgColor.copy(alpha = 0.65f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(
                                        color = primaryColor,
                                        modifier = Modifier.semantics { contentDescription = labels.loadingAria },
                                    )
                                }
                            }
                            }
                        }

                        // Error hint (below survey)
                        if (errorHint != null) {
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                color = Color(0xFFFFF7ED),
                                border = BorderStroke(1.dp, Color(0xFFFCD34D))
                            ) {
                                Text(
                                    text = errorHint!!,
                                    color = Color(0xFF92400E),
                                    fontSize = 13.sp,
                                    modifier = Modifier.padding(10.dp)
                                )
                            }
                        }

                        // Footer buttons, driven by state. Mientras se envia una pagina se
                        // quedan los del ultimo estado (sin responder): antes la fila desaparecia,
                        // la tarjeta encogia unos 48 dp y volvia a crecer al llegar la pagina.
                        val pageLoading = viewState == ViewState.Loading
                        val footerAlpha by animateFloatAsState(
                            targetValue = if (showSpinner) PageTransition.FOOTER_BUSY_ALPHA else 1f,
                            label = "footerAlpha",
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth().alpha(footerAlpha),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            when (if (pageLoading) lastSettledState else viewState) {
                                null, ViewState.Loading -> { /* primera carga: aun no hay botones */ }
                                ViewState.Start -> {
                                    Button(
                                        modifier = Modifier.fillMaxWidth(),
                                        onClick = {
                                            if (pageLoading) return@Button
                                            surveyController?.startForm()
                                            // Move to first in-progress state so the footer shows the Send button
                                            viewState = ViewState.InProgressFirst
                                            errorHint = null
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = primaryColor)
                                    ) { Text(actionLabel(popup.actions.start?.label, DefaultLabels.Slot.START), color = Color.White) }
                                }
                                ViewState.InProgressFirst -> {
                                    Spacer(modifier = Modifier.weight(1f))
                                    Button(
                                        onClick = { if (!pageLoading) surveyController?.send() },
                                        colors = ButtonDefaults.buttonColors(containerColor = primaryColor)
                                    ) { Text(actionLabel(popup.actions.accept?.label, DefaultLabels.Slot.ACCEPT), color = Color.White) }
                                }
                                ViewState.InProgressNext -> {
                                    OutlinedButton(
                                        onClick = { if (!pageLoading) surveyController?.back() },
                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = primaryColor),
                                        border = BorderStroke(1.dp, primaryColor)
                                    ) { Text(actionLabel(popup.actions.back?.label, DefaultLabels.Slot.BACK)) }
                                    Spacer(modifier = Modifier.weight(1f))
                                    Button(
                                        onClick = { if (!pageLoading) surveyController?.send() },
                                        colors = ButtonDefaults.buttonColors(containerColor = primaryColor)
                                    ) { Text(actionLabel(popup.actions.accept?.label, DefaultLabels.Slot.ACCEPT), color = Color.White) }
                                }
                                ViewState.Completed -> {
                                    Button(
                                        modifier = Modifier.fillMaxWidth(),
                                        onClick = {
                                            if (pageLoading) return@Button
                                            val complete = popup.actions.complete
                                            if (complete != null) onAction(complete) else onAction(Action.Complete(label = "" ))
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = primaryColor)
                                    ) { Text(actionLabel(popup.actions.complete?.label, DefaultLabels.Slot.COMPLETE), color = Color.White) }
                                }
                                ViewState.Error -> {
                                    Button(
                                        onClick = { if (!pageLoading) popup.actions.decline?.let { onAction(it) } },
                                        colors = ButtonDefaults.buttonColors(containerColor = primaryColor)
                                    ) { Text(actionLabel(popup.actions.decline?.label, DefaultLabels.Slot.DECLINE), color = Color.White) }
                                }
                            }
                        }
                    }

                    // Auto-scroll to reveal the error banner when it appears (banner sits
                    // just above the footer, so we jump to the end of the scroll range).
                    LaunchedEffect(errorHint) {
                        if (errorHint != null) {
                            scrollState.animateScrollTo(scrollState.maxValue)
                        }
                    }

                }
            }
                }
            }
        }
    }
}

@Composable
private fun mapPosition(position: Position): Alignment = when (position) {
    Position.TopLeft -> Alignment.TopStart
    Position.TopRight -> Alignment.TopEnd
    Position.BottomLeft -> Alignment.BottomStart
    Position.BottomRight -> Alignment.BottomEnd
    Position.Center -> Alignment.Center
}

@Composable
private fun HtmlParagraphView(paragraph: HtmlParagraph, color: Color) {
    val text: AnnotatedString = buildAnnotatedString {
        paragraph.runs.forEach { run ->
            pushStyle(
                SpanStyle(
                    color = color,
                    fontWeight = if (run.bold) FontWeight.Bold else FontWeight.Normal,
                    fontStyle = if (run.italic) FontStyle.Italic else FontStyle.Normal
                )
            )
            append(run.text + " ")
            pop()
        }
    }
    Text(text = text, fontSize = 16.sp)
}
