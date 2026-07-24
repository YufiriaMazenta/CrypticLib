dependencies {
    compileOnly("net.md-5:bungeecord-chat:1.16-R0.4")
    compileOnly("net.kyori:adventure-api:${rootProject.findProperty("platform.bukkit.adventure-api.version")}")
    compileOnly("net.kyori:adventure-key:${rootProject.findProperty("platform.bukkit.adventure-api.version")}")
    compileOnly("net.kyori:adventure-text-logger-slf4j:${rootProject.findProperty("platform.bukkit.adventure-api.version")}")
    compileOnly("net.kyori:adventure-text-minimessage:${rootProject.findProperty("platform.bukkit.adventure-api.version")}")
    compileOnly("net.kyori:adventure-text-serializer-gson:${rootProject.findProperty("platform.bukkit.adventure-api.version")}")
    compileOnly("net.kyori:adventure-text-serializer-json:${rootProject.findProperty("platform.bukkit.adventure-api.version")}")
    compileOnly("net.kyori:adventure-text-serializer-legacy:${rootProject.findProperty("platform.bukkit.adventure-api.version")}")
    compileOnly("net.kyori:adventure-text-serializer-plain:${rootProject.findProperty("platform.bukkit.adventure-api.version")}")
    compileOnly("me.clip:placeholderapi:2.11.6")
    compileOnly("dev.folia:folia-api:1.20.2")
    implementation(project(":platform:common"))
    implementation(project(":module:common:script"))
}

tasks.shadowJar {
    relocate("me.lucko.jarrelocator", "crypticlib.libs.jarrelocator")
}