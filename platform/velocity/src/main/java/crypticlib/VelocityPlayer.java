package crypticlib;

import com.velocitypowered.api.proxy.Player;
import crypticlib.chat.VelocityTextProcessor;
import crypticlib.command.VelocityCommandInvoker;
import crypticlib.util.StringHelper;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import net.kyori.adventure.util.Ticks;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;
import java.util.Map;
import java.util.UUID;

public class VelocityPlayer extends VelocityCommandInvoker implements CommonPlayer {

    protected @NotNull Player platformPlayer;

    public VelocityPlayer(@NotNull Player platformPlayer) {
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
        return platformPlayer.getPlayerSettings().getLocale();
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
        platformPlayer.showTitle(titleObj);
    }

    @Override
    public void sendActionBar(String text, Map<String, String> replaceMap) {
        if (text == null)
            return;
        text = StringHelper.replaceStrings(text, replaceMap);
        Component component = VelocityTextProcessor.deserializeLegacyText(text);
        platformPlayer.sendActionBar(component != null ? component : Component.text(""));
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
    public CommonPlayer asPlayer() {
        return this;
    }

}
