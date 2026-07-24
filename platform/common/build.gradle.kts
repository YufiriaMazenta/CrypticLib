dependencies {
    compileOnly("com.google.code.gson:gson:2.10.1")
    compileOnly("org.ow2.asm:asm:9.10.1")
    compileOnly("org.ow2.asm:asm-commons:9.10.1")
    implementation("me.lucko:jar-relocator:1.7") {
        exclude(group = "org.ow2.asm")
    }
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.2")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testImplementation("org.spigotmc:spigot-api:1.20.4-R0.1-SNAPSHOT")
}

tasks.shadowJar {
    relocate("me.lucko.jarrelocator", "crypticlib.libs.jarrelocator")
}

tasks.test {
    useJUnitPlatform()
}
