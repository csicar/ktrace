import java.net.URI

repositories {
    mavenCentral()
}

plugins {
    kotlin("jvm") version "1.9.21"

    id("com.squareup.wire") version "4.9.6"

    `maven-publish`
    `java-library`
}

group = "de.csicar"
version = "1.0-SNAPSHOT"


dependencies {
    testImplementation("org.jetbrains.kotlin:kotlin-test")
    testImplementation("io.kotest:kotest-runner-junit5:5.5.5")
    testImplementation("io.mockk:mockk:1.13.9")
    testImplementation("io.strikt:strikt-core:0.34.1")
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
    sourceSets.main {
        kotlin.srcDir("build/generated/source/wire")
    }
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

publishing {
    publications {
        create<MavenPublication>("ktrace") {
            from(components["java"])
        }
    }
    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/csicar/ktrace")
            credentials {
                username = System.getenv("GITHUB_ACTOR")
                password = System.getenv("GITHUB_TOKEN")
            }
        }
    }
}
