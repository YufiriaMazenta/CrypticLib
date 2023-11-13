## CrypticLib

### 简介

CrypticLib是一个基于BukkitAPI和FoliaAPI编写的库，提供各种简便开发流程的工具，目前正在开发中...

### 使用方式

将CrypticLib作为依赖打包到插件中，并将crypticlib包relocate

参考 https://github.com/YufiriaMazenta/CrypticLibExample

#### pom.xml
```xml
<repositories>
	<repository>
	    <id>crypticlib</id>
	    <url>http://repo.crypticlib.com:8081/repository/maven-public/</url>
	</repository>
</repositories>
```
```xml
<dependencies>
    <dependency>
        <groupId>com.crypticlib</groupId>
        <artifactId>CrypticLib</artifactId>
        <version>${version}</version>
    </dependency>
</dependencies>
```

#### build.gradle.kts
```kotlin
repositories {
    maven("http://repo.crypticlib.com:8081/repository/maven-public/") {
        isAllowInsecureProtocol = true
    }
}
```
```kotlin
dependencies {
    implementation("com.crypticlib:CrypticLib:${version}")
}
```

#### build.gradle
```groovy
repositories {
    maven {
        url = "http://repo.crypticlib.com:8081/repository/maven-public/"
        allowInsecureProtocol = true
    }
}
```
```groovy
dependencies {
    implementation 'com.crypticlib:CrypticLib:${version}'
}
```