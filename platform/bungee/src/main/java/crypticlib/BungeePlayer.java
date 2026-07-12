package crypticlib;

import crypticlib.chat.BungeeTextProcessor;
import crypticlib.util.StringHelper;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;
import java.util.Map;
import java.util.UUID;

public class BungeePlayer extends BungeeInvoker implements CommonPlayer {

    protected @NotNull UUID playerId;

    @ApiStatus.Internal
    protected BungeePlayer(@NotNull ProxiedPlayer platformPlayer) {
        super(platformPlayer);
        this.playerId = platformPlayer.getUniqueId();
    }

    @Override
    public @NotNull UUID getUniqueId() {
        return playerId;
    }

    @Override
    public @NotNull Locale getLocale() {
        ProxiedPlayer player = ProxyServer.getInstance().getPlayer(playerId);
        if (player != null) {
            return player.getLocale();
        }
        return Locale.getDefault();
    }

    @Override
    public void sendTitle(@Nullable String title, @Nullable String subtitle, int fadeIn, int stay, int fadeOut, @Nullable Map<String, String> replaceMap) {
        ProxiedPlayer player = ProxyServer.getInstance().getPlayer(playerId);
        if (player == null) {
            return;
        }
        title = title != null ? title : "";
        subtitle = subtitle != null ? subtitle : "";
        title = StringHelper.replaceStrings(title, replaceMap);
        subtitle = StringHelper.replaceStrings(subtitle, replaceMap);
        title = BungeeTextProcessor.color(title);
        subtitle = BungeeTextProcessor.color(subtitle);
        ProxyServer.getInstance().createTitle()
            .title(BungeeTextProcessor.toComponent(title))
            .subTitle(BungeeTextProcessor.toComponent(subtitle))
            .fadeIn(fadeIn)
            .fadeOut(fadeOut)
            .stay(stay)
            .send(player);
    }

    @Override
    public void sendActionBar(String text, Map<String, String> replaceMap) {
        ProxiedPlayer player = ProxyServer.getInstance().getPlayer(playerId);
        if (player == null || text == null) {
            return;
        }
        text = StringHelper.replaceStrings(text, replaceMap);
        text = BungeeTextProcessor.color(text);
        BaseComponent component = BungeeTextProcessor.toComponent(text);
        player.sendMessage(ChatMessageType.ACTION_BAR, component);
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
    public CommonPlayer asPlayer() {
        return this;
    }

    @Override
    public InvokerType invokerType() {
        return InvokerType.PLAYER;
    }

    public static BungeePlayer byProxiedPlayer(ProxiedPlayer player) {
        return new BungeePlayer(player);
    }

}
