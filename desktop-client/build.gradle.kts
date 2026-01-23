plugins {
    kotlin("jvm")
    id("org.jetbrains.compose") version "1.7.0"
}

kotlin {
    jvmToolchain(17)
}

repositories {
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
}

dependencies {
    implementation(project(":client"))
    implementation(project(":common"))
    implementation(compose.desktop.currentOs)
    implementation(compose.material3)
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-swing:1.10.2")
}

compose.desktop {
    application {
        mainClass = "MainKt"
    }
}
