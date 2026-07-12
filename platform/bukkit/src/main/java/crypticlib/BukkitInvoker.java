package crypticlib;

import crypticlib.chat.BukkitTextProcessor;
import crypticlib.util.StringHelper;
import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.CommandException;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class BukkitInvoker implements Invoker {

    protected @NotNull CommandSender platformInvoker;

    @ApiStatus.Internal
    protected BukkitInvoker(@NotNull CommandSender platformInvoker) {
        this.platformInvoker = platformInvoker;
    }

    @Override
    public @NotNull Object getPlatformInvoker() {
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
        return platformInvoker instanceof ConsoleCommandSender;
    }

    @Override
    public CommonPlayer asPlayer() {
        if (!isPlayer()) {
            throw new ClassCastException("Invoker is not a Player");
        }
        if (this instanceof BukkitPlayer) {
            return (CommonPlayer) this;
        }
        return BukkitPlayer.byPlayer((Player) platformInvoker);
    }

    public static BukkitInvoker byCommandSender(CommandSender commandSender) {
        return new BukkitInvoker(commandSender);
    }

    @Override
    public InvokerType invokerType() {
        if (platformInvoker instanceof Player) {
            return InvokerType.PLAYER;
        } else if (platformInvoker instanceof ConsoleCommandSender) {
            return InvokerType.CONSOLE;
        } else if (platformInvoker instanceof BlockCommandSender) {
            return InvokerType.BLOCK;
        } else if (platformInvoker instanceof Entity) {
            return InvokerType.ENTITY;
        }
        throw new CommandException("Unknown invoker type: " + platformInvoker.getClass().getName());
    }

}
