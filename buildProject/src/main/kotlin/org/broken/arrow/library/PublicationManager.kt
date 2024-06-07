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
        val projectName = project.name

        val javaDocName = "${projectName}_javadocJar"
        val sourcesName = "${projectName}_sourcesJar"

        project.tasks {
            if (findByName(sourcesName) == null) {
                val sourceSets = project.extensions.getByName("sourceSets") as SourceSetContainer
                register<Jar>(sourcesName) {
                    archiveClassifier.set("sources")
                    from(sourceSets["main"].allSource)
                }}
            if (findByName(javaDocName) == null) {
                register<Jar>(javaDocName) {
                    archiveClassifier.set("${projectName}_javadoc")
                    from(project.tasks.named("javadoc"))
                }}
        }
    /*    val javadocJarTask = project.tasks.findByName("${projectName}javadocJar") as? Jar
            ?: project.tasks.register<Jar>("${projectName}javadocJar") {
                archiveClassifier.set("javadoc")
                from(project.tasks.named("javadoc"))
            }*/

        project.afterEvaluate {
            project.extensions.configure<org.gradle.api.publish.PublishingExtension> {
                publications {
                    create<MavenPublication>("${projectName}_maven"){
                        configure(this)

                        artifact(project.tasks.named<Jar>(sourcesName).get()) {
                            classifier = "sources"
                        }
                       /* artifact(project.tasks.named<Jar>("javaDocName").get()) {
                            classifier = "javadoc"
                        }*/
                        from (components["java"])
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



