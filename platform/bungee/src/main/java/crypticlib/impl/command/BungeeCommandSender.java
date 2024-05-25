package crypticlib.impl.command;

import crypticlib.api.command.ICommandSender;
import crypticlib.internal.Platform;
import crypticlib.internal.annotation.PlatformSide;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.StringJoiner;
import java.util.UUID;

@PlatformSide(platform = Platform.BUNGEE)
public class BungeeCommandSender implements ICommandSender {

    private final CommandSender originCommandSender;

    public BungeeCommandSender(@NotNull CommandSender originCommandSender) {
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
        StringJoiner stringJoiner = new StringJoiner("");
        for (String s : message) {
            stringJoiner.add(s);
        }
        originCommandSender.sendMessage(stringJoiner.toString());
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
        originCommandSender.sendMessage(message);
    }

    @Override
    public void sendMessage(BaseComponent... message) {
        originCommandSender.sendMessage(message);
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
    public boolean hasPermission(String permission) {
        return originCommandSender.hasPermission(permission);
    }

    @Override
    public boolean isOp() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void setOp(boolean value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean dispatchCommand(String command) {
        return ProxyServer.getInstance().getPluginManager().dispatchCommand(originCommandSender, command);
    }

    @Override
    public CommandSender getPlatformCommandSender() {
        return originCommandSender;
    }

}
