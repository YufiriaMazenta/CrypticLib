plugins {
    id("com.github.johnrengelman.shadow") version "7.1.2"
}

dependencies {
    compileOnly("net.md-5:bungeecord-api:1.21-R0.1-SNAPSHOT")
    implementation(project(":platform:common"))
}