package crypticlib.command;

import crypticlib.BukkitPlayer;
import crypticlib.CommonPlayer;
import crypticlib.chat.BukkitTextProcessor;
import crypticlib.util.StringHelper;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class BukkitCommandInvoker implements CommandInvoker {

    protected @NotNull CommandSender platformInvoker;

    public BukkitCommandInvoker(@NotNull CommandSender platformInvoker) {
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
        if (platformInvoker instanceof Player)
            msg = BukkitTextProcessor.placeholder((Player) platformInvoker, msg);
        platformInvoker.spigot().sendMessage(BukkitTextProcessor.toComponent(BukkitTextProcessor.color(msg)));
    }

    @Override
    public boolean hasPermission(String permission) {
        return platformInvoker.hasPermission(permission);
    }

    @Override
    public boolean isPlayer() {
        return platformInvoker instanceof Player;
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
        if (this instanceof BukkitPlayer) {
            return (CommonPlayer) this;
        }
        return new BukkitPlayer((Player) platformInvoker);
    }

}
