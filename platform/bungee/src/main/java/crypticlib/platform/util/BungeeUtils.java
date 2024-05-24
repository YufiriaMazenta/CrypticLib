package crypticlib.platform.util;

import crypticlib.impl.command.BungeeWrappedCommandSender;
import crypticlib.internal.Platform;
import crypticlib.internal.annotation.PlatformSide;
import net.md_5.bungee.api.CommandSender;

@PlatformSide(platform = Platform.BUNGEE)
public class BungeeUtils {

    public static BungeeWrappedCommandSender wrapCommandSender(CommandSender sender) {
        return new BungeeWrappedCommandSender(sender);
    }

}
