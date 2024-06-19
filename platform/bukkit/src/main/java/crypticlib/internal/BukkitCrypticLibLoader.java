package crypticlib.internal;

import crypticlib.CrypticLib;
import crypticlib.impl.command.BukkitCommandManager;
import crypticlib.impl.scheduler.BukkitScheduler;
import crypticlib.impl.scheduler.FoliaScheduler;
import crypticlib.internal.annotation.PlatformSide;

@PlatformSide(platform = Platform.BUKKIT)
public class BukkitCrypticLibLoader implements ICrypticLibLoader {
    @Override
    public void loadPlatform() {
        Platform.setCurrent(Platform.BUKKIT);
    }

    @Override
    public void loadCommandManager() {
        CrypticLib.setCommandManager(BukkitCommandManager.INSTANCE);
    }

    @Override
    public void loadPlatformAdapter() {
        //TODO
    }

    @Override
    public void loadScheduler() {
        //加载插件任务调度器
        try {
            Class<?> pluginMetaClass = Class.forName("io.papermc.paper.plugin.configuration.PluginMeta");
            pluginMetaClass.getMethod("isFoliaSupported");
            CrypticLib.setScheduler(FoliaScheduler.INSTANCE);
        } catch (ClassNotFoundException | NoSuchMethodException e) {
            CrypticLib.setScheduler(BukkitScheduler.INSTANCE);
        }
    }
}
