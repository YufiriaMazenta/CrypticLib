package crypticlib.internal;

public interface ICrypticLibLoader {

    default void loadCrypticLib() {
        loadPlatform();
        loadCommandManager();
        loadPlatformAdapter();
        loadScheduler();
    }

    void loadPlatform();

    void loadCommandManager();

    void loadPlatformAdapter();

    void loadScheduler();

}
