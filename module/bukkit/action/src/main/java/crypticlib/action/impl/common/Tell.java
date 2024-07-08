package crypticlib.action.impl.common;

import crypticlib.action.BaseAction;
import crypticlib.chat.BukkitMsgSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class Tell extends BaseAction {

    public String message;

    public Tell(String message) {
        this.message = message;
    }

    @Override
    public String toActionStr() {
        return "tell " + message;
    }

    @Override
    public void run(Player player, Plugin plugin) {
        BukkitMsgSender.INSTANCE.sendMsg(player, message);
        runNext(player, plugin);
    }

}
