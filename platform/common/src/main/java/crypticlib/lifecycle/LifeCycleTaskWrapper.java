package crypticlib.lifecycle;

import java.util.List;

public class LifeCycleTaskWrapper {

    protected final LifeCycleTask lifeCycleTask;
    protected final List<Class<? extends Throwable>> ignoreExceptions;
    protected final List<Class<? extends Throwable>> printExceptions;
    protected final int priority;

    public LifeCycleTaskWrapper(
        LifeCycleTask lifeCycleTask,
        int priority,
        List<Class<? extends Throwable>> ignoreExceptions,
        List<Class<? extends Throwable>> printExceptions
    ) {
        this.lifeCycleTask = lifeCycleTask;
        this.ignoreExceptions = ignoreExceptions;
        this.printExceptions = printExceptions;
        this.priority = priority;
    }

    public void runLifecycleTask(Object plugin, LifeCycle lifeCycle) {
        try {
            lifeCycleTask.lifecycle(plugin, lifeCycle);
        } catch (Throwable throwable) {
            if (ignoreExceptions.contains(throwable.getClass())) {
                return;
            }
            if (printExceptions.contains(throwable.getClass())) {
                throwable.printStackTrace();
                return;
            }
            throw new RuntimeException(throwable);
        }
    }

    public int priority() {
        return priority;
    }

    public LifeCycleTask lifeCycleTask() {
        return lifeCycleTask;
    }

    public List<Class<? extends Throwable>> ignoreExceptions() {
        return ignoreExceptions;
    }

    public List<Class<? extends Throwable>> printExceptions() {
        return printExceptions;
    }

}
