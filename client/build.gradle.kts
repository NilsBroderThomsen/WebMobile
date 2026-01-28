plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.multiplatform)
}

kotlin {
    jvmToolchain(17)
    jvm()
    androidTarget()

    applyDefaultHierarchyTemplate()

    sourceSets {
        commonMain {
            dependencies {
                api(project(":common"))
                implementation(libs.ktor.client.core)
                implementation(libs.ktor.client.content.negotiation)
                implementation(libs.ktor.serialization.kotlinx.json)
                implementation(libs.logback.classic)
            }
        }
        jvmMain {
            dependencies {
                implementation(libs.ktor.client.cio)
            }
        }
        androidMain {
            dependencies {
                implementation(libs.ktor.client.okhttp)
            }
        }
    }
}

android {
    namespace = "de.hsflensburg.moodtracker.client"
    compileSdk = 34

    defaultConfig {
        minSdk = 24
    }
}
