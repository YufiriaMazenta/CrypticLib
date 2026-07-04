package crypticlib;

import crypticlib.chat.BukkitTextProcessor;
import crypticlib.command.BukkitCommandInvoker;
import crypticlib.util.LocaleHelper;
import crypticlib.util.StringHelper;
import net.md_5.bungee.api.ChatMessageType;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;
import java.util.Map;
import java.util.UUID;

public class BukkitPlayer extends BukkitCommandInvoker implements CommonPlayer {

    protected @NotNull Player platformPlayer;

    public BukkitPlayer(@NotNull Player platformPlayer) {
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
        return LocaleHelper.languageTag2Local(platformPlayer.getLocale());
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
        title = BukkitTextProcessor.color(BukkitTextProcessor.placeholder(platformPlayer, title));
        subtitle = BukkitTextProcessor.color(BukkitTextProcessor.placeholder(platformPlayer, subtitle));
        platformPlayer.sendTitle(title, subtitle, fadeIn, stay, fadeOut);
    }

    @Override
    public void sendActionBar(String text, Map<String, String> replaceMap) {
        text = StringHelper.replaceStrings(text, replaceMap);
        text = BukkitTextProcessor.color(BukkitTextProcessor.placeholder(platformPlayer, text));
        platformPlayer.spigot().sendMessage(ChatMessageType.ACTION_BAR, BukkitTextProcessor.toComponent(text));
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
