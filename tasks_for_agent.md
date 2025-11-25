# Deepdots SDK – Tareas para Copilot / ChatGPT 5
Guía completa de instrucciones para construir el SDK multiplataforma (Android + iOS) con Kotlin Multiplatform + Compose Multiplatform.

> Cada tarea está escrita para que el agente pueda ejecutarla directamente en el proyecto.  
> Puedes decir: “Ejecuta la Tarea 4”, o “Completa todas las tareas del 1 al 6”.

---

## ✅ TAREA 1 — Crear modelos de triggers y condiciones
Crea en `shared/commonMain/kotlin/com/deepdots/sdk/models/` los modelos:
- `sealed class Trigger`
- `Trigger.TimeOnPage(value: Int, condition: List<Condition>)`
- `data class Condition(answered: Boolean, cooldownDays: Int)`
La estructura debe ser equivalente al SDK TypeScript proporcionado.

---

## ✅ TAREA 2 — Crear InitOptions, ShowOptions y Event models
Crear:
- `InitOptions(debug, mode, popups)`
- `ShowOptions(surveyId, productId, data)`
- enum `Mode { Client, Server }`
- enum `Event { PopupShown, PopupClicked, SurveyCompleted }`
- `EventData(popupId, surveyId, productId, extra)`
Ponlos en el mismo package de modelos.

---

## ✅ TAREA 3 — Crear EventBus multiplataforma
Crear un EventBus simple:
- registrar listeners
- permitir múltiples listeners por evento
- emitir eventos en cualquier orden
- ser thread-safe
No dependas de plataforma.

---

## ✅ TAREA 4 — Crear PlatformContext expect/actual
En `commonMain`:
- `expect class PlatformContext`

En `androidMain`:
- `actual class PlatformContext(val activity: Activity)`

En `iosMain`:
- `actual class PlatformContext(val viewController: UIViewController)`

---

## ✅ TAREA 5 — Implementar clase DeepdotsPopups
Crear clase con:
- `init(options)`
- `show(options, context)`
- `on(event, listener)`
- manejo de `activePopups`
- emitir eventos correctos
- uso del EventBus
- llamada a `PopupRenderer.show`

Debe ser equivalente al comportamiento del SDK TypeScript.

---

## ✅ TAREA 6 — Implementar PopupRenderer expect/actual
En `commonMain`:
```
expect object PopupRenderer {
    fun show(popup: PopupDefinition, context: PlatformContext, onAction: (Action) -> Unit)
}
```

Android:
- Usar ComposeView
- Insertar dinámicamente en una Activity
- Renderizar PopupView
- Mapear acciones

iOS:
- Usar `ComposeUIViewController`
- Presentarlo modalmente sobre el UIViewController dado

---

## ✅ TAREA 7 — Crear PopupView en Compose Multiplatform
Crear en `commonMain` un composable:
- `@Composable fun PopupView(popup: PopupDefinition, onAction: (Action) -> Unit)`
Debe incluir:
- título
- mensaje HTML parseado básico
- botones accept/decline (Material3)

---

## ✅ TAREA 8 — Soporte HTML básico
Implementar parseo simple de HTML (p, b, i).  
Si no hay librería multiplataforma, implementar un parser básico para:
- eliminar tags
- procesar saltos de línea
- soportar `<p>` como párrafo

---

## ✅ TAREA 9 — Manejo de acciones
En `DeepdotsPopups`:
- Crear `handleAction(Action)`
- Emitir eventos correctos
- Permitir cierre de popup (se implementará después)

---

## ✅ TAREA 10 — Triggers automáticos + cola de popups
Implementar:
- `Trigger.TimeOnPage`
- contador interno con coroutines en commonMain
- mostrar popups automáticamente cuando cumplan trigger
- condiciones: cooldownDays, answered
- segmentación por lang y path (lambdas configurables desde init)

---

## ✅ TAREA 11 — Persistencia expect/actual para cooldowns
Crear expect/actual:
```
interface KeyValueStorage {
    fun getLong(key: String): Long?
    fun putLong(key: String, value: Long)
}
```
Android → SharedPreferences  
iOS → NSUserDefaults

---

## ✅ TAREA 12 — API pública limpia
Crear `Deepdots.kt` que reexporte:
- `DeepdotsPopups`
- modelos
- tipos
- helpers

Debe ser el entrypoint del SDK.

---

## ✅ TAREA 13 — Proyecto de ejemplo Android
Crear módulo `example-android` que:
- inicialice el SDK
- pase PlatformContext(this)
- llame a `show(...)`
- escuche eventos

---

## ✅ TAREA 14 — Proyecto de ejemplo iOS
Crear proyecto Swift:
- Importar framework KMP generado
- Llamar a init()
- Llamar a show()
- Pasar UIViewController desde Swift

---

## ✅ TAREA 15 — Documentación Markdown del SDK
Reescribe `README.md` con:
- Overview
- Instalación Android
- Instalación iOS
- Uso de DeepdotsPopups
- Ejemplos completos
- API pública
- Cómo construir frameworks

---

## ✅ TAREA 16 — Script de build multiplataforma
Crear un script Gradle + shell para:
- construir AAR Android
- construir frameworks iOS (simulator + device)
- crear carpeta `/dist` con el SDK final

---

✅ Progress bar: 16 / 20

---

## ✅ TAREA 17 — Tests comunes
Crear tests en `commonTest` para:
- EventBus
- Triggers
- Conditions
- activePopups

---

## ✅ TAREA 18 — Producción (limpieza final)
Eliminar:
- logs
- código de debug
- tooling de preview
- imports no usados

---

## ✅ TAREA 19 — Roadmap futuro
Crear `ROADMAP.md` con:
- theming avanzado
- server-driven popups
- animaciones
- timers de pantalla
- persistencia avanzada

---

## ✅ TAREA 20 — Análisis de calidad
Integrar:
- ktlint
- detekt
- revisión automática antes de commit
Optimizar build gradle (configuración del proyecto).

---

# FIN DEL DOCUMENTO

