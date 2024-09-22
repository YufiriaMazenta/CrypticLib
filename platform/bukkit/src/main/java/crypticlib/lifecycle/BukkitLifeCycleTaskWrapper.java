package crypticlib.lifecycle;

import org.bukkit.plugin.Plugin;

public class BukkitLifeCycleTaskWrapper extends LifeCycleTaskWrapper<Plugin> {

    public BukkitLifeCycleTaskWrapper(BukkitLifeCycleTask lifeCycleTask, int priority) {
        super(lifeCycleTask, priority);
    }

}
