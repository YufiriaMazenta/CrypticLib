package crypticlib.action.impl.common;

import crypticlib.action.Action;
import crypticlib.action.ActionCompiler;
import crypticlib.action.BaseAction;
import crypticlib.action.condition.CommonCondition;
import crypticlib.action.condition.Condition;
import crypticlib.action.condition.ConditionFactory;
import crypticlib.util.MapHelper;
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
 * 示例:runIf {condition_type:"placeholderapi",condition:"%player_has_permission_crypticlib.admin%",action:"command say hello",else:"tell &c你不是op,无法进行此行为"}
 * condition_type为可选项,默认为placeholderapi
 */
public class RunIf extends BaseAction {

    private final String condition;
    private final Action action;
    private final Action actionElse;
    private final Condition conditionType;

    public RunIf(String args) {
        Map<String, String> map = MapHelper.keyValueText2Map(Objects.requireNonNull(args));
        this.condition = map.getOrDefault("condition", "false");
        this.conditionType = ConditionFactory.INSTANCE.getConditionOpt(map.get("condition_type")).orElse(CommonCondition.INSTANCE);
        if (map.containsKey("action")) {
            this.action = ActionCompiler.INSTANCE.compile(map.get("action"));
        } else {
            this.action = null;
        }
        if (map.containsKey("else")) {
            this.actionElse = ActionCompiler.INSTANCE.compile(map.get("else"));
        } else {
            this.actionElse = null;
        }
    }

    @Override
    public String toActionStr() {
        Map<String, String> map = new HashMap<>();
        map.put("condition", condition);
        if (action != null) {
            map.put("action", action.toActionStr());
        }
        if (actionElse != null) {
            map.put("else", actionElse.toActionStr());
        }
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
            if (action != null) {
                action.run(player, plugin, argPreprocessor);
            }
        } else {
            if (actionElse != null) {
                actionElse.run(player, plugin, argPreprocessor);
            }
        }
        runNext(player, plugin, argPreprocessor);
    }

}
