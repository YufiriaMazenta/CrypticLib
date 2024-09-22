package crypticlib.lifecycle;

import net.md_5.bungee.api.plugin.Plugin;

public class BungeeLifeCycleTaskWrapper extends LifeCycleTaskWrapper<Plugin> {

    public BungeeLifeCycleTaskWrapper(BungeeLifeCycleTask lifeCycleTask, int priority) {
        super(lifeCycleTask, priority);
    }

}
