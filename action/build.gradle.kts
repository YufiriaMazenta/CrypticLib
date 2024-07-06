dependencies {
    compileOnly("org.spigotmc:spigot-api:1.20.4-R0.1-SNAPSHOT")
    compileOnly(project(":bukkit"))
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
