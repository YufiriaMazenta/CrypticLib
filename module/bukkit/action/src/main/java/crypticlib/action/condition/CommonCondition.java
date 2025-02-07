package crypticlib.action.condition;

import crypticlib.chat.BukkitTextProcessor;
import org.bukkit.OfflinePlayer;

public enum CommonCondition implements Condition {

    INSTANCE;

    @Override
    public String conditionType() {
        return "common";
    }

    @Override
    public boolean test(OfflinePlayer targetPlayer, String conditionStr) {
        String parsedCondition = BukkitTextProcessor.placeholder(targetPlayer, conditionStr);
        //如果解析完直接就变成了true或者false,就直接返回结果
        if (parsedCondition.equals("true") || parsedCondition.equals("false")) {
            return Boolean.parseBoolean(parsedCondition);
        }
        //进行解析
        return CommonConditionComparator.compare(parsedCondition);
    }

}
