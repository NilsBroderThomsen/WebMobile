plugins {
    kotlin("jvm")
    application
}
dependencies {
    implementation(project(":client"))
    // TODO: Fügen Sie Coroutines hinzu
}
application {
    mainClass.set("MainKt")
}