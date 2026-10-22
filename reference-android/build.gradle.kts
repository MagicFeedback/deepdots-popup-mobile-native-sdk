plugins {
    alias(libs.plugins.androidApplication)
    kotlin("android")
    alias(libs.plugins.composeCompiler)
}

// App de referencia: reproduce la estructura de una app de cliente (varias Activities,
// Fragments, Compose, una Activity "plana", modales propios, Back, deep link y lifecycle
// de proceso) para probar el SDK como lo integra un cliente, no como lo usa el demo.
// La usan las pruebas manuales en dispositivo y los E2E.
android {
    namespace = "com.deepdots.reference"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.deepdots.reference"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        // Integración de pruebas en api-dev. Se puede cambiar sin tocar código:
        // ./gradlew :reference-android:installDebug -PdeepdotsPublicKey=... -PdeepdotsEnv=prod
        val publicKey = (project.findProperty("deepdotsPublicKey") as String?)
            ?: "12mGEGK4YXHXHrxZ45bJOsH6fiOl6ew1"
        val env = (project.findProperty("deepdotsEnv") as String?) ?: "dev"
        buildConfigField("String", "DEEPDOTS_PUBLIC_KEY", "\"$publicKey\"")
        buildConfigField("String", "DEEPDOTS_ENV", "\"$env\"")
    }
    buildFeatures {
        buildConfig = true
        compose = true
    }
    buildTypes {
        // El release también se prueba: R8 puede romper el SDK de formas que el debug no ve.
        release {
            isMinifyEnabled = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"))
            signingConfig = signingConfigs.getByName("debug")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions { jvmTarget = "17" }
}

dependencies {
    // Módulo local: la app prueba el código del SDK que se va a publicar.
    implementation(project(":shared"))

    // Parte "clásica" de la app (Views + Fragments), como la mayoría de apps de cliente.
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.recyclerview)
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.fragment:fragment-ktx:1.8.5")
    implementation("androidx.lifecycle:lifecycle-process:2.9.4")

    // Parte Compose (la ficha de producto).
    implementation(libs.androidx.activity.compose)
    implementation("androidx.compose.ui:ui:1.7.0")
    implementation("androidx.compose.material3:material3:1.2.0")
}
