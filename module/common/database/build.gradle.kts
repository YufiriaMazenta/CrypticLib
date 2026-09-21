dependencies {
    compileOnly(project(":platform:common"))
    testImplementation(project(":platform:common"))
    testImplementation("org.jetbrains:annotations:24.0.1")
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.2")
    testImplementation("com.h2database:h2:2.2.224")
    // 真机后端：SQLite 是纯 Java 驱动；MySQL/PostgreSQL 需要本地可用的服务（连不上时用例会被跳过而不是失败）
    testImplementation("org.xerial:sqlite-jdbc:3.45.1.0")
    testImplementation("com.mysql:mysql-connector-j:8.0.33")
    testImplementation("org.postgresql:postgresql:42.7.3")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

//// 同一套测试用例跑在哪个后端上，由 TestDatabases 读取该属性决定
//tasks.test {
//    useJUnitPlatform()
//    systemProperty("crypticlib.test.database", "h2")
//}
//
//// 与 test 完全相同的用例集，只把后端换成 sqlite / mysql / postgresql
//listOf("sqlite", "mysql", "postgresql").forEach { backend ->
//    val backendTaskName = "test" + backend.replaceFirstChar { it.uppercaseChar() }
//    tasks.register<Test>(backendTaskName) {
//        description = "在 $backend 后端上运行 database 模块的完整测试用例"
//        group = "verification"
//        testClassesDirs = tasks.test.get().testClassesDirs
//        classpath = tasks.test.get().classpath
//        useJUnitPlatform()
//        systemProperty("crypticlib.test.database", backend)
//        // 允许用 -Dcrypticlib.test.<backend>.url/user/password 从命令行覆盖连接信息
//        listOf("url", "user", "password").forEach { key ->
//            val property = "crypticlib.test.$backend.$key"
//            System.getProperty(property)?.let { systemProperty(property, it) }
//        }
//    }
//}
//
//tasks.register("testAll") {
//    description = "在 H2 / SQLite / MySQL / PostgreSQL 四个后端上运行完整测试（数据库不可达时自动跳过）"
//    group = "verification"
//    dependsOn(tasks.test)
//    dependsOn("testSqlite", "testMysql", "testPostgresql")
//}
//
//// 构建时一并回归真机后端；连不上的后端对应用例会被跳过
//tasks.check {
//    dependsOn("testSqlite", "testMysql", "testPostgresql")
//}