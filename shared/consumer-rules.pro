# Reglas que el AAR aplica en la app del cliente cuando esta minifica (R8). Sin ellas, una app
# con `isMinifyEnabled = true` ni siquiera compila el release (lo destapó reference-android).

# API pública y modelos (@Serializable, enums leídos por nombre).
-keep class com.deepdots.sdk.** { *; }

# Ktor arrastra slf4j-api, que busca un binder opcional que nadie trae: R8 falla con
# "Missing class org.slf4j.impl.StaticLoggerBinder". Sin binder, slf4j usa su no-op.
-dontwarn org.slf4j.impl.StaticLoggerBinder
