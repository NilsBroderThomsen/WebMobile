plugins {
    kotlin("multiplatform")
}

kotlin {
    jvm()
    linuxX64()
    js(IR) { browser() }

    applyDefaultHierarchyTemplate()

    sourceSets {
        commonMain {
            dependencies {
                implementation(project(":common"))
            }
        }
        jvmMain {
            dependencies {
                // TODO: JVM HTTP Engine (z.B. CIO)
            }
        }
        jsMain {
            dependencies {
                // TODO: JS HTTP Engine
            }
        }
    }
}