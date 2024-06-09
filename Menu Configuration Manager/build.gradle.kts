import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.broken.arrow.library.PublicationManager

plugins {
    alias(libs.plugins.shadow)

    id ("java")
    id ("java-library")
    id("org.broken.arrow.library.LoadDependency")
}

group = "org.broken.arrow.library.menu.configuration.library"
description = "MenuConfiguration-Manager"
version = "1.0-SNAPSHOT"

dependencies {
    api(project(":yaml-utility"))
    api(project(":serialize-utility"))
    api(project(":log-and-validate"))
    compileOnly(libs.org.spigotmc.spigotapi)
    compileOnly(libs.google.findbugs.jsr305)
}

java {
    withJavadocJar()
}

tasks {

    PublicationManager(project) {
        val shadowJar by getting(ShadowJar::class) {
            archiveClassifier.set("${description}_all")
            mergeServiceFiles()
        }
        artifact(shadowJar) {
            classifier = "all"
        }
    }
}