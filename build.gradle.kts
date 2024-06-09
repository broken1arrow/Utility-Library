import org.apache.tools.ant.taskdefs.Java

plugins {
    `java-library`
    java
    `maven-publish`
    `kotlin-dsl`
}

group = "org.broken.arrow.library"
version = "1.0-SNAPSHOT"
apply(plugin = "java")
apply(plugin = "maven-publish")

repositories {
    mavenCentral()
    mavenLocal()
    gradlePluginPortal()
}

subprojects {
    apply(plugin = "maven-publish")
    apply(plugin = "java")
    tasks {
        javadoc {
            options.encoding = Charsets.UTF_8.name()
        }
        compileJava {
            options.encoding = Charsets.UTF_8.name()

            // Set the release flag. This configures what version bytecode the compiler will emit, as well as what JDK APIs are usable.
            // See https://openjdk.java.net/jeps/247 for more information.
            //options.release.set(8)

            java.sourceCompatibility = JavaVersion.VERSION_1_8
            java.targetCompatibility = JavaVersion.VERSION_1_8
        }
    }

}

fun setProjectVersion(project: Project) {
    if (project.version == "" || project.version == "1.0-SNAPSHOT") project.version = version
}

tasks {
    javadoc {
        options.encoding = Charsets.UTF_8.name() // We want UTF-8 for everything
    }
    compileJava {
        options.encoding = Charsets.UTF_8.name()

        // Set the release flag. This configures what version bytecode the compiler will emit, as well as what JDK APIs are usable.
        // See https://openjdk.java.net/jeps/247 for more information.
        options.release.set(8)

        java.sourceCompatibility = JavaVersion.VERSION_1_8
        java.targetCompatibility = JavaVersion.VERSION_1_8
    }
}

