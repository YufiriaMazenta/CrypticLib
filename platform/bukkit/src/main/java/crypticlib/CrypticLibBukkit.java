package crypticlib;

import crypticlib.serveradapter.BukkitServerAdapter;
import crypticlib.serveradapter.FoliaServerAdapter;
import crypticlib.serveradapter.ServerAdapter;
import crypticlib.serveradapter.PaperServerAdapter;
import crypticlib.scheduler.BukkitScheduler;
import org.jetbrains.annotations.NotNull;

public class CrypticLibBukkit {

    private static ServerAdapter serverAdapter;
    private static boolean isFolia, isPaper;

    static {
        loadServerAdapter();
    }

    /**
     * 获取当前运行的平台实例
     *
     * @return 当前运行的平台实例
     */
    @NotNull
    public static ServerAdapter serverAdapter() {
        return serverAdapter;
    }

    @NotNull
    public static BukkitScheduler scheduler() {
        return serverAdapter().scheduler();
    }

    private static void loadServerAdapter() {
        try {
            Class<?> pluginMetaClass = Class.forName("io.papermc.paper.plugin.configuration.PluginMeta");
            pluginMetaClass.getMethod("isFoliaSupported");
            serverAdapter = FoliaServerAdapter.INSTANCE;
            isFolia = true;
            isPaper = true;
        } catch (ClassNotFoundException e) {
            serverAdapter = BukkitServerAdapter.INSTANCE;
            isFolia = false;
            isPaper = false;
        } catch (NoSuchMethodException e) {
            serverAdapter = PaperServerAdapter.INSTANCE;
            isPaper = true;
            isFolia = false;
        }
    }

    public static boolean isFolia() {
        return isFolia;
    }

    public static boolean isPaper() {
        return isPaper;
    }

}
