plugins {
    alias(libs.plugins.android.library)
    kotlin("multiplatform")
    kotlin("plugin.serialization")
}

kotlin {
    jvmToolchain(17)
    jvm()
    androidTarget()

    applyDefaultHierarchyTemplate()

    sourceSets {
        commonMain {
            dependencies {
                implementation(libs.kotlinx.datetime)
                implementation(libs.kotlinx.serialization.json)
            }
        }
    }

    compilerOptions {
        freeCompilerArgs.add("-opt-in=kotlin.time.ExperimentalTime")
    }
}

android {
    namespace = "de.hsflensburg.moodtracker.common"
    compileSdk = 34

    defaultConfig {
        minSdk = 24
    }
}
