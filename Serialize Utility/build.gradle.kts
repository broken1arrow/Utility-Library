
plugins {
    java
    id ("java-library")
    id("org.broken.arrow.library.LoadDependency")
}

dependencies {
    api(project(":Log_and_Validate"))
    compileOnly(libs.org.spigotmc.spigotapi)
    compileOnly(libs.google.findbugs.jsr305)
}

description = "Serialize-Utility"
version = "1.0-SNAPSHOT"

java {
    withJavadocJar()
}
