package com.deepdots.sdk.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.deepdots.sdk.models.*
import com.deepdots.sdk.util.HtmlParagraph
import com.deepdots.sdk.util.parsePopupHtml

// Top-level enum to avoid local enum compile restriction
private enum class ViewState { Loading, Start, InProgressFirst, InProgressNext, Completed, Error }

@Composable
fun PopupView(
    popup: PopupDefinition,
    onAction: (Action) -> Unit
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

    // Initialize in first-page state so spinner doesn’t cover content until survey explicitly signals loading
    var viewState by remember { mutableStateOf(ViewState.Loading) }
    var errorHint by remember { mutableStateOf<String?>(null) }
    var surveyController: SurveyController? by remember { mutableStateOf(null) }

    var imageUrlOverride by remember { mutableStateOf<String?>(null) }
    var imageMaxHeight by remember { mutableStateOf(120.dp) }
    var imageAlignment by remember { mutableStateOf(Alignment.Center) }
    var imageHorizontalPadding by remember { mutableStateOf(PaddingValues(0.dp)) }
    var popupMaxWidth by remember { mutableStateOf(420.dp) }
    var popupMaxHeightFraction by remember { mutableStateOf(0.9f) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0x66000000)),
        contentAlignment = mapPosition(popup.style.position)
    ) {
        Surface(
            modifier = Modifier
                .padding(16.dp)
                .widthIn(max = popupMaxWidth)
                .wrapContentHeight(),
            shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
            color = bgColor,
            tonalElevation = 6.dp,
            shadowElevation = 8.dp
        ) {
            BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                val maxPopupHeight = maxHeight * popupMaxHeightFraction
                val minSurveyHeight = (maxPopupHeight * 0.35f).coerceAtLeast(280.dp)
                val scrollState = rememberScrollState()
                Box(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier
                            .padding(16.dp)
                            .heightIn(max = maxPopupHeight)
                            .verticalScroll(scrollState),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Header row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = popup.title,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = textColor
                            )
                            IconButton(
                                onClick = { popup.actions.decline?.let { onAction(it) } },
                                modifier = Modifier.size(32.dp)
                            ) { Text("✕", color = textColor, fontSize = 18.sp) }
                        }

                        // Optional image placeholder (supports runtime override via loaded style)
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

                        // Message HTML
                        Row(modifier = Modifier.fillMaxWidth()) {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                paragraphs.forEach { p -> HtmlParagraphView(p, textColor) }
                            }
                        }

                        // Survey render area (give it a decent min height so inner WebView can scroll)
                        Row(modifier = Modifier.fillMaxWidth()) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f, fill = false) // take available vertical space, keep footer visible
                                    .heightIn(min = minSurveyHeight)
                            ) {
                                SurveyView(
                                    popup.surveyId,
                                    popup.productId,
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
                                        when (name) {
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
                                                    "small" -> imageMaxHeight = 80.dp
                                                    "medium" -> imageMaxHeight = 120.dp
                                                    "large" -> imageMaxHeight = 160.dp
                                                    else -> { /* keep default */ }
                                                }
                                                when (payloadValue("imagePosition") ?: payloadValue("logoPosition")) {
                                                    "left" -> { imageAlignment = Alignment.CenterStart; imageHorizontalPadding = PaddingValues(start = 0.dp, end = 16.dp, top = 0.dp, bottom = 21.dp) }
                                                    "right" -> { imageAlignment = Alignment.CenterEnd; imageHorizontalPadding = PaddingValues(start = 16.dp, end = 0.dp, top = 0.dp, bottom = 21.dp) }
                                                    "center" -> { imageAlignment = Alignment.Center; imageHorizontalPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 0.dp, bottom = 21.dp) }
                                                    else -> { /* center by default */ }
                                                }
                                                // Optional popup sizing overrides
                                                payloadValue("popupMaxWidth")?.toFloatOrNull()?.let { w -> popupMaxWidth = w.dp }
                                                payloadValue("popupMaxHeightFraction")?.toFloatOrNull()?.let { f -> popupMaxHeightFraction = f.coerceIn(0.5f, 0.98f) }
                                            }
                                            "before_submit" -> { viewState = ViewState.Loading }
                                            // Broaden validation match
                                            "validation_error_required" -> {
                                                errorHint = "Please answer the required question to continue."
                                                viewState = ViewState.InProgressNext
                                            }
                                            else -> {
                                                if (name.startsWith("validation_error")) {
                                                    errorHint = payloadValue("message") ?: "Please check your answers and try again."
                                                    viewState = ViewState.InProgressNext
                                                } else if (name == "submit_error") {
                                                    // Treat submit error as inline banner so user can correct and retry
                                                    errorHint = payloadValue("message") ?: "An error occurred while submitting. Please try again."
                                                    viewState = ViewState.InProgressNext
                                                } else if (name == "survey_completed") {
                                                    viewState = ViewState.Completed
                                                } else if (name == "after_submit") {
                                                    val progress = Regex("\"progress\"\\s*:\\s*(\\d+)").find(payload ?: "")?.groupValues?.get(1)?.toIntOrNull() ?: 0
                                                    val total = Regex("\"total\"\\s*:\\s*(\\d+)").find(payload ?: "")?.groupValues?.get(1)?.toIntOrNull() ?: 0
                                                    viewState = if (total > 1 && progress in 1 until total) ViewState.InProgressNext else ViewState.InProgressFirst
                                                    errorHint = null
                                                } else if (name == "back") {
                                                    val progress = Regex("\"progress\"\\s*:\\s*(\\d+)").find(payload ?: "")?.groupValues?.get(1)?.toIntOrNull() ?: 0
                                                    viewState = if (progress == 0) ViewState.InProgressFirst else ViewState.InProgressNext
                                                    errorHint = null
                                                } else if (name == "popup_close") {
                                                    popup.actions.decline?.let { onAction(it) }
                                                }
                                            }
                                        }
                                    },
                                    onController = { controller -> surveyController = controller }
                                )
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

                        // Footer buttons, driven by state
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            when (viewState) {
                                ViewState.Loading -> { /* hide buttons while loading */ }
                                ViewState.Start -> {
                                    Button(
                                        modifier = Modifier.fillMaxWidth(),
                                        onClick = {
                                            surveyController?.startForm()
                                            // Move to first in-progress state so the footer shows the Send button
                                            viewState = ViewState.InProgressFirst
                                            errorHint = null
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = primaryColor)
                                    ) { Text(popup.actions.start?.label ?: "Start survey", color = Color.White) }
                                }
                                ViewState.InProgressFirst -> {
                                    Spacer(modifier = Modifier.weight(1f))
                                    Button(
                                        onClick = { surveyController?.send() },
                                        colors = ButtonDefaults.buttonColors(containerColor = primaryColor)
                                    ) { Text(popup.actions.accept?.label ?: "Send", color = Color.White) }
                                }
                                ViewState.InProgressNext -> {
                                    OutlinedButton(
                                        onClick = { surveyController?.back() },
                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = primaryColor),
                                        border = BorderStroke(1.dp, primaryColor)
                                    ) { Text(popup.actions.back?.label ?: "Back") }
                                    Spacer(modifier = Modifier.weight(1f))
                                    Button(
                                        onClick = { surveyController?.send() },
                                        colors = ButtonDefaults.buttonColors(containerColor = primaryColor)
                                    ) { Text(popup.actions.accept?.label ?: "Send", color = Color.White) }
                                }
                                ViewState.Completed -> {
                                    Button(
                                        modifier = Modifier.fillMaxWidth(),
                                        onClick = { popup.actions.complete?.let { onAction(it) } ?: popup.actions.decline?.let { onAction(it) } },
                                        colors = ButtonDefaults.buttonColors(containerColor = primaryColor)
                                    ) { Text(popup.actions.complete?.label ?: "Complete survey", color = Color.White) }
                                }
                                ViewState.Error -> {
                                    Button(
                                        onClick = { popup.actions.decline?.let { onAction(it) } },
                                        colors = ButtonDefaults.buttonColors(containerColor = primaryColor)
                                    ) { Text(popup.actions.decline?.label ?: "Close", color = Color.White) }
                                }
                            }
                        }
                    }

                    // Auto-scroll to reveal error banner
                    LaunchedEffect(errorHint) {
                        if (errorHint != null) {
                            scrollState.animateScrollTo(scrollState.maxValue)
                        }
                    }

                    // Overlay spinner centered above all content (matching popup bounds)
                    if (viewState == ViewState.Loading) {
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .background(Color.White.copy(alpha = 0.65f)),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = primaryColor)
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
