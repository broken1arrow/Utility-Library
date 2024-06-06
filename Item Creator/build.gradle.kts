import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.broken.arrow.library.PublicationManager

plugins {
    java
    alias(libs.plugins.shadow)

    id ("java-library")
    id("org.broken.arrow.library.LoadDependency")
}

group = "org.broken.arrow.library.item.creator"
description = "Item_Creator"
version = "1.0-SNAPSHOT"

dependencies {
    api(project(":Color_Conversion"))
    api(project(":NBT"))
    api(project(":Log_and_Validate"))
    compileOnly(libs.org.spigotmc.spigotapi)
    compileOnly(libs.google.findbugs.jsr305)
    compileOnly(libs.mojang.authlib)
}

java {
    withJavadocJar()
}

tasks {

    tasks{
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
}