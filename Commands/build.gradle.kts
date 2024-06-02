
plugins {
    java
    id("java-library")
    id("com.github.johnrengelman.shadow") version ("8.1.1")
    id("org.broken.arrow.library.LoadDependency")
}

dependencies {
    implementation(project(":Color_Conversion"))
    compileOnly(libs.org.spigotmc.spigotapi)
    compileOnly(libs.google.findbugs.jsr305)
}

description = "Commands"
version = "1.0-SNAPSHOT"

java {
    withJavadocJar()
}

tasks.shadowJar {
    exclude("D:/idea/Utility Library/Color Conversion/build/libs/Color_Conversion-1.0-SNAPSHOT_javadoc.jar")
    //mustRunAfter(":Color_Conversion:shadowJar")
}

