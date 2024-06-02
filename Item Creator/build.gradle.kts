
plugins {
    java
    id ("java-library")
    id("org.broken.arrow.library.LoadDependency")
}

dependencies {
    api(project(":Color_Conversion"))
    api(project(":NBT"))
    api(project(":Log_and_Validate"))
    compileOnly(libs.org.spigotmc.spigotapi)
    compileOnly(libs.google.findbugs.jsr305)
    compileOnly(libs.mojang.authlib)
}

description = "Item_Creator"
version = "1.0-SNAPSHOT"

java {
    withJavadocJar()
}
