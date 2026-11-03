// NORDLY test app (MF-3244). A standalone Gradle build, like a customer's app: it consumes
// the published SDK from Maven Central. `-PsdkVersion=local` swaps it for this repo's
// :shared module through a composite build.
rootProject.name = "NordlyTestApp"

pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
    }
}

include(":app")
// Minimal host: init + attachContext + setPath("/") and nothing else (a client app that only loads the SDK).
include(":basic")

val sdkVersion = providers.gradleProperty("sdkVersion").getOrElse("0.7.3")
if (sdkVersion == "local") {
    includeBuild("../..") {
        dependencySubstitution {
            substitute(module("com.deepdots.sdk:shared-android")).using(project(":shared"))
        }
    }
}
