package crypticlib.action.condition;

import crypticlib.chat.BukkitTextProcessor;
import org.bukkit.OfflinePlayer;

public enum PlaceholderAPICondition implements Condition {

    INSTANCE;

    @Override
    public String conditionType() {
        return "placeholderapi";
    }

    @Override
    public boolean test(OfflinePlayer targetPlayer, String conditionStr) {
        return Boolean.parseBoolean(BukkitTextProcessor.placeholder(targetPlayer, conditionStr));
    }

}
