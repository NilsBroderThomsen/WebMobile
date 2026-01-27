plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
    application
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":common"))

    // Kotlin
    implementation(kotlin("stdlib"))
    implementation(libs.kotlinx.datetime)

    // Ktor Server
    implementation(libs.ktor.server.core.jvm)
    implementation(libs.ktor.server.netty.jvm)
    implementation(libs.ktor.server.content.negotiation.jvm)
    implementation(libs.ktor.server.cors.jvm)
    implementation(libs.ktor.server.call.logging)
    implementation(libs.ktor.server.auth.jwt)

    // Exposed ORM
    implementation(libs.exposed.core)
    implementation(libs.exposed.dao)
    implementation(libs.exposed.jdbc)
    implementation(libs.exposed.java.time)

    // Database Driver
    implementation(libs.sqlite.jdbc)

    // Kodein DI
    implementation(libs.kodein.di)
    implementation(libs.kodein.di.framework.ktor.server.jvm)

    // Serialization
    implementation(libs.ktor.serialization.kotlinx.json)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kotlin.csv.jvm)

    // Logging
    implementation(libs.logback.classic)

    // Security
    implementation(libs.jbcrypt)
}

kotlin {
    jvmToolchain(17)

    compilerOptions {
        freeCompilerArgs.add("-opt-in=kotlin.time.ExperimentalTime")
    }
}

tasks.test {
    useJUnitPlatform()
}

application {
    mainClass.set("ApplicationKt")
}
