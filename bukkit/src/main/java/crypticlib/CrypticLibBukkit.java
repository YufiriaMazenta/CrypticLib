package crypticlib;

import crypticlib.command.manager.BukkitCommandManager;
import crypticlib.platform.bukkit.BukkitPlatform;
import crypticlib.platform.bukkit.FoliaPlatform;
import crypticlib.platform.bukkit.IPlatform;
import crypticlib.platform.bukkit.PaperPlatform;
import crypticlib.scheduler.bukkit.IScheduler;
import org.jetbrains.annotations.NotNull;

public class CrypticLibBukkit {

    private static final BukkitCommandManager commandManager;
    private static IPlatform platform;
    private static boolean debug = false;

    static {
        loadPlatform();
        loadVersion();
        commandManager = BukkitCommandManager.INSTANCE;
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

    private static void loadVersion() {

    }

    @NotNull
    public static BukkitCommandManager commandManager() {
        return commandManager;
    }

    public static boolean debug() {
        return debug;
    }

    public static void setDebug(boolean debug) {
        CrypticLibBukkit.debug = debug;
    }

    public static boolean isFolia() {
        return platform.platform().equals(IPlatform.Platform.FOLIA);
    }

    public static boolean isPaper() {
        return platform.platform().equals(IPlatform.Platform.PAPER) || platform.platform().equals(IPlatform.Platform.FOLIA);
    }

}
