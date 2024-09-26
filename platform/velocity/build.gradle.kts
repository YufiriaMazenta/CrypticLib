plugins {
    id("com.github.johnrengelman.shadow") version "7.1.2"
}

dependencies {
    compileOnly("com.velocitypowered:velocity-api:3.3.0-SNAPSHOT")
    compileOnly("org.yaml:snakeyaml:1.33")
    compileOnly("com.electronwill.night-config:core:3.6.7")
    implementation(project(":platform:common"))
}
