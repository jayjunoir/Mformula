rootProject.name = "Mformula"

pluginManagement {
    repositories {
        maven { url = java.net.URI("https://dl.google.com/dl/android/maven2/") }
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.10.0"
}

dependencyResolutionManagement {
    repositories {
        maven { url = java.net.URI("https://dl.google.com/dl/android/maven2/") }
        google()
        mavenCentral()
    }
}

include(":androidApp")
include(":shared")
