import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.plugin.mpp.apple.XCFramework
import org.gradle.api.publish.maven.MavenPublication

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
    // removed iosX64 due to missing actuals; rely on arm64 simulator

    // Apply ObjC generics and minimum iOS deployment target to all iOS native binaries
    targets.withType<org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget>().configureEach {
        if (name.startsWith("ios")) {
            binaries.all {
                freeCompilerArgs += listOf("-Xobjc-generics")
                linkerOpts("-mios-version-min=13.0")
            }
            binaries.framework {
                baseName = "ComposeApp"
                isStatic = true
                xcf.add(this)
            }
        }
    }

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
    // (Removed duplicate configuration to avoid creating the same framework twice)
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

publishing {
    publications {
        withType<MavenPublication>().configureEach {
            groupId = project.group.toString()
            artifactId = "shared-${name.replace("Publication", "").lowercase()}"
            version = project.version.toString()

            pom {
                name.set("Deepdots SDK")
                description.set("Deepdots Popup SDK - Kotlin Multiplatform library for Android and iOS")
                url.set("https://github.com/MagicFeedback/deepdots-popup-mobile-native-sdk")
                licenses {
                    license {
                        name.set("The Apache License, Version 2.0")
                        url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
                    }
                }
                scm {
                    connection.set("scm:git:https://github.com/MagicFeedback/deepdots-popup-mobile-native-sdk.git")
                    developerConnection.set("scm:git:ssh://git@github.com:MagicFeedback/deepdots-popup-mobile-native-sdk.git")
                    url.set("https://github.com/MagicFeedback/deepdots-popup-mobile-native-sdk")
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
            url = uri("https://ossrh-staging-api.central.sonatype.com/service/local/staging/deploy/maven2/")

            credentials {
                username = providers.gradleProperty("ossrhUsername").get()
                password = providers.gradleProperty("ossrhPassword").get()
            }
        }
    }
}

// Signing con GPG (macOS friendly, funciona en CI)
signing {
    val keyPath = providers.gradleProperty("signing.secretKeyRingFile").orNull
    val keyPassword = providers.gradleProperty("signing.password").orNull
    if (keyPath != null && keyPassword != null) {
        useInMemoryPgpKeys(file(keyPath).readText(), keyPassword)
        sign(publishing.publications)
    } else {
        logger.lifecycle("PGP signing not configured (set signing.secretKeyRingFile and signing.password in ~/.gradle/gradle.properties). Skipping signing until publish.")
    }
}

// Removed CocoaPods podspec generation: using SPM binary distribution for iOS

tasks.register("publishToMavenCentral") {
    group = "publishing"
    description = "Publishes all publications to OSSRH staging (Maven Central)."
    dependsOn("publishAllPublicationsToOSSRHRepository")
}
