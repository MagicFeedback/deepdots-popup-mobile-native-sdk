package com.deepdots.example

// Reuse the same demo Activity; it will link against the local shared module via dependencies.
// If we need package changes, keep the same package to avoid code duplication.

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
import android.provider.Settings
import android.os.Build
import java.util.Locale
import android.content.pm.PackageManager

private fun Context.findActivity(): Activity? {
    var ctx: Context? = this
    while (ctx is ContextWrapper) {
        if (ctx is Activity) return ctx
        ctx = ctx.baseContext
    }
    return null
}

private fun Context.appVersionName(): String {
    return try {
        val pm = packageManager
        val pkg = packageName
        val info = if (Build.VERSION.SDK_INT >= 33) {
            pm.getPackageInfo(pkg, PackageManager.PackageInfoFlags.of(0))
        } else {
            @Suppress("DEPRECATION")
            pm.getPackageInfo(pkg, 0)
        }
        info.versionName ?: ""
    } catch (_: Exception) { "" }
}

private fun Context.appVersionCode(): Long {
    return try {
        val pm = packageManager
        val pkg = packageName
        val info = if (Build.VERSION.SDK_INT >= 33) {
            pm.getPackageInfo(pkg, PackageManager.PackageInfoFlags.of(0))
        } else {
            @Suppress("DEPRECATION")
            pm.getPackageInfo(pkg, 0)
        }
        if (Build.VERSION.SDK_INT >= 28) info.longVersionCode else @Suppress("DEPRECATION") info.versionCode.toLong()
    } catch (_: Exception) { 0L }
}

class MainActivity : ComponentActivity() {
    private val sdk = Deepdots.create()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sdk.attachContext(PlatformContext(this))
        sdk.setPath("/home")

        // Build useful metadata for MagicFeedback
        val appVersion = applicationContext.appVersionName()
        val appBuild = applicationContext.appVersionCode().toString()
        val userId = "d59c2082c6c91cd3" // "d59c2082c6c91cd2"
        val locale = Locale.getDefault()
        val lang = locale.language
        val country = locale.country
        val deviceModel = listOfNotNull(Build.MANUFACTURER?.takeIf { it.isNotBlank() }, Build.MODEL?.takeIf { it.isNotBlank() }).joinToString(" ").trim()
        val osVersion = Build.VERSION.RELEASE ?: Build.VERSION.SDK_INT.toString()
        val metadata = mapOf(
            "userId" to userId,
            "appVersion" to appVersion,
            "appBuild" to appBuild,
            "lang" to lang,
            "country" to country,
            "device_model" to deviceModel,
            "os_version" to osVersion
        )

        sdk.init(
            InitOptions(
                debug = true,
                mode = Mode.Server,
                popupOptions = PopupOptions(
                    publicKey = "GpG9BAQMDFZXRJ6LrDr48W2foWgMURgy"
                ),
                autoLaunch = true,
                provideLang = { "en" },
                metadata = metadata
            )
        )

        sdk.on(Event.PopupShown) { event -> println("[ExampleLocal] PopupShown: ${'$'}{event.popupId}") }
        sdk.on(Event.PopupClicked) { event ->
            val action = event.extra["action"]
            println("[ExampleLocal] PopupClicked: ${'$'}{event.popupId} action=$action")
        }
        sdk.on(Event.SurveyCompleted) { event -> println("[ExampleLocal] SurveyCompleted: ${'$'}{event.surveyId}") }

        setContent {
            MaterialTheme {
                Surface(color = MaterialTheme.colorScheme.background) { AppRoot(sdk) }
            }
        }
    }
}

private enum class Screen { Home, FakePage }

@Composable
private fun AppRoot(sdk: DeepdotsPopupsSdk) {
    var currentScreen by remember { mutableStateOf(Screen.Home) }
    LaunchedEffect(currentScreen) {
        val path = when (currentScreen) {
            Screen.Home -> "/home"
            // Match server segments to auto-trigger popup after 2s
            Screen.FakePage -> "/secondary"
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
    Box(modifier = Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(24.dp)) {
            Surface(modifier = Modifier.size(120.dp), shape = MaterialTheme.shapes.medium, color = MaterialTheme.colorScheme.primaryContainer) {
                Box(contentAlignment = Alignment.Center) { Text("Deepdots Local", color = MaterialTheme.colorScheme.onPrimaryContainer) }
            }
            Button(onClick = onNavigate, modifier = Modifier.fillMaxWidth()) { Text("Go to test page") }
        }
    }
}

@Composable
private fun FakePageScreen(sdk: DeepdotsPopupsSdk, onBack: () -> Unit) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val activity = remember(context) { context.findActivity() }
    Column(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("Test page", style = MaterialTheme.typography.headlineSmall)
        Text("Fake content with elements to try popups.")
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(onClick = { activity?.let { sdk.show(ShowOptions(surveyId = "a9c8c170-bb1c-11f0-9d29-d5fe3dd521d0", productId = "02b809f20e024bce47c57f123cff8735"), PlatformContext(it)) } }) { Text("Show popup manually") }
            OutlinedButton(onClick = onBack) { Text("Back") }
        }
        Spacer(modifier = Modifier.height(12.dp))
        Button(onClick = { activity?.let { sdk.show(ShowOptions(surveyId = "eeb4e590-d0eb-11f0-b3ab-f13d725acff5", productId = "e5c8241506ac83ddcf061a01f5b0f567"), PlatformContext(it)) } }) { Text("Action 1") }
        Button(onClick = { /* Simulate action */ }) { Text("Action 2") }
        OutlinedButton(onClick = { /* Simulate action */ }) { Text("Open fake dialog") }
    }
}
