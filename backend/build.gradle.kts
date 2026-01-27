plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
    application
}

val ktorVersion = "3.2.2"
val exposedVersion = "0.61.0"
val kodeinVersion = "7.23.0"

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":common"))

    // Kotlin
    implementation(kotlin("stdlib"))
    implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.7.1")
    implementation(libs.kotlinx.datetime)

    // Ktor Server
    implementation("io.ktor:ktor-server-core-jvm:$ktorVersion")
    implementation("io.ktor:ktor-server-netty-jvm:$ktorVersion")
    implementation("io.ktor:ktor-server-content-negotiation-jvm:$ktorVersion")
    implementation("io.ktor:ktor-server-cors-jvm:$ktorVersion")
    implementation("io.ktor:ktor-server-status-pages-jvm:$ktorVersion")
    implementation("io.ktor:ktor-server-call-logging:$ktorVersion")
    implementation("io.ktor:ktor-server-html-builder:$ktorVersion")

    // Ktor Client
    implementation("io.ktor:ktor-client-core:$ktorVersion")
    implementation("io.ktor:ktor-client-cio:$ktorVersion")
    implementation("io.ktor:ktor-client-content-negotiation:$ktorVersion")
    implementation("io.ktor:ktor-client-logging:$ktorVersion")
    implementation(libs.ktor.server.core.jvm)
    implementation(libs.ktor.server.netty.jvm)
    implementation(libs.ktor.server.content.negotiation.jvm)
    implementation(libs.ktor.server.cors.jvm)
    implementation(libs.ktor.server.call.logging)
    implementation(libs.ktor.server.auth.jwt)

    // Exposed ORM
    implementation("org.jetbrains.exposed:exposed-core:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-dao:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-jdbc:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-java-time:$exposedVersion")
    implementation(libs.exposed.core)
    implementation(libs.exposed.dao)
    implementation(libs.exposed.jdbc)
    implementation(libs.exposed.java.time)

    // Database Driver
    implementation("org.xerial:sqlite-jdbc:3.47.1.0")
    implementation(libs.sqlite.jdbc)

    // Kodein DI
    implementation("org.kodein.di:kodein-di:$kodeinVersion")
    implementation("org.kodein.di:kodein-di-framework-ktor-server-jvm:$kodeinVersion")
    implementation(libs.kodein.di)
    implementation(libs.kodein.di.framework.ktor.server.jvm)

    // Serialization
    implementation("io.ktor:ktor-serialization-kotlinx-json:$ktorVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.9.0")
    implementation("com.github.doyaaaaaken:kotlin-csv-jvm:1.10.0")
    implementation(libs.ktor.serialization.kotlinx.json)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kotlin.csv.jvm)


    // Logging
    implementation("ch.qos.logback:logback-classic:1.5.11")
    implementation(libs.logback.classic)

    // Security
    implementation("org.mindrot:jbcrypt:0.4")
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
