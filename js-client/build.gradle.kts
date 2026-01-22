plugins {
    kotlin("multiplatform")
}
kotlin {
    js(IR) {
        browser {
            commonWebpackConfig {
                cssSupport.enabled = true
            }
        }
        binaries.executable()
    }
    sourceSets {
        jsMain {
            dependencies {
                implementation(project(":client"))
                implementation("org.jetbrains.kotlinx:kotlinx-html
                        js:0.11.0")
            }
        }
    }
}