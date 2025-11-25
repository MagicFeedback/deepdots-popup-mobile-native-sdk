package com.deepdots.example

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
import com.deepdots.sdk.PlatformContext
import com.deepdots.sdk.models.*
import com.deepdots.sdk.models.Trigger.TimeOnPage
import com.deepdots.sdk.models.Condition
import com.deepdots.sdk.models.Action

class MainActivity : ComponentActivity() {
    private val sdk = Deepdots.create()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sdk.attachContext(PlatformContext(this))

        // Definición de ejemplo
        val popupDef = PopupDefinition(
            id = "popup-welcome",
            title = "Bienvenido",
            message = "<p>Gracias por probar <b>Deepdots</b> SDK.</p><p>¿Nos ayudas con una encuesta?</p>",
            trigger = TimeOnPage(value = 3, condition = listOf(Condition(answered = false, cooldownDays = 1))),
            actions = Actions(
                accept = Action.Accept(label = "Sí", surveyId = "survey-1"),
                decline = Action.Decline(label = "No", cooldownDays = 1)
            ),
            surveyId = "survey-1",
            productId = "product-xyz",
            style = Style(theme = Theme.Light, position = Position.Center),
            segments = null
        )

        sdk.init(
            InitOptions(
                debug = true,
                popups = listOf(popupDef),
                autoLaunch = true,
                provideLang = { "es" },
                providePath = { "/home" }
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
                    ExampleScreen { showManually ->
                        if (showManually) {
                            sdk.show(
                                ShowOptions(
                                    surveyId = "survey-1",
                                    productId = "product-xyz",
                                    data = mapOf("source" to "manual_button")
                                ),
                                PlatformContext(this)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ExampleScreen(onManualShow: (Boolean) -> Unit) {
    var counter by remember { mutableStateOf(0) }
    Button(onClick = {
        counter++
        onManualShow(true)
    }) {
        Text("Mostrar popup manual ($counter)")
    }
}
