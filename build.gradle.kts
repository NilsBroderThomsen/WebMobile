plugins {
    kotlin("jvm") version "2.2.0"
    kotlin("plugin.serialization") version "2.2.20"
    application
}

group = "org.example"
version = "1.0-SNAPSHOT"
val ktorVersion = "3.2.2"

repositories {
    mavenCentral()
}

dependencies {
    // Exposed ORM
    implementation("org.jetbrains.exposed:exposed-core:0.61.0")
    implementation("org.jetbrains.exposed:exposed-dao:0.61.0")
    implementation("org.jetbrains.exposed:exposed-jdbc:0.61.0")
    implementation("org.jetbrains.exposed:exposed-kotlin-datetime:0.61.0")

    // SQLite JDBC Treiber
    implementation("org.xerial:sqlite-jdbc:3.47.1.0")

    // Ktor Server
    implementation("io.ktor:ktor-server-core:$ktorVersion")
    implementation("io.ktor:ktor-server-netty:$ktorVersion")
    implementation("io.ktor:ktor-server-html-builder:$ktorVersion")
    implementation("io.ktor:ktor-server-call-logging:$ktorVersion")

    // Content Negotiation & Serialization (NEU!)
    implementation("io.ktor:ktor-server-content-negotiation:$ktorVersion")
    implementation("io.ktor:ktor-serialization-kotlinx-json:$ktorVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.9.0")

    // CSV (NEU!)
    implementation("com.github.doyaaaaaken:kotlin-csv-jvm:1.10.0")

    // Aus vorherigen Wochen
    implementation("ch.qos.logback:logback-classic:1.5.11")
    implementation("org.jetbrains.kotlinx:kotlinx-html-jvm:0.11.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")
    implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.7.1")
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(21)
}