package crypticlib.internal;

import crypticlib.CrypticLib;
import org.bukkit.plugin.Plugin;

public class BukkitUtil {

    public static Plugin getPluginIns() {
        return (Plugin) CrypticLib.getPluginIns();
    }

}
