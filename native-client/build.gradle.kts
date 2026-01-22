plugins {
    kotlin("multiplatform")
}
kotlin {
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
            }
        }
    }
}