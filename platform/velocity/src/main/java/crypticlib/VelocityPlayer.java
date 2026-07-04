package crypticlib;

import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import crypticlib.chat.VelocityTextProcessor;
import crypticlib.command.VelocityCommandInvoker;
import crypticlib.util.ReflectionHelper;
import crypticlib.util.StringHelper;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import net.kyori.adventure.util.Ticks;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;
import java.util.Map;
import java.util.UUID;

public class VelocityPlayer extends VelocityCommandInvoker implements CommonPlayer {

    protected @NotNull UUID playerId;
    protected @NotNull ProxyServer proxyServer;

    @ApiStatus.Internal
    protected VelocityPlayer(@NotNull Player platformPlayer) {
        super(platformPlayer);
        this.playerId = platformPlayer.getUniqueId();
        this.proxyServer = ((VelocityPlugin) ReflectionHelper.getPluginInstance()).proxyServer;
    }

    @Override
    public Player getPlatformPlayer() {
        return proxyServer.getPlayer(playerId).orElse(null);
    }

    @Override
    public @NotNull UUID getUniqueId() {
        return playerId;
    }

    @Override
    public @NotNull Locale getLocale() {
        Player player = proxyServer.getPlayer(playerId).orElse(null);
        if (player != null) {
            return player.getPlayerSettings().getLocale();
        }
        return Locale.getDefault();
    }

    @Override
    public void sendTitle(@Nullable String title, @Nullable String subtitle, int fadeIn, int stay, int fadeOut, @Nullable Map<String, String> replaceMap) {
        Player player = proxyServer.getPlayer(playerId).orElse(null);
        if (player == null) {
            return;
        }
        title = title != null ? title : "";
        subtitle = subtitle != null ? subtitle : "";
        title = StringHelper.replaceStrings(title, replaceMap);
        subtitle = StringHelper.replaceStrings(subtitle, replaceMap);
        Component titleComponent = VelocityTextProcessor.deserializeLegacyText(title);
        Component subTitleComponent = VelocityTextProcessor.deserializeLegacyText(subtitle);
        Title titleObj = Title.title(
            titleComponent != null ? titleComponent : Component.text(""),
            subTitleComponent != null ? subTitleComponent : Component.text(""),
            Title.Times.times(
                Ticks.duration(fadeIn),
                Ticks.duration(stay),
                Ticks.duration(fadeOut)
            )
        );
        player.showTitle(titleObj);
    }

    @Override
    public void sendActionBar(String text, Map<String, String> replaceMap) {
        Player player = proxyServer.getPlayer(playerId).orElse(null);
        if (player == null || text == null) {
            return;
        }
        text = StringHelper.replaceStrings(text, replaceMap);
        Component component = VelocityTextProcessor.deserializeLegacyText(text);
        player.sendActionBar(component != null ? component : Component.text(""));
    }

    @Override
    public @NotNull String getName() {
        Player player = proxyServer.getPlayer(playerId).orElse(null);
        if (player != null) {
            return player.getUsername();
        }
        return "Unknown";
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

    public static VelocityPlayer byPlayer(Player player) {
        return new VelocityPlayer(player);
    }

}
