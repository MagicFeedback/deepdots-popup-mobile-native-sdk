package com.deepdots.server

import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.request.*
import io.ktor.server.routing.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.plugins.callloging.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.util.UUID
import io.ktor.http.HttpStatusCode

fun main() {
    val port = System.getenv("PORT")?.toIntOrNull() ?: 8080
    embeddedServer(Netty, port = port) {
        install(CallLogging)
        install(CORS) {
            anyHost()
            allowNonSimpleContentTypes = true
        }
        install(ContentNegotiation) {
            json(
                Json {
                    ignoreUnknownKeys = true
                    prettyPrint = true
                    isLenient = true
                    encodeDefaults = true
                }
            )
        }
        routing { sdkRoutes() }
    }.start(wait = true)
}

fun Routing.sdkRoutes() {
    route("/sdk") {
        get("/{publicKey}/popups") {
            val publicKey = call.parameters["publicKey"] ?: return@get call.respondText(
                "Missing publicKey", status = HttpStatusCode.BadRequest
            )

            // Optional JSON in query parameter: filter={"filter":{"metadata":{...}}}
            val filterParam = call.request.queryParameters["filter"]
            val filter = if (filterParam != null) {
                try {
                    Json.decodeFromString(Filter.serializer(), filterParam)
                } catch (e: Exception) {
                    return@get call.respondText(
                        "Invalid filter JSON", status = HttpStatusCode.BadRequest
                    )
                }
            } else null

            // TODO: replace with real lookup using publicKey + filter
            val sessionId = UUID.randomUUID().toString()
            val popups = samplePopups()

            call.respond(PopupsResponse(popups = popups, sessionId = sessionId))
        }
    }
}

@Serializable
data class Filter(val filter: FilterBody? = null) {
    @Serializable
    data class FilterBody(val metadata: Map<String, String> = emptyMap())
}

@Serializable
data class PopupsResponse(val popups: List<Popup>, val sessionId: String)

@Serializable
data class Popup(
    val id: String,
    val title: String,
    val message: String,
    val surveyId: String,
    val productId: String,
)

private fun samplePopups(): List<Popup> = listOf(
    Popup(
        id = "welcome",
        title = "Hello",
        message = "<p><b>Can you help us?</b></p>",
        surveyId = "survey-123",
        productId = "product-xyz"
    )
)
