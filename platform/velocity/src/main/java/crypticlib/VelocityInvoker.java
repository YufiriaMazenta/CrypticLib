package crypticlib;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.ConsoleCommandSource;
import com.velocitypowered.api.proxy.Player;
import crypticlib.chat.VelocityTextProcessor;
import crypticlib.util.StringHelper;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class VelocityInvoker implements Invoker {

    protected final CommandSource platformInvoker;

    @ApiStatus.Internal
    protected VelocityInvoker(CommandSource platformInvoker) {
        this.platformInvoker = platformInvoker;
    }

    @Override
    public @NotNull Object getPlatformInvoker() {
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
        return platformInvoker instanceof ConsoleCommandSource;
    }

    @Override
    public CommonPlayer asPlayer() {
        if (!isPlayer()) {
            throw new ClassCastException("Invoker is not a Player");
        }
        if (this instanceof VelocityPlayer) {
            return (CommonPlayer) this;
        }
        return VelocityPlayer.byPlayer((Player) platformInvoker);
    }

    @Override
    public InvokerType invokerType() {
        if (platformInvoker instanceof Player) {
            return InvokerType.PLAYER;
        } else {
            return InvokerType.CONSOLE;
        }
    }

    public static VelocityInvoker byCommandSource(CommandSource commandSource) {
        return new VelocityInvoker(commandSource);
    }

}
