package crypticlib.impl.command;

import crypticlib.api.command.ICommandSender;
import crypticlib.internal.Platform;
import crypticlib.internal.annotation.PlatformSide;
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.UUID;

@PlatformSide(platform = Platform.BUKKIT)
public class BukkitCommandSender implements ICommandSender {

    private final CommandSender originCommandSender;

    public BukkitCommandSender(@NotNull CommandSender originCommandSender) {
        this.originCommandSender = Objects.requireNonNull(originCommandSender);
    }

    @Override
    public String getName() {
        return originCommandSender.getName();
    }

    @Override
    public void sendMessage(String message) {
        originCommandSender.sendMessage(message);
    }

    @Override
    public void sendMessage(String... message) {
        originCommandSender.sendMessage(message);
    }

    @Override
    public void sendMessage(UUID uuid, String message) {
        originCommandSender.sendMessage(uuid, message);
    }

    @Override
    public void sendMessage(UUID uuid, String... message) {
        originCommandSender.sendMessage(uuid, message);
    }

    @Override
    public void sendMessage(BaseComponent message) {
        originCommandSender.spigot().sendMessage(message);
    }

    @Override
    public void sendMessage(BaseComponent... message) {
        originCommandSender.spigot().sendMessage(message);
    }

    @Override
    public void sendMessage(UUID uuid, BaseComponent message) {
        originCommandSender.spigot().sendMessage(uuid, message);
    }

    @Override
    public void sendMessage(UUID uuid, BaseComponent... message) {
        originCommandSender.spigot().sendMessage(uuid, message);
    }

    @Override
    public boolean hasPermission(String permission) {
        return originCommandSender.hasPermission(permission);
    }

    @Override
    public boolean isOp() {
        return originCommandSender.isOp();
    }

    @Override
    public void setOp(boolean value) {
        originCommandSender.setOp(value);
    }

    @Override
    public boolean dispatchCommand(String command) {
        return Bukkit.getServer().dispatchCommand(originCommandSender, command);
    }

    @Override
    public CommandSender getPlatformCommandSender() {
        return originCommandSender;
    }
    
}
