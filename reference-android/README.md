# App de referencia (Android)

App de pruebas con la estructura de una app de cliente, para probar el SDK como lo integra un
cliente y no como lo usa el demo. Es la base de las pruebas manuales en dispositivo y de los E2E.

## Qué reproduce

| Pieza | Dónde | Por qué |
|---|---|---|
| SDK inicializado una vez en `Application.onCreate` | `ReferenceApp` | Arranque en frío real; los triggers pueden llegar antes que las definiciones. |
| Contexto que sigue a la Activity visible | `ReferenceApp.ContextFollower` | Con varias Activities el popup tiene que pintarse en la que está en pantalla. |
| Foreground/background por proceso | `ProcessLifecycleOwner` en `ReferenceApp` | Cambiar de Activity no debe cerrar la sesión de analytics. |
| Splash previo a la pantalla principal | `SplashActivity` | La primera Activity que ve el SDK no es la principal. |
| Views + Fragments + BottomNavigationView | `MainActivity`, `Fragments.kt` | La forma más común de las apps de cliente. |
| Compose en otra Activity, con scroll largo | `ProductDetailActivity` | Triggers de tiempo, scroll, click y salida. |
| `android.app.Activity` plana | `CheckoutActivity` | Sin los owners de Compose, como `FlutterActivity`. |
| Modales del host (diálogo, bottom sheet, date picker) | pestaña Account | El SDK no debe tocarlos ni quedar tapado por ellos. |
| Deep link `deepdots-ref://product/{id}` | `ProductDetailActivity` | Entrar a una pantalla sin la principal debajo. |
| Cambio de usuario (`setUserId`) | pestaña Account | Login/logout. |
| Sonda de respuesta ("Probe taps") | todas las pantallas | Si deja de subir tras cerrar un popup, la app ha quedado bloqueada (el bug de la 0.7.0). |
| Diagnóstico (identidad, popups cargados, registro) | `DiagnosticsActivity` | Ver qué ha hecho el SDK sin leer logcat. |

Los elementos tienen ids estables (`resource-id` en Views, `testTag` expuesto como resource-id en
Compose) para poder automatizarlos.

## Rutas y popups

La app usa la configuración de popups **"Reference app (native SDK tests)"** del proyecto
`MAGICFEEDBACK_DEV` (clave `GCt7N5GG7jrvOI2GMRpjY99H3v6u3pIx`, api-dev), creada para ella. Todos
los popups abren el survey **"Reference app - Basic"** (valoración 1-10 obligatoria + texto libre
opcional, en dos páginas). Re-display por defecto: 7 días tras mostrarse, así que para repetir un
caso hay que borrar los datos de la app (`adb shell pm clear com.deepdots.reference`).

| Ruta / disparo | Trigger configurado |
|---|---|
| `/detail/1` (producto 1) | tiempo en página, 2 s |
| `/detail/2` (producto 2) | scroll 50 % |
| `/detail/3` (producto 3) | salida |
| evento `custom-event` (cualquier ruta) | evento |
| click `add-to-cart` (cualquier ficha) | click |

## Uso

```bash
JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home" ./gradlew :reference-android:installDebug
```

Otra integración sin tocar código:

```bash
./gradlew :reference-android:installDebug -PdeepdotsPublicKey=<clave> -PdeepdotsEnv=prod
```

El build `release` va con R8 activado a propósito: la minificación puede romper el SDK de formas
que el debug no ve.

Registro de la app: `adb logcat -s DeepdotsRef`.

## Casos de prueba

Pestaña Account → **Test cases** (`TestCasesActivity`): cada caso dice cómo reproducirlo y qué
tiene que pasar. Son bugs ya arreglados; si uno vuelve a fallar, es una regresión.

1. Popup disparado con un bottom sheet del host abierto.
2. Popup disparado con un diálogo del host abierto.
3. Toque fuera del popup (la sonda no debe subir).
4. Survey que no carga.
5. Escribir en el survey con el teclado abierto (Back y Send).

## Hallazgos (2026-10-22, emulador API 36, SDK en `dev` = 0.7.1)

1. **La integración de pruebas antigua de `api-dev` (clave `12mGEGK4…`) está rota en el
   backend.** Sus popups apuntan al `productId` `40c20557…`, y
   `GET /sdk/app/{surveyId}/{productId}/info` responde 406 `Public key not found`. **Resuelto**
   creando una configuración propia para esta app (ver "Rutas y popups").
2. **Con el survey caído no se podía cerrar el popup desde la UI.** El WebView, sin tamaño
   propio, medía 0 px y pintaba su fondo encima de la cabecera (la X desaparecía), y el fallo
   de carga llegaba al popup como una carga correcta (un "Send" sobre un survey vacío).
   **Arreglado** (caso 4).
3. **Con un modal del host abierto, el popup se pintaba debajo** de su velo, sin poder tocarse.
   **Arreglado:** espera a que el modal se cierre (casos 1 y 2).
4. **Los toques fuera del popup llegaban a la app de debajo.** **Arreglado** (caso 3).
5. **Con el teclado abierto, el Back cerraba el popup** (y se perdía lo escrito), y el teclado
   tapaba el Send. **Arreglado** (caso 5).
