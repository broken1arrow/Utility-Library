
plugins {
    java
    id ("java-library")
    id("org.broken.arrow.library.LoadDependency")
}


dependencies {
    compileOnly(libs.com.google.code.findbugs.jsr305)
}

description = "Log_and_Validate"
version = "1.0-SNAPSHOT"

java {
    withJavadocJar()
}
