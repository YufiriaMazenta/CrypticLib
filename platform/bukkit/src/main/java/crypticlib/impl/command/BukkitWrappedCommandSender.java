package crypticlib.impl.command;

import crypticlib.api.command.WrappedCommandSender;
import crypticlib.internal.Platform;
import crypticlib.internal.annotation.PlatformSide;
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.UUID;

@PlatformSide(platform = Platform.BUKKIT)
public class BukkitWrappedCommandSender implements WrappedCommandSender<CommandSender> {

    private final CommandSender bukkit;

    public BukkitWrappedCommandSender(@NotNull CommandSender bukkit) {
        this.bukkit = Objects.requireNonNull(bukkit);
    }

    @Override
    public void sendMessage(String message) {
        bukkit.sendMessage(message);
    }

    @Override
    public void sendMessage(String... message) {
        bukkit.sendMessage(message);
    }

    @Override
    public void sendMessage(UUID uuid, String message) {
        bukkit.sendMessage(uuid, message);
    }

    @Override
    public void sendMessage(UUID uuid, String... message) {
        bukkit.sendMessage(uuid, message);
    }

    @Override
    public void sendMessage(BaseComponent message) {
        bukkit.spigot().sendMessage(message);
    }

    @Override
    public void sendMessage(BaseComponent... message) {
        bukkit.spigot().sendMessage(message);
    }

    @Override
    public void sendMessage(UUID uuid, BaseComponent message) {
        bukkit.spigot().sendMessage(uuid, message);
    }

    @Override
    public void sendMessage(UUID uuid, BaseComponent... message) {
        bukkit.spigot().sendMessage(uuid, message);
    }

    @Override
    public boolean isOp() {
        return bukkit.isOp();
    }

    @Override
    public void setOp(boolean value) {
        bukkit.setOp(value);
    }

    @Override
    public boolean hasPermission(String permission) {
        return bukkit.hasPermission(permission);
    }

    @Override
    public boolean isConsole() {
        return bukkit instanceof ConsoleCommandSender;
    }

    @Override
    public boolean isPlayer() {
        return bukkit instanceof Player;
    }

    @Override
    public CommandSender getPlatformSender() {
        return bukkit;
    }
    
}
