package org.broken.arrow.library

import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.jvm.tasks.Jar
import org.gradle.kotlin.dsl.*
import java.net.URI


class LoadDependency : Plugin<Project> {

    override fun apply(project: Project) {
        project.plugins.apply(JavaPlugin::class.java)
        configureRepositories(project)
        //configureDependencies(project)
        //configureTasks(project)
    }

    private fun configureRepositories(project: Project) {
        project.repositories.apply {
            mavenCentral()
            mavenLocal()
            gradlePluginPortal()
            maven { url = URI("https://jitpack.io") }
            maven { url = URI("https://repo.codemc.io/repository/maven-public/") }
            maven { url = URI("https://hub.spigotmc.org/nexus/content/repositories/snapshots/") }
            maven { url = URI("https://oss.sonatype.org/content/groups/public/") }
            maven { url = URI("https://oss.sonatype.org/content/repositories/snapshots/") }
            maven { url = URI("https://repo.maven.apache.org/maven2/") }
            maven { url = URI("https://libraries.minecraft.net/") }
        }
    }

    private fun configureDependencies(project: Project) {
        project.dependencies {
            "compileOnly"("de.tr7zw:item-nbt-api:2.12.4")
            "compileOnly"("com.google.code.findbugs:jsr305:3.0.2")
            "compileOnly"("org.spigotmc:spigot-api:1.20.2-R0.1-SNAPSHOT")
        }
    }

    private fun configureTasks(project: Project) {
        project.tasks.apply {
            withType<org.gradle.api.tasks.javadoc.Javadoc> {
                options.encoding = Charsets.UTF_8.name()
            }
            withType<JavaCompile> {
                options.encoding = Charsets.UTF_8.name()
                sourceCompatibility = JavaVersion.VERSION_1_8.toString()
                targetCompatibility = JavaVersion.VERSION_1_8.toString()
            }
            withType<Jar> {
                archiveFileName.set("${project.name}-${project.version}_javadoc.jar")
            }
        }
    }
}
