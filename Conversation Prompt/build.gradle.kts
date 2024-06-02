
plugins {
    java
    id ("java-library")
    id("org.broken.arrow.library.LoadDependency")
}

dependencies {
    api(project(":Log_and_Validate"))
    compileOnly(libs.org.spigotmc.spigot.api)
    compileOnly(libs.com.google.code.findbugs.jsr305)
}

description = "Conversation-Prompt"

java {
    withJavadocJar()
}
