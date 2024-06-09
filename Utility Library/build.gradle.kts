import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.broken.arrow.library.*

plugins {
    alias(libs.plugins.shadow)

    id("java")
    id("java-library")
    id("org.broken.arrow.library.LoadDependency")
}

group = "org.broken.arrow.library.utility"
description = "Utility-Library"
version = "1.0-SNAPSHOT"

dependencies {
    api(project(":menu-library"))
    api(project(":item-creator"))
    api(project(":database"))
    api(project(":commands"))
    api(project(":yaml-utility"))
    api(project(":block-visualization"))
    api(project(":title-update"))
    api(project(":conversation-prompt"))
    api(project(":localization"))
    api(project(":menu-configuration-manager"))
    api(project(":serialize-utility"))
    api(project(":nbt"))
    api(project(":log-and-validate"))

    compileOnly(libs.org.spigotmc.spigotapi)
    compileOnly(libs.google.findbugs.jsr305)
}

java {
    withJavadocJar()
}

tasks {

    shadowJar {
        mustRunAfter(":block-visualization:shadowJar")
        mustRunAfter(":nbt:shadowJar")
        val shadeLogic = ShadeLogic(project, this) {
            setArchiveFileName()
            dependencies {
                exclusions.forEach { exclude(it) }
            }
            relocate("de.tr7zw.changeme.nbtapi", formatDependency("nbt"))
        }
    }
    processResources {
        filteringCharset = Charsets.UTF_8.name() // We want UTF-8 for everything
        filesMatching("plugin.yml") {
            expand(
                mapOf(
                    "version" to project.version,
                    "main" to "${project.group}.UtilityLibrary",//"${project.group}.${project.name}",
                    "name" to project.name
                )
            )
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