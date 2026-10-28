package com.deepdots.sdk.release

import com.deepdots.sdk.DeepdotsPopups
import com.deepdots.sdk.models.InitOptions
import com.deepdots.sdk.models.Trigger
import java.io.File
import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlin.reflect.full.primaryConstructor
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.fail
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.descriptors.PolymorphicKind
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.SerialKind
import kotlinx.serialization.descriptors.StructureKind
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.add
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray
import kotlinx.serialization.json.putJsonObject

/**
 * Contrato de la release (MF-3242, docs/VERSIONING.md). Espejo de lo que en el SDK web hacen
 * `npm run release:schema` y `src/core/release-fixtures.test.ts`:
 *
 * - `release/config.schema.json` es exactamente el que sale del código: `InitOptions` (por
 *   reflexión) y la definición de popup que manda `GET /sdk/popups` (el descriptor de
 *   `ServerPopupDto`). Si cambian, este test falla hasta regenerarlo con
 *   `UPDATE_RELEASE_SCHEMA=1 ./gradlew :shared:testDebugUnitTest --tests '*ReleaseContractTest*'`.
 * - Cada fixture de `release/compatibility.json` pasa por el parser real del SDK sin perder
 *   ningún popup y con el trigger que dice el manifiesto.
 */
class ReleaseContractTest {

    private val root = File("..").canonicalFile
    private val manifest by lazy { Json.parseToJsonElement(File(root, MANIFEST).readText()).jsonObject }

    @Test
    fun configSchemaMatchesTheCode() {
        val path = manifest["configSchema"]!!.jsonObject["path"]!!.jsonPrimitive.content
        val file = File(root, path)
        val generated = generateConfigSchema()
        if (System.getenv("UPDATE_RELEASE_SCHEMA") == "1") {
            file.writeText(generated)
            println("$path regenerado.")
            return
        }
        assertTrue(file.exists(), "Falta $path. Regeneralo con UPDATE_RELEASE_SCHEMA=1 (ver docs/VERSIONING.md).")
        assertEquals(
            generated,
            file.readText(),
            "$path no corresponde al código: InitOptions o la definición de popup han cambiado. " +
                "Regeneralo con UPDATE_RELEASE_SCHEMA=1 ./gradlew :shared:testDebugUnitTest --tests '*ReleaseContractTest*'",
        )
    }

    @Test
    fun fixturesParseWithTheSdk() {
        val fixtures = manifest["fixtures"]!!.jsonArray
        assertTrue(fixtures.isNotEmpty(), "El manifiesto no tiene fixtures.")
        for (fixture in fixtures.map { it.jsonObject }) {
            val id = fixture["id"]!!.jsonPrimitive.content
            val text = File(root, fixture["path"]!!.jsonPrimitive.content).readText()
            val expected = Json.parseToJsonElement(text).jsonArray.size
            val popups = DeepdotsPopups().debugParseServerPayload(text)
            assertEquals(expected, popups.size, "Fixture $id: el SDK descarta ${expected - popups.size} popup(s).")
            val event = fixture["trigger"]!!.jsonObject["event"]!!.jsonPrimitive.content
            val events = popups.flatMap { it.triggers }.filterIsInstance<Trigger.Event>().map { it.name }
            if (event !in events) fail("Fixture $id: ningún popup se dispara con el evento \"$event\" ($events).")
        }
    }

    // ---------- generación del schema ----------

    @OptIn(ExperimentalSerializationApi::class)
    private fun generateConfigSchema(): String {
        val sdk = DeepdotsPopups()
        val defs = sortedMapOf<String, JsonObject>()
        val wire = WireSchema(defs, sdk.debugServerStyleDescriptor())
        wire.define(sdk.debugServerPopupDescriptor())
        KotlinSchema(defs).define(InitOptions::class)
        val schema = buildJsonObject {
            put("\$schema", "http://json-schema.org/draft-07/schema#")
            put("title", "Deepdots native SDK configuration")
            put(
                "description",
                "Generado por shared/src/androidUnitTest/.../release/ReleaseContractTest.kt; no editar a mano. " +
                    "init = InitOptions (API Kotlin; x-kotlin es el tipo exacto). " +
                    "popups = respuesta de GET /sdk/popups tal como la acepta el SDK.",
            )
            put("type", "object")
            putJsonObject("properties") {
                putJsonObject("init") { put("\$ref", "#/definitions/${KotlinSchema.nameOf(InitOptions::class)}") }
                putJsonObject("popups") {
                    put("type", "array")
                    putJsonObject("items") { put("\$ref", "#/definitions/PopupDefinition") }
                }
            }
            put("definitions", JsonObject(defs))
        }
        return Json { prettyPrint = true; prettyPrintIndent = "  " }.encodeToString(JsonObject.serializer(), schema) + "\n"
    }

