import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.broken.arrow.library.PublicationManager
import org.broken.arrow.library.ShadeLogic

plugins {
    java
    `maven-publish`
    alias(libs.plugins.shadow)

    id("java-library")
    id("org.broken.arrow.library.LoadDependency")
}

group = "org.broken.arrow.library.nbt"
description = "NBT"
version = "1.0-SNAPSHOT"


dependencies {
    api(project(":Log-and-Validate"))
    api(libs.tr7zw.item.nbt.api)
    compileOnly(libs.org.spigotmc.spigotapi)
    compileOnly(libs.google.findbugs.jsr305)
}


java {
    withJavadocJar()
}

tasks {

    shadowJar {
        val shadeLogic = ShadeLogic(project, this)

        shadeLogic.shadowProject {
            setArchiveFileName()
            dependencies {
                exclusions.forEach { exclude(it) }
            }
            relocate("de.tr7zw.changeme.nbtapi", formatDependency("nbt"))
        }
    }

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

