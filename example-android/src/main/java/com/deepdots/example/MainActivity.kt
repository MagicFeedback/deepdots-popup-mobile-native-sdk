package com.deepdots.example

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.Composable
import com.deepdots.sdk.Deepdots
import com.deepdots.sdk.platform.PlatformContext
import com.deepdots.sdk.models.*
import com.deepdots.sdk.models.Trigger.TimeOnPage
import com.deepdots.sdk.models.Condition
import com.deepdots.sdk.models.Action
import androidx.compose.foundation.layout.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.material3.OutlinedButton
import androidx.compose.runtime.*
import androidx.activity.compose.LocalActivity
import com.deepdots.sdk.DeepdotsPopupsSdk
import androidx.compose.runtime.LaunchedEffect

// Helper to safely resolve an Activity from a Context without casting
private fun Context.findActivity(): Activity? {
    var ctx: Context? = this
    while (ctx is ContextWrapper) {
        if (ctx is Activity) return ctx
        ctx = ctx.baseContext
    }
    return null
}

class MainActivity : ComponentActivity() {
    private val sdk = Deepdots.create()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sdk.attachContext(PlatformContext(this))
        // Set initial app path (update on navigation changes)
        sdk.setPath("/home")

        // Definición de ejemplo
        val popupDef = PopupDefinition(
            id = "popup-welcome",
            title = "",
            message = "",
            trigger = TimeOnPage(value = 3, condition = listOf(Condition(answered = false, cooldownDays = 1))),
            actions = Actions(
                accept = Action.Accept(label = "Send", surveyId = "a9c8c170-bb1c-11f0-9d29-d5fe3dd521d0"),
                decline = Action.Decline(label = "Cancel", cooldownDays = 1)
            ),
            surveyId = "a9c8c170-bb1c-11f0-9d29-d5fe3dd521d0",
            productId = "02b809f20e024bce47c57f123cff8735",
            style = Style(theme = Theme.Light, position = Position.Center),
            segments = Segments(
                path = listOf("/home"),
                lang = listOf("en")
            )
        )

        val popupDef2 = PopupDefinition(
            id = "popup-demo-1",
            title = "",
            message = "",
            trigger = TimeOnPage(value = 1, condition = listOf(Condition(answered = false, cooldownDays = 0))),
            actions = Actions(
                accept = Action.Accept(label = "Næste", surveyId = "eeb4e590-d0eb-11f0-b3ab-f13d725acff5"),
                decline = Action.Decline(label = "Tæt", cooldownDays = 1),
                complete = Action.Complete(label = "Indsend"),
                start = Action.Start(label = "Start"),
                back = Action.Back(label = "Tilbage")
            ),
            surveyId = "eeb4e590-d0eb-11f0-b3ab-f13d725acff5",
            productId = "e5c8241506ac83ddcf061a01f5b0f567",
            style = Style(theme = Theme.Light, position = Position.Center),
            segments = Segments(
                lang = listOf("en"),
                path = listOf("/fake")
            )
        )

        sdk.init(
            InitOptions(
                debug = true,
                popupOptions = PopupOptions(
                    id = "main-options",
                    publicKey = null,
                    popups = listOf(popupDef, popupDef2),
                    companyId = null
                ),
                autoLaunch = true,
                provideLang = { "en" }
            )
        )

        sdk.on(Event.PopupShown) { event ->
            println("[Example] PopupShown: ${event.popupId}")
        }
        sdk.on(Event.PopupClicked) { event ->
            println("[Example] PopupClicked: ${event.popupId} action=${event.extra["action"]}")
        }
        sdk.on(Event.SurveyCompleted) { event ->
            println("[Example] SurveyCompleted: ${event.surveyId}")
        }

        setContent {
            MaterialTheme {
                Surface(color = MaterialTheme.colorScheme.background) {
                    AppRoot(sdk)
                }
            }
        }
    }
}

private enum class Screen { Home, FakePage }

@Composable
private fun AppRoot(sdk: DeepdotsPopupsSdk) {
    var currentScreen by remember { mutableStateOf(Screen.Home) }
    // Update SDK path when screen changes
    LaunchedEffect(currentScreen) {
        val path = when (currentScreen) {
            Screen.Home -> "/home"
            Screen.FakePage -> "/fake"
        }
        sdk.setPath(path)
    }
    when (currentScreen) {
        Screen.Home -> HomeScreen(onNavigate = { currentScreen = Screen.FakePage })
        Screen.FakePage -> FakePageScreen(sdk = sdk, onBack = { currentScreen = Screen.Home })
    }
}

@Composable
private fun HomeScreen(onNavigate: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(24.dp)) {
            // Simple logo placeholder
            Surface(
                modifier = Modifier.size(120.dp),
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Box(contentAlignment = Alignment.Center) { Text("Deepdots", color = MaterialTheme.colorScheme.onPrimaryContainer) }
            }
            // Full-width button at bottom to navigate
            Button(onClick = onNavigate, modifier = Modifier.fillMaxWidth()) {
                Text("Go to test page")
            }
        }
    }
}

@Composable
private fun FakePageScreen(sdk: DeepdotsPopupsSdk, onBack: () -> Unit) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val activity = remember(context) { context.findActivity() }
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Test page", style = MaterialTheme.typography.headlineSmall)
        Text("Fake content with elements to try popups." )
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(onClick = {
                activity?.let { sdk.show(ShowOptions(surveyId = "a9c8c170-bb1c-11f0-9d29-d5fe3dd521d0", productId = "02b809f20e024bce47c57f123cff8735"), PlatformContext(it)) }
            }) { Text("Show popup manually") }
            OutlinedButton(onClick = onBack) { Text("Back") }
        }
        Spacer(modifier = Modifier.height(12.dp))
        // More fake controls
        Button(onClick = {
            activity?.let { sdk.show(ShowOptions(surveyId = "eeb4e590-d0eb-11f0-b3ab-f13d725acff5", productId = "e5c8241506ac83ddcf061a01f5b0f567"), PlatformContext(it)) }
        }) { Text("Action 1") }
        Button(onClick = { /* Simulate action */ }) { Text("Action 2") }
        OutlinedButton(onClick = { /* Simulate action */ }) { Text("Open fake dialog") }
    }
}
