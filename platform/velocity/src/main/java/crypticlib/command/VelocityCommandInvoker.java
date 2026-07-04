package crypticlib.command;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.ConsoleCommandSource;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import crypticlib.CommonPlayer;
import crypticlib.VelocityPlayer;
import crypticlib.chat.VelocityTextProcessor;
import crypticlib.util.StringHelper;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class VelocityCommandInvoker implements CommandInvoker {

    protected final CommandSource platformInvoker;

    @ApiStatus.Internal
    protected VelocityCommandInvoker(CommandSource platformInvoker) {
        this.platformInvoker = platformInvoker;
    }

    @Override
    public Object getPlatformInvoker() {
        return platformInvoker;
    }

    @Override
    public @NotNull String getName() {
        if (platformInvoker instanceof Player) {
            return ((Player) platformInvoker).getUsername();
        }
        return "Server";
    }

    @Override
    public void sendMsg(String msg, Map<String, String> replaceMap) {
        if (msg == null)
            return;
        msg = StringHelper.replaceStrings(msg, replaceMap);
        Component component = VelocityTextProcessor.deserializeLegacyText(msg);
        if (component != null) {
            platformInvoker.sendMessage(component);
        }
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
        if (this instanceof VelocityPlayer) {
            return (CommonPlayer) this;
        }
        return VelocityPlayer.byPlayer((Player) platformInvoker);
    }

    public static VelocityCommandInvoker byCommandSource(CommandSource commandSource) {
        return new VelocityCommandInvoker(commandSource);
    }

}
