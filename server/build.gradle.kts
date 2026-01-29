plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
    application
}

group = "com.deepdots"
version = "0.1.3"

kotlin {
    jvmToolchain(17)
}

repositories { mavenCentral() }

dependencies {
    implementation(libs.ktor.server.core)
    implementation(libs.ktor.server.netty)
    implementation(libs.ktor.server.contentNegotiation)
    implementation(libs.ktor.serialization.kotlinx.json)
    implementation(libs.ktor.server.callLogging)
    implementation(libs.ktor.server.cors)
    implementation(libs.logback)
}

application {
    mainClass.set("com.deepdots.server.MainKt")
}

// Enable fat-jar packaging for quick run

tasks.register<Jar>("fatJar") {
    archiveClassifier.set("all")
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    from(sourceSets.main.get().output)
    dependsOn(configurations.runtimeClasspath)
    manifest { attributes["Main-Class"] = "com.deepdots.server.MainKt" }
    from({ configurations.runtimeClasspath.get().filter { it.name.endsWith(".jar") }.map { zipTree(it) } })
}
