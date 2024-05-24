rootProject.group = "com.crypticlib"
rootProject.version = "1.0.0-alpha"
//当全项目重构时更新大版本号,当添加模块或有较大更改时更新子版本号,当bug修复和功能补充时更新小版本号

var repositoryUrl = if (rootProject.version.toString().endsWith("SNAPSHOT")) {
    "http://repo.crypticlib.com:8081/repository/maven-snapshots/"
} else {
    "http://repo.crypticlib.com:8081/repository/maven-releases"
}
val javaVersion = JavaVersion.VERSION_17

java.sourceCompatibility = javaVersion
java.targetCompatibility = javaVersion

plugins {
    id("java")
    id("maven-publish")
    id("com.github.johnrengelman.shadow").version("7.1.2")
}

repositories {
    mavenLocal()
    mavenCentral()
}

dependencies {
    implementation(project(":common"))
    implementation(project(":platform:bukkit"))
    implementation(project(":platform:bungee"))
//    implementation(project(":ui"))
//    implementation(project(":conversation"))
//    implementation(project(":action"))
}

tasks {
    compileJava {
        options.encoding = "UTF-8"
    }
    build {
        dependsOn(shadowJar)
    }
    shadowJar {
        archiveFileName.set("${rootProject.name}-${version}.jar")
    }
}

publishing {
    publications.create<MavenPublication>("maven") {
        from(components["java"])
        groupId = rootProject.group as String?
    }
    repositories {
        maven {
            url = uri(repositoryUrl)
            isAllowInsecureProtocol = true
            credentials {
                username = project.findProperty("maven_username").toString()
                password = project.findProperty("maven_password").toString()
            }
        }
    }
}

subprojects {
    apply(plugin = "java")
    apply(plugin = "maven-publish")
    version = rootProject.version
    repositories {
        mavenLocal()
        mavenCentral()
    }
    dependencies {
        compileOnly("org.jetbrains:annotations:24.0.1")
    }
    tasks {
        compileJava {
            options.encoding = "UTF-8"
        }
    }
    publishing {
        publications.create<MavenPublication>("maven") {
            from(components["java"])
            var path = project.path
            val lastColonIndex = path.lastIndexOf(":")
            val name = path.substring(lastColonIndex + 1)
            path = path.substring(0, lastColonIndex).replace(":", ".")
            groupId = "${rootProject.group}${path}"
            artifactId = name
        }
        repositories {
            maven {
                url = uri(repositoryUrl)
                isAllowInsecureProtocol = true
                credentials {
                    username = project.findProperty("maven_username").toString()
                    password = project.findProperty("maven_password").toString()
                }
            }
        }
    }
    java.sourceCompatibility = javaVersion
    java.targetCompatibility = javaVersion
}