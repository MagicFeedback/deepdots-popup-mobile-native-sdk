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

La app usa la integración de pruebas de `api-dev`, que segmenta así:

| Ruta / disparo | Trigger configurado |
|---|---|
| `/detail/1` (producto 1) | tiempo en página, 2 s |
| `/detail/2` (producto 2) | scroll 50 % |
| `/detail/3` (producto 3) | salida |
| evento `custom-event` (cualquier ruta) | evento |
| click `add-to-cart` | sin popup configurado todavía |

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

## Primeros hallazgos (2026-10-22, emulador API 36, SDK en `dev` = 0.7.1)

1. **La integración de pruebas de `api-dev` está rota en el backend.** Sus popups apuntan al
   `productId` `40c20557…`, y `GET /sdk/app/{surveyId}/{productId}/info` responde 406
   `Public key not found` en dev y en prod. Ningún survey carga. Hace falta una integración de
   pruebas válida (idealmente una solo para esta app, con un popup por tipo de trigger).
2. **Cuando el survey no carga, el popup no se puede cerrar desde la UI.** Sale
   "Survey load timeout" con un botón "Send" y sin la X. En Android se sale con Back; en iOS,
   que no tiene Back, el usuario se queda atrapado.
3. **Con un modal del host abierto, el popup se pinta debajo.** Disparando el evento desde el
   bottom sheet, el popup aparece detrás del velo del sheet y no se puede tocar hasta cerrar el
   sheet. El SDK monta el popup en la ventana de la Activity y el modal es otra ventana encima.

Verificado sin problemas: arranque con splash, popup por tiempo en una Activity Compose,
cierre con Back, la app sigue respondiendo tras cerrar el popup, y Back cierra antes el modal del
host que el popup.
