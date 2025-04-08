java.sourceCompatibility = JavaVersion.VERSION_1_8
java.targetCompatibility = JavaVersion.VERSION_1_8
rootProject.group = "com.crypticlib"
rootProject.version = "1.12.3.4"
//当全项目重构时更新大版本号,当添加模块或有较大更改时更新子版本号,当bug修复和功能补充时更新小版本号

var repositoryUrl = "https://repo.crypticlib.com:8081/repository/"
repositoryUrl = if (rootProject.version.toString().endsWith("SNAPSHOT")) {
    repositoryUrl.plus("maven-snapshots/")
} else {
    repositoryUrl.plus("maven-releases/")
}

plugins {
    id("java")
    id("maven-publish")
    id("com.github.johnrengelman.shadow") version "7.1.2"
}

subprojects {
    apply(plugin = "java")
    apply(plugin = "maven-publish")
    apply(plugin = "com.github.johnrengelman.shadow")
    version = rootProject.version
    repositories {
        mavenLocal()
        maven("https://hub.spigotmc.org/nexus/content/repositories/public/")
        maven("https://oss.sonatype.org/content/groups/public/")
        maven("https://repo.rosewooddev.io/repository/public/")
        maven("https://repo.extendedclip.com/content/repositories/placeholderapi/")
        maven("https://repo.maven.apache.org/maven2/")
        maven("https://mvn.lumine.io/repository/maven-public/")
        maven("https://repo.papermc.io/repository/maven-public/")
        maven("https://nexus.phoenixdevt.fr/repository/maven-public/")
        maven("https://r.irepo.space/maven/")
        maven("https://repo.codemc.io/repository/nms/")
        maven("https://libraries.minecraft.net")
        maven("https://repo.crypticlib.com:8081/repository/maven-public/")
        maven("https://jitpack.io")
        mavenCentral()
    }
    dependencies {
        compileOnly("org.jetbrains:annotations:24.0.1")
    }
    tasks {
        compileJava {
            options.encoding = "UTF-8"
        }
        build {
            dependsOn(shadowJar)
        }
    }
    publishing {
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
        publications.create<MavenPublication>("maven") {
            artifact(tasks["shadowJar"]) {
                classifier = null
            }
            val path = project.path
            val name = when  {
                arrayOf(":module:bukkit", ":module:common",":module:bungee", ":module:velocity").contains(path) -> {
                    path.substring(1)
                }
                path.startsWith(":module:") -> {
                    path.replaceFirst(":module:", "")
                }
                path.startsWith(":platform:") -> {
                    path.replaceFirst(":platform:", "")
                }
                else -> path.substring(1)
            }.replace(":", "-")
            groupId = "${rootProject.group}"
            artifactId = name
        }
    }
    java.sourceCompatibility = JavaVersion.VERSION_1_8
    java.targetCompatibility = JavaVersion.VERSION_1_8
}