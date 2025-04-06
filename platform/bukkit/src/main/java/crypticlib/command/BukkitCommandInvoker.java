package crypticlib.command;

import crypticlib.chat.BukkitMsgSender;
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
        BukkitMsgSender.INSTANCE.sendMsg(platformInvoker, msg, replaceMap);
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
    public PlayerCommandInvoker asPlayer() {
        if (!isPlayer()) {
            throw new ClassCastException("CommandInvoker is not a Player");
        }
        if (this instanceof BukkitPlayerCommandInvoker) {
            return (PlayerCommandInvoker) this;
        }
        return new BukkitPlayerCommandInvoker((Player) platformInvoker);
    }

}
