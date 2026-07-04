package crypticlib;

import crypticlib.chat.BungeeTextProcessor;
import crypticlib.command.BungeeCommandInvoker;
import crypticlib.util.StringHelper;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;
import java.util.Map;
import java.util.UUID;

public class BungeePlayer extends BungeeCommandInvoker implements CommonPlayer {

    protected ProxiedPlayer platformPlayer;

    public BungeePlayer(@NotNull ProxiedPlayer platformPlayer) {
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
    public Locale getLocale() {
        return platformPlayer.getLocale();
    }

    @Override
    public void sendTitle(@Nullable String title, @Nullable String subtitle, int fadeIn, int stay, int fadeOut, @Nullable Map<String, String> replaceMap) {
        if (title == null) {
            title = "";
        }
        if (subtitle == null) {
            subtitle = "";
        }
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
            .send(platformPlayer);
    }

    @Override
    public void sendActionBar(String text, Map<String, String> replaceMap) {
        if (text == null)
            return;
        text = StringHelper.replaceStrings(text, replaceMap);
        text = BungeeTextProcessor.color(text);
        BaseComponent component = BungeeTextProcessor.toComponent(text);
        platformPlayer.sendMessage(ChatMessageType.ACTION_BAR, component);
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

}
