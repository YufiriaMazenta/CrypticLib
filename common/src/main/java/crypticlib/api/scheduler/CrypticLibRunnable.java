package crypticlib.api.scheduler;

import crypticlib.CrypticLib;
import crypticlib.api.scheduler.task.ITaskWrapper;

public abstract class CrypticLibRunnable implements Runnable {

    protected ITaskWrapper taskWrapper;

    @Override
    public abstract void run();

    public ITaskWrapper runTask() {
        checkTaskNotNull();
        return setTaskWrapper(CrypticLib.getScheduler().runTask(this));
    }

    public ITaskWrapper runTaskLater(long delayTicks) {
        checkTaskNotNull();
        return setTaskWrapper(CrypticLib.getScheduler().runTaskLater(this, delayTicks));
    }

    public ITaskWrapper runTaskTimer(long delayTicks, long periodTicks) {
        checkTaskNotNull();
        return setTaskWrapper(CrypticLib.getScheduler().runTaskTimer(this, delayTicks, periodTicks));
    }

    public ITaskWrapper runTaskAsync() {
        checkTaskNotNull();
        return setTaskWrapper(CrypticLib.getScheduler().runTaskAsync(this));
    }

    public ITaskWrapper runTaskLaterAsync(long delayTicks) {
        checkTaskNotNull();
        return setTaskWrapper(CrypticLib.getScheduler().runTaskLaterAsync(this, delayTicks));
    }

    public ITaskWrapper runTaskTimerAsync(long delayTicks, long periodTicks) {
        checkTaskNotNull();
        return setTaskWrapper(CrypticLib.getScheduler().runTaskTimerAsync(this, delayTicks, periodTicks));
    }

    public void cancel() {
        checkTaskNull();
        this.taskWrapper.cancel();
    }

    public boolean isCancelled() {
        checkTaskNull();
        return this.taskWrapper.isCancelled();
    }

    protected ITaskWrapper setTaskWrapper(ITaskWrapper taskWrapper) {
        this.taskWrapper = taskWrapper;
        return this.taskWrapper;
    }

    protected void checkTaskNotNull() {
        if (this.taskWrapper != null) {
            throw new IllegalArgumentException("Runnable is null");
        }
    }

    protected void checkTaskNull() {
        if (this.taskWrapper == null) {
            throw new IllegalArgumentException("Task is null");
        }
    }
    
}
