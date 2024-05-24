package crypticlib.platform.util;

import crypticlib.impl.command.BukkitWrappedCommandSender;
import crypticlib.internal.Platform;
import crypticlib.internal.annotation.PlatformSide;
import org.bukkit.command.CommandSender;

@PlatformSide(platform = Platform.BUKKIT)
public class BukkitUtils {

    public static BukkitWrappedCommandSender wrapCommandSender(CommandSender sender) {
        return new BukkitWrappedCommandSender(sender);
    }

}
