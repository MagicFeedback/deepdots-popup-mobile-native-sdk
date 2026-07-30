package com.deepdots.sdk.contact

import com.deepdots.sdk.storage.KeyValueStorage
import kotlinx.serialization.Serializable

/**
 * Atributos de contact del usuario IDENTIFICADO (el `userId` del init). Espejo de
 * `src/contact/contact-manager.ts` (Web).
 *
 * Es info interna que solo conoce el host (plan, edad, idioma preferido…) y se persiste en el
 * Contact del backend para segmentación/targeting de popups. Solo se envía cuando cambia
 * respecto a lo último persistido, así que el host puede llamar a `setAttributes` en cada
 * identificación sin generar tráfico.
 */

/** Clave de storage con el último payload enviado (para el diff). */
const val CONTACT_ATTRIBUTES_KEY = "deepdots.contact.attributes"

/** Body de `POST /sdk/popups/contact`. */
@Serializable
data class ContactBody(
    val publicKey: String,
    val userId: String,
    val userAttributes: Map<String, String>,
)

class ContactManager(
    private val storage: KeyValueStorage,
    private val publicKey: String,
    private val userId: String,
    /** Envío real (inyectable en tests): hace el `POST /sdk/popups/contact`. */
    private val post: suspend (ContactBody) -> Unit,
) {
    /**
     * Envía los atributos si cambiaron respecto a lo último enviado.
     * @return `true` si se envió, `false` si no hubo cambios.
     */
    suspend fun setAttributes(attributes: Map<String, Any?>): Boolean {
        val normalized = attributes
            .filterKeys { it.isNotBlank() }
            .mapNotNull { (k, v) -> v?.let { k to it.toString() } }
            .toMap()
        val serialized = stableSerialize(normalized)
        if (storage.getString(CONTACT_ATTRIBUTES_KEY) == serialized) return false

        post(ContactBody(publicKey = publicKey, userId = userId, userAttributes = normalized))
        storage.putString(CONTACT_ATTRIBUTES_KEY, serialized)
        return true
    }

    /** Serialización estable (claves ordenadas) para que el diff no dependa del orden. */
    private fun stableSerialize(attrs: Map<String, String>): String =
        attrs.keys.sorted().joinToString(",", prefix = "{", postfix = "}") { key ->
            "\"$key\":\"${attrs[key]}\""
        }
}
