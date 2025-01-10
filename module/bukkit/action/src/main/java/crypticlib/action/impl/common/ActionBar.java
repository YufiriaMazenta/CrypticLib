package crypticlib.action.impl.common;

import crypticlib.action.BaseAction;
import crypticlib.chat.BukkitMsgSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.function.Function;

public class ActionBar extends BaseAction {

    public final String message;

    public ActionBar(String message) {
        this.message = Objects.requireNonNull(message);
    }

    @Override
    public String toActionStr() {
        return "actionbar " + message;
    }

    @Override
    public void run(Player player, @NotNull Plugin plugin, @Nullable Function<String, String> argPreprocessor) {
        String message = this.message;
        if (argPreprocessor != null) {
            message = argPreprocessor.apply(toActionStr());
        }
        BukkitMsgSender.INSTANCE.sendActionBar(player, message);
        runNext(player, plugin, argPreprocessor);
    }

}
