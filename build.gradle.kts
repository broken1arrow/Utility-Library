import org.apache.tools.ant.taskdefs.Java

plugins {
    `java-library`
    //java
    `maven-publish`
    //`kotlin-dsl`
    signing
    checkstyle
    alias(libs.plugins.shadow)
    id("io.github.gradle-nexus.publish-plugin") version "2.0.0"
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
checkstyle {
    toolVersion = "10.12.1"
    configFile = file("config/checkstyle/checkstyle.xml")
}

subprojects {
    plugins.apply("maven-publish")
    plugins.apply("java-library")
    plugins.apply("signing")
    plugins.apply("checkstyle")

 /*   java {
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(8))
        }
    }
*/
    tasks.withType<JavaCompile> {
      //  sourceCompatibility = "1.8"
       // targetCompatibility = "1.8"
        options.encoding = Charsets.UTF_8.name()
        options.release.set(8)
    }
    tasks {
        register("checkstyleAll") {
            dependsOn(subprojects.map { it.tasks.matching { t -> t.name.startsWith("checkstyle") } })
        }
        javadoc {
            options.encoding = Charsets.UTF_8.name()
            options.apply {
                // Disable Javadoc complains as it is handle with checkstyle.
                (this as StandardJavadocDocletOptions).addStringOption("Xdoclint:none", "-quiet")
            }
        }


        val sourceSets = project.extensions.getByName("sourceSets") as SourceSetContainer
        register<Jar>("sources") {
            archiveClassifier.set("sources")
            from(sourceSets["main"].allSource)
        }
    }
    project.afterEvaluate {
        publishing {
            publications {
                create<MavenPublication>("mavenJava") {

                    artifact(project.tasks.named<Jar>("sources").get()) {
                        classifier = "sources"
                    }
                    from(components["java"])
                    artifactId = project.name
                    groupId = project.group.toString()
                    artifactId = project.name
                    version = "0.107"
                    pom {
                        name.set(project.name)
                        description.set("Description for ${project.name}")
                        url.set("https://github.com/broken1arrow/Utility-Library")

                        developers {
                            developer {
                                id.set("yourId")
                                name.set("broken-arrow")
                                email.set("not set")
                            }
                        }
                        scm {
                            connection.set("scm:git:git://github.com/broken1arrow/Utility-Library")
                            developerConnection.set("scm:git:ssh://github.com/broken1arrow/Utility-Library")
                            url.set("https://github.com/broken1arrow/Utility-Library")
                        }
                    }
                }
            }

            repositories {
                mavenLocal()
                maven {
                    name = "sonatype"
                    url = uri("https://oss.sonatype.org/service/local/staging/deploy/maven2/")
                    credentials {
                        username = findProperty("ossrhUsername") as String? ?: ""
                        password = findProperty("ossrhPassword") as String? ?: ""
                    }
                }
            }
        }

        signing {
            val signatoryConfigured = project.hasProperty("signing.keyId") &&
                    project.hasProperty("signing.password") &&
                    project.hasProperty("signing.secretKeyRingFile")
            if (signatoryConfigured) {
                val signingKey = findProperty("signing.key") as String?
                //signingKey?.let { file(it).readText(Charsets.UTF_8) }

                useInMemoryPgpKeys(
                    findProperty("signing.keyId") as String?,
                    signingKey,
                    findProperty("signing.password") as String?
                )
                sign(publishing.publications["mavenJava"])
            }
        }
    }
}

nexusPublishing {
    repositories {
        sonatype {
            username.set(findProperty("ossrhUsername") as String?)
            password.set(findProperty("ossrhPassword") as String?)
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

     //   sourceCompatibility = JavaVersion.VERSION_1_8.toString()
      //  targetCompatibility = JavaVersion.VERSION_1_8.toString()
    }
}

