package com.deepdots.sdk.contact

import com.deepdots.sdk.storage.KeyValueStorage
import kotlinx.coroutines.CancellationException
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
    /**
     * Envío real (inyectable en tests): hace el `POST /sdk/popups/contact`. Tiene que LANZAR si
     * la API no acepta el envío (sin red o respuesta no-2xx): solo así se sabe que no hay que
     * guardar la caché y que la llamada siguiente debe reintentar.
     */
    private val post: suspend (ContactBody) -> Unit,
) {
    /**
     * Envía los atributos si cambiaron respecto a lo último enviado. La caché solo se guarda
     * cuando la API acepta el envío; si falla, la llamada siguiente reintenta. No lanza (salvo
     * la cancelación de la corrutina).
     * @return `true` si se envió, `false` si no hubo cambios o el envío falló.
     */
    suspend fun setAttributes(attributes: Map<String, Any?>): Boolean {
        val normalized = attributes
            .filterKeys { it.isNotBlank() }
            .mapNotNull { (k, v) -> v?.let { k to it.toString() } }
            .toMap()
        val serialized = stableSerialize(normalized)
        if (storage.getString(CONTACT_ATTRIBUTES_KEY) == serialized) return false

        try {
            post(ContactBody(publicKey = publicKey, userId = userId, userAttributes = normalized))
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            return false
        }
        storage.putString(CONTACT_ATTRIBUTES_KEY, serialized)
        return true
    }

    /**
     * Serialización estable (claves ordenadas) para que el diff no dependa del orden. Incluye el
     * `userId`: la clave de storage es una sola por dispositivo, y sin él un segundo usuario con
     * los mismos atributos que el anterior no llegaría a enviar los suyos.
     */
    private fun stableSerialize(attrs: Map<String, String>): String =
        attrs.keys.sorted().joinToString(",", prefix = "{\"userId\":\"$userId\",\"attributes\":{", postfix = "}}") { key ->
            "\"$key\":\"${attrs[key]}\""
        }
}
