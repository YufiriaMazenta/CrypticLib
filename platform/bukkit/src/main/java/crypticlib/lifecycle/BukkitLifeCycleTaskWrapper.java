package crypticlib.lifecycle;

import org.bukkit.plugin.Plugin;

import java.util.List;

public class BukkitLifeCycleTaskWrapper extends LifeCycleTaskWrapper<Plugin> {

    public BukkitLifeCycleTaskWrapper(
        LifeCycleTask<Plugin> lifeCycleTask,
        int priority,
        List<Class<? extends Throwable>> ignoreExceptions,
        List<Class<? extends Throwable>> printExceptions
    ) {
        super(lifeCycleTask, priority, ignoreExceptions, printExceptions);
    }

}
