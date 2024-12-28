## CrypticLib

### 简介

CrypticLib是一个Minecraft插件开发库，提供各种简便开发流程的工具，目前正在开发中...

#### 目前支持平台

Bukkit

Bungee

Velocity

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
        <artifactId>所需模块</artifactId>
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
    implementation("com.crypticlib:所需模块:${version}")
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
    implementation 'com.crypticlib:所需模块:${version}'
}
```
