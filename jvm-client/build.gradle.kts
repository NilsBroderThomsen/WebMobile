plugins {
    kotlin("jvm")
    application
}
dependencies {
    implementation(project(":client"))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")
}
application {
    mainClass.set("MainKt")
}