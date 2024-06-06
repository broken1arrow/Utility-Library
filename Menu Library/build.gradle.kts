import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.broken.arrow.library.PublicationManager

plugins {
    java
    alias(libs.plugins.shadow)

    id ("java-library")
    id("org.broken.arrow.library.LoadDependency")
}

group = "org.broken.arrow.library.menu.library"
description = "Menu_Library"
version = "1.0-SNAPSHOT"

dependencies {
    api(project(":Color_Conversion"))
    api(project(":Item_Creator"))
    api(project(":NBT"))
    api(project(":Title-Update"))
    api(project(":Log_and_Validate"))

    compileOnly(libs.org.spigotmc.spigotapi)
    compileOnly(libs.com.google.code.gson.gson)
    compileOnly(libs.google.findbugs.jsr305)
    compileOnly(libs.mojang.authlib)
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