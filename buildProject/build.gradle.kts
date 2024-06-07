
plugins {
    `kotlin-dsl`
    java
    `java-gradle-plugin`
    `java-library`
    `maven-publish`
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

group = "org.broken.arrow"
version = "1.0-SNAPSHOT"

gradlePlugin {
    plugins.register("LoadDependency") {
        id = "org.broken.arrow.library.LoadDependency"
        implementationClass = "org.broken.arrow.library.LoadDependency"
    }
}


apply(plugin = "com.github.johnrengelman.shadow")
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
        sourceCompatibility = "8"//JavaVersion.VERSION_1_8.toString()
        targetCompatibility = "8"//JavaVersion.VERSION_1_8.toString()
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


/*tasks.test {
    useJUnitPlatform()
}*/
kotlin {
    jvmToolchain(8)
}