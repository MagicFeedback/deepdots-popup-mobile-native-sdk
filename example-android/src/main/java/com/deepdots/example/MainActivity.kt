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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Divider
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.Scaffold
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.foundation.clickable
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.foundation.lazy.rememberLazyListState
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import androidx.compose.runtime.snapshotFlow
import kotlin.math.roundToInt

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
        // Attach context early
        sdk.attachContext(PlatformContext(this))

        setContent {
            MaterialTheme {
                Surface(color = MaterialTheme.colorScheme.background) {
                    DemoApp(sdk)
                }
            }
        }
    }
}

private enum class ScreenState { Login, Home, Detail }

private data class EventItem(val title: String, val description: String, val path: String)

@Composable
private fun DemoApp(sdk: DeepdotsPopupsSdk) {
    var screen by remember { mutableStateOf(ScreenState.Login) }
    var selectedUserId by remember { mutableStateOf("alpha-01") }
    var customUserId by remember { mutableStateOf("") }
    val events = remember {
        listOf(
            EventItem("Event 1", "Popup on enter", "/detail/1"),
            EventItem("Event 2", "Popup on scroll", "/detail/2"),
            EventItem("Event 3", "Popup on exit", "/detail/3")
        )
    }
    var detailTitle by remember { mutableStateOf("") }
    val context = androidx.compose.ui.platform.LocalContext.current
    val activity = remember(context) { context.findActivity() }

    // Update SDK path when screen changes
    LaunchedEffect(screen, detailTitle) {
        when (screen) {
            ScreenState.Login -> sdk.setPath("/login")
            ScreenState.Home -> sdk.setPath("/home")
            ScreenState.Detail -> {}
        }
    }

    // Event listeners once
    LaunchedEffect(Unit) {
        sdk.on(Event.PopupShown) { event -> println("[Android] PopupShown: ${'$'}{event.popupId}") }
        sdk.on(Event.PopupClicked) { event ->
            val action = event.extra["action"]
            println("[Android] PopupClicked: ${'$'}{event.popupId} action=${'$'}action")
        }
        sdk.on(Event.SurveyCompleted) { event -> println("[Android] SurveyCompleted: ${'$'}{event.surveyId}") }
    }

    when (screen) {
        ScreenState.Login -> LoginScreen(
            selectedUserId = selectedUserId,
            onSelectUser = { selectedUserId = it },
            customUserId = customUserId,
            onCustomUserChange = { customUserId = it },
            onStart = {
                val uid = if (customUserId.isBlank()) selectedUserId else customUserId
                initSdkForUser(sdk, uid)
                screen = ScreenState.Home
            }
        )
        ScreenState.Home -> HomeScreen(
            events = events,
            onSelect = { item ->
                sdk.setPath(item.path)
                detailTitle = item.title
                screen = ScreenState.Detail
            },
            onLogout = {
                // For demo: re-init with no metadata and go back
                initSdkForUser(sdk, selectedUserId) // keep basic init to continue demo
                screen = ScreenState.Login
            }
        )
        ScreenState.Detail -> DetailScreen(
            sdk = sdk,
            title = detailTitle,
            onBack = {
                sdk.setPath("/detail")
                screen = ScreenState.Home
            },
            onShowPopup = {
                activity?.let {
                    sdk.show(
                        ShowOptions(
                            surveyId = "a9c8c170-bb1c-11f0-9d29-d5fe3dd521d0",
                            productId = "02b809f20e024bce47c57f123cff8735"
                        ),
                        PlatformContext(it)
                    )
                }
            }
        )
    }
}

private fun initSdkForUser(sdk: DeepdotsPopupsSdk, userId: String) {
    // Server-like config mirroring iOS demo
    val options = InitOptions(
        debug = true,
        mode = Mode.Server,
        popupOptions = PopupOptions(
            publicKey = "12mGEGK4YXHXHrxZ45bJOsH6fiOl6ew1"
        ),
        autoLaunch = true,
        provideLang = { "en" },
        metadata = mapOf("userId" to userId)
    )
    sdk.init(options)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LoginScreen(
    selectedUserId: String,
    onSelectUser: (String) -> Unit,
    customUserId: String,
    onCustomUserChange: (String) -> Unit,
    onStart: () -> Unit
) {
    val presets = listOf("alpha-01", "beta-01", "gamma-01")
    Scaffold(topBar = { TopAppBar(title = { Text("deepdots", fontWeight = FontWeight.Bold) }) }) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Select a User ID or enter a custom one", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f))
            // User preset selector
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                presets.forEach { id ->
                    val selected = id == selectedUserId
                    val onClick = { onSelectUser(id) }
                    if (selected) {
                        Button(onClick = onClick) { Text(id) }
                    } else {
                        OutlinedButton(onClick = onClick) { Text(id) }
                    }
                }
            }
            Divider()
            // Custom user id
            androidx.compose.material3.OutlinedTextField(
                value = customUserId,
                onValueChange = onCustomUserChange,
                label = { Text("Custom User ID") },
                modifier = Modifier.fillMaxWidth()
            )
            Button(onClick = onStart, modifier = Modifier.fillMaxWidth()) { Text("Start") }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HomeScreen(
    events: List<EventItem>,
    onSelect: (EventItem) -> Unit,
    onLogout: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("deepdots", fontWeight = FontWeight.Bold) },
                actions = { OutlinedButton(onClick = onLogout) { Text("Sign out") } }
            )
        }
    ) { padding ->
        LazyColumn(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(events.take(3)) { item ->
                EventCard(item = item, onTap = { onSelect(item) })
            }
        }
    }
}

@Composable
private fun EventCard(item: EventItem, onTap: () -> Unit) {
    androidx.compose.material3.Card(
        modifier = Modifier.fillMaxWidth().clickable { onTap() }
    ) {
        Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(item.title, style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(item.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f))
            }
            Text(
                ">",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DetailScreen(sdk: DeepdotsPopupsSdk, title: String, onBack: () -> Unit, onShowPopup: () -> Unit) {
    val lorem = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum. "
    val listState = rememberLazyListState()
    BindLazyListScroll(sdk, listState)

    Scaffold(topBar = {
        TopAppBar(title = { Text(title) }, navigationIcon = {
            OutlinedButton(onClick = {
                sdk.onExit()
                onBack()
            }) { Text("Back") }
        }, actions = { Button(onClick = onShowPopup) { Text("Show popup") } })
    }) { padding ->
        LazyColumn(state = listState, modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp), verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(12.dp)) {
            items((0 until 20).toList()) { _ ->
                Text(lorem)
            }
        }
    }
}

@Composable
private fun BindLazyListScroll(sdk: DeepdotsPopupsSdk, listState: androidx.compose.foundation.lazy.LazyListState) {
    LaunchedEffect(listState) {
        snapshotFlow {
            val layout = listState.layoutInfo
            val total = layout.totalItemsCount.coerceAtLeast(1)
            val lastIndex = layout.visibleItemsInfo.lastOrNull()?.index ?: 0
            val progress = ((lastIndex + 1).toFloat() / total.toFloat()).coerceIn(0f, 1f)
            (progress * 100f).roundToInt()
        }
            .distinctUntilChanged()
            .collectLatest { pct -> sdk.onScroll(pct) }
    }
}
