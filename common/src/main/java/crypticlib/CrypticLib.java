package crypticlib;

import crypticlib.command.manager.CommandManager;
import crypticlib.perm.PermManager;
import crypticlib.platform.BukkitPlatform;
import crypticlib.platform.FoliaPlatform;
import crypticlib.platform.IPlatform;
import crypticlib.platform.PaperPlatform;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

public class CrypticLib {


    private static final CommandManager commandManager;
    private static final PermManager PERM_MANAGER;
    private static IPlatform platform;
    private static Integer minecraftVersion;
    private static String nmsVersion;

    static {
        loadPlatform();
        loadVersion();
        commandManager = CommandManager.INSTANCE;
        PERM_MANAGER = PermManager.INSTANCE;
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

    /**
     * 获取当前运行的Minecraft版本
     * 示例:当前版本为1.20.2时,返回12002
     * 当前版本为1.7.10时,返回10710(虽然CrypticLib并不支持这个版本)
     *
     * @return 当前运行的Minecraft版本
     */
    public static Integer minecraftVersion() {
        return minecraftVersion;
    }

    /**
     * 获取当前运行的NMS版本
     *
     * @return 当前运行的NMS版本
     */
    public static String nmsVersion() {
        return nmsVersion;
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
        //获取游戏版本
        String versionStr = Bukkit.getBukkitVersion();
        versionStr = versionStr.substring(0, versionStr.indexOf("-"));
        String[] split = versionStr.split("\\.");
        minecraftVersion = 0;
        minecraftVersion += (Integer.parseInt(split[0]) * 10000);
        minecraftVersion += (Integer.parseInt(split[1]) * 100);
        if (split.length > 2)
            minecraftVersion += Integer.parseInt(split[2]);

        //获取NMS的版本
        nmsVersion = Bukkit.getServer().getClass().getPackage().getName().substring(Bukkit.getServer().getClass().getPackage().getName().lastIndexOf('.') + 1);
    }

    @NotNull
    public static CommandManager commandManager() {
        return commandManager;
    }

    @NotNull
    public static PermManager permissionManager() {
        return PERM_MANAGER;
    }

}
