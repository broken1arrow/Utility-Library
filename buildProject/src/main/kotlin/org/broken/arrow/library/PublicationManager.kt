package org.broken.arrow.library

import org.gradle.api.tasks.bundling.Jar
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.kotlin.dsl.*
import org.gradle.api.Project

/**
 * Configures Maven publications for a Gradle project.
 *
 * @param project The Gradle project to configure.
 * @param configure The configuration block for the MavenPublication.
 */
class PublicationManager(project: Project, configure: MavenPublication.() -> Unit = {}) {

    init {
        apply(project,configure);
    }

    /**
     * Applies the Maven publishing configuration to the given project.
     *
     * @param project The Gradle project to configure.
     * @param configure The configuration block for the MavenPublication.
     */
    private fun apply(project: Project,configure: MavenPublication.() -> Unit) {
        //project.plugins.apply("maven-publish")

        val sourceSets = project.extensions.getByName("sourceSets") as SourceSetContainer

        project.tasks {
            register<Jar>("sourcesJar") {
                archiveClassifier.set("sources")
                from(sourceSets["main"].allSource)
            }
        }

        project.afterEvaluate {
            project.extensions.configure<org.gradle.api.publish.PublishingExtension> {
                publications {
                    create<MavenPublication>("maven_Java_task"){
                        configure(this)

                        artifact(project.tasks.named<Jar>("sourcesJar").get()) {
                            classifier = "sources"
                        }
                        artifact(project.tasks.named<Jar>("javadocJar").get()) {
                            classifier = "javadocs"
                        }
                        groupId = project.group.toString()
                        artifactId = project.name
                        version = "0.105"
                    }
                }
                repositories {
                    mavenLocal()
                }
            }
        }
    }
}



