package crypticlib.action.impl.common;

import crypticlib.action.Action;
import crypticlib.action.ActionCompiler;
import crypticlib.action.BaseAction;
import crypticlib.action.condition.Condition;
import crypticlib.action.condition.ConditionFactory;
import crypticlib.action.condition.PlaceholderAPICondition;
import crypticlib.util.MapHelper;
import crypticlib.util.StringHelper;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

/**
 * 使用placeholderapi解析condition,决定是否执行动作
 * 示例:runIf {condition_type:"placeholderapi",condition:"%player_has_permission_crypticlib.admin%",action:"command say hello"}
 * condition_type为可选项,默认为placeholderapi
 */
public class RunIf extends BaseAction {

    private final String condition;
    private final Action action;
    private final Condition conditionType;

    public RunIf(String args) {
        Map<String, String> map = MapHelper.keyValueText2Map(Objects.requireNonNull(args));
        this.condition = map.get("condition");
        this.conditionType = ConditionFactory.INSTANCE.getConditionOpt(map.get("condition")).orElse(PlaceholderAPICondition.INSTANCE);
        this.action = ActionCompiler.INSTANCE.compile(map.get("action"));
    }

    @Override
    public String toActionStr() {
        Map<String, String> map = new HashMap<>();
        map.put("condition", condition);
        map.put("action", action.toActionStr());
        map.put("condition_type", conditionType.conditionType());
        String keyValueStr = MapHelper.map2KeyValueText(map);
        return "runIf " + keyValueStr;
    }

    @Override
    public void run(Player player, @NotNull Plugin plugin, @Nullable Function<String, String> argPreprocessor) {
        String condition = this.condition;
        if (argPreprocessor != null) {
            condition = argPreprocessor.apply(condition);
        }
        if (conditionType.test(player, condition)) {
            action.run(player, plugin, argPreprocessor);
        }
        runNext(player, plugin, argPreprocessor);
    }

}
