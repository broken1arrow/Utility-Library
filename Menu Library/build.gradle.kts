
plugins {
    java
    id ("java-library")
    id("org.broken.arrow.library.LoadDependency")
}

dependencies {
    api(project(":Color_Conversion"))
    api(project(":Item_Creator"))
    api(project(":NBT"))
    api(project(":Title-Update"))
    api(project(":Log_and_Validate"))

    compileOnly(libs.org.spigotmc.spigotapi)
    compileOnly(libs.com.google.code.gson.gson)
    compileOnly(libs.google.findbugs.jsr305)
    compileOnly(libs.mojang.authlib)
}

description = "Menu_Library"
version = "1.0-SNAPSHOT"

java {
    withJavadocJar()
}
