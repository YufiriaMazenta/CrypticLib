package crypticlib.platform.util;

import crypticlib.impl.command.BukkitCommandSender;
import crypticlib.internal.Platform;
import crypticlib.internal.annotation.PlatformSide;
import org.bukkit.command.CommandSender;

@PlatformSide(platform = Platform.BUKKIT)
public class BukkitUtils {

    public static BukkitCommandSender wrapCommandSender(CommandSender sender) {
        return new BukkitCommandSender(sender);
    }

}
