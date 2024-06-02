import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.broken.arrow.library.*

plugins {
    java
    `maven-publish`
    id("java-library")

    alias(libs.plugins.shadow)
    id("org.broken.arrow.library.LoadDependency")
}

dependencies {
    api(project(":Menu_Library"))
    api(project(":Item_Creator"))
    api(project(":Database"))
    api(project(":Commands"))
    api(project(":Yaml_Utility"))
    api(project(":Block-Visualization"))
    api(project(":Title-Update"))
    api(project(":Conversation-Prompt"))
    api(project(":Localization"))
    api(project(":MenuConfiguration-Manager"))
    api(project(":Serialize-Utility"))
    api(project(":NBT"))

    compileOnly(libs.org.spigotmc.spigotapi)
    compileOnly(libs.google.findbugs.jsr305)
}
group = "org.broken.arrow.library.utility"
description = "Utility-Library"
version = "1.0-SNAPSHOT"

java {
    withJavadocJar()
}

tasks.withType<PublishToMavenLocal> {
    dependsOn(tasks.shadowJar)
}

val shadeLogic = ShadeLogic(project)

tasks {

    shadowJar {
        shadeLogic.shadowProject { fileName, destination, exclusions ->
            archiveFileName.set(fileName)
            relocate("de.tr7zw.changeme.nbtapi", "$destination.nbt")
            dependencies { exclusions.forEach { exclude(it) } }
        }
    }

    val shadowJar by getting(ShadowJar::class) {
        archiveClassifier.set("all")
        mergeServiceFiles()
    }

    publishing {

        publications {
            create<MavenPublication>("mavenJava") {
                artifact(shadowJar) {
                    classifier = "all"
                }
                groupId = "org.broken.arrow.library"
                artifactId = project.name
                version = project.version.toString()
            }
        }

        repositories {
            mavenLocal()
        }
    }
}