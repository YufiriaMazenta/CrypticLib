package crypticlib.action.condition;

import org.bukkit.OfflinePlayer;

/**
 * 限制条件,目前只用于runIf动作
 */
public interface Condition {

    String conditionType();

    boolean test(OfflinePlayer targetPlayer, String conditionStr);

}
