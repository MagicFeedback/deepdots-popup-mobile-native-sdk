package com.deepdots.sdk.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
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

@Composable
fun PopupView(
    popup: PopupDefinition,
    onAction: (Action) -> Unit
) {
    val bgColor = when (popup.style.theme) {
        Theme.Light -> Color.White
        Theme.Dark -> Color(0xFF2B2B2B)
    }
    val textColor = if (popup.style.theme == Theme.Light) Color.Black else Color.White
    val paragraphs = remember(popup.message) { parsePopupHtml(popup.message) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0x66000000)),
        contentAlignment = mapPosition(popup.style.position)
    ) {
        Surface(
            modifier = Modifier
                .padding(16.dp)
                .widthIn(max = 420.dp)
                .wrapContentHeight(),
            shape = RoundedCornerShape(16.dp),
            color = bgColor,
            tonalElevation = 6.dp,
            shadowElevation = 8.dp
        ) {
            // Outer padding 16dp as requested
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                var surveyController: SurveyController? = null
                var completed by remember { mutableStateOf(false) }

                // Row 1: Header (title + 32x32 close button)
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
                    ) {
                        Text("✕", color = textColor, fontSize = 18.sp)
                    }
                }

                // Row 2: Image (optional)
                popup.style.imageUrl?.let { _ ->
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp)
                                .background(Color.Gray.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Image", color = textColor.copy(alpha = 0.6f))
                        }
                    }
                }

                // Row 3: Message (HTML paragraphs)
                Row(modifier = Modifier.fillMaxWidth()) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        paragraphs.forEach { p ->
                            HtmlParagraphView(p, textColor)
                        }
                    }
                }

                // Row 4: Survey render
                Row(modifier = Modifier.fillMaxWidth()) {
                    SurveyView(
                        popup.surveyId,
                        popup.productId,
                        onEvent = { event ->
                            when (event) {
                                "popup_clicked" -> { /* loaded */ }
                                "survey_completed" -> {
                                    completed = true
                                    popup.actions.accept?.let { onAction(it) }
                                }
                                "popup_close" -> {
                                    popup.actions.decline?.let { onAction(it) }
                                }
                                else -> { }
                            }
                        },
                        onController = { controller -> surveyController = controller }
                    )
                }

                // Row 5: Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (!completed) {
                        Button(onClick = { surveyController?.send() }) {
                            Text(popup.actions.accept?.label ?: "Send")
                        }
                        OutlinedButton(onClick = { popup.actions.decline?.let { onAction(it) } }) {
                            Text(popup.actions.decline?.label ?: "Cancel")
                        }
                    } else {
                        Button(onClick = { popup.actions.decline?.let { onAction(it) } }) { Text("complet") }
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
