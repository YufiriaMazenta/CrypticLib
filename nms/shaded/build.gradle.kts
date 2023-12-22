repositories {
    maven("https://repo.tabooproject.org/repository/releases")
}
dependencies {
    compileOnly("org.spigotmc:spigot-api:1.16.5-R0.1-SNAPSHOT")
    compileOnly("net.kyori:adventure-api:4.14.0")
    compileOnly("org.jetbrains:annotations:24.0.1")
    compileOnly("com.google.code.gson:gson:2.10.1")
    compileOnly("ink.ptms:nms-all:1.0.0")
    compileOnly("org.spigotmc:spigot:1.13-R0.1-20190527.160257-2")
    compileOnly(project(":common"))
    compileOnly(project(":nms:common"))
}