plugins {
    id ("java-library")
    id("org.broken.arrow.library.LoadDependency")
}

group = "org.broken.arrow.library"
version = "1.0-SNAPSHOT"


dependencies {
    api(project(":log-and-validate"))

    compileOnly(libs.org.spigotmc.spigotapi)
    compileOnly(libs.google.findbugs.jsr305)
    compileOnly(libs.mojang.authlib)


}

