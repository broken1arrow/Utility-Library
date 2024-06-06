import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.broken.arrow.library.PublicationManager
import org.broken.arrow.library.ShadeLogic

plugins {
    java
    alias(libs.plugins.shadow)

    id("java-library")
    id("org.broken.arrow.library.LoadDependency")
}

group = "org.broken.arrow.library.block.visualization"
description = "Block-Visualization"
version = "1.0-SNAPSHOT"


dependencies {
    api(project(":Log_and_Validate"))
    api(project(":Color_Conversion"))
    implementation(libs.tr7zw.item.nbt.api)
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

