package crypticlib.action.impl.common;

import crypticlib.action.BaseAction;
import crypticlib.chat.BukkitMsgSender;
import crypticlib.util.StringHelper;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Objects;

public class ActionBar extends BaseAction {

    public String message;

    public ActionBar(String message) {
        this.message = Objects.requireNonNull(message);
    }

    @Override
    public String toActionStr() {
        return "actionbar " + message;
    }

    @Override
    public void run(Player player, @NotNull Plugin plugin, Map<String, String> args) {
        BukkitMsgSender.INSTANCE.sendActionBar(player, StringHelper.replaceStrings(message, args));
        runNext(player, plugin, args);
    }

}
