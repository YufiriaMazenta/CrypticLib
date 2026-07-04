package crypticlib.command;

import crypticlib.BungeePlayer;
import crypticlib.CommonPlayer;
import crypticlib.chat.BungeeTextProcessor;
import crypticlib.util.StringHelper;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class BungeeCommandInvoker implements CommandInvoker {

    protected final CommandSender platformInvoker;

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
        if (msg == null)
            return;
        msg = StringHelper.replaceStrings(msg, replaceMap);
        platformInvoker.sendMessage(BungeeTextProcessor.toComponent(BungeeTextProcessor.color(msg)));
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
    public CommonPlayer asPlayer() {
        if (!isPlayer()) {
            throw new ClassCastException("CommandInvoker is not a Player");
        }
        if (this instanceof BungeePlayer) {
            return (CommonPlayer) this;
        }
        return new BungeePlayer((ProxiedPlayer) platformInvoker);
    }

}
