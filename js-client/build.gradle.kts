plugins {
    kotlin("multiplatform")
}
kotlin {
    js(IR) {
        browser {
//            cssSupport {
//                enabled = true
//            }
        }
        binaries.executable()
    }
    sourceSets {
        jsMain {
            dependencies {
                implementation(project(":client"))
                implementation(project(":common"))
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")
                implementation("org.jetbrains.kotlinx:kotlinx-html-js:0.11.0")
            }
        }
    }
}
