package com.deepdots.testapp

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

/** SDK Lab: what this run is (version, env, ids), what to test next, and the run log. */
@OptIn(ExperimentalMaterial3Api::class, androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
fun LabPanel(app: TestApp, onClose: () -> Unit) {
    val activity = LocalContext.current as Activity
    var draft by remember { mutableStateOf(app.config) }

    Dialog(onDismissRequest = onClose, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(Modifier.fillMaxSize().testIds().testTag("lab-panel"), color = Color(0xFFF2F2F7)) {
            Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("SDK Lab", fontSize = 28.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                    TextButton(onClick = onClose, modifier = Modifier.testTag("lab-close")) { Text("Close") }
                }
                Card {
                    InfoRow("SDK", "${BuildConfig.SDK_VERSION} · ${BuildConfig.SDK_SOURCE}", "lab-info-version")
                    InfoRow("Environment", "DEV · ${DEV_API.removePrefix("https://")}", "lab-info-env")
                    InfoRow("Public key", app.config.publicKey, "lab-info-key")
                    InfoRow("User id", if (app.initialized) app.sdk.getUserId() ?: "—" else "not initialized", "lab-info-user")
                    InfoRow("Session id", if (app.initialized) app.sdk.getSessionId() ?: "—" else "—", "lab-info-session")
                }

                Section("Scenario")
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Scenarios.names.forEach { name ->
                        FilterChip(selected = draft.scenario == name, onClick = { draft = draft.copy(scenario = name) },
                            label = { Text(name) }, modifier = Modifier.testTag("lab-scenario-$name"))
                    }
                }
                Choice("Navigation", listOf("history", "silent"), draft.nav) { draft = draft.copy(nav = it) }
                Choice("Language", listOf("en", "es"), draft.lang) { draft = draft.copy(lang = it) }
                Choice("Load", listOf("immediate", "consent"), draft.load) { draft = draft.copy(load = it) }

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedButton(onClick = { app.reset(); app.restart(activity) }, modifier = Modifier.weight(1f).height(48.dp).testTag("lab-reset")) {
                        Text("Reset", color = Color(0xFFB91C1C))
                    }
                    Button(onClick = { draft.save(activity); app.restart(activity) }, modifier = Modifier.weight(1f).height(48.dp).testTag("lab-apply")) {
                        Text("Apply & restart")
                    }
                }

                val events = Scenarios.events[app.config.scenario].orEmpty()
                if (events.isNotEmpty()) {
                    Section("Host events")
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        events.forEach { e ->
                            OutlinedButton(onClick = {
                                Lab.log("host", "triggerEvent($e)")
                                onClose()
                                app.sdk.triggerEvent(e)
                            }, modifier = Modifier.testTag("lab-event-$e")) { Text(e) }
                        }
                    }
                }

                Section("Log · logcat [lab]")
                Column(Modifier.fillMaxWidth().background(Color(0xFF0F1115), RoundedCornerShape(12.dp)).padding(10.dp).testTag("lab-panel-log")) {
                    Lab.lines.takeLast(40).forEach {
                        Text(it, color = Color(0xFFC9CCD3), fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                    }
                }
            }
        }
    }
}

/** Dialogs and sheets are separate windows: each one needs testTagsAsResourceId again. */
@OptIn(androidx.compose.ui.ExperimentalComposeUiApi::class)
fun Modifier.testIds(): Modifier = semantics { testTagsAsResourceId = true }

@Composable
private fun Card(content: @Composable () -> Unit) {
    Column(Modifier.fillMaxWidth().background(Color.White, RoundedCornerShape(12.dp)).padding(vertical = 4.dp)) { content() }
}

@Composable
private fun InfoRow(label: String, value: String, tag: String) {
    Row(Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 10.dp)) {
        Text(label, modifier = Modifier.weight(1f))
        Text(value, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, modifier = Modifier.testTag(tag))
    }
}

@Composable
private fun Section(title: String) {
    Text(title.uppercase(), fontSize = 12.sp, color = Color(0xFF4B5563), fontWeight = FontWeight.SemiBold)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun Choice(title: String, options: List<String>, value: String, onPick: (String) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(title, modifier = Modifier.weight(1f))
        options.forEach { o ->
            FilterChip(selected = value == o, onClick = { onPick(o) }, label = { Text(o) },
                modifier = Modifier.testTag("lab-${title.lowercase()}-$o"))
        }
    }
}
