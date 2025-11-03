plugins {
    kotlin("jvm") version "2.2.0"
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    // Ktor Server
    implementation("io.ktor:ktor-server-core:3.2.2")
    implementation("io.ktor:ktor-server-netty:3.2.2")
    implementation("io.ktor:ktor-server-html-builder:3.2.2")
    implementation("io.ktor:ktor-server-call-logging:3.2.2")
    // Logging
    implementation("ch.qos.logback:logback-classic:1.5.11")
    // HTML DSL
    implementation("org.jetbrains.kotlinx:kotlinx-html-jvm:0.11.0")
    // Aus vorherigen Wochen
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")
    implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.7.1")
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(21)
}