    /** JSON Schema de lo que deserializa el SDK, desde los descriptores de kotlinx.serialization. */
    @OptIn(ExperimentalSerializationApi::class)
    private class WireSchema(private val defs: MutableMap<String, JsonObject>, private val style: SerialDescriptor) {

        fun define(descriptor: SerialDescriptor): String {
            val name = nameOf(descriptor)
            if (name in defs) return name
            defs[name] = JsonObject(emptyMap()) // evita recursión infinita
            defs[name] = buildJsonObject {
                put("type", "object")
                putJsonObject("properties") {
                    for (i in 0 until descriptor.elementsCount) {
                        val field = descriptor.getElementName(i)
                        put(field, override(name, field) ?: schema(descriptor.getElementDescriptor(i)))
                    }
                }
                putJsonArray("required") {
                    for (i in 0 until descriptor.elementsCount) {
                        if (!descriptor.isElementOptional(i)) add(descriptor.getElementName(i))
                    }
                }
            }
            return name
        }

        private fun schema(d: SerialDescriptor): JsonObject {
            val base: JsonObject = when {
                d.serialName.removeSuffix("?").startsWith("kotlinx.serialization.json.Json") -> JsonObject(emptyMap())
                d.kind == PrimitiveKind.STRING -> type("string")
                d.kind == PrimitiveKind.BOOLEAN -> type("boolean")
                d.kind in listOf(PrimitiveKind.INT, PrimitiveKind.LONG, PrimitiveKind.SHORT, PrimitiveKind.BYTE) -> type("integer")
                d.kind in listOf(PrimitiveKind.DOUBLE, PrimitiveKind.FLOAT) -> type("number")
                d.kind == SerialKind.ENUM -> buildJsonObject {
                    putJsonArray("enum") { for (i in 0 until d.elementsCount) add(d.getElementName(i)) }
                }
                d.kind == StructureKind.LIST -> buildJsonObject {
                    put("type", "array")
                    put("items", schema(d.getElementDescriptor(0)))
                }
                d.kind == StructureKind.MAP -> buildJsonObject {
                    put("type", "object")
                    put("additionalProperties", schema(d.getElementDescriptor(1)))
                }
                d.kind == StructureKind.CLASS -> ref(define(d))
                d.kind is PolymorphicKind -> JsonObject(emptyMap())
                else -> error("Tipo sin traducir a JSON Schema: ${d.serialName} (${d.kind})")
            }
            return if (d.isNullable) nullable(base) else base
        }

        /**
         * Campos que el SDK recibe como `JsonElement` y parsea a mano en `mapServerPopup`
         * (`mapTrigger`, `parseCooldown`, `parseLegacyConditions`, `parseServerStyle`). Si cambia
         * ese parseo, hay que cambiar esto.
         */
        private fun override(def: String, field: String): JsonObject? = when ("$def.$field") {
            "PopupDefinition.trigger", "PopupDefinition.triggers" -> nullable(
                buildJsonObject {
                    defs["ServerTrigger"] = triggerItem
                    putJsonArray("anyOf") {
                        add(ref("ServerTrigger"))
                        add(arrayOfItems(ref("ServerTrigger")))
                    }
                },
            )
            "PopupDefinition.cooldown" -> nullable(
                arrayOfItems(
                    buildJsonObject {
                        put("type", "object")
                        putJsonObject("properties") {
                            putJsonObject("answered") { putJsonArray("enum") { add("SHOWED"); add("PARTIAL"); add("COMPLETED") } }
                            put("cooldownDays", numberLike)
                        }
                        putJsonArray("required") { add("answered") }
                    },
                ),
            )
            "PopupDefinition.conditions" -> nullable(
                arrayOfItems(
                    buildJsonObject {
                        put("type", "object")
                        putJsonObject("properties") {
                            put("answered", nullable(type("boolean")))
                            put("cooldownDays", numberLike)
                        }
                    },
                ),
            )
            "PopupDefinition.style" -> nullable(
                buildJsonObject {
                    putJsonArray("anyOf") {
                        add(ref(define(style)))
                        add(buildJsonObject { put("type", "string"); put("description", "El mismo objeto, serializado como JSON.") })
                    }
                },
            )
            else -> null
        }

        private val numberLike = buildJsonObject { putJsonArray("type") { add("number"); add("string") } }

        private val triggerItem = buildJsonObject {
            put("type", "object")
            putJsonObject("properties") {
                putJsonObject("type") {
                    put("description", "El SDK no distingue mayúsculas. Un tipo desconocido se ignora; un popup sin triggers válidos se descarta.")
                    putJsonArray("enum") {
                        TRIGGER_TYPES.forEach { add(it) }
                        TRIGGER_TYPES.forEach { add(it.uppercase()) }
                    }
                }
                put("value", buildJsonObject { putJsonArray("type") { add("string"); add("number"); add("null") } })
                putJsonObject("condition") { put("description", "Condiciones legacy de ese trigger (como conditions).") }
            }
            putJsonArray("required") { add("type") }
        }

