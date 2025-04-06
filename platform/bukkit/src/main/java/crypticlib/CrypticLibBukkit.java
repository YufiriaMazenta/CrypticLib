package crypticlib;

import crypticlib.platformadapter.BukkitPlatformAdapter;
import crypticlib.platformadapter.FoliaPlatformAdapter;
import crypticlib.platformadapter.PlatformAdapter;
import crypticlib.platformadapter.PaperPlatformAdapter;
import crypticlib.scheduler.Scheduler;
import org.jetbrains.annotations.NotNull;

public class CrypticLibBukkit {

    private static PlatformAdapter platformAdapter;

    static {
        loadPlatformAdapter();
    }

    /**
     * 获取当前运行的平台实例
     *
     * @return 当前运行的平台实例
     */
    @NotNull
    public static PlatformAdapter platformAdapter() {
        return platformAdapter;
    }

    @NotNull
    public static Scheduler scheduler() {
        return platformAdapter().scheduler();
    }

    private static void loadPlatformAdapter() {
        try {
            Class<?> pluginMetaClass = Class.forName("io.papermc.paper.plugin.configuration.PluginMeta");
            pluginMetaClass.getMethod("isFoliaSupported");
            platformAdapter = FoliaPlatformAdapter.INSTANCE;
        } catch (ClassNotFoundException e) {
            platformAdapter = BukkitPlatformAdapter.INSTANCE;
        } catch (NoSuchMethodException e) {
            platformAdapter = PaperPlatformAdapter.INSTANCE;
        }
    }

    public static boolean isFolia() {
        return platformAdapter.type().equals(PlatformAdapter.PlatformType.FOLIA);
    }

    public static boolean isPaper() {
        return platformAdapter.type().equals(PlatformAdapter.PlatformType.PAPER) || platformAdapter.type().equals(PlatformAdapter.PlatformType.FOLIA);
    }

}
