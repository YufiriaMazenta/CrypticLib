package crypticlib;

import crypticlib.serveradapter.BukkitServerAdapter;
import crypticlib.serveradapter.FoliaServerAdapter;
import crypticlib.serveradapter.ServerAdapter;
import crypticlib.serveradapter.PaperServerAdapter;
import crypticlib.scheduler.Scheduler;
import org.jetbrains.annotations.NotNull;

public class CrypticLibBukkit {

    private static ServerAdapter serverAdapter;

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
    public static Scheduler scheduler() {
        return serverAdapter().scheduler();
    }

    private static void loadServerAdapter() {
        try {
            Class<?> pluginMetaClass = Class.forName("io.papermc.paper.plugin.configuration.PluginMeta");
            pluginMetaClass.getMethod("isFoliaSupported");
            serverAdapter = FoliaServerAdapter.INSTANCE;
        } catch (ClassNotFoundException e) {
            serverAdapter = BukkitServerAdapter.INSTANCE;
        } catch (NoSuchMethodException e) {
            serverAdapter = PaperServerAdapter.INSTANCE;
        }
    }

    public static boolean isFolia() {
        return serverAdapter.type().equals(ServerAdapter.ServerType.FOLIA);
    }

    public static boolean isPaper() {
        return serverAdapter.type().equals(ServerAdapter.ServerType.PAPER) || serverAdapter.type().equals(ServerAdapter.ServerType.FOLIA);
    }

}
