import groovy.json.JsonSlurper

// Minimal host app: the SDK is created, initialised, attached and told the screen, nothing else
// (no lifecycle calls, no events, no Lab). It shows the client-exact-launch popup by itself.
plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

val sdkVersion = providers.gradleProperty("sdkVersion").get()

@Suppress("UNCHECKED_CAST")
val scenario = ((JsonSlurper().parse(rootDir.resolve("../scenarios.json")) as Map<String, Any?>)["scenarios"]
    as Map<String, Map<String, Any?>>)["client-exact-launch"]!!

android {
    namespace = "com.deepdots.basicapp"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.deepdots.basicapp"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"
        buildConfigField("String", "SDK_VERSION", "\"$sdkVersion\"")
        buildConfigField("String", "PUBLIC_KEY", "\"${scenario["publicKey"]}\"")
    }
    buildFeatures { buildConfig = true }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions { jvmTarget = "17" }
}

dependencies {
    implementation("com.deepdots.sdk:shared-android:${if (sdkVersion == "local") "+" else sdkVersion}")
    implementation("androidx.activity:activity-ktx:1.11.0")
}
