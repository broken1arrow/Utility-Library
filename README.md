# Utility-Library

[![Java CI with Gradle](https://github.com/broken1arrow/Utility-Library/actions/workflows/gradle.yml/badge.svg)](https://github.com/broken1arrow/Utility-Library/actions/workflows/gradle.yml)

To import the library, please refer to the wiki for more details. If you want to compile specific modules only, you can find more information there as well. 
You will find the full wiki here [utility library wiki](https://broken-arrow.gitbook.io/utility-library/).

If you do want to use the Utility Library plugin, you can follow the instructions below. 

Note because I change from Maven to Gradle will not below instructions work in versions after 0.100. Will update with new information soon. 
In main time you must run this project locally if you want to use later versions.


```xml

<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>

<dependencies>
<dependency>
    <groupId>com.github.broken1arrow.Utility-Library</groupId>
    <artifactId>Utility-Library</artifactId>
    <version>latest</version>
    <scope>provided</scope>
</dependency>
</dependencies>

```
