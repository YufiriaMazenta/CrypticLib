plugins {
    id("com.github.johnrengelman.shadow") version "7.1.2"
}

val nightConfigVer = rootProject.findProperty("nightconfig-ver").toString()

dependencies {
    compileOnly("net.md-5:bungeecord-chat:1.16-R0.4")
    compileOnly("net.kyori:adventure-api:4.14.0")
    compileOnly("me.clip:placeholderapi:2.11.1")
    compileOnly("org.spigotmc:spigot:1.20")
    compileOnly("dev.folia:folia-api:1.20.2")
    compileOnly("com.electronwill.night-config:yaml:$nightConfigVer")
    compileOnly("com.electronwill.night-config:hocon:$nightConfigVer")
    compileOnly("com.electronwill.night-config:toml:$nightConfigVer")
    compileOnly("com.electronwill.night-config:json:$nightConfigVer")
    implementation(project(":common"))
}

tasks {
    build {
        dependsOn(shadowJar)
    }
    shadowJar {
//        archiveFileName.set("${project.name}-${version}.jar")
        relocate("com.electronwill.nightconfig", "crypticlib.libs.config")
        relocate("com.typesafe", "crypticlib.libs.typesafe")
        dependencies {
            exclude(dependency("org.yaml:snakeyaml"))
        }
    }
    publish {
        dependsOn(build)
    }
}

publishing {
    publications.create<MavenPublication>("maven") {
        artifact(tasks.shadowJar.get()) {
            classifier = null // 如果你想让这个artifact成为主artifact，设置classifier为null
        }
        pom {
            // 配置POM文件
        }
        var path = project.path
        val lastColonIndex = path.lastIndexOf(":")
        val name = path.substring(lastColonIndex + 1)
        path = path.substring(0, lastColonIndex).replace(":", ".")
        groupId = "${rootProject.group}${path}"
        artifactId = name
    }
}