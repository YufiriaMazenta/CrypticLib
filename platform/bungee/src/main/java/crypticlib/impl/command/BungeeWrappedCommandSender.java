package crypticlib.impl.command;

import crypticlib.api.command.WrappedCommandSender;
import crypticlib.internal.Platform;
import crypticlib.internal.annotation.PlatformSide;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.StringJoiner;
import java.util.UUID;

@PlatformSide(platform = Platform.BUNGEE)
public class BungeeWrappedCommandSender implements WrappedCommandSender<CommandSender> {

    private final CommandSender bungee;

    public BungeeWrappedCommandSender(@NotNull CommandSender bungee) {
        this.bungee = Objects.requireNonNull(bungee);
    }

    @Override
    public void sendMessage(String message) {
        bungee.sendMessage(message);
    }

    @Override
    public void sendMessage(String... message) {
        StringJoiner stringJoiner = new StringJoiner("");
        for (String s : message) {
            stringJoiner.add(s);
        }
        bungee.sendMessage(stringJoiner.toString());
    }

    @Override
    public void sendMessage(UUID uuid, String message) {
        sendMessage(message);
    }

    @Override
    public void sendMessage(UUID uuid, String... message) {
        sendMessage(message);
    }

    @Override
    public void sendMessage(BaseComponent message) {
        bungee.sendMessage(message);
    }

    @Override
    public void sendMessage(BaseComponent... message) {
        bungee.sendMessage(message);
    }

    @Override
    public void sendMessage(UUID uuid, BaseComponent message) {
        sendMessage(message);
    }

    @Override
    public void sendMessage(UUID uuid, BaseComponent... message) {
        sendMessage(message);
    }

    @Override
    public boolean isOp() {
        return isConsole();
    }

    @Override
    public void setOp(boolean value) {
        throw new UnsupportedOperationException("Bungee command sender cannot set OP");
    }

    @Override
    public boolean hasPermission(String permission) {
        return bungee.hasPermission(permission);
    }

    @Override
    public boolean isConsole() {
        return !isPlayer();
    }

    @Override
    public boolean isPlayer() {
        return bungee instanceof ProxiedPlayer;
    }

    @Override
    public CommandSender getPlatformSender() {
        return bungee;
    }

}
