package crypticlib.command;

import crypticlib.chat.BungeeMsgSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.UUID;

public class BungeePlayerCommandInvoker extends BungeeCommandInvoker implements PlayerCommandInvoker {

    protected ProxiedPlayer platformPlayer;

    public BungeePlayerCommandInvoker(ProxiedPlayer platformPlayer) {
        super(platformPlayer);
        this.platformPlayer = platformPlayer;
    }

    @Override
    public Object getPlatformPlayer() {
        return platformPlayer;
    }

    @Override
    public UUID getUniqueId() {
        return platformPlayer.getUniqueId();
    }

    @Override
    public void sendTitle(@Nullable String title, @Nullable String subtitle, int fadeIn, int stay, int fadeOut, @Nullable Map<String, String> replaceMap) {
        BungeeMsgSender.INSTANCE.sendTitle(platformPlayer, title, subtitle, fadeIn, stay, fadeOut, replaceMap);
    }

    @Override
    public void sendActionBar(String text, Map<String, String> replaceMap) {
        BungeeMsgSender.INSTANCE.sendActionBar(platformPlayer, text, replaceMap);
    }

    @Override
    public boolean isPlayer() {
        return true;
    }

    @Override
    public boolean isConsole() {
        return false;
    }

    @Override
    public PlayerCommandInvoker asPlayer() {
        return this;
    }

}
