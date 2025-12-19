plugins {
    alias(libs.plugins.shadow)

    id("java")
    id("java-library")
    id("org.broken.arrow.library.LoadDependency")
}

group = "org.broken.arrow.library"
description = "Item_Creator"
version = "1.0-SNAPSHOT"

dependencies {
    api(project(":color-conversion"))
    api(project(":nbt"))
    api(project(":log-and-validate"))
    compileOnly(libs.org.spigotmc.spigotapi)
    compileOnly(libs.google.findbugs.jsr305)
    compileOnly(libs.mojang.authlib)
}

java {
    withJavadocJar()
}


tasks {
/*    PublicationManager(project) {
        val shadowJar by getting(ShadowJar::class) {
            archiveClassifier.set("${description}_all")
            mergeServiceFiles()
        }
        artifact(shadowJar) {
            classifier = "all"
        }
    }*/
/*    shadowJar {
        duplicatesStrategy = DuplicatesStrategy.INHERIT
        ShadeLogic(project, this) {
            setArchiveFileName()
            dependencies {
                exclusions.forEach { exclude(it) }
            }
            relocate("de.tr7zw.changeme", "org.broken.arrow.library.nbt.dependents.nbt")
        }
    }*/
}
