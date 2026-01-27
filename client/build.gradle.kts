plugins {
    alias(libs.plugins.kotlin.multiplatform)
}

val ktorVersion = "3.2.2"

kotlin {
    jvmToolchain(17)
    jvm()

    applyDefaultHierarchyTemplate()

    sourceSets {
        commonMain {
            dependencies {
                implementation(project(":common"))
                implementation("io.ktor:ktor-client-core:$ktorVersion")
                implementation("io.ktor:ktor-client-content-negotiation:$ktorVersion")
                implementation("io.ktor:ktor-serialization-kotlinx-json:$ktorVersion")
                implementation("ch.qos.logback:logback-classic:1.5.11")
                implementation(libs.ktor.client.core)
                implementation(libs.ktor.client.content.negotiation)
                implementation(libs.ktor.serialization.kotlinx.json)
                implementation(libs.logback.classic)
            }
        }
        jvmMain {
            dependencies {
                implementation("io.ktor:ktor-client-cio:$ktorVersion")
                implementation(libs.ktor.client.cio)
            }
        }
    }
}