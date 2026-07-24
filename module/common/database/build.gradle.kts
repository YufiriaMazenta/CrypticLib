dependencies {
    compileOnly(project(":platform:common"))
    compileOnly("com.zaxxer:HikariCP:5.1.0")
}


tasks {
    shadowJar {
        relocate("com.zaxxer.hikari", "crypticlib.libs.hikari")
    }
}