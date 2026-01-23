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
            }
        }
    }
}