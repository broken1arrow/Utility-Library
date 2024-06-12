import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.broken.arrow.library.PublicationManager
import org.broken.arrow.library.ShadeLogic

plugins {
    alias(libs.plugins.shadow)

    id("java")
    id("java-library")
    id("org.broken.arrow.library.LoadDependency")
}

group = "org.broken.arrow.library"
description = "Block-Visualization"
version = "1.0-SNAPSHOT"


dependencies {
    api(project(":log-and-validate"))
    api(project(":color-conversion"))
    implementation(libs.tr7zw.item.nbt.api)
    compileOnly(libs.org.spigotmc.spigotapi)
    compileOnly(libs.google.findbugs.jsr305)

}

java {
    withJavadocJar()
}

tasks {

   shadowJar {
        ShadeLogic(project, this) {
            setArchiveFileName()
            dependencies {
                exclusions.forEach { exclude(it) }
            }
            relocate("de.tr7zw.changeme.nbtapi", formatDependency("nbt"))
        }
    }

/*    PublicationManager(project) {
        val shadowJar by getting(ShadowJar::class) {
            archiveClassifier.set("${description}_all")
            mergeServiceFiles()
        }
        artifact(shadowJar) {
            classifier = "all"
        }
    }*/
}

