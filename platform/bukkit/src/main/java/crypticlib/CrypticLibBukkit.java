package crypticlib;

import crypticlib.platform.BukkitPlatform;
import crypticlib.platform.FoliaPlatform;
import crypticlib.platform.IPlatform;
import crypticlib.platform.PaperPlatform;
import crypticlib.scheduler.IScheduler;
import org.jetbrains.annotations.NotNull;

public class CrypticLibBukkit {

    private static IPlatform platform;

    static {
        loadPlatform();
    }

    /**
     * 获取当前运行的平台实例
     *
     * @return 当前运行的平台实例
     */
    @NotNull
    public static IPlatform platform() {
        return platform;
    }

    @NotNull
    public static IScheduler scheduler() {
        return platform().scheduler();
    }

    private static void loadPlatform() {
        try {
            Class<?> pluginMetaClass = Class.forName("io.papermc.paper.plugin.configuration.PluginMeta");
            pluginMetaClass.getMethod("isFoliaSupported");
            platform = FoliaPlatform.INSTANCE;
        } catch (ClassNotFoundException e) {
            platform = BukkitPlatform.INSTANCE;
        } catch (NoSuchMethodException e) {
            platform = PaperPlatform.INSTANCE;
        }
    }

    public static boolean isFolia() {
        return platform.type().equals(IPlatform.PlatformType.FOLIA);
    }

    public static boolean isPaper() {
        return platform.type().equals(IPlatform.PlatformType.PAPER) || platform.type().equals(IPlatform.PlatformType.FOLIA);
    }

}
