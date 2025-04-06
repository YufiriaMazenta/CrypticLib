package crypticlib.command;

import com.velocitypowered.api.proxy.Player;
import crypticlib.chat.VelocityMsgSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.UUID;

public class VelocityPlayerCommandInvoker extends VelocityCommandInvoker implements PlayerCommandInvoker {

    protected @NotNull Player platformPlayer;

    public VelocityPlayerCommandInvoker(@NotNull Player platformPlayer) {
        super(platformPlayer);
        this.platformPlayer = platformPlayer;
    }

    @Override
    public Object getPlatformPlayer() {
        return platformPlayer;
    }

    @Override
    public UUID getUniqueId() {
        return platformPlayer.getUniqueId();
    }

    @Override
    public void sendTitle(@Nullable String title, @Nullable String subtitle, int fadeIn, int stay, int fadeOut, @Nullable Map<String, String> replaceMap) {
        VelocityMsgSender.INSTANCE.sendTitle(platformPlayer, title, subtitle, fadeIn, stay, fadeOut, replaceMap);
    }

    @Override
    public void sendActionBar(String text, Map<String, String> replaceMap) {
        VelocityMsgSender.INSTANCE.sendActionBar(platformPlayer, text, replaceMap);
    }

    @Override
    public @NotNull String getName() {
        return platformPlayer.getUsername();
    }

    @Override
    public boolean isPlayer() {
        return true;
    }

    @Override
    public boolean isConsole() {
        return false;
    }

    @Override
    public PlayerCommandInvoker asPlayer() {
        return this;
    }

}
