package crypticlib.internal;

public enum Platform {

    BUKKIT, BUNGEE;

    private static Platform CURRENT;

    boolean isBukkit() {
        return this == BUKKIT;
    }

    boolean isBungee() {
        return this == BUNGEE;
    }

    public static Platform getCurrent() {
        return CURRENT;
    }

    public static void setCurrent(Platform platform) {
        if (CURRENT == null) {
            CURRENT = platform;
        } else {
            throw new RuntimeException("Current platform is already set");
        }
    }

}
