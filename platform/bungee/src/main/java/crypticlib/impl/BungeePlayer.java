package crypticlib.impl;

import crypticlib.api.IPlayer;
import crypticlib.impl.command.BungeeCommandSender;
import crypticlib.internal.Platform;
import crypticlib.internal.annotation.PlatformSide;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import org.jetbrains.annotations.NotNull;

import java.net.InetSocketAddress;
import java.util.UUID;

@PlatformSide(platform = Platform.BUNGEE)
public class BungeePlayer extends BungeeCommandSender implements IPlayer {

    private final ProxiedPlayer originPlayer;

    public BungeePlayer(@NotNull ProxiedPlayer originPlayer) {
        super(originPlayer);
        this.originPlayer = originPlayer;
    }

    @Override
    public UUID getUniqueId() {
        return null;
    }

    @Override
    public String getDisplayName() {
        return "";
    }

    @Override
    public String getLocale() {
        return "";
    }

    @Override
    public InetSocketAddress getAddress() {
        return null;
    }

    @Override
    public int getViewDistance() {
        return 0;
    }

    @Override
    public void sendTitle(String title, String subTitle, int fadeIn, int stay, int fadeOut) {

    }

    @Override
    public void sendActionBar(String text) {

    }

    @Override
    public ProxiedPlayer getPlatformPlayer() {
        return originPlayer;
    }
}
