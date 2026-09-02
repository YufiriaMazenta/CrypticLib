package crypticlib.database;

import crypticlib.env.dependency.Dependency;
import crypticlib.env.dependency.DependencyLoader;

public class Database {

    private static boolean initialized = false;

    public static synchronized void init() {
        if (initialized) {
            return;
        }
        try {
            DependencyLoader.INSTANCE.loadDependency(
                Dependency.builder("com.zaxxer", "HikariCP", "4.0.3")
                    .test("!com.zaxxer.hikari.HikariDataSource")
                    .build()
            );
        } catch (Throwable e) {
            throw new RuntimeException("Failed to load HikariCP", e);
        }
        initialized = true;
    }

}
