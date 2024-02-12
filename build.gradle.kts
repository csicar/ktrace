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
        javaInterop = true
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
