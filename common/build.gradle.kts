plugins {
    id("com.github.johnrengelman.shadow") version "7.1.2"
}

val nightConfigVer = rootProject.findProperty("nightconfig-ver").toString()

repositories {
    mavenCentral()
}

dependencies {
    implementation("com.electronwill.night-config:yaml:$nightConfigVer")
    implementation("com.electronwill.night-config:hocon:$nightConfigVer")
    implementation("com.electronwill.night-config:toml:$nightConfigVer")
    implementation("com.electronwill.night-config:json:$nightConfigVer")
}

tasks {
    build {
        dependsOn(shadowJar)
    }
    shadowJar {
        archiveFileName.set("${project.name}-${version}.jar")
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
}