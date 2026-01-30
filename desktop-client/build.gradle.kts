plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.compose.compiler)
}

kotlin {
    jvmToolchain(17)

    compilerOptions {
        freeCompilerArgs.add("-opt-in=kotlin.time.ExperimentalTime")
    }
}

dependencies {
    implementation(project(":client"))
    implementation(project(":common"))
    implementation(libs.kotlinx.coroutines.core)
    implementation(compose.desktop.currentOs)
    implementation(libs.compose.material3)
    implementation(libs.kotlinx.serialization.core)
    implementation(libs.kotlinx.datetime)
}

compose.desktop {
    application {
        mainClass = "MainKt"
    }
}
