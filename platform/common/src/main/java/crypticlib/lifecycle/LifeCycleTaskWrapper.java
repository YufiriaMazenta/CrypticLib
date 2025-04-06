package crypticlib.lifecycle;

public abstract class LifeCycleTaskWrapper<Plugin> {

    protected final LifeCycleTask<Plugin> lifeCycleTask;
    protected final int priority;

    public LifeCycleTaskWrapper(LifeCycleTask<Plugin> lifeCycleTask, int priority) {
        this.lifeCycleTask = lifeCycleTask;
        this.priority = priority;
    }

    public void runLifecycleTask(Plugin plugin, LifeCycle lifeCycle) {
        lifeCycleTask.lifecycle(plugin, lifeCycle);
    }

    public int priority() {
        return priority;
    }

    public LifeCycleTask<Plugin> lifeCycleTask() {
        return lifeCycleTask;
    }

}
