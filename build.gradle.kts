
plugins {
    `java-library`
    java
    `maven-publish`
    `kotlin-dsl`
}

group = "org.broken.arrow.library"
version = "1.0-SNAPSHOT"
apply(plugin = "java")

repositories {
    mavenCentral()
    mavenLocal()
    gradlePluginPortal()
}





allprojects {
    apply(plugin = "java")
}



subprojects {
    tasks {
        javadoc {
            options.encoding = Charsets.UTF_8.name()
        }
        compileJava {
            options.encoding = Charsets.UTF_8.name()

            // Set the release flag. This configures what version bytecode the compiler will emit, as well as what JDK APIs are usable.
            // See https://openjdk.java.net/jeps/247 for more information.
            //options.release.set(8)
            sourceCompatibility = JavaVersion.VERSION_1_8.toString()
            targetCompatibility = JavaVersion.VERSION_1_8.toString()
        }
        jar {
            setProjectVersion(project)
            archiveFileName.set("${project.name}-${project.version}_sources.jar")
        }
        processResources {
            filteringCharset = Charsets.UTF_8.name() // We want UTF-8 for everything
            filesMatching("plugin.yml") {
                expand(
                    mapOf(
                        "version" to project.version,
                        "main" to "${project.group}.UtilityLibrary",//"${project.group}.${project.name}",
                        "name" to project.name
                    )
                )
            }
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
}


private val exclusions = listOf(
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
/*todo This give circle dependency currently. See it this could be solved.
subprojects{
    apply(plugin = "com.github.johnrengelman.shadow")
    version = "1.0-SNAPSHOT"
    if (project.group == "" || project.group == "Utility_Library_Core")
        project.group = "org.broken.arrow.library"
    val config = ConfigurationUtilityLibrary()
    config.apply(project)
    tasks {
        if (project.name != "buildProject") {
            shadowJar {
                val shadowSettings = config.shadowProject(project)
                archiveFileName.set(shadowSettings.archiveFileName)
                dependencies {
                    shadowSettings.exclusions.forEach { exclude(it) }
                }
                shadowSettings.relocateList.forEach { relocateItem ->
                    relocate(relocateItem.pattern, relocateItem.destination)
                }
            }
        }
        processResources {
            filteringCharset = Charsets.UTF_8.name()
            filesMatching("plugin.yml") {
                expand(
                    mapOf(
                        "version" to project.version,
                        "main" to "${project.group}.UtilityLibrary",
                        "name" to project.name
                    )
                )
            }
        }
    }
}*/
