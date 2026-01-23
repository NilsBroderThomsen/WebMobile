plugins {
    kotlin("multiplatform")
}
kotlin {
    jvmToolchain(17)
    js(IR) {
        browser {
//            cssSupport {
//                cssSupport.enabled = true
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

tasks.register("jsBrowserRun") {
    group = "application"
    description = "Alias for jsBrowserDevelopmentRun."
    dependsOn("jsBrowserDevelopmentRun")
}