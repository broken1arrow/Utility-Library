import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.broken.arrow.library.*

plugins {
    java
    id("java-library")
    alias(libs.plugins.shadow)
    id("org.broken.arrow.library.LoadDependency")
}

group = "org.broken.arrow.library.utility"
description = "Utility-Library"
version = "1.0-SNAPSHOT"

dependencies {
    api(project(":Menu-Library"))
    api(project(":Item-Creator"))
    api(project(":Database"))
    api(project(":Commands"))
    api(project(":Yaml-Utility"))
    api(project(":Block-Visualization"))
    api(project(":Title-Update"))
    api(project(":Conversation-Prompt"))
    api(project(":Localization"))
    api(project(":MenuConfiguration-Manager"))
    api(project(":Serialize-Utility"))
    api(project(":NBT"))
    api(project(":Log-and-Validate"))

    compileOnly(libs.org.spigotmc.spigotapi)
    compileOnly(libs.google.findbugs.jsr305)
}

java {
    withJavadocJar()
}

tasks {

    shadowJar {
        mustRunAfter(":Block-Visualization:shadowJar")
        mustRunAfter(":NBT:shadowJar")
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
            archiveClassifier.set("${description}_all")
            mergeServiceFiles()
        }
        artifact(shadowJar) {
            classifier = "all"
        }
    }
}