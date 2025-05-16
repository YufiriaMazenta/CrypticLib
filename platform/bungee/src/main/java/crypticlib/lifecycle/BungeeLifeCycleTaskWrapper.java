package crypticlib.lifecycle;

import net.md_5.bungee.api.plugin.Plugin;

import java.util.List;

public class BungeeLifeCycleTaskWrapper extends LifeCycleTaskWrapper<Plugin> {

    public BungeeLifeCycleTaskWrapper(LifeCycleTask<Plugin> lifeCycleTask, int priority, List<Class<? extends Throwable>> ignoreExceptions, List<Class<? extends Throwable>> printExceptions) {
        super(lifeCycleTask, priority, ignoreExceptions, printExceptions);
    }

}
