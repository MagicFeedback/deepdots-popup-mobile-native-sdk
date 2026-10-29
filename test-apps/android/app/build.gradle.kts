import groovy.json.JsonSlurper

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
}

val sdkVersion = providers.gradleProperty("sdkVersion").get()
val sdkSource = if (sdkVersion == "local") "local :shared" else "Maven Central"

// Scenario -> DEV public key, from the file both apps share.
@Suppress("UNCHECKED_CAST")
val scenarios = (JsonSlurper().parse(rootDir.resolve("../scenarios.json")) as Map<String, Any?>)["scenarios"] as Map<String, Map<String, Any?>>
val scenarioKeys = scenarios.entries.joinToString(";") { (name, s) -> "$name=${s["publicKey"]}" }
val scenarioEvents = scenarios.entries.joinToString(";") { (name, s) ->
    "$name=${(s["events"] as? List<*>)?.joinToString(",").orEmpty()}"
}

android {
    namespace = "com.deepdots.testapp"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.deepdots.testapp"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"
        buildConfigField("String", "SDK_VERSION", "\"$sdkVersion\"")
        buildConfigField("String", "SDK_SOURCE", "\"$sdkSource\"")
        buildConfigField("String", "SCENARIO_KEYS", "\"$scenarioKeys\"")
        buildConfigField("String", "SCENARIO_EVENTS", "\"$scenarioEvents\"")
    }
    buildFeatures {
        compose = true
        buildConfig = true
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
    implementation("com.deepdots.sdk:shared-android:${if (sdkVersion == "local") "+" else sdkVersion}")

    implementation(platform("androidx.compose:compose-bom:2025.09.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.activity:activity-compose:1.11.0")
    implementation("androidx.lifecycle:lifecycle-process:2.9.4")
}

// `./gradlew :app:labSdkInfo` prints the SDK version Gradle actually resolved, so the CI log
// shows it next to the one that was asked for.
tasks.register("labSdkInfo") {
    val classpath = configurations.named("debugRuntimeClasspath")
    doLast {
        val resolved = classpath.get().incoming.resolutionResult.allComponents
            .mapNotNull { it.moduleVersion }
            .firstOrNull { it.group == "com.deepdots.sdk" && it.name == "shared-android" }
        println("[lab] sdk requested=$sdkVersion resolved=${resolved?.version ?: "local :shared"} source=$sdkSource")
    }
}
