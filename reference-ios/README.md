# App de referencia (iOS)

Gemela de `reference-android`: una app con la estructura de una app de cliente iOS, para probar el
SDK como lo integra un cliente y no como lo usa el demo (`iosApp`). Mismas rutas, mismos popups y
mismos casos de prueba que la de Android, para que las dos plataformas se prueben igual.

## Qué reproduce

| Pieza | Dónde | Por qué |
|---|---|---|
| SDK inicializado una vez en `AppDelegate` | `AppDelegate.swift` | Arranque en frío real. |
| Contexto = `rootViewController` de la ventana | `SceneDelegate.swift` | Es lo que hace un cliente (y el demo, y la guía). |
| Foreground/background en el `SceneDelegate` | `sceneWillEnterForeground` / `sceneDidEnterBackground` | Nunca en `sceneWillResignActive`: en iOS es transitorio. |
| UIKit: `UITabBarController` + `UINavigationController` | `MainTabBarController`, `HomeViewController` | La forma más común de una app iOS ya hecha. |
| Ficha con scroll largo enganchado con `bindScrollView` | `ProductDetailViewController.swift` | Triggers de tiempo, scroll, click y salida. |
| Una pantalla en SwiftUI (`UIHostingController`) | `SearchViewController.swift` | Apps que migran por partes. |
| Pantalla presentada a pantalla completa | `CheckoutViewController.swift` | El equivalente iOS de abrir otra Activity. |
| Modales del host (alerta, sheet, date picker) | pestaña Account | El SDK no debe tocarlos ni perderse por ellos. |
| Deep link `deepdots-ref://product/{id}` | `SceneDelegate.swift` | Entrar a una pantalla directamente. |
| Cambio de usuario (`setUserId`) | pestaña Account | Login/logout. |
| Sonda de respuesta ("Probe taps") | todas las pantallas | Si deja de subir tras cerrar un popup, la app ha quedado bloqueada. |
| Diagnóstico (identidad, popups cargados, registro) | `DiagnosticsViewController.swift` | Ver qué ha hecho el SDK sin leer la consola. |
| `Info.plist` **sin** `CADisableMinimumFrameDurationOnPhone` | `ReferenceIos/Info.plist` | A propósito: la mayoría de apps no la tienen (ver hallazgos). |

Los controles llevan `accessibilityIdentifier` estables (`probe_button`, `case_*`, `detail_*`…)
para automatizarlos.

## Rutas y popups

La misma configuración que Android: **"Reference app (native SDK tests)"** del proyecto
`MAGICFEEDBACK_DEV` (api-dev, clave en `Configuration/Config.xcconfig`), con el survey
**"Reference app - Basic"**.

| Ruta / disparo | Trigger configurado |
|---|---|
| `/detail/1` (producto 1) | tiempo en página, 2 s |
| `/detail/2` (producto 2) | scroll 50 % |
| `/detail/3` (producto 3) | salida |
| evento `custom-event` (cualquier ruta) | evento |
| click `add-to-cart` (cualquier ficha) | click |

Los cinco popups comparten survey: al **completarlo** una vez, el SDK no vuelve a enseñar ninguno;
y cerrar un popup aplica 7 días de cooldown a ese popup. Para repetir un caso, reinstala la app
(`xcrun simctl uninstall booted com.deepdots.reference`, o bórrala del dispositivo).

## Uso

```bash
reference-ios/run.sh
```

Compila el SDK para el simulador, regenera `dist/spm-local/DeepdotsSDK.xcframework`, compila la app,
la instala en el simulador y la lanza con la consola enganchada. Opciones: `SIM="iPhone 17 Pro"`,
`DEEPDOTS_PUBLIC_KEY=<clave>`, `DEEPDOTS_ENV=prod`. En dispositivo físico: abre
`reference-ios/ReferenceIos.xcodeproj` en Xcode (el XCFramework local tiene que incluir `iosArm64`).

## Casos de prueba

Pestaña Account → **Test cases**. Los cinco de Android más uno propio de iOS:

1. Popup disparado con un sheet del host abierto.
2. Popup disparado con una alerta del host abierta.
3. Toque fuera del popup (la sonda no debe subir).
4. Survey que no carga.
5. Escribir en el survey con el teclado abierto.
6. Popup disparado desde una pantalla presentada a pantalla completa.

## Hallazgos (2026-10-23, simulador iPhone 17, iOS 26)

1. **El SDK cerraba la app al abrir el popup** si el `Info.plist` del host no tenía
   `CADisableMinimumFrameDurationOnPhone = true`. Compose Multiplatform lo comprueba al crear el
   controlador y lanza una excepción que nadie captura, en cualquier iPhone (no solo en los de
   120 Hz). La documentación lo pedía como requisito, pero una app de cliente normalmente no la
   tiene. **Arreglado:** el SDK desactiva esa comprobación; sin la clave el popup se anima a 60 Hz.
2. **Con un sheet o una pantalla completa del host encima, el popup se perdía.** El SDK lo
   presentaba desde el controlador del contexto, que ya estaba presentando otra cosa, y UIKit lo
   rechazaba sin avisar: el popup contaba como mostrado (evento, reporte, cooldown) y nadie lo veía.
   **Arreglado:** se presenta desde lo más alto de la cadena; con una alerta del host encima espera
   a que se cierre (casos 1, 2 y 6).

Verificado sin problemas: flujo completo del survey (valoración, texto, envío, pantalla final),
toque fuera del popup (caso 3), survey que no carga (caso 4, con una clave rota), la app responde
tras cerrar el popup. **No verificado:** el teclado en pantalla (el simulador lleva el teclado
físico conectado) y dispositivos físicos.
