
plugins {
    java
    id("java-library")
    id("org.broken.arrow.library.LoadDependency")
}

dependencies {
    api(project(":Log_and_Validate"))
    api(project(":Color_Conversion"))
    compileOnly(libs.org.spigotmc.spigotapi)
    compileOnly(libs.google.findbugs.jsr305)
}

description = "Title-Update"
version = "1.0-SNAPSHOT"

java {
    withJavadocJar()
}
