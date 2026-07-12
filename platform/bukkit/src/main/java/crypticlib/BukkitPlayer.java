package crypticlib;

import crypticlib.chat.BukkitTextProcessor;
import crypticlib.util.LocaleHelper;
import crypticlib.util.StringHelper;
import net.md_5.bungee.api.ChatMessageType;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;
import java.util.Map;

public class BukkitPlayer extends BukkitEntity implements CommonPlayer {

    @ApiStatus.Internal
    protected BukkitPlayer(@NotNull Player platformPlayer) {
        super(platformPlayer);
    }

    @Override
    public @NotNull Locale getLocale() {
        Player player = Bukkit.getPlayer(uuid);
        if (player != null) {
            return LocaleHelper.languageTag2Local(player.getLocale());
        }
        return Locale.getDefault();
    }

    @Override
    public void sendTitle(@Nullable String title, @Nullable String subtitle, int fadeIn, int stay, int fadeOut, @Nullable Map<String, String> replaceMap) {
        title = title != null ? title : "";
        subtitle = subtitle != null ? subtitle : "";
        Player player = Bukkit.getPlayer(uuid);
        if (player == null) {
            return;
        }
        title = StringHelper.replaceStrings(title, replaceMap);
        subtitle = StringHelper.replaceStrings(subtitle, replaceMap);
        title = BukkitTextProcessor.color(BukkitTextProcessor.placeholder(player, title));
        subtitle = BukkitTextProcessor.color(BukkitTextProcessor.placeholder(player, subtitle));
        player.sendTitle(title, subtitle, fadeIn, stay, fadeOut);
    }

    @Override
    public void sendActionBar(String text, Map<String, String> replaceMap) {
        Player player = Bukkit.getPlayer(uuid);
        if (player == null) {
            return;
        }
        text = StringHelper.replaceStrings(text, replaceMap);
        text = BukkitTextProcessor.color(BukkitTextProcessor.placeholder(player, text));
        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, BukkitTextProcessor.toComponent(text));
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

    public static BukkitPlayer byPlayer(Player player) {
        return new BukkitPlayer(player);
    }

}
