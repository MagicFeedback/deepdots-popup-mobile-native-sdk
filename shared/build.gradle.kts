import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}

kotlin {
    androidTarget { // proper android target registration
        compilerOptions { jvmTarget.set(JvmTarget.JVM_17) }
    }
    iosArm64()
    iosSimulatorArm64()

    sourceSets {
        androidMain.dependencies {
            // Keep only activity-compose; rely on commonMain's compose.* modules
            implementation(libs.androidx.activity.compose)
        }
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.androidx.lifecycle.runtimeCompose)
        }
        commonTest.dependencies { implementation(libs.kotlin.test) }
    }

    // Configure iOS frameworks
    listOf(
        targets.getByName("iosArm64"),
        targets.getByName("iosSimulatorArm64")
    ).forEach { t ->
        (t as org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget).binaries.framework {
            baseName = "ComposeApp"
            isStatic = true
        }
    }
}

android {
    namespace = "com.deepdots.sdk"
    compileSdk = libs.versions.android.compileSdk.get().toInt()
    defaultConfig { minSdk = libs.versions.android.minSdk.get().toInt() }
    packaging { resources { excludes += "/META-INF/{AL2.0,LGPL2.1}" } }
    buildTypes { getByName("release") { isMinifyEnabled = false }; getByName("debug") { } }
    compileOptions { sourceCompatibility = JavaVersion.VERSION_17; targetCompatibility = JavaVersion.VERSION_17 }
    publishing { singleVariant("release") { withSourcesJar() } }
}

// Configuración de toolchain Java para asegurar JDK 17
java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

dependencies { debugImplementation(compose.uiTooling) }

val copyAarToDist by tasks.registering(Copy::class) {
    dependsOn(tasks.named("assembleRelease"))
    from(layout.buildDirectory.file("outputs/aar/shared-release.aar"))
    into(rootProject.layout.projectDirectory.dir("dist/android"))
}

val copyIosFrameworksToDist by tasks.registering(Copy::class) {
    dependsOn(tasks.named("assemble"))
    from(layout.buildDirectory.dir("bin")) {
        include("iosArm64/releaseFramework/**")
        include("iosSimulatorArm64/releaseFramework/**")
    }
    into(rootProject.layout.projectDirectory.dir("dist/ios"))
}

tasks.register("buildSdkDist") {
    group = "distribution"
    description = "Builds Android AAR and iOS frameworks and copies them to dist/"
    dependsOn(copyAarToDist, copyIosFrameworksToDist)
}
