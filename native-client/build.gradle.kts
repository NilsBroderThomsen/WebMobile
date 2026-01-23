plugins {
    kotlin("multiplatform")
}
kotlin {
    jvmToolchain(17)
    linuxX64 {
        binaries {
            executable {
                entryPoint = "main"
            }
        }
    }
    sourceSets {
        linuxX64Main {
            dependencies {
                implementation(project(":client"))
                implementation(project(":common"))
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")
            }
        }
    }
}