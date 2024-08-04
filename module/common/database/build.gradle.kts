dependencies {
    implementation("com.zaxxer:HikariCP:5.1.0")
}

tasks {
    shadowJar {
        relocate("com.zaxxer.hikari", "crypticlib.libs.hikari")
        dependencies {
            exclude(dependency("org.slf4j:slf4j-api"))
        }
    }
}