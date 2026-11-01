dependencies {
    compileOnly("org.spigotmc:spigot-api:1.21.4-R0.1-SNAPSHOT")
    compileOnly(project(":platform:common"))
    compileOnly(project(":platform:bukkit"))
    implementation(project(":module:bukkit:util"))

    testImplementation(project(":platform:common"))
    testImplementation(project(":platform:bukkit"))
    testImplementation("org.spigotmc:spigot-api:1.21.4-R0.1-SNAPSHOT")
    testImplementation("org.jetbrains:annotations:24.0.1")
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.2")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.test {
    useJUnitPlatform()
}
