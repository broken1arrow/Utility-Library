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
}
