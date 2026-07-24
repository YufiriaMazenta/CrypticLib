package crypticlib.database;

import crypticlib.dependency.Dependency;
import crypticlib.dependency.DependencyLoader;

public class Database {

    private static boolean initialized = false;

    public static synchronized void init() {
        if (initialized) {
            return;
        }
        try {
            DependencyLoader.INSTANCE.loadDependency(
                Dependency.builder("com.zaxxer", "HikariCP", "5.1.0")
                    .test("!com%zaxxer%hikari%HikariDataSource")
                    .relocate("com%zaxxer%hikari", "crypticlib%lib%hikari")
                    .build()
            );
        } catch (Throwable e) {
            throw new RuntimeException("Failed to load HikariCP", e);
        }
        initialized = true;
    }

}
