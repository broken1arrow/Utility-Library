import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    `kotlin-dsl`

    id("java-gradle-plugin")
    id("java-library")
   // id("java")
    id("maven-publish")
   // id("com.gradleup.shadow") version "8.3.5"
}

group = "org.broken.arrow"
version = "1.0-SNAPSHOT"

gradlePlugin {
    plugins.register("LoadDependency") {
        id = "org.broken.arrow.library.LoadDependency"
        implementationClass = "org.broken.arrow.library.LoadDependency"
    }
}


apply(plugin = "maven-publish")

buildscript {
    repositories {
        mavenCentral()
        mavenLocal()
        maven {
            url = uri("https://plugins.gradle.org/m2/")
        }
    }
    dependencies {
        classpath("com.github.johnrengelman:shadow:8.1.1")
    }
}

tasks {
    compileJava {
        options.release.set(8)
        //sourceCompatibility = JavaVersion.VERSION_1_8.toString()
        //targetCompatibility = JavaVersion.VERSION_1_8.toString()
    }
}

repositories {
    mavenCentral()
    mavenLocal()
}

dependencies {
    testImplementation("org.jetbrains.kotlin:kotlin-test")
   //implementation("com.github.jengelman.gradle.plugins:shadow:8.1.1")
   // implementation("com.github.jengelman.gradle.plugins:shadow:6.1.0")
     //implementation("com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar:8.1.1")
}

tasks.compileKotlin {
    compilerOptions {
        compilerOptions.jvmTarget.set(JvmTarget.JVM_1_8)
    }
   // kotlinOptions.jvmTarget = "1.8"
}

kotlin {
    jvmToolchain(21)
}
