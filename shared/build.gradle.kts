import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.plugin.mpp.apple.XCFramework

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    id("maven-publish")
    id("signing")
}

group = providers.gradleProperty("PUBLISHING_GROUP").orNull ?: "com.deepdots"
version = providers.gradleProperty("PUBLISHING_VERSION").orNull ?: "0.1.0"

kotlin {
    val xcf = XCFramework()
    androidTarget { // proper android target registration
        compilerOptions { jvmTarget.set(JvmTarget.JVM_17) }
        publishLibraryVariants("release") // publish the Android release variant
    }
    iosArm64()
    iosSimulatorArm64()

    sourceSets {
        androidMain.dependencies {
            implementation(libs.androidx.activity.compose)
            // Use explicit dependency to avoid catalog parse issues
            implementation("io.coil-kt:coil-compose:2.7.0")
        }
        val commonMain by getting {
            dependencies {
                implementation(compose.runtime)
                implementation(compose.foundation)
                implementation(compose.material3)
                implementation(compose.ui)
                implementation(compose.components.resources)
                implementation(compose.components.uiToolingPreview)
                implementation(libs.androidx.lifecycle.viewmodelCompose)
                implementation(libs.androidx.lifecycle.runtimeCompose)
                // No image loader in commonMain; platform-specific actuals will handle it
            }
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
            xcf.add(this)
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

    sourceSets.getByName("main") {
        assets.srcDirs("src/androidMain/assets")
    }
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

// Eliminar tareas automáticas de descarga; usaremos vendorización manual mediante scripts

// Configure publications for Maven Central
publishing {
    publications {
        // Configure POM metadata for all Maven publications (including 'kotlinMultiplatform' and Android variant)
        withType<MavenPublication> {
            pom {
                name.set("Deepdots SDK")
                description.set("Deepdots Popup SDK - Kotlin Multiplatform library for Android and iOS")
                url.set("https://github.com/deepdots/DeepdotsPopupSDK")
                licenses {
                    license {
                        name.set("The Apache License, Version 2.0")
                        url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
                    }
                }
                scm {
                    connection.set("scm:git:https://github.com/deepdots/DeepdotsPopupSDK.git")
                    developerConnection.set("scm:git:ssh://git@github.com:deepdots/DeepdotsPopupSDK.git")
                    url.set("https://github.com/deepdots/DeepdotsPopupSDK")
                }
                developers {
                    developer {
                        id.set("deepdots")
                        name.set("Deepdots Team")
                        url.set("https://deepdots.com")
                    }
                }
            }
        }
    }
    repositories {
        maven {
            name = "OSSRH"
            url = uri("https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/")
            credentials {
                username = providers.gradleProperty("ossrhUsername").orNull ?: System.getenv("OSSRH_USERNAME")
                password = providers.gradleProperty("ossrhPassword").orNull ?: System.getenv("OSSRH_PASSWORD")
            }
        }
    }
}

signing {
    // Only require signing when publishing to OSSRH (Maven Central)
    setRequired({
        gradle.taskGraph.allTasks.any { it.name.contains("publish") && it.name.contains("ToOSSRH") }
    })
    useInMemoryPgpKeys(
        providers.gradleProperty("signing.keyId").orNull,
        providers.gradleProperty("signing.secretKeyRingFile").orNull?.let { file(it).readText() },
        providers.gradleProperty("signing.password").orNull
    )
    sign(publishing.publications)
}

// CocoaPods: generate a Podspec from the built XCFramework
val generatePodspec by tasks.registering {
    group = "distribution"
    description = "Generates DeepdotsSDK.podspec pointing to built iOS frameworks"
    doLast {
        val podspec = rootProject.layout.projectDirectory.file("DeepdotsSDK.podspec").asFile
        val version = project.version.toString()
        val spec = """
        Pod::Spec.new do |s|
          s.name         = 'DeepdotsSDK'
          s.version      = '${version}'
          s.summary      = 'Deepdots Popup SDK - iOS framework (Kotlin Multiplatform).'
          s.homepage     = 'https://github.com/MagicFeedback/deepdots-popup-mobile-native-sdk'
          s.license      = { :type => 'Apache 2.0', :file => 'LICENSE' }
          s.author       = { 'Deepdots' => 'sdk@deepdots.com' }
          s.source       = { :git => 'https://github.com/MagicFeedback/deepdots-popup-mobile-native-sdk.git', :tag => s.version }
          s.platform     = :ios, '13.0'
          s.swift_version = '5.7'
          # Build only iOS frameworks (XCFramework) to avoid requiring Android SDK
          s.prepare_command = './gradlew :shared:assembleSharedReleaseXCFramework'
          s.vendored_frameworks = 'shared/build/XCFrameworks/release/shared.xcframework'
        end
        """.trimIndent()
        podspec.writeText(spec)
        println("Written podspec to: ${podspec}")
    }
}

tasks.register("publishToMavenCentral") {
    group = "publishing"
    description = "Publishes all publications to OSSRH staging (Maven Central)."
    dependsOn("publishAllPublicationsToOSSRHRepository")
}
