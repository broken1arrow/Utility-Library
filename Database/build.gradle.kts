
plugins {
    java
    id ("java-library")
    id("org.broken.arrow.library.LoadDependency")
}

dependencies {
    api(project(":Log_and_Validate"))
    api(project(":Serialize-Utility"))
    compileOnly(libs.org.xerial.sqlite.jdbc)
    compileOnly(libs.com.zaxxer.hikaricp)
    compileOnly(libs.mysql.connector.j)
    compileOnly(libs.org.mongodb.mongodb.driver.sync)
    compileOnly(libs.google.findbugs.jsr305)
    compileOnly(libs.org.apache.logging.log4j.log4j.api)
    compileOnly(libs.apache.logging.log4j.core)
}

description = "Database"
version = "1.0-SNAPSHOT"

java {
    withJavadocJar()
}
