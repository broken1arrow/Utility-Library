
plugins {
    java
    id ("java-library")
    id("org.broken.arrow.library.LoadDependency")
}

dependencies {
    implementation(project(":Log_and_Validate"))
    compileOnly(libs.com.google.code.gson.gson)
    compileOnly(libs.org.spigotmc.spigot.api)
    compileOnly(libs.com.google.code.findbugs.jsr305)
}

description = "Color_Conversion"
version = "1.0-SNAPSHOT"

java {
    withJavadocJar()
}
