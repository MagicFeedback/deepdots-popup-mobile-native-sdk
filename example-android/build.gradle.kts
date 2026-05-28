plugins {
    alias(libs.plugins.androidApplication)
    kotlin("android")
    alias(libs.plugins.composeCompiler)
}

android {
    namespace = "com.deepdots.example"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.deepdots.example"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
    }
    buildTypes {
        release { isMinifyEnabled = false }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions { jvmTarget = "17" }
}

dependencies {
    // Use the local :shared module so the example exercises the latest SDK
    // source (including features still en route to a Maven release).
    implementation(project(":shared"))
    implementation(libs.androidx.activity.compose)
    implementation("androidx.compose.ui:ui:1.7.0")
    implementation("androidx.compose.material3:material3:1.2.0")
    implementation("androidx.compose.material:material-icons-extended:1.7.0")
}
