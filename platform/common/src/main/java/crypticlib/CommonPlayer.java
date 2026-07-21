package crypticlib;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;

public interface CommonPlayer extends Invoker {

    default <T> Optional<T> getPlatformPlayer(Function<@NotNull UUID, @Nullable T> playerGetter) {
        return Optional.ofNullable(playerGetter.apply(getUniqueId()));
    }

    @NotNull Locale getLocale();

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
