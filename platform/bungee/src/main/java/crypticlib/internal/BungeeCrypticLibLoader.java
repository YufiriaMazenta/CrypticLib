package crypticlib.internal;

import crypticlib.CrypticLib;
import crypticlib.impl.command.BungeeCommandManager;
import crypticlib.impl.schduler.BungeeScheduler;

public class BungeeCrypticLibLoader implements ICrypticLibLoader {

    @Override
    public void loadPlatform() {
        Platform.setCurrent(Platform.BUNGEE);
    }

    @Override
    public void loadCommandManager() {
        CrypticLib.setCommandManager(BungeeCommandManager.INSTANCE);
    }

    @Override
    public void loadPlatformAdapter() {
        //TODO
    }

    @Override
    public void loadScheduler() {
        CrypticLib.setScheduler(BungeeScheduler.INSTANCE);
    }
}
