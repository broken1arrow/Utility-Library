
plugins {
    java
    id ("java-library")
    id("org.broken.arrow.library.LoadDependency")
}


dependencies {
    api(project(":Serialize-Utility"))
    compileOnly(libs.org.spigotmc.spigotapi)
    compileOnly(libs.google.findbugs.jsr305)
}

description = "Yaml_Utility"
version = "1.0-SNAPSHOT"

java {
    withJavadocJar()
}
