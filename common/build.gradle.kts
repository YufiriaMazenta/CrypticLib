plugins {
    id("com.github.johnrengelman.shadow") version "7.1.2"
}

tasks {
    build {
        dependsOn(shadowJar)
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