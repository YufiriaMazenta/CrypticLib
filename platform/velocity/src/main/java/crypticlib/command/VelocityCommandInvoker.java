package crypticlib.command;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.ConsoleCommandSource;
import com.velocitypowered.api.proxy.Player;
import crypticlib.chat.VelocityMsgSender;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class VelocityCommandInvoker implements CommandInvoker {

    protected CommandSource platformInvoker;

    public VelocityCommandInvoker(CommandSource platformInvoker) {
        this.platformInvoker = platformInvoker;
    }

    @Override
    public Object getPlatformInvoker() {
        return platformInvoker;
    }

    @Override
    public @NotNull String getName() {
        throw new UnsupportedOperationException("Velocity command source do not has name");
    }

    @Override
    public void sendMsg(String msg, Map<String, String> replaceMap) {
        VelocityMsgSender.INSTANCE.sendMsg(platformInvoker, msg, replaceMap);
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
        return platformInvoker instanceof ConsoleCommandSource;
    }

    @Override
    public PlayerCommandInvoker asPlayer() {
        if (!isPlayer()) {
            throw new ClassCastException("CommandInvoker is not a Player");
        }
        if (this instanceof VelocityPlayerCommandInvoker) {
            return (PlayerCommandInvoker) this;
        }
        return new VelocityPlayerCommandInvoker((Player) platformInvoker);
    }

}
