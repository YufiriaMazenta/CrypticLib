package crypticlib.action.impl.common;

import crypticlib.action.BaseAction;
import crypticlib.chat.BukkitMsgSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class Title extends BaseAction {

    public String message;

    public Title(String message) {
        this.message = message;
    }

    @Override
    public String toActionStr() {
        return "title " + message;
    }

    @Override
    public void run(Player player, Plugin plugin) {
        BukkitMsgSender.INSTANCE.sendTitle(player, message, "");
        runNext(player, plugin);
    }

}
