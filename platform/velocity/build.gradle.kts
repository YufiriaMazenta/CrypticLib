plugins {
    id("com.github.johnrengelman.shadow") version "7.1.2"
}

dependencies {
    compileOnly("com.velocity:velocity:3.4.0")
    compileOnly("org.yaml:snakeyaml:1.33")
    compileOnly("com.electronwill.night-config:core:3.6.7")
    implementation(project(":platform:common"))
}