pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}
rootProject.name = "moodtracker"

include(":common")
include(":backend")
include(":client")
include(":desktop-client")
include(":android-client")
