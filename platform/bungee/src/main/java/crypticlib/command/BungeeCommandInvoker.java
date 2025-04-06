package crypticlib.command;

import crypticlib.chat.BungeeMsgSender;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class BungeeCommandInvoker implements CommandInvoker {

    private final CommandSender platformInvoker;

    public BungeeCommandInvoker(CommandSender platformInvoker) {
        this.platformInvoker = platformInvoker;
    }

    @Override
    public Object getPlatformInvoker() {
        return platformInvoker;
    }

    @Override
    public @NotNull String getName() {
        return platformInvoker.getName();
    }

    @Override
    public void sendMsg(String msg, Map<String, String> replaceMap) {
        BungeeMsgSender.INSTANCE.sendMsg(platformInvoker, msg, replaceMap);
    }

    @Override
    public boolean hasPermission(String permission) {
        return platformInvoker.hasPermission(permission);
    }

    @Override
    public boolean isPlayer() {
        return platformInvoker instanceof ProxiedPlayer;
    }

    @Override
    public boolean isConsole() {
        return !isPlayer();
    }

    @Override
    public PlayerCommandInvoker asPlayer() {
        if (!isPlayer()) {
            throw new ClassCastException("CommandInvoker is not a Player");
        }
        if (this instanceof BungeePlayerCommandInvoker) {
            return (PlayerCommandInvoker) this;
        }
        return new BungeePlayerCommandInvoker((ProxiedPlayer) platformInvoker);
    }

}
