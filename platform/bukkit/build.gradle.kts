repositories {
    //Spigot API
    maven("https://hub.spigotmc.org/nexus/content/repositories/releases/")
    //Folia API
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    compileOnly("net.md-5:bungeecord-chat:1.16-R0.4")
    compileOnly("net.kyori:adventure-api:4.14.0")
    compileOnly("me.clip:placeholderapi:2.11.1")
    compileOnly("dev.folia:folia-api:1.20.1-R0.1-SNAPSHOT")
    compileOnly(project(":common"))
}