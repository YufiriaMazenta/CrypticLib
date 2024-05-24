## CrypticLib

### 简介

CrypticLib是一个用于编写Bungee/Bukkit多端插件的库，提供各种简便开发流程的工具，目前正在开发中...

计划支持版本`1.16-1.20.6`，需要使用`Java17`进行编译

### 目前进度：

完成基础架构，线程管理器

### 使用方式

将CrypticLib作为依赖打包到插件中，并将crypticlib包relocate

参考 https://github.com/YufiriaMazenta/CrypticLibExample

### 模块介绍

暂无

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
