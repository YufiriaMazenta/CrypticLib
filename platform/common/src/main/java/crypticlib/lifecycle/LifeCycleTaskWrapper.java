package crypticlib.lifecycle;

import crypticlib.CrypticLibPlugin;

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

    public void runLifecycleTask(CrypticLibPlugin plugin, LifeCycle lifeCycle) {
        try {
            lifeCycleTask.lifecycle(plugin, lifeCycle);
        } catch (Throwable throwable) {
            if (CrypticLibPlugin.isExceptionMatched(ignoreExceptions, throwable)) {
                return;
            }
            if (CrypticLibPlugin.isExceptionMatched(printExceptions, throwable)) {
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
