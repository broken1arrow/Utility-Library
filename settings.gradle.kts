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
            library("tr7zw.item.nbt.api", "de.tr7zw:item-nbt-api:2.12.4")
            library("google.findbugs.jsr305", "com.google.code.findbugs:jsr305:3.0.2")
            library("mojang.authlib","com.mojang:authlib:1.6.25");
            plugin("shadow", "com.github.johnrengelman.shadow").version("8.1.1")
        }
    }
}

/**
 * Version: 8.1.1
 */
 val shadowVersion = "8.1.1"


rootProject.name = "Utility-Library"
include(":Title-Update")
include(":Menu_Library")
include(":Block-Visualization")
include(":Commands")
include(":Color_Conversion")
include(":Utility-Library")
include(":MenuConfiguration-Manager")
include(":Database")
include(":Yaml_Utility")
include(":Conversation-Prompt")
include(":Log_and_Validate")
include(":Localization")
include(":Item_Creator")
include(":Serialize-Utility")
include(":NBT")
project(":Title-Update").projectDir = file("Title Update")
project(":Menu_Library").projectDir = file("Menu Library")
project(":Block-Visualization").projectDir = file("Block Visualization")
project(":Color_Conversion").projectDir = file("Color Conversion")
project(":Utility-Library").projectDir = file("Utility Library")
project(":MenuConfiguration-Manager").projectDir = file("MenuConfiguration Manager")
project(":Yaml_Utility").projectDir = file("Yaml Utility")
project(":Conversation-Prompt").projectDir = file("Conversation Prompt")
project(":Log_and_Validate").projectDir = file("Log and Validate")
project(":Item_Creator").projectDir = file("Item Creator")
project(":Serialize-Utility").projectDir = file("Serialize Utility")

