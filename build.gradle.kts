plugins {
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.kotlin.multiplatform) apply false
    alias(libs.plugins.compose.multiplatform) apply false
    alias(libs.plugins.compose.compiler) apply false
}

allprojects {
    group = "de.hs-flensburg.moodtracker"
    version = "1.0-SNAPSHOT"
    repositories {
        google()
        mavenCentral()

    }
}
