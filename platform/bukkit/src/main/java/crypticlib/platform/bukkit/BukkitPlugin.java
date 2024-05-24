package crypticlib.platform.bukkit;

import crypticlib.CrypticLib;
import crypticlib.impl.command.BukkitCommandManager;
import crypticlib.internal.Platform;
import crypticlib.internal.PlatformPlugin;
import crypticlib.internal.PluginUtil;
import crypticlib.internal.annotation.PlatformSide;
import crypticlib.internal.reflect.PluginScanner;
import crypticlib.impl.scheduler.BukkitScheduler;
import crypticlib.impl.scheduler.FoliaScheduler;
import org.bukkit.plugin.java.JavaPlugin;

@PlatformSide(platform = Platform.BUKKIT)
public class BukkitPlugin extends JavaPlugin implements PlatformPlugin {

    public static BukkitPlugin INSTANCE;

    @Override
    public void onLoad() {
        INSTANCE = this;

        //加载插件任务调度器
        try {
            Class<?> pluginMetaClass = Class.forName("io.papermc.paper.plugin.configuration.PluginMeta");
            pluginMetaClass.getMethod("isFoliaSupported");
            CrypticLib.setScheduler(FoliaScheduler.INSTANCE);
        } catch (ClassNotFoundException | NoSuchMethodException e) {
            CrypticLib.setScheduler(BukkitScheduler.INSTANCE);
        }
        Platform.setCurrent(Platform.BUKKIT);
        CrypticLib.setCommandManager(BukkitCommandManager.INSTANCE);

        PluginScanner.INSTANCE.scanJar(this.getFile());

        PluginUtil.initPluginImpl(getFile(), getDataFolder(), getLogger(), getClassLoader());
        PluginUtil.loadPluginImpl();
    }

    @Override
    public void onEnable() {
        regCommands();
        regListeners();
        PluginUtil.enablePluginImpl();
    }

    @Override
    public void onDisable() {
        PluginUtil.disablePluginImpl();
    }


    @Override
    public void regListeners() {

    }

}
