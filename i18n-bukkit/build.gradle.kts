dependencies {
    compileOnly("net.md-5:bungeecord-chat:1.16-R0.4")
    compileOnly("net.kyori:adventure-api:4.14.0")
    compileOnly("me.clip:placeholderapi:2.11.1")
    compileOnly("org.spigotmc:spigot:1.20")
    compileOnly("org.spigotmc:spigot-api:1.20.4-R0.1-SNAPSHOT")
    compileOnly(project(":bukkit"))
    compileOnly(project(":common"))
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