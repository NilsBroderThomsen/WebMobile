plugins {
    kotlin("jvm") version "2.2.20" apply false
    kotlin("plugin.serialization") version "2.2.20" apply false
    kotlin("plugin.compose") version "2.2.20" apply false
}

allprojects {
    group = "de.hs-flensburg.moodtracker"
    version = "1.0-SNAPSHOT"
    repositories {
        google()
        mavenCentral()
    }
}
