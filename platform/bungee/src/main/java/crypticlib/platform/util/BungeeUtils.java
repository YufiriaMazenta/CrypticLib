package crypticlib.platform.util;

import crypticlib.impl.command.BungeeCommandSender;
import crypticlib.internal.Platform;
import crypticlib.internal.annotation.PlatformSide;
import net.md_5.bungee.api.CommandSender;

@PlatformSide(platform = Platform.BUNGEE)
public class BungeeUtils {

    public static BungeeCommandSender wrapCommandSender(CommandSender sender) {
        return new BungeeCommandSender(sender);
    }

}
