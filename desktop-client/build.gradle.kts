plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.compose.compiler)
}

kotlin {
    jvmToolchain(17)
}

dependencies {
    implementation(project(":client"))
    implementation(project(":common"))
    implementation(libs.kotlinx.coroutines.core)
    implementation(compose.desktop.currentOs)
    implementation(libs.compose.material3)
    implementation(libs.compose.navigation)
    implementation(libs.kotlinx.serialization.core)
}

compose.desktop {
    application {
        mainClass = "MainKt"
    }
}
