# Utility-Library

[![Static Badge](https://img.shields.io/badge/Repsy-0.132-brightgreen?style=flat&logo=codio&logoColor=%23b112cf&labelColor=gray)](https://repo.repsy.io/mvn/broken-arrow/utility-library/org/broken/arrow/library/)
[![Java CI with Gradle](https://github.com/broken1arrow/Utility-Library/actions/workflows/gradle.yml/badge.svg)](https://github.com/broken1arrow/Utility-Library/actions/workflows/gradle.yml)

To import the library, please refer to the wiki for more details. If you want to compile specific modules only, you can find more information there as well. 
You will find the full wiki here [utility library wiki](https://broken-arrow.gitbook.io/utility-library/).


If you do want to use the Utility Library plugin, you can follow the instructions below. 
<details>
<summary><b>🪶 Maven</b></summary>

```xml
<repositories>
    <repository>
        <id>repsy</id>
        <url>https://repo.repsy.io/broken-arrow/utility-library</url>
    </repository>
</repositories>

<dependencies>
    <dependency>
       <groupId>org.broken.arrow.library</groupId>
       <artifactId>utility-library</artifactId>
       <version>type version</version>
       <scope>provided</scope>
    </dependency>
</dependencies>
```
</details>

<details>
<summary><b>🐘 Gradle (Kotlin DSL)</b></summary>

```kotlin
repositories {
    maven { url = uri("https://repo.repsy.io/broken-arrow/utility-librar") }
}

dependencies {
implementation("org.broken.arrow.library:utility-library:type version")
}
```
</details>
