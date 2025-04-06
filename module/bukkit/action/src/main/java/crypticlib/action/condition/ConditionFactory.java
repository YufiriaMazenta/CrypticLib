package crypticlib.action.condition;

import crypticlib.lifecycle.AutoTask;
import crypticlib.lifecycle.BukkitLifeCycleTask;
import crypticlib.lifecycle.LifeCycle;
import crypticlib.lifecycle.TaskRule;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@AutoTask(rules = @TaskRule(lifeCycle = LifeCycle.ENABLE, priority = Integer.MIN_VALUE))
public enum ConditionFactory implements BukkitLifeCycleTask {

    INSTANCE;

    private final Map<String, Condition> conditions = new HashMap<>();

    public Optional<Condition> getConditionOpt(final String name) {
        return Optional.ofNullable(conditions.get(name));
    }

    public @Nullable Condition getCondition(final String name) {
        return conditions.get(name);
    }

    public boolean registerCondition(@NotNull Condition condition) {
        return registerCondition(condition, false);
    }

    public boolean registerCondition(@NotNull Condition condition, boolean force) {
        if (conditions.containsKey(condition.conditionType())) {
            if (force) {
                conditions.put(condition.conditionType(), condition);
                return true;
            } else {
                return false;
            }
        } else {
            conditions.put(condition.conditionType(), condition);
            return true;
        }
    }

    @Override
    public void lifecycle(Plugin plugin, LifeCycle lifeCycle) {
        registerCondition(CommonCondition.INSTANCE);
    }
}
