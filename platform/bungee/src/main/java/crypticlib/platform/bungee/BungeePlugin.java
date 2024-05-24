package crypticlib.platform.bungee;

import crypticlib.CrypticLib;
import crypticlib.impl.command.BungeeCommandManager;
import crypticlib.internal.Platform;
import crypticlib.internal.PlatformPlugin;
import crypticlib.internal.PluginUtil;
import crypticlib.internal.annotation.PlatformSide;
import crypticlib.internal.reflect.PluginScanner;
import crypticlib.impl.schduler.BungeeScheduler;
import net.md_5.bungee.api.plugin.Plugin;

@PlatformSide(platform = Platform.BUNGEE)
public class BungeePlugin extends Plugin implements PlatformPlugin {

    public static BungeePlugin INSTANCE;

    @Override
    public void onLoad() {
        INSTANCE = this;

        Platform.setCurrent(Platform.BUNGEE);
        CrypticLib.setScheduler(BungeeScheduler.INSTANCE);
        CrypticLib.setCommandManager(BungeeCommandManager.INSTANCE);

        PluginScanner.INSTANCE.scanJar(getFile());

        PluginUtil.initPluginImpl(getFile(), getDataFolder(), getLogger(), getClass().getClassLoader());
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