        private fun nameOf(d: SerialDescriptor): String {
            val simple = d.serialName.removeSuffix("?").substringAfterLast('.')
            return if (simple == "ServerPopupDto") "PopupDefinition" else simple.removeSuffix("Dto")
        }
    }

    /** JSON Schema de la API Kotlin (InitOptions y lo que cuelga de ella), por reflexión. */
    private class KotlinSchema(private val defs: MutableMap<String, JsonObject>) {

        fun define(kClass: KClass<*>): String {
            val name = nameOf(kClass)
            if (name in defs) return name
            defs[name] = JsonObject(emptyMap())
            defs[name] = when {
                kClass.isSealed -> buildJsonObject {
                    put("x-kotlin", name)
                    putJsonArray("oneOf") {
                        kClass.sealedSubclasses.sortedBy { it.simpleName }.forEach { add(ref(define(it))) }
                    }
                }
                kClass.java.isEnum -> buildJsonObject {
                    put("x-kotlin", name)
                    putJsonArray("enum") { kClass.java.enumConstants.forEach { add((it as Enum<*>).name) } }
                }
                kClass.objectInstance != null || kClass.primaryConstructor == null -> buildJsonObject {
                    put("x-kotlin", name)
                    if (kClass.java.isInterface) put("description", "Interfaz que implementa el host.")
                }
                else -> {
                    val params = kClass.primaryConstructor!!.parameters
                    buildJsonObject {
                        put("type", "object")
                        put("x-kotlin", name)
                        putJsonObject("properties") { params.forEach { put(it.name!!, schema(it.type)) } }
                        putJsonArray("required") { params.filterNot { it.isOptional }.forEach { add(it.name!!) } }
                    }
                }
            }
            return name
        }

        private fun schema(kType: KType): JsonObject {
            val kClass = kType.classifier as? KClass<*>
            val qualified = kClass?.qualifiedName.orEmpty()
            val base: Map<String, JsonElement> = when {
                kClass == null -> emptyMap()
                qualified == "kotlin.Boolean" -> type("boolean")
                qualified == "kotlin.String" -> type("string")
                qualified in listOf("kotlin.Int", "kotlin.Long", "kotlin.Short", "kotlin.Byte") -> type("integer")
                qualified in listOf("kotlin.Double", "kotlin.Float") -> type("number")
                qualified in listOf("kotlin.collections.List", "kotlin.collections.Set", "kotlin.collections.Collection") ->
                    buildJsonObject {
                        put("type", "array")
                        kType.arguments.firstOrNull()?.type?.let { put("items", schema(it)) }
                    }
                qualified == "kotlin.collections.Map" -> buildJsonObject {
                    put("type", "object")
                    kType.arguments.getOrNull(1)?.type?.let { put("additionalProperties", schema(it)) }
                }
                qualified.startsWith("com.deepdots.sdk.") -> ref(define(kClass))
                else -> emptyMap() // Any, funciones: solo x-kotlin
            }
            val withType = JsonObject(base + ("x-kotlin" to JsonPrimitive(label(kType))))
            return if (kType.isMarkedNullable && base.isNotEmpty()) nullable(withType) else withType
        }

        /** `kotlin.Function0<kotlin.String?>` → `Function0<String?>`. */
        private fun label(type: KType): String =
            type.toString().replace(Regex("""\b(?:[a-z][a-z0-9_]*\.)+(?=[A-Z])"""), "")

        companion object {
            fun nameOf(kClass: KClass<*>): String = kClass.qualifiedName!!.removePrefix("com.deepdots.sdk.")
        }
    }

    private companion object {
        const val MANIFEST = "release/compatibility.json"

        /** Los que entiende `mapTrigger`. */
        val TRIGGER_TYPES = listOf("time_on_page", "scroll", "exit", "event", "click")

        fun type(name: String) = buildJsonObject { put("type", name) }
        fun ref(name: String) = buildJsonObject { put("\$ref", "#/definitions/$name") }
        fun arrayOfItems(items: JsonObject) = buildJsonObject { put("type", "array"); put("items", items) }

        /** Añade `null` a lo que acepta el campo. */
        fun nullable(schema: JsonObject): JsonObject {
            val current = schema["type"]
            return when {
                current is JsonPrimitive -> JsonObject(schema + ("type" to buildJsonArray { add(current); add("null") }))
                current is JsonArray -> if (JsonPrimitive("null") in current) schema else JsonObject(schema + ("type" to JsonArray(current + JsonPrimitive("null"))))
                schema.isEmpty() -> schema
                else -> buildJsonObject {
                    putJsonArray("anyOf") { add(schema); add(type("null")) }
                }
            }
        }
    }
}
