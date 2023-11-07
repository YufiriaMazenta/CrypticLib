## CrypticLib

### 简介

CrypticLib是一个基于BukkitAPI和FoliaAPI编写的库，提供各种简便开发流程的工具，目前正在开发中...

### 使用方式

将CrypticLib作为依赖打包到插件中，并将crypticlib包relocate

[![](https://jitpack.io/v/com.crypticlib/crypticlib.svg)](https://jitpack.io/#com.crypticlib/crypticlib)

参考 https://github.com/YufiriaMazenta/CrypticLibExample

#### pom.xml
```xml
<repositories>
	<repository>
	    <id>jitpack.io</id>
	    <url>https://jitpack.io</url>
	</repository>
</repositories>
```
```xml
<dependencies>
    <dependency>
        <groupId>com.crypticlib</groupId>
        <artifactId>crypticlib</artifactId>
        <version>Tag</version>
    </dependency>
</dependencies>
```

#### build.gradle.kts
```kotlin
repositories {
    maven("https://jitpack.io")
}
```
```kotlin
dependencies {
    implementation("com.crypticlib:crypticlib:0.0.1")
}
```

#### build.gradle
```groovy
repositories {
    maven { url 'https://jitpack.io' }
}
```
```kotlin
dependencies {
    implementation 'com.crypticlib:crypticlib:Tag'
}
```