package crypticlib.command;

import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.UUID;

public interface PlayerCommandInvoker extends CommandInvoker {

    Object getPlatformPlayer();

    UUID getUniqueId();

    default void sendTitle(@Nullable String title, @Nullable String subtitle) {
        sendTitle(title, subtitle, 10, 70, 20);
    }

    default void sendTitle(@Nullable String title, @Nullable String subtitle, int fadeIn, int stay, int fadeOut) {
        sendTitle(title, subtitle, fadeIn, stay, fadeOut, null);
    }

    void sendTitle(@Nullable String title, @Nullable String subtitle, int fadeIn, int stay, int fadeOut, @Nullable Map<String, String> replaceMap);

    default void sendActionBar(String text) {
        sendActionBar(text, null);
    }

    void sendActionBar(String text, Map<String, String> replaceMap);

}
