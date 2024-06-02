import org.broken.arrow.library.ShadeLogic

plugins {
    java
    id ("java-library")
    alias(libs.plugins.shadow)
    id("org.broken.arrow.library.LoadDependency")
}

group = "org.broken.arrow.library.nbt"
description = "NBT"
version = "1.0-SNAPSHOT"
val shadeLogic = ShadeLogic(project)


dependencies {
    api(project(":Log_and_Validate"))
    api(libs.tr7zw.item.nbt.api)
    compileOnly(libs.org.spigotmc.spigotapi)
    compileOnly(libs.google.findbugs.jsr305)
}


java {
    withJavadocJar()
}

tasks {
    shadowJar {
        shadeLogic.shadowProject{ fileName, destination, exclusions ->
            archiveFileName.set(fileName)
            relocate("de.tr7zw.changeme.nbtapi", "$destination.nbt")
            dependencies {
                exclusions.forEach { exclude(it) }
            }
        }
    }
}
