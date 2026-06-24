package crypticlib.database;

import crypticlib.libloader.LibLoader;
import crypticlib.libloader.Library;

import java.util.Collections;

public class Database {

    private static final Library HIKARI_LIBRARY = new Library(
        "https://repo.maven.apache.org/maven2/",
        "com.zaxxer:HikariCP:5.1.0",
        Collections.singletonMap("com.zaxxer.hikari", "crypticlib.lib.hikari")
    );

    private static boolean initialized = false;

    public static synchronized void init() {
        if (initialized) {
            return;
        }
        LibLoader.loadLibrary(HIKARI_LIBRARY);
        initialized = true;
    }

}
