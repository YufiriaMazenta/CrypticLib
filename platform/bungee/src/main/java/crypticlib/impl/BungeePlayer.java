package crypticlib.impl;

import crypticlib.api.IPlayer;
import crypticlib.impl.command.BungeeCommandSender;
import crypticlib.internal.Platform;
import crypticlib.internal.annotation.PlatformSide;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import org.jetbrains.annotations.NotNull;

@PlatformSide(platform = Platform.BUNGEE)
public class BungeePlayer extends BungeeCommandSender implements IPlayer {

    private final ProxiedPlayer originPlayer;

    public BungeePlayer(@NotNull ProxiedPlayer originPlayer) {
        super(originPlayer);
        this.originPlayer = originPlayer;
    }

    @Override
    public ProxiedPlayer getPlatformPlayer() {
        return originPlayer;
    }
}
