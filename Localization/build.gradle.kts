import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.broken.arrow.library.PublicationManager

plugins {
    java
    alias(libs.plugins.shadow)
    id ("java-library")
    id("org.broken.arrow.library.LoadDependency")
}

group = "org.broken.arrow.library.localization"
description = "Localization"
version = "1.0-SNAPSHOT"

dependencies {
    api(project(":Serialize-Utility"))
    api(project(":Yaml-Utility"))
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