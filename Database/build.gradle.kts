import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.broken.arrow.library.PublicationManager

plugins {
    java
    alias(libs.plugins.shadow)

    id ("java-library")
    id("org.broken.arrow.library.LoadDependency")
}

group = "org.broken.arrow.library.database"
description = "Database"
version = "1.0-SNAPSHOT"

dependencies {
    api(project(":Log-and-Validate"))
    api(project(":Serialize-Utility"))
    compileOnly(libs.org.xerial.sqlite.jdbc)
    compileOnly(libs.com.zaxxer.hikaricp)
    compileOnly(libs.mysql.connector.j)
    compileOnly(libs.org.mongodb.mongodb.driver.sync)
    compileOnly(libs.google.findbugs.jsr305)
    compileOnly(libs.org.apache.logging.log4j.log4j.api)
    compileOnly(libs.apache.logging.log4j.core)
}

java {
    withJavadocJar()
}

tasks {

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
}