
plugins {
    java
    id ("java-library")
    id("org.broken.arrow.library.LoadDependency")
}

dependencies {
    api(project(":Yaml_Utility"))
    api(project(":Serialize-Utility"))
    api(project(":Log_and_Validate"))
    compileOnly(libs.org.spigotmc.spigotapi)
    compileOnly(libs.google.findbugs.jsr305)
}

description = "MenuConfiguration-Manager"
version = "1.0-SNAPSHOT"

java {
    withJavadocJar()
}
