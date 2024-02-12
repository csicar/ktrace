
repositories {
    mavenCentral()
}

plugins {
    kotlin("jvm") version "1.9.21"

    id("com.squareup.wire") version "4.9.6"
}

group = "de.csicar"
version = "1.0-SNAPSHOT"


dependencies {
    testImplementation("org.jetbrains.kotlin:kotlin-test")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.1")
    implementation("com.github.ajalt.mordant:mordant:2.2.0")
    api("com.squareup.wire:wire-runtime:4.9.6")
    api("com.squareup.wire:wire-grpc-client:4.9.6")
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(17)
}


wire {
    sourcePath {
        srcDir("opentelemetry-proto")
    }


    kotlin {
        rpcRole = "client"
        rpcCallStyle = "suspending"
    }
}

buildscript {
    dependencies {
        classpath("com.squareup.wire:wire-gradle-plugin")
    }
}
