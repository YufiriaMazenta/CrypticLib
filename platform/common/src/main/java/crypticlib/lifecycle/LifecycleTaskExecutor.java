package crypticlib.lifecycle;

import crypticlib.CrypticLibPlugin;

import java.util.List;

public class LifecycleTaskExecutor {

    protected final LifecycleTask lifecycleTask;
    protected final List<Class<? extends Throwable>> ignoreExceptions;
    protected final List<Class<? extends Throwable>> printExceptions;
    protected final int priority;

    public LifecycleTaskExecutor(
        LifecycleTask lifecycleTask,
        int priority,
        List<Class<? extends Throwable>> ignoreExceptions,
        List<Class<? extends Throwable>> printExceptions
    ) {
        this.lifecycleTask = lifecycleTask;
        this.ignoreExceptions = ignoreExceptions;
        this.printExceptions = printExceptions;
        this.priority = priority;
    }

    public void execute(CrypticLibPlugin plugin, LifecyclePhase phase) {
        try {
            lifecycleTask.onLifecycle(plugin, phase);
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

    public LifecycleTask lifecycleTask() {
        return lifecycleTask;
    }

    public List<Class<? extends Throwable>> ignoreExceptions() {
        return ignoreExceptions;
    }

    public List<Class<? extends Throwable>> printExceptions() {
        return printExceptions;
    }

}
