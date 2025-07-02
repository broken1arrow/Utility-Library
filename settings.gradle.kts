includeBuild("buildProject")

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.5.0"
}

dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            library("mysql.connector.j", "com.mysql:mysql-connector-j:8.4.0")
            library("apache.logging.log4j.core", "org.apache.logging.log4j:log4j-core:2.17.1")
            library("org.spigotmc.spigotapi", "org.spigotmc:spigot-api:1.20.2-R0.1-SNAPSHOT")
            library("tr7zw.item.nbt.api", "de.tr7zw:item-nbt-api:2.15.1")
            library("google.findbugs.jsr305", "com.google.code.findbugs:jsr305:3.0.2")
            library("mojang.authlib","com.mojang:authlib:1.6.25");
            plugin("shadow", "com.gradleup.shadow").version("8.3.5")
        }
    }
}

/**
 * Version: 8.1.1
 */
 val shadowVersion = "8.1.1"


rootProject.name = "utility-library-core"
include(":title-update")
include(":menu-library")
include(":block-visualization")
include(":commands")
include(":color-conversion")
include(":utility-library")
include(":menu-configuration-manager")
include(":database")
include(":yaml-utility")
include(":conversation-prompt")
include(":log-and-validate")
include(":localization")
include(":item-creator")
include(":serialize-utility")
include(":nbt")
project(":title-update").projectDir = file("Title Update")
project(":menu-library").projectDir = file("Menu Library")
project(":block-visualization").projectDir = file("Block Visualization")
project(":commands").projectDir = file("Commands")
project(":color-conversion").projectDir = file("Color Conversion")
project(":utility-library").projectDir = file("Utility Library")
project(":menu-configuration-manager").projectDir = file("Menu Configuration Manager")
project(":database").projectDir = file("Database")
project(":yaml-utility").projectDir = file("Yaml Utility")
project(":conversation-prompt").projectDir = file("Conversation Prompt")
project(":log-and-validate").projectDir = file("Log and Validate")
project(":localization").projectDir = file("Localization")
project(":item-creator").projectDir = file("Item Creator")
project(":serialize-utility").projectDir = file("Serialize Utility")
project(":nbt").projectDir = file("NBT")


