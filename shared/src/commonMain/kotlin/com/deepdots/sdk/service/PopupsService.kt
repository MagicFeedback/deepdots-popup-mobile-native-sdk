package com.deepdots.sdk.service

import com.deepdots.sdk.SdkRuntime
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
import kotlin.math.log

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
     */
    suspend fun postPopupEvent(publicKey: String, status: String, popupId: String, userId: String?): String
}

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

    override suspend fun postPopupEvent(publicKey: String, status: String, popupId: String, userId: String?): String {
        val endpoint = "${baseUrl()}/sdk/popups"
        val body = PopupEventBody(publicKey = publicKey, status = status, popupId = popupId, userId = userId)
        return httpClient.post(endpoint) {
            contentType(ContentType.Application.Json)
            setBody(body)
        }.bodyAsText()
    }
}
