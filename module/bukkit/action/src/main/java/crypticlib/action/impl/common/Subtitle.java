package crypticlib.action.impl.common;

import crypticlib.action.BaseAction;
import crypticlib.chat.BukkitMsgSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class Subtitle extends BaseAction {

    public String message;

    public Subtitle(String message) {
        this.message = message;
    }

    @Override
    public String toActionStr() {
        return "subtitle " + message;
    }

    @Override
    public void run(Player player, Plugin plugin) {
        BukkitMsgSender.INSTANCE.sendTitle(player, "", message);
        runNext(player, plugin);
    }

}
