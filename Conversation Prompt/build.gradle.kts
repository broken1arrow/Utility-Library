import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.broken.arrow.library.PublicationManager

plugins {

    alias(libs.plugins.shadow)
    id ("java")
    id ("java-library")
    id("org.broken.arrow.library.LoadDependency")
}

group = "org.broken.arrow.library.conversation.prompt"
description = "Conversation-Prompt"
version = "1.0-SNAPSHOT"

dependencies {
    api(project(":Log-and-Validate"))
    compileOnly(libs.org.spigotmc.spigot.api)
    compileOnly(libs.com.google.code.findbugs.jsr305)
}

java {
    withJavadocJar()
}

tasks {

    tasks {
        PublicationManager(project) {
            val shadowJar by getting(ShadowJar::class) {
                archiveClassifier.set("${description}_all")
                mergeServiceFiles()
            }
            artifact(shadowJar) {
                classifier = "all_t"
            }
        }
    }
}