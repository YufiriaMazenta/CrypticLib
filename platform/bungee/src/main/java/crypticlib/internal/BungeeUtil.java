package crypticlib.internal;

import crypticlib.CrypticLib;
import crypticlib.internal.annotation.PlatformSide;
import net.md_5.bungee.api.plugin.Plugin;

@PlatformSide(platform = Platform.BUNGEE)
public class BungeeUtil {

    public static Plugin getPluginIns() {
        return (Plugin) CrypticLib.getPluginIns();
    }

}
