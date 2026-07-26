java.sourceCompatibility = JavaVersion.VERSION_1_8
java.targetCompatibility = JavaVersion.VERSION_1_8
rootProject.group = "com.crypticlib"
rootProject.version = rootProject.findProperty("version").toString()
// 全项目重构时更新大版本号
// 添加模块或有较大更改时更新次版本号
// 有API变动(新增/删除/更改声明)时更新修订号
// 仅内部修改,例如BUG修复时更新额外版本号

var repositoryUrl = "https://repo.crypticlib.incrafttime.top/repository/"
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
    // 跳过没有源码的中间项目（如 platform、module、module:bukkit 等）
    if (!file("src").exists()) return@subprojects

    apply(plugin = "java")
    apply(plugin = "maven-publish")
    apply(plugin = "com.github.johnrengelman.shadow")
    version = rootProject.version
    java {
        withSourcesJar()
    }
    repositories {
        // 优先中央仓库, 避免中央坐标(gson/asm/annotations 等)被第三方仓库抢先解析(依赖混淆)
        mavenCentral()
        maven("https://hub.spigotmc.org/nexus/content/repositories/public/") {
            content {
                includeGroup("org.spigotmc")
                includeGroup("net.md-5")
            }
        }
        maven("https://oss.sonatype.org/content/groups/public/")
        maven("https://repo.rosewooddev.io/repository/public/") {
            content {
                includeGroup("dev.rosewood")
            }
        }
        maven("https://repo.extendedclip.com/content/repositories/placeholderapi/") {
            content {
                includeGroup("me.clip")
            }
        }
        maven("https://mvn.lumine.io/repository/maven-public/") {
            content {
                includeGroup("io.lumine")
            }
        }
        maven("https://repo.papermc.io/repository/maven-public/") {
            content {
                includeGroup("io.papermc")
                includeGroup("dev.folia")
                includeGroup("com.velocitypowered")
                includeGroup("net.md-5")
            }
        }
        maven("https://nexus.phoenixdevt.fr/repository/maven-public/")
        maven("https://r.irepo.space/maven/")
        maven("https://repo.codemc.io/repository/nms/")
        maven("https://libraries.minecraft.net")
        //CrypticLib(自有仓库, 同时托管 folia-api 与 velocity 构件)
        maven("https://repo.crypticlib.incrafttime.top/repository/maven-public/") {
            content {
                includeGroup("com.crypticlib")
                includeGroup("dev.folia")
                includeGroup("com.velocity")
            }
        }
        maven("https://jitpack.io") {
            content {
                includeGroupByRegex("com\\.github\\..*")
            }
        }
        // mavenLocal 置于最后, 避免本地脏构件静默覆盖远端版本
        mavenLocal()
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
            artifact(tasks["sourcesJar"])
            fun artifactIdOf(path: String): String {
                return when  {
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
            }
            groupId = "${rootProject.group}"
            artifactId = artifactIdOf(project.path)
            // 为发布物 POM 写入对 CrypticLib platform 构件的 provided 依赖:
            // 模块以 compileOnly(project(...)) 依赖 platform 且不被 shade, 消费者需据此补充平台坐标
            pom.withXml {
                val projectDeps = configurations["compileOnly"].dependencies
                    .filterIsInstance<ProjectDependency>()
                if (projectDeps.isNotEmpty()) {
                    val dependenciesNode = asNode().appendNode("dependencies")
                    projectDeps.forEach { dep ->
                        val depNode = dependenciesNode.appendNode("dependency")
                        depNode.appendNode("groupId", rootProject.group.toString())
                        depNode.appendNode("artifactId", artifactIdOf(dep.dependencyProject.path))
                        depNode.appendNode("version", rootProject.version.toString())
                        depNode.appendNode("scope", "provided")
                    }
                }
            }
        }
    }
    java.sourceCompatibility = JavaVersion.VERSION_1_8
    java.targetCompatibility = JavaVersion.VERSION_1_8
}