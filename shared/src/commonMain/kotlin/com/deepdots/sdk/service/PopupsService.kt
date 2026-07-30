package com.deepdots.sdk.service

import com.deepdots.sdk.SdkRuntime
import com.deepdots.sdk.analytics.AnalyticsFeedbackBody
import com.deepdots.sdk.analytics.analyticsFeedbackJson
import com.deepdots.sdk.contact.ContactBody
import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonPrimitive

/**
 * Service to handle network calls related to popups.
 */
interface PopupsService {
    /**
     * Fetches popups JSON from the server for the current environment and public key.
     * Base URL is derived from SdkRuntime.env ("dev" -> api-dev, otherwise api).
     * @param publicKey project public key
     * @param filter optional LoopBack-style filter JSON string
     * @return raw response body as text (JSON array)
     */
    suspend fun fetchPopups(publicKey: String, filter: String? = null): String

    /**
     * Posts a popup event to the server.
     * Endpoint: POST /sdk/popups
     * @return the `sessionId` the backend stitched for this user, or null if it didn't send one
     */
    suspend fun postPopupEvent(publicKey: String, status: String, popupId: String, userId: String?): String?

    /**
     * Posts an analytics batch as a Feedback of the analytics integration.
     * Endpoint: POST /sdk/feedback
     *
     * @return the `sessionId` of the record, or null when the response carries none
     * @throws RetryableFeedbackException on transient failures (5xx, 408, 429) so the caller
     *   can re-queue the batch; non-retryable 4xx are logged and swallowed (batch dropped).
     */
    suspend fun postFeedback(body: AnalyticsFeedbackBody): String?

    /**
     * Persists the host-owned user attributes on the backend Contact (popup segmentation).
     * Endpoint: POST /sdk/popups/contact
     */
    suspend fun postContact(body: ContactBody)
}

/**
 * Fallo TRANSITORIO enviando un lote de analytics: merece reintento en el flush siguiente.
 * Un 4xx (p. ej. 406 Contact) no usa esta excepción — se descarta el lote.
 */
class RetryableFeedbackException(message: String) : Exception(message)

class DefaultPopupsService(
    private val httpClient: HttpClient = HttpClient {
        install(ContentNegotiation) {
            json(
                Json {
                    ignoreUnknownKeys = true
                    isLenient = true
                }
            )
        }
    }
) : PopupsService {
    private fun baseUrl(): String = if (SdkRuntime.env == "dev") {
        "https://api-dev.deepdots.com"
    } else {
        "https://api.deepdots.com"
    }

    @Serializable
    private data class PopupEventBody(
        val publicKey: String,
        val status: String,
        val popupId: String,
        val userId: String? = null,
    )

    override suspend fun fetchPopups(publicKey: String, filter: String?): String {
        val endpoint = "${baseUrl()}/sdk/$publicKey/popups"
        // Add log to know which endpoint is being called, the filter, and the publicKey
        // println("Fetching popups from $endpoint with filter: $filter and publicKey: $publicKey")
        return httpClient.get(endpoint) {
            if (!filter.isNullOrBlank()) parameter("filter", filter)
        }.bodyAsText()
    }

    override suspend fun postPopupEvent(publicKey: String, status: String, popupId: String, userId: String?): String? {
        val endpoint = "${baseUrl()}/sdk/popups"
        val body = PopupEventBody(publicKey = publicKey, status = status, popupId = popupId, userId = userId)
        val responseText = httpClient.post(endpoint) {
            contentType(ContentType.Application.Json)
            setBody(body)
        }.bodyAsText()
        // El backend es dueño del session_id: lo devuelve en la respuesta (lo cose por user_id).
        return parseSessionId(responseText)
    }

    override suspend fun postFeedback(body: AnalyticsFeedbackBody): String? {
        val endpoint = "${baseUrl()}/sdk/feedback"
        // Se serializa aparte (encodeDefaults) para que `completed`/`metrics` viajen igual que
        // en Web; el Json del cliente ktor no puede cambiarse sin afectar a /sdk/popups.
        val json = analyticsFeedbackJson.encodeToString(AnalyticsFeedbackBody.serializer(), body)
        val response = httpClient.post(endpoint) {
            contentType(ContentType.Application.Json)
            setBody(json)
        }
        val status = response.status.value
        val text = runCatching { response.bodyAsText() }.getOrDefault("")
        if (status < 200 || status >= 300) {
            if (status >= 500 || status == 408 || status == 429) {
                // Reintentable: que el manager re-encole el lote.
                throw RetryableFeedbackException("POST /sdk/feedback $status: ${text.take(500)}")
            }
            // 4xx (p. ej. 406 Contact not found): visible y descartado, nunca en silencio.
            println("[DeepdotsAnalytics] POST /sdk/feedback rechazado con $status; lote DESCARTADO: ${text.take(500)}")
            return null
        }
        return parseSessionId(text)
    }

    override suspend fun postContact(body: ContactBody) {
        val endpoint = "${baseUrl()}/sdk/popups/contact"
        val response = httpClient.post(endpoint) {
            contentType(ContentType.Application.Json)
            setBody(analyticsFeedbackJson.encodeToString(ContactBody.serializer(), body))
        }
        val status = response.status.value
        if (status < 200 || status >= 300) {
            val text = runCatching { response.bodyAsText() }.getOrDefault("")
            println("[DeepdotsPopups] POST /sdk/popups/contact rechazado con $status: ${text.take(500)}")
        }
    }

    /** Saca `sessionId` de la respuesta del backend; null si no viene o no es JSON. */
    private fun parseSessionId(responseText: String): String? = runCatching {
        val obj = lenientJson.parseToJsonElement(responseText) as? JsonObject ?: return null
        obj["sessionId"]?.jsonPrimitive?.content?.takeIf { it.isNotBlank() && it != "null" }
    }.getOrNull()

    private companion object {
        val lenientJson = Json { ignoreUnknownKeys = true; isLenient = true }
    }
}
