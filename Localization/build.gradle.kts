
plugins {
    java
    id ("java-library")
    id("org.broken.arrow.library.LoadDependency")
}

dependencies {
    api(project(":Serialize-Utility"))
    api(project(":Yaml_Utility"))
    api(project(":Color_Conversion"))
    compileOnly(libs.org.spigotmc.spigotapi)
    compileOnly(libs.google.findbugs.jsr305)
}

description = "Localization"
version = "1.0-SNAPSHOT"

java {
    withJavadocJar()
}
