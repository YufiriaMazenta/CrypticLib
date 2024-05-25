package crypticlib.impl;

import crypticlib.api.IPlayer;
import crypticlib.impl.command.BukkitCommandSender;
import crypticlib.internal.Platform;
import crypticlib.internal.annotation.PlatformSide;
import org.bukkit.entity.Player;

import java.net.InetSocketAddress;
import java.util.UUID;

@PlatformSide(platform = Platform.BUKKIT)
public class BukkitPlayer extends BukkitCommandSender implements IPlayer {

    private final Player originPlayer;

    public BukkitPlayer(Player originPlayer) {
        super(originPlayer);
        this.originPlayer = originPlayer;
    }

    @Override
    public UUID getUniqueId() {
        return originPlayer.getUniqueId();
    }

    @Override
    public String getDisplayName() {
        return originPlayer.getDisplayName();
    }

    @Override
    public String getLocale() {
        return originPlayer.getLocale();
    }

    @Override
    public InetSocketAddress getAddress() {
        return originPlayer.getAddress();
    }

    @Override
    public int getViewDistance() {
        return originPlayer.getViewDistance();
    }

    @Override
    public void sendTitle(String title, String subTitle, int fadeIn, int stay, int fadeOut) {
        originPlayer.sendTitle(title, subTitle, fadeIn, stay, fadeOut);
    }

    @Override
    public void sendActionBar(String text) {
        originPlayer.spigot().sendMessage();
    }

    @Override
    public Player getPlatformPlayer() {
        return originPlayer;
    }

}
