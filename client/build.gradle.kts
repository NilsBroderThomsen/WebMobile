plugins {
    kotlin("multiplatform")
}

val ktorVersion = "3.2.2"

kotlin {
    jvm()
    linuxX64()
    js(IR) { browser() }

    applyDefaultHierarchyTemplate()

    sourceSets {
        commonMain {
            dependencies {
                implementation(project(":common"))
                implementation("io.ktor:ktor-client-core:$ktorVersion")
                implementation("io.ktor:ktor-client-content-negotiation:$ktorVersion")
                implementation("io.ktor:ktor-serialization-kotlinx-json:$ktorVersion")
                implementation("ch.qos.logback:logback-classic:1.5.11")
            }
        }
        jvmMain {
            dependencies {
                implementation("io.ktor:ktor-client-cio:$ktorVersion")
            }
        }
        jsMain {
            dependencies {
                implementation("io.ktor:ktor-client-js:$ktorVersion")
            }
        }
    }
}