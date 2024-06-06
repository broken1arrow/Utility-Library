import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.broken.arrow.library.PublicationManager

plugins {
    java
    id("java-library")
    alias(libs.plugins.shadow)
    id("org.broken.arrow.library.LoadDependency")
}

group = "org.broken.arrow.library.title.update"
description = "Title-Update"
version = "1.0-SNAPSHOT"

dependencies {
    api(project(":Log-and-Validate"))
    api(project(":Color-Conversion"))
    compileOnly(libs.org.spigotmc.spigotapi)
    compileOnly(libs.google.findbugs.jsr305)
}

java {
    withJavadocJar()
}

tasks {

    PublicationManager(project) {
        val shadowJar by getting(ShadowJar::class) {
            archiveClassifier.set("all")
            mergeServiceFiles()
        }
        artifact(shadowJar) {
            classifier = "all"
        }
    }
}