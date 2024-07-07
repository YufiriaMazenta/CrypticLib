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
    implementation(project(":common"))
}

tasks {
    build {
        dependsOn(shadowJar)
    }
}

publishing {
    publications.create<MavenPublication>("maven") {
        artifact(tasks.shadowJar.get()) {
            classifier = null
        }
        pom {

        }
        var path = project.path
        val lastColonIndex = path.lastIndexOf(":")
        val name = path.substring(lastColonIndex + 1)
        path = path.substring(0, lastColonIndex).replace(":", ".")
        groupId = "${rootProject.group}${path}"
        artifactId = name
    }
}