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





allprojects {

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
            sourceCompatibility = JavaVersion.VERSION_1_8.toString()
            targetCompatibility = JavaVersion.VERSION_1_8.toString()
        }
        jar {
            setProjectVersion(project)
           // archiveFileName.set("${project.name}-${project.version}_sources.jar")
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
/*
publishing {

    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])

            val sourcesJar by tasks.registering(Jar::class) {
                archiveClassifier.set("sources")
                from(sourceSets.main.get().allSource)
            }

            val javadocJar by tasks.registering(Jar::class) {
                archiveClassifier.set("javadoc")
                from(tasks.named("javadoc"))
            }

            groupId = "org.broken.arrow.library"
            artifactId = project.name.toString().lowercase()
            version = "0.106"
        }
    }
    repositories {
        maven {
            name = "GitHubPackages"

            url = uri("https://maven.pkg.github.com/broken1arrow/Utility-Library")
            credentials {
                username = "broken1arrow"
                password = System.getProperty("token")
            }
        }
    }*/
}



fun setProjectVersion(project: Project) {
if (project.version == "" || project.version == "1.0-SNAPSHOT") project.version = version
}

tasks {
javadoc {
    options.encoding = Charsets.UTF_8.name() // We want UTF-8 for everything
}
}

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
