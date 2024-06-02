package org.broken.arrow.library

import org.gradle.api.Project

open class ShadeLogic(private val project: Project) {

    init {
        // project.apply(plugin = "com.github.johnrengelman.shadow")
    }

    fun create(): ShadowTask? {
        return null;
    }

    fun shadowProject(shadowTask :ShadowTask) {
        shadowTask.execute(
            "${project.name}-${project.version}.jar",
            "${project.group}.dependencies",
            exclusions
        )
    }

    data class ShadeSettings(
        val archiveFileName: String,
        val exclusions: List<String>,
        val relocateList: List<Relocate>
    )

    data class Relocate(val pattern: String, val destination: String)
    fun interface ShadowTask {
        fun execute(archiveFileName: String, destination: String, exclusions: List<String>)
    }

    val exclusions = listOf(
        "*exclude.jar",
        "com/github/angeschossen/",
        "org/spigotmc/",
        "org/bukkit/",
        "org/yaml/snakeyaml/",
        "com/google/",
        "net/md_5/bungee/",
        "org/apache/commons/",
        "mojang-translations/",
        "javax/annotation/",
        "org/joml/",
        "org/checkerframework/",
        "META-INF/proguard/",
        "META-INF/versions/",
        "META-INF/maven/com.google.code.findbugs/",
        "META-INF/maven/com.google.code.gson/",
        "META-INF/maven/com.google.errorprone/",
        "META-INF/maven/com.google.guava/",
        "META-INF/maven/net.md-5/",
        "META-INF/maven/org.joml/",
        "META-INF/maven/org.spigotmc/",
        "META-INF/maven/org.yaml/"
    )

